package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.WhicoDepartment;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * whico平台院系、专业数据保存
 * 
 *<ul>
 *	<pre>院系、专业接口的操作说明</pre>
 *	<li>将获得接口的院系、专业数据插入临时表中。</li>
 *	<li>将临时表数据插入到单位表（sync_unit）中，作为教师的组织机构使用</li>
 *	<li>临时表与院系、专业表（edu_major）进行对比：
 *		<p>1.如果临时表存在的code是院系、专业表中未存在的，将进行新增操作，插入到院系、专业表中</p>
 *		<p>2.如果临时表存在的code是院系、专业表中已存在的，将进行更新操作，以code为条件更新到院系、专业表中</p>
 *		<p>3.如果院系、专业表存在的code是临时表中未存在的，已院系、专业表为主，不进行操作</p>
 *	</li>
 *	<li>清空并删除临时表中。</li>
 *</ul>	
 * @author jiangnan
 * @date 2015.11.11
 */
public class WhicoDepartmentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_department
	private static final String SQL_DROP_TMP_DEPARTMENT = " DROP TABLE IF EXISTS `tmp_department`";
    // 创建 临时表 tmp_department TEMPORARY 
	private static final String SQL_CREATE_TMP_DEPARTMENT = " CREATE TEMPORARY TABLE `tmp_department` ("     
												       + "    `id` int(10) NOT NULL auto_increment ,"
												       + "    `unitno` varchar(32) NOT NULL COMMENT '院系,专业号',"
												       + "    `unitname` varchar(128) NOT NULL COMMENT '院系,专业名称'," 
												       + "    `parentno` varchar(32) default NULL COMMENT '隶属单位号',"
												       + "    `unittype` varchar(32) default NULL COMMENT '单位类别码',"
												       + "    PRIMARY KEY  (`id`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_department sql语句的前缀
	private static final String SQL_INSERT_TMP_DEPARTMENT = " INSERT INTO `tmp_department` (unitno, unitname, parentno) VALUES (?,?,?)";
	
	
	// 插入 sync_unit表
	private static final String SQL_INSERT_UNIT = " INSERT INTO sync_unit (code, title, parent_code, path, last_modify_user_id, create_date, modify_date) " 
															 + " VALUES (?, ?, ?, 1,1, NOW(), NOW())";
	
	// 删除 临时表 tmp_department
	private static final String SQL_TRUNCATE_SYNC_UNIT = "  TRUNCATE TABLE `sync_unit`";
	
		
	//将所有 只在 edu_department中存在 而在 tmp_department中不存在的ID，设置status,logic_delete为屏蔽
	private static final String SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE = 
														" UPDATE edu_department AS a INNER JOIN"
													  + " ("
													  + "  SELECT edu_department.code "
													  + "    FROM edu_department "
													  + "    LEFT JOIN tmp_department "
													  + "      ON tmp_department.unitno = edu_department.code "
													  + "   WHERE tmp_department.unitno IS NULL "
													  + "     AND edu_department.`logic_delete` = 0"
													  + "     AND edu_department.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0 ,"
													  + "    a.`logic_delete` = 1 ";	
	
	
	//将所有 只在 edu_Major中存在 而在 tmp_department中不存在的ID，设置status,logic_delete为屏蔽
	private static final String SQL_UPDATE_MAJOR_ONLY_UNAVAILABLE = 
														" UPDATE edu_major AS a INNER JOIN"
													  + " ("
													  + "  SELECT edu_major.code "
													  + "    FROM edu_major "
													  + "    LEFT JOIN tmp_department "
													  + "      ON tmp_department.unitno = edu_major.code "
													  + "   WHERE tmp_department.unitno IS NULL "
													  + "     AND edu_major.`logic_delete` = 0"
													  + "     AND edu_major.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0 ,"
													  + "   a.`logic_delete` = 1 ";		
	
	private static final String SQL_SELECT_EXIST_DEPARTMENT = 
											             " SELECT tmp_department.unitno,   "
											           + "        tmp_department.unitname, "
											           + "        tmp_department.parentno  "
											           + "  FROM  edu_department, tmp_department"
											           + "  WHERE edu_department.code = tmp_department.unitno"
											           + "  AND tmp_department.parentno = 1";
	
	private static final String SQL_SELECT_EXIST_MAJOR = 
											            " SELECT tmp_department.unitno,   "
											          + "        tmp_department.unitname, "
											          + "        tmp_department.parentno  "
											          + "  FROM  edu_major, tmp_department"
											          + "  WHERE edu_major.code = tmp_department.unitno"
											          + "  AND tmp_department.parentno <> 1";
	

	// 根据系部no更新系部表
	private static final String SQL_UPDATE_DEPARTMENT = " UPDATE edu_department SET `title` = ? WHERE code = ? ";

	// 根据教师工号 更新 teacher表，工号为job_no
	private static final String SQL_UPDATE_MAJOR = " UPDATE edu_major  "
													+ " LEFT JOIN tmp_department  ON edu_major.`code` = tmp_department.unitno "
													+ " LEFT JOIN  edu_department ON tmp_department.parentno = edu_department.`code`"
													+ " SET edu_major.`title` = ? , edu_major.department_id = edu_department.id  WHERE  edu_major.code = ?";
		
	// 根据 tmp_department中的 工号，查询edu_department中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_DEPARTMENT = " SELECT tmp_department.* "
																  	+ "   FROM tmp_department "
																  	+ "   LEFT JOIN edu_department "
																  	+ "     ON tmp_department.unitno = edu_department.code "
																  	+ "  WHERE edu_department.code IS NULL "
																  	+ "AND tmp_department.parentno = 1  "
																  	+ "AND tmp_department.unitname like '%系'  "
																  	+ "AND  tmp_department.parentno <> ''  ";

	// 根据 tmp_department中的 工号，查询edu_major中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_MAJOR = " SELECT tmp_department.* ,edu_department.id as depid"
																  	+ "   FROM tmp_department "
																  	+ "   LEFT JOIN edu_major "
																  	+ "     ON tmp_department.unitno = edu_major.code "
																  	+ "   INNER JOIN edu_department "
																  	+ "     ON tmp_department.parentno = edu_department.`code`"
																  	+ "  WHERE edu_major.code IS NULL "
																  	+ "AND tmp_department.parentno <> 1 "
																  	+ "AND  tmp_department.parentno <> ''";
	// 插入 edu_department表
	private static final String SQL_INSERT_DEPARTMENT = " INSERT INTO edu_department (code, title, parent_id, path, last_modify_user_id, create_date, modify_date) " 
														 + " VALUES (?, ?, 1, 1, 1, NOW(), NOW())";	

	// 插入 edu_Major表
	private static final String SQL_INSERT_MAJOR = " INSERT INTO edu_Major (code, title, department_id,  last_modify_user_id, create_date, modify_date) " 
														+ " VALUES (?, ?, ?, 1, NOW(), NOW())";	
		
		
	
	private List<WhicoDepartment> mDepartmentList;
	
	public WhicoDepartmentDataSaver(List<WhicoDepartment> departments) {
		mDepartmentList = departments;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_department
	 * 2.left join sys_user表，查出所有 tmp_department中不存在的记录，更新status为不可用
	 * 3.将tmp_department中数据 插入/更新到 sys_user表中
	 * @return
	 */
	public boolean doSave() {
		if (mDepartmentList == null || mDepartmentList.size() <= 0) {
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
			 * 创建临时表 -- tmp_department
			 */
			logger.info("创建临时表 -- tmp_department");
			recreateTmpTable(dbConn);
			
			logger.info("将院系，专业数据插入临时表");
			batchInsertTmpTable(dbConn, mDepartmentList);
			
			logger.info("清空同步单位表 -- sync_unit");
			truncateSyncUnit(dbConn);
			logger.info("将院系，专业数据插入单位表");
			batchInsertSyncUnitData(dbConn, mDepartmentList);
			
			
//			logger.info("将所有 只在 edu_department中存在 而在 tmp_department中不存在的ID，设置status,logic_delete为屏蔽");
//			updateNotExistDepartmentUnavailable(dbConn);

//			logger.info("将所有 只在 edu_major中存在 而在 tmp_department中不存在的ID，设置status,logic_delete为屏蔽");
//			updateNotExistMajorUnavailable(dbConn);
 
			logger.info("更新所有 在 tmp_department 和 edu_department中都存在的信息 ");
			updateExistDepartmentData(dbConn);
			
			logger.info("更新所有 在 tmp_department 和 edu_major中都存在的信息 ");
			updateExistMajorData(dbConn);
			
			logger.info("将只在 tmp_department 中存在的数据 插入 edu_department 表 ");
			insertNewDepartmentData(dbConn);

			logger.info("将只在 tmp_department 中存在的数据 插入 edu_major 表 ");
			insertNewMajorData(dbConn);

			logger.info("删除临时表 tmp_department");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将院系、专业数据保存到数据库 操作失败: " + e);
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
	
	
	// 重新创建临时表 tmp_department
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		logger.info(SQL_CREATE_TMP_DEPARTMENT);
		
		stmt.executeUpdate(SQL_CREATE_TMP_DEPARTMENT);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_DEPARTMENT);
		
		stmt.executeUpdate(SQL_DROP_TMP_DEPARTMENT);
		stmt.close();
	}
	
	/**
	 * 清空同步单位表
	 * @param conn
	 * @throws SQLException
	 */
	private void truncateSyncUnit(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_TRUNCATE_SYNC_UNIT);
		
		stmt.executeUpdate(SQL_TRUNCATE_SYNC_UNIT);
		stmt.close();
	}
	
	// 将所有临时表数据数据插入sync_unit
	private void batchInsertSyncUnitData(Connection conn, List<WhicoDepartment> departments) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_UNIT);
		
		//unitno, unitname, parentno
		for (WhicoDepartment department : departments) {
			prepStmt.setString(1, department.getUnitNo());
			prepStmt.setString(2, department.getName());
			prepStmt.setString(3, (department.getParentNo().equals("1") || department.getParentNo().equals("")) ? "0" :department.getParentNo());

			logger.info(prepStmt.toString() + ";" 
			          + department.getUnitNo() + ","
					  + department.getName() + ","
			          + department.getParentNo());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		prepStmt.executeBatch();
		prepStmt.close();
	}
		
	// 将所有department数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<WhicoDepartment> departments) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_DEPARTMENT);
		
		//unitno, unitname, parentno
		for (WhicoDepartment department : departments) {
		
			prepStmt.setString(1, department.getUnitNo());
			prepStmt.setString(2, department.getName());
			prepStmt.setString(3, department.getParentNo());
			
			logger.info(prepStmt.toString() + ";" 
			          + department.getUnitNo() + ","
					  + department.getName() + ","
			          + department.getParentNo());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	
	/* 将所有只存在于 tmp_department 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistDepartmentUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}


	/* 将所有只存在于 tmp_department 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistMajorUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_MAJOR_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_MAJOR_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}
	
	/*
	 * 需要更新title
	 */
	private void updateExistDepartmentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_UPDATE_DEPARTMENT);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_DEPARTMENT); 
		if (rs != null) {
			
			String unitno = "", unitname = "";
			while(rs.next()) {
				unitname = rs.getString("unitname");
				departmentPst.setString(1, unitname);
				
				unitno = rs.getString("unitno");
				departmentPst.setString(2, unitno);
				
				

				logger.info(departmentPst.toString() + ";" + unitname +","+unitno);
				departmentPst.addBatch();
			};
			departmentPst.executeBatch();
		}
		
		stmt.close();
		departmentPst.close();
	}
	
	private void updateExistMajorData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_UPDATE_MAJOR);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_MAJOR); 
		if (rs != null) {
			
			String unitno = "", unitname = "";
			while(rs.next()) {		
				
				unitname = rs.getString("unitname");
				departmentPst.setString(1, unitname);
				
				unitno = rs.getString("unitno");
				departmentPst.setString(2, unitno);
				
				logger.info(departmentPst.toString() + ";" + unitname +","+unitno);
				departmentPst.addBatch();
			};
			departmentPst.executeBatch();
		}
		
		stmt.close();
		departmentPst.close();
	}
	
	/*
	 * 将只在 tmp_department 中存在的数据 插入 edu_p 表
	 */
	private void insertNewDepartmentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_INSERT_DEPARTMENT);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_BY_DEPARTMENT); 
		if (rs != null) {
			String unitno = "", unitname = "", parentno = "";
			while(rs.next()) {
				unitno = rs.getString("unitno");
				departmentPst.setString(1, unitno);
				
				unitname = rs.getString("unitname");
				departmentPst.setString(2, unitname);

				logger.info(departmentPst.toString() + ";" + unitname +","+unitno);
				
				departmentPst.addBatch();
			};

			departmentPst.executeBatch();
		}
		
		stmt.close();
		departmentPst.close();
	}
	
	private void insertNewMajorData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement majorPst = conn.prepareStatement(SQL_INSERT_MAJOR);
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_BY_MAJOR); 
		if (rs != null) {
			String unitno = "", unitname = "";
			int depid = 0;
			while(rs.next()) {
				unitno = rs.getString("unitno");
				majorPst.setString(1, unitno);
				
				unitname = rs.getString("unitname");
				majorPst.setString(2, unitname);
				
				depid = rs.getInt("depid");
				majorPst.setInt(3, depid);
				
				logger.info(majorPst.toString() + ";" + unitname +","+unitno+"," + depid);
				
				majorPst.addBatch();
			};

			majorPst.executeBatch();
		}
		
		stmt.close();
		majorPst.close();
	}
}
