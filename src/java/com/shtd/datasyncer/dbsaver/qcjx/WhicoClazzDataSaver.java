package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.WhicoClazz;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYin;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * whico平台班级数据保存
 * 
 *<ul>
 *	<pre>班级接口的操作说明</pre>
 *	<li>创建班级临时表。字段(班级号,班级名称,专业名称,年级)</li>
 *	<li>将获得接口的班级数据插入临时表中。</li>
 *	<li>临时表与班级表进行对比：
 *		<p>1.如果临时表存在的code是班级表中未存在的，将进行新增操作（获取系部ID,和专业ID），插入到班级表中</p>
 *		<p>2.如果临时表存在的code是班级表中已存在的，将进行更新操作，以code为条件更新到班级表中</p>
 *		<p>3.如果班级表存在的code是临时表中未存在的，已班级表为主，不进行操作</p>
 *	</li>
 *	<li>清空并删除临时表中。</li>
 *</ul>	
 * @author jiangnan
 * @date 2015.11.11
 */
public class WhicoClazzDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_clazz
	private static final String SQL_DROP_TMP_CLAZZ = " DROP TABLE IF EXISTS `tmp_clazz`";
    // 创建 临时表 tmp_clazz TEMPORARY
	private static final String SQL_CREATE_TMP_CLAZZ = " CREATE TEMPORARY  TABLE `tmp_clazz` ("     
												       + "    `id` int(10) NOT NULL auto_increment ,"
												       + "    `code` varchar(32) NOT NULL COMMENT '班级号',"
												       + "    `title` varchar(128) NOT NULL COMMENT '班级名称'," 
												       + "    `majorname` varchar(32) default NULL COMMENT '专业名称',"
												       + "    `grade` varchar(32) default NULL COMMENT '年级',"
												       + "    PRIMARY KEY  (`id`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_clazz sql语句的前缀
	private static final String SQL_INSERT_TMP_CLAZZ = " INSERT INTO `tmp_clazz` (code, title, majorname,grade) VALUES (?,?,?,?)";
	
	//将所有 只在 edu_clazz中存在 而在 tmp_clazz中不存在的ID，设置status,logic_delete为屏蔽
	private static final String SQL_UPDATE_CLAZZ_ONLY_UNAVAILABLE = 
														" UPDATE edu_clazz AS a INNER JOIN"
													  + " ("
													  + "  SELECT edu_clazz.code "
													  + "    FROM edu_clazz "
													  + "    LEFT JOIN tmp_clazz "
													  + "      ON tmp_clazz.code = edu_clazz.code "
													  + "   WHERE tmp_clazz.code IS NULL "
													  + "     AND edu_clazz.`logic_delete` = 0"
													  + "     AND edu_clazz.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0 ,"
													  + "    a.`logic_delete` = 1 ";	
	
	
	 	
	// 根据 tmp_clazz中的code，查询edu_clazzr中相关数据
	private static final String SQL_SELECT_EXIST_CLAZZ = 
											             " SELECT tmp_clazz.code,   "
											           + "        tmp_clazz.title, "
											           + "        tmp_clazz.majorname,"
											           + "		  tmp_clazz.grade  "
											           + "  FROM  edu_clazz, tmp_clazz"
											           + "  WHERE edu_clazz.code = tmp_clazz.code";
 
	

	// 根据班级code 更新 一次edu_clazz 防止其他内容有变化
	private static final String SQL_UPDATE_CLAZZ =  "UPDATE edu_clazz"
													+ " LEFT JOIN tmp_clazz  ON tmp_clazz.`code` = edu_clazz.`code`"
													+ " LEFT JOIN edu_major ON edu_major.code = tmp_clazz.majorname "
													+ " LEFT JOIN edu_department ON edu_major.department_id = edu_department.id"
													+ " SET edu_clazz.`title` = ?,"
													+ " edu_clazz.pinyin = ?,"
													+ " edu_clazz.jianpin = ?,"
													+ " edu_clazz.grade = ?,"
													+ " edu_clazz.department_id = edu_major.department_id,"
													+ " edu_clazz.major_id = edu_major.id"
													+ " WHERE"
													+ " edu_clazz.`code` = ?";

	// 根据 tmp_class中的code，查询tmp_clazz中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_CLAZZ = " SELECT tmp_clazz.* ,edu_major.id as majorId ,edu_major.department_id as depId"
																  	+ "   FROM tmp_clazz "
																  	+ "   LEFT JOIN edu_clazz "
																  	+ "     ON tmp_clazz.code = edu_clazz.code "
																	+ "   LEFT JOIN edu_major "
																  	+ "     ON edu_major.code = tmp_clazz.majorname "
																	+ "   LEFT JOIN edu_department "
																  	+ "     ON edu_major.department_id = edu_department.code "
																  	+ "  WHERE edu_clazz.code IS NULL ";

 
	// 插入 edu_clazz表
	private static final String SQL_INSERT_CLAZZ = " INSERT INTO edu_clazz (code, title, pinyin, jianpin,major_id, department_id, grade, clazz_type, last_modify_user_id, create_date, modify_date) " 
														 + " VALUES (?, ?, ?, ?, ?, ?, ?, 1, 1, NOW(), NOW())";	

		
	
	
	private List<WhicoClazz> mWhicoClazzList;
	
	public WhicoClazzDataSaver(List<WhicoClazz> clazzs) {
		mWhicoClazzList = clazzs;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_teacher
	 * 2.left join sys_user表，查出所有 tmp_teacher中不存在的记录，更新status为不可用
	 * 3.将tmp_teacher中数据 插入/更新到 sys_user表中
	 * @return
	 */
	public boolean doSave() {
		if (mWhicoClazzList == null || mWhicoClazzList.size() <= 0) {
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
			batchInsertTmpTable(dbConn, mWhicoClazzList);
			
//			logger.info("将所有 只在 edu_clazz中存在 而在 tmp_clazz中不存在的ID，设置status,logic_delete为屏蔽");
//			updateNotExistClazzUnavailable(dbConn);

			logger.info("更新所有 在 tmp_clazz 和 edu_clazz中都存在的信息 ");
			updateExistClazzData(dbConn);
			
			logger.info("将只在 tmp_clazz 中存在的数据 插入 edu_clazz 表 ");
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
	private void batchInsertTmpTable(Connection conn, List<WhicoClazz> clazzs) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_CLAZZ);
		
		//code, title, departmentNo
		for (WhicoClazz clazz : clazzs) {
			prepStmt.setString(1, clazz.getClazzNo());
			prepStmt.setString(2, clazz.getClazzName());
			prepStmt.setString(3, clazz.getDepartmentNo());
			prepStmt.setString(4, clazz.getGrade());

			logger.info(prepStmt.toString() + ";" 
			          + clazz.getClazzNo() + ","
					  + clazz.getClazzName() + ","
			          + clazz.getDepartmentNo() + ","
					  + clazz.getGrade());
			
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
		 
			String code, title, pinyin, jianpin, grade ;
			while(rs.next()) {
				title = rs.getString("title");
				clazzPst.setString(1, title);
				
				pinyin = PinYin.getPingYin(title);
				clazzPst.setString(2, pinyin);
				
				jianpin = PinYin.getPinYinHeadChar(title);
				clazzPst.setString(3, jianpin);
				
				grade = rs.getString("grade");
				clazzPst.setString(4, grade);
				
				code = rs.getString("code");
				clazzPst.setString(5, code);
				
				logger.info(clazzPst.toString() + ";" + title +","+code +","+pinyin +","+jianpin);
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
			String code, title, pinyin, jianpin, grade ;
			int majorId , depId = 0;
			while(rs.next()) {
				
				code = rs.getString("code");
				clazzPst.setString(1, code);
				
				title = rs.getString("title");
				clazzPst.setString(2, title);

				pinyin = PinYin.getPingYin(title);
				clazzPst.setString(3, pinyin);
				
				jianpin = PinYin.getPinYinHeadChar(title);
				clazzPst.setString(4, jianpin);
				
				majorId = rs.getInt("majorId");
				clazzPst.setInt(5, majorId);
				
				depId = rs.getInt("depId");
				clazzPst.setInt(6, depId);
				
				grade = rs.getString("grade");
				clazzPst.setString(7, grade);
				logger.info(clazzPst.toString() + ";" + title +","+code +","+pinyin +","+jianpin +","+majorId +","+depId +","+grade);
				
				clazzPst.addBatch();
			};

			clazzPst.executeBatch();
		}
		
		stmt.close();
		clazzPst.close();
	}
	
 
}
