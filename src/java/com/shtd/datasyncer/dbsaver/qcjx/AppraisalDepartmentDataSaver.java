package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.AppraisalDepartment;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * 教学评价系统系、专业数据保存
 * @author Josh
 */
public class AppraisalDepartmentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_faculty
	private static final String SQL_DROP_TMP_DEPARTMENT = " DROP TABLE IF EXISTS `tmp_faculty`";
    // 创建 临时表 tmp_faculty TEMPORARY
	private static final String SQL_CREATE_TMP_DEPARTMENT = " CREATE  TEMPORARY TABLE `tmp_faculty` ("     
												       + "    `id` int(11) NOT NULL auto_increment ,"
												       + "    `unitno` varchar(32) NOT NULL COMMENT '院系,专业号',"
												       + "    `unitname` varchar(128) NOT NULL COMMENT '院系,专业名称'," 
												       + "    `parentno` varchar(32) default NULL COMMENT '隶属单位号',"
												       + "    PRIMARY KEY  (`id`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_faculty sql语句的前缀
	private static final String SQL_INSERT_TMP_DEPARTMENT = " INSERT INTO `tmp_faculty` (unitno, unitname, parentno) VALUES (?,?,?)";
	
	//将所有 只在 faculty中存在 而在 tmp_faculty中不存在的ID，设置status为禁用
	private static final String SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE = 
														" UPDATE faculty AS a INNER JOIN"
													  + " ("
													  + "  SELECT faculty.code "
													  + "    FROM faculty "
													  + "    LEFT JOIN tmp_faculty "
													  + "      ON tmp_faculty.unitno = faculty.code "
													  + "   WHERE tmp_faculty.unitno IS NULL "
													  + "     AND faculty.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0";
	
	
	//将所有 只在 major中存在 而在 tmp_faculty中不存在的ID，设置status为屏蔽
	private static final String SQL_UPDATE_MAJOR_ONLY_UNAVAILABLE = 
														" UPDATE major AS a INNER JOIN"
													  + " ("
													  + "  SELECT major.code "
													  + "    FROM major "
													  + "    LEFT JOIN tmp_faculty "
													  + "      ON tmp_faculty.unitno = major.code "
													  + "   WHERE tmp_faculty.unitno IS NULL "
													  + "     AND major.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0 ";
	
	private static final String SQL_SELECT_EXIST_DEPARTMENT = 
											             " SELECT tmp_faculty.unitno,   "
											           + "        tmp_faculty.unitname, "
											           + "        tmp_faculty.parentno  "
											           + "  FROM  faculty, tmp_faculty"
											           + "  WHERE faculty.code = tmp_faculty.unitno"
											           + "  AND tmp_faculty.parentno = 1";
	
	private static final String SQL_SELECT_EXIST_MAJOR = 
											            " SELECT tmp_faculty.unitno,   "
											          + "        tmp_faculty.unitname, "
											          + "        tmp_faculty.parentno  "
											          + "  FROM  major, tmp_faculty"
											          + "  WHERE major.code = tmp_faculty.unitno"
											          + "  AND tmp_faculty.parentno <> 1";
	

	// 根据系部code更新系部表
	private static final String SQL_UPDATE_DEPARTMENT = " UPDATE faculty SET `title` = ? WHERE code = ? ";

	// 根据专业编码code 更新 major表
	private static final String SQL_UPDATE_MAJOR = " UPDATE major  "
													+ " LEFT JOIN tmp_faculty  ON major.`code` = tmp_faculty.unitno "
													+ " LEFT JOIN  faculty ON tmp_faculty.parentno = faculty.`code`"
													+ " SET major.`title` = ? , major.faculty_id = faculty.id  WHERE  major.code = ?";
		
	// 根据 tmp_faculty，查询faculty中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_DEPARTMENT = " SELECT tmp_faculty.* "
																  	+ "   FROM tmp_faculty "
																  	+ "   LEFT JOIN faculty "
																  	+ "     ON tmp_faculty.unitno = faculty.code "
																  	+ "  WHERE faculty.code IS NULL "
																  	+ "AND tmp_faculty.parentno = 1  "
																  	+ "AND tmp_faculty.parentno <> '' ";

	// 根据 tmp_faculty，查询major中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_MAJOR = " SELECT tmp_faculty.* ,faculty.id as facultyId"
																  	+ "   FROM tmp_faculty "
																  	+ "   LEFT JOIN major "
																  	+ "     ON tmp_faculty.unitno = major.code "
																  	+ "   LEFT JOIN faculty "
																  	+ "     ON tmp_faculty.parentno = faculty.`code`"
																  	+ "  WHERE major.code IS NULL "
																  	+ "AND tmp_faculty.parentno <> 1 "
																  	+ "AND tmp_faculty.parentno <> ''";
	// 插入 faculty表
	private static final String SQL_INSERT_DEPARTMENT = " INSERT INTO faculty (code, title, create_date, update_date) " 
														 + " VALUES (?, ?, NOW(), NOW())";	

	// 插入 major表
	private static final String SQL_INSERT_MAJOR = " INSERT INTO major (code, title, faculty_id, create_date, update_date) " 
														+ " VALUES (?, ?, ?, NOW(), NOW())";	
		
	private List<AppraisalDepartment> mDepartmentList;
	
	public AppraisalDepartmentDataSaver(List<AppraisalDepartment> departments) {
		mDepartmentList = departments;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_faculty
	 * 2.left join sys_user表，查出所有 tmp_faculty中不存在的记录，更新status为不可用
	 * 3.将tmp_faculty中数据 插入/更新到 sys_user表中
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
			 * 创建临时表 -- tmp_faculty
			 */
			logger.info("创建临时表 -- tmp_faculty");
			recreateTmpTable(dbConn);
			
			logger.info("将院系，专业数据插入临时表");
			batchInsertTmpTable(dbConn, mDepartmentList);
			
			logger.info("将所有 只在 faculty中存在 而在 tmp_faculty中不存在的ID，设置status为屏蔽");
			updateNotExistDepartmentUnavailable(dbConn);

			logger.info("将所有 只在 major中存在 而在 tmp_faculty中不存在的ID，设置status为屏蔽");
			updateNotExistMajorUnavailable(dbConn);
 
			logger.info("更新所有 在 tmp_faculty 和 faculty中都存在的信息 ");
			updateExistDepartmentData(dbConn);
			
			logger.info("更新所有 在 tmp_faculty 和 major中都存在的信息 ");
			updateExistMajorData(dbConn);
			
			logger.info("将只在 tmp_faculty 中存在的数据 插入 faculty 表 ");
			insertNewDepartmentData(dbConn);

			logger.info("将只在 tmp_faculty 中存在的数据 插入 major 表 ");
			insertNewMajorData(dbConn);

			logger.info("删除临时表 tmp_faculty");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将班级数据保存到数据库 操作失败: " + e);
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
	
	
	// 重新创建临时表 tmp_faculty
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
	
	// 将所有department数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<AppraisalDepartment> departments) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_DEPARTMENT);
		
		//unitno, unitname, parentno
		for (AppraisalDepartment department : departments) {
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
	
	
	/* 将所有只存在于 tmp_faculty 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistDepartmentUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}


	/* 将所有只存在于 tmp_faculty 表 而在 security表中不存在的帐号，置为不可用状态
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
	 * 将只在 tmp_faculty 中存在的数据 插入 edu_p 表
	 */
	private void insertNewDepartmentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_INSERT_DEPARTMENT);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_BY_DEPARTMENT); 
		if (rs != null) {
			String unitno = "", unitname = "";
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
			int facultyId = 0;
			while(rs.next()) {
				unitno = rs.getString("unitno");
				majorPst.setString(1, unitno);
				
				unitname = rs.getString("unitname");
				majorPst.setString(2, unitname);
				
				facultyId = rs.getInt("facultyId");
				majorPst.setInt(3, facultyId);
				
				logger.info(majorPst.toString() + ";" + unitname +","+unitno+"," + facultyId);
				
				majorPst.addBatch();
			};

			majorPst.executeBatch();
		}
		
		stmt.close();
		majorPst.close();
	}
}