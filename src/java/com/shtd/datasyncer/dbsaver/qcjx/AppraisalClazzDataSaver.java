package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.AppraisalClazz;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * 教学评价系统班级数据保存
 * @author Josh
 */
public class AppraisalClazzDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_clazz
	private static final String SQL_DROP_TMP_CLAZZ = " DROP TABLE IF EXISTS `tmp_clazz`";
    // 创建 临时表 tmp_clazz TEMPORARY
	private static final String SQL_CREATE_TMP_CLAZZ = " CREATE TEMPORARY  TABLE `tmp_clazz` ("     
												       + "    `id` int(11) NOT NULL auto_increment ,"
												       + "    `code` varchar(32) NOT NULL COMMENT '班级号',"
												       + "    `title` varchar(128) NOT NULL COMMENT '班级名称'," 
												       + "    `majorname` varchar(32) default NULL COMMENT '专业名称',"
												       + "    PRIMARY KEY  (`id`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_clazz sql语句的前缀
	private static final String SQL_INSERT_TMP_CLAZZ = " INSERT INTO `tmp_clazz` (code, title, majorname) VALUES (?,?,?)";
	
	//将所有 只在clazz中存在 而在 tmp_clazz中不存在的ID，设置status为屏蔽
	private static final String SQL_UPDATE_CLAZZ_ONLY_UNAVAILABLE = 
														" UPDATE clazz AS a INNER JOIN"
													  + " ("
													  + "  SELECT clazz.code "
													  + "    FROM clazz "
													  + "    LEFT JOIN tmp_clazz "
													  + "      ON tmp_clazz.code = clazz.code "
													  + "   WHERE tmp_clazz.code IS NULL "
													  + "     AND clazz.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0";	
	
	
	 	
	// 根据 tmp_clazz中的编码，查询clazz中相关数据
	private static final String SQL_SELECT_EXIST_CLAZZ = " SELECT tmp_clazz.code, "
											           + "        tmp_clazz.title, "
											           + "        tmp_clazz.majorname "
											           + "  FROM  clazz, tmp_clazz"
											           + "  WHERE clazz.code = tmp_clazz.code";
 
	

	// 根据班级code 更新 一次clazz 防止其他内容有变化
	private static final String SQL_UPDATE_CLAZZ =  "UPDATE clazz"
													+ " LEFT JOIN tmp_clazz  ON tmp_clazz.`code` = clazz.`code`"
													+ " LEFT JOIN major ON major.code = tmp_clazz.majorname "
													+ " LEFT JOIN faculty ON major.faculty_id = faculty.id"
													+ " SET clazz.`title` = ?,"
													+ " clazz.major_id = major.id"
													+ " WHERE"
													+ " clazz.`code` = ?";

	// 根据 tmp_clazz中的code，查询clazz中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_CLAZZ = " SELECT tmp_clazz.* ,major.id as majorId ,major.faculty_id as facultyId"
																  	+ "   FROM tmp_clazz "
																  	+ "   LEFT JOIN clazz "
																  	+ "     ON tmp_clazz.code = clazz.code "
																	+ "   LEFT JOIN major "
																  	+ "     ON major.code = tmp_clazz.majorname "
																	+ "   LEFT JOIN faculty "
																  	+ "     ON major.faculty_id = faculty.code "
																  	+ "  WHERE clazz.code IS NULL ";

 
	// 插入 clazz表
	private static final String SQL_INSERT_CLAZZ = " INSERT INTO clazz (code, title, major_id, create_date, update_date) " 
														 + " VALUES (?, ?, ?, NOW(), NOW())";	

		
	
	
	private List<AppraisalClazz> mAppraisalClazzList;
	
	public AppraisalClazzDataSaver(List<AppraisalClazz> clazzs) {
		mAppraisalClazzList = clazzs;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_teacher
	 * 2.left join sys_user表，查出所有 tmp_teacher中不存在的记录，更新status为不可用
	 * 3.将tmp_teacher中数据 插入/更新到 sys_user表中
	 * @return
	 */
	public boolean doSave() {
		if (mAppraisalClazzList == null || mAppraisalClazzList.size() <= 0) {
			logger.error("不存在待保存数据，操作结束");
			return true;
		}
		
		MysqlDb db = new MysqlDb();
		Connection dbConn = null;
		
		try {
			db.initConn();
			dbConn = db.getConn();
			
			if (dbConn == null) {
				logger.error("未获取有效数据库连接，操作失败");
				return false;
			}
			
			dbConn.setAutoCommit(false);
			
			
			/* 
			 * 创建临时表 -- tmp_clazz
			 */
			logger.info("创建临时表 -- tmp_clazz");
			recreateTmpTable(dbConn);
			
			logger.info("将班级数据插入临时表");
			batchInsertTmpTable(dbConn, mAppraisalClazzList);
			
			logger.info("将所有 只在 clazz中存在 而在 tmp_clazz中不存在的ID，设置status为失效");
			updateNotExistClazzUnavailable(dbConn);

			logger.info("更新所有 在 tmp_clazz 和 clazz中都存在的信息 ");
			updateExistClazzData(dbConn);
			
			logger.info("将只在 tmp_clazz 中存在的数据 插入 clazz 表 ");
			insertNewClazzData(dbConn);

			logger.info("删除临时表 tmp_clazz");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将教师数据保存到数据库 操作失败: " + e);
			e.printStackTrace();
			
		} finally {
			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (Exception e) {
				}
			}
			
			db.closeAll();
		}
		
		return false;
	}
	
	
	// 重新创建临时表 tmp_clazz
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		logger.info(SQL_CREATE_TMP_CLAZZ);
		
		stmt.executeUpdate(SQL_CREATE_TMP_CLAZZ);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_CLAZZ);
		
		stmt.executeUpdate(SQL_DROP_TMP_CLAZZ);
		stmt.close();
	}
	
	// 将所有clazz数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<AppraisalClazz> clazzs) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_CLAZZ);
		
		//code, title, departmentNo
		for (AppraisalClazz clazz : clazzs) {
			prepStmt.setString(1, clazz.getClazzNo());
			prepStmt.setString(2, clazz.getClazzName());
			prepStmt.setString(3, clazz.getDepartmentNo());

			logger.info(prepStmt.toString() + ";" 
			          + clazz.getClazzNo() + ","
					  + clazz.getClazzName() + ","
			          + clazz.getDepartmentNo());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	/* 将所有只存在于 tmp_teacher 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistClazzUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_CLAZZ_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_CLAZZ_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}


	 
	
	/*
	 * 需要更新title
	 */
	private void updateExistClazzData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement clazzPst = conn.prepareStatement(SQL_UPDATE_CLAZZ);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_CLAZZ); 
		if (rs != null) {
		 
			String code, title;
			while(rs.next()) {
				title = rs.getString("title");
				clazzPst.setString(1, title);
				
				code = rs.getString("code");
				clazzPst.setString(2, code);
				
				logger.info(clazzPst.toString() + ";" + title +","+code);
				clazzPst.addBatch();
			};
			clazzPst.executeBatch();
		}
		
		stmt.close();
		clazzPst.close();
	}
	
	 
	
	/*
	 * 将只在 tmp_clazz 中存在的数据 插入 edu_p 表
	 */
	private void insertNewClazzData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement clazzPst = conn.prepareStatement(SQL_INSERT_CLAZZ);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_BY_CLAZZ); 
		if (rs != null) {
			String code, title;
			int majorId;
			while(rs.next()) {
				
				code = rs.getString("code");
				clazzPst.setString(1, code);
				
				title = rs.getString("title");
				clazzPst.setString(2, title);

				majorId = rs.getInt("majorId");
				clazzPst.setInt(3, majorId);
				
				logger.info(clazzPst.toString() + ";" + title +","+code +","+majorId);
				
				clazzPst.addBatch();
			};

			clazzPst.executeBatch();
		}
		
		stmt.close();
		clazzPst.close();
	}
}
