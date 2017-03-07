package com.shtd.datasyncer.dbsaver.tms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.tms.Employee;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

public class EmployeeDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	// 删除 临时表 tmp_employee
	private static final String SQL_DROP_TMP_EMPLOYEE = " DROP TABLE IF EXISTS `tmp_employee`";

	// 创建 临时表 tmp_employee
	private static final String SQL_CREATE_TMP_EMPLOYEE = "CREATE TABLE `tmp_employee` (" +
														  "`id` int(11) unsigned NOT NULL auto_increment COMMENT '流水号'," +
														  "`user_id` int(11) default NULL COMMENT '系统用户流水号'," +
														  "`user_no` varchar(64) NOT NULL COMMENT '员工号'," +
														  "`username` varchar(128) NOT NULL COMMENT '用户真实姓名'," +
														  "`email` varchar(64) NOT NULL COMMENT '用户邮箱'," +
														  "`mobile` varchar(32) NOT NULL COMMENT '用户手机号'," +
														  "`gender` int(2) NOT NULL COMMENT '性别  男，女'," +
														  "`post_title` varchar(128) default NULL COMMENT '职务名称'," +
														  "`department_id` int(11) default NULL COMMENT '部门'," +
														  "`department` varchar(128) NOT NULL COMMENT '部门'," +
														  "`post_type_id` int(11) default NULL COMMENT '岗位分类'," +
														  "`post_type` varchar(128) NOT NULL COMMENT '岗位分类'," +
														  "`staff_type_id` int(11) default NULL COMMENT '编制类型'," +
														  "`staff_type` varchar(128) NOT NULL COMMENT '编制类型'," +
														  "`post_level_id` int(11) default NULL COMMENT '行政职务等级'," +
														  "`post_level` varchar(128) NOT NULL COMMENT '行政职务等级'," +
														  "`teach_flag` int(2) NOT NULL default '1' COMMENT '是否任课  是，否'," +
														  "`retire_flag` int(2) NOT NULL default '0' COMMENT '是否退休  否，是'," +
														  "`cert_flag` int(2) NOT NULL default '0' COMMENT '是否具备教师资格  否，是'," +
														  "`status` int(2) NOT NULL default '1' COMMENT '状态  不可用，可用'," +
														  "PRIMARY KEY  (`id`)" +
														") ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=utf8 COMMENT='教职工数据临时表'";
	 
	// 批量插入 tmp_employee  
	private static final String SQL_INSERT_TMP_EMPLOYEE = "INSERT INTO tmp_employee"
														+ "(user_no,username,email,mobile,"
														+ "gender,post_title,department,post_type,"
														+ "staff_type,post_level,teach_flag,retire_flag,"
														+ "cert_flag,status) "
														+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String SQL_UPDATE_TMP_EMPLOYEE = "UPDATE tmp_employee " +
														"SET user_id = (SELECT id FROM sys_user WHERE `status` = 1 AND logic_delete = 0 AND user_no = tmp_employee.user_no LIMIT 1)," + 
															"department_id = (SELECT id FROM dict_edu_common_department WHERE `status` = 1 AND logic_delete = 0 AND title = tmp_employee.department LIMIT 1)," +
															"post_type_id = (SELECT id FROM dict_edu_tch_post_type WHERE `status` = 1 AND logic_delete = 0 AND title = tmp_employee.post_type LIMIT 1)," +
															"staff_type_id = (SELECT id FROM dict_edu_tch_staff_type WHERE `status` = 1 AND logic_delete = 0 AND title = tmp_employee.staff_type LIMIT 1)," +
															"post_level_id = (SELECT id FROM dict_edu_tch_post_level WHERE `status` = 1 AND logic_delete = 0 AND title = tmp_employee.post_level LIMIT 1)";

	// 只在sys_user存在，但是在tmp_employee中不存在的教师帐号，置为‘逻辑删除’状态
	private static final String SQL_UPDATE_ONLY_IN_SYS_USER = " UPDATE sys_user SET logic_delete = 1 WHERE id IN ( " +
																" SELECT u.id FROM ( " +
																		" SELECT su.id FROM edu_tch_employee ete " +
																		" LEFT JOIN sys_user su ON su.id = ete.user_id " +
																		" LEFT JOIN tmp_employee te ON te.user_no = su.user_no " +
																		" WHERE te.user_no is NULL " +
																		" AND su.`status` = 1 " +
																		" AND su.logic_delete = 0 " +
																		" AND ete.`status` = 1 " +
																		" AND ete.logic_delete = 0) u) ";
	
	// 根据tmp_employee中数据,更新sys_user信息
	private static final String SQL_UPDATE_SYS_USER_FROM_TMP = " UPDATE sys_user " + 
															" SET username = (SELECT username FROM tmp_employee WHERE user_id = sys_user.id LIMIT 1)," + 
															" email = (SELECT email FROM tmp_employee WHERE user_id = sys_user.id LIMIT 1)," +
															" mobile = (SELECT mobile FROM tmp_employee WHERE user_id = sys_user.id LIMIT 1)";

	// 根据tmp_employee中数据,更新edu_tch_employee信息
	private static final String SQL_UPDATE_EMPLOYEE_FROM_TMP = " UPDATE edu_tch_employee " + 
															" SET username = (SELECT username FROM tmp_employee WHERE user_id = sys_user.id LIMIT 1)," + 
															" email = (SELECT email FROM tmp_employee WHERE user_id = sys_user.id LIMIT 1)," +
															" mobile = (SELECT mobile FROM tmp_employee WHERE user_id = sys_user.id LIMIT 1)";

	private static final String SQL_SELECT_EMPLOYEE = "";
	

	// 将只在tmp_employee中存在的数据全部插入 sys_user
	private static final String SQL_INSERT_SYS_USER_FROM_TMP = " insert into PUB_User "
			                                                         + "        (Name, User_Name, Class_ID, User_Pass, User_Group, State, Sex, Birthday)"
			                                                         + " select tmp_teacher.Name, "
			                                                         + "        tmp_teacher.User_Name, "
			                                                         + "        tmp_teacher.Class_ID, "
			                                                         + "        tmp_teacher.User_Pass, "
			                                                         + "        tmp_teacher.User_Group, "
			                                                         + "        tmp_teacher.State, "
			                                                         + "        tmp_teacher.Sex, "
			                                                         + "        tmp_teacher.Birthday "
			                                                         + "   from tmp_teacher"
			                                                         + "   left join PUB_User"
			                                                         + "     on tmp_teacher.User_Name = PUB_User.User_Name"
			                                                         + "  where PUB_User.User_Name is null";
	
	private List<Employee> mEmployeeList;
	
	public EmployeeDataSaver(List<Employee> employees) {
		mEmployeeList = employees;
	}
	
	/**
	 * @return
	 */
	public boolean doSave() {
		if (mEmployeeList == null || mEmployeeList.size() <= 0) {
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
			 * 创建临时表 -- tmp_employee  
			 */
			logger.info("创建临时表 -- tmp_employee");
//			recreateTmpTable(dbConn);
			
			logger.info("将教师数据插入临时表");
//			batchInsertTmpTable(dbConn, mEmployeeList);
			
			logger.info("更新临时表tmp_employee");
//			updateTmpEmployee(dbConn);
			
			logger.info("sys_user存在，但是在tmp_employee中不存在的教师帐号，置为‘逻辑删除’状态");
//			updateAccountOnlyInSysUser(dbConn);

			logger.info("根据tmp_employee中数据,更新sys_user信息");
//			updateSysUser(dbConn);
			
			logger.info("根据tmp_employee中数据,更新edu_tch_employee信息");
//			updateEmployee(dbConn);

			logger.info("将只在tmp_employee中存在的数据全部插入 sys_user");
			insertNewSysUser(dbConn);
			
			logger.info("将只在tmp_employee中存在的数据全部插入 edu_tch_employee");
			insertNewEmployee(dbConn);
			
			logger.info("删除临时表 tmp_employee");
//			dropTmpTable(dbConn);
			
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
					e.printStackTrace();
				}
			}
			
			db.closeAll();
		}
		
		return false;
	}

	// 重新创建临时表 tmp_teacher
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		logger.info(SQL_CREATE_TMP_EMPLOYEE);
		
		stmt.executeUpdate(SQL_CREATE_TMP_EMPLOYEE);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_EMPLOYEE);
		
		stmt.executeUpdate(SQL_DROP_TMP_EMPLOYEE);
		stmt.close();
	}
	
	// 将所有employee数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<Employee> employeeList) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_EMPLOYEE);
		
		for (Employee employee : employeeList) {
			prepStmt.setString(1,  employee.getUserNo());
			prepStmt.setString(2,  employee.getUsername());
			prepStmt.setString(3, employee.getEmail());
			prepStmt.setString(4, employee.getMobile());
			prepStmt.setInt(5, employee.getGender());
			prepStmt.setString(6, employee.getPostTitle());
			prepStmt.setString(7, employee.getDepartment());
			prepStmt.setString(8, employee.getPostType());
			prepStmt.setString(9, employee.getStaffType());
			prepStmt.setString(10, employee.getPostLevel());
			prepStmt.setInt(11, employee.getTeachFlag());
			prepStmt.setInt(12, employee.getRetireFlag());
			prepStmt.setInt(13, employee.getCertFlag());
			prepStmt.setInt(14, employee.getStatus());

			logger.info(prepStmt.toString() + ";" + employee.toString());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		
		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	// 更新临时表tmp_employee
	private void updateTmpEmployee(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		logger.info(SQL_UPDATE_TMP_EMPLOYEE);
		stmt.executeUpdate(SQL_UPDATE_TMP_EMPLOYEE);
		stmt.close();
	}

	// sys_user存在，但是在tmp_employee中不存在的教师帐号，置为‘逻辑删除’状态
	private void updateAccountOnlyInSysUser(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		logger.info(SQL_UPDATE_ONLY_IN_SYS_USER);
		stmt.executeUpdate(SQL_UPDATE_ONLY_IN_SYS_USER);
		stmt.close();
	}

	// PUB_User中不可用的，但是在tmp_teacher中存在的教师帐号，置为正常状态
	private void updateAccountValidExistInTmp(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

//		logger.info(SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT);
//		stmt.executeUpdate(SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT);
		stmt.close();
	}


	/**
	 * 根据tmp_employee中数据,更新sys_user信息
	 */
	private void updateSysUser(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_SYS_USER_FROM_TMP);
		stmt.executeUpdate(SQL_UPDATE_SYS_USER_FROM_TMP);
		stmt.close();
	}
	
	/**
	 * 根据tmp_employee中数据,更新edu_tch_employee信息
	 */
	private void updateEmployee(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_EMPLOYEE_FROM_TMP);
		stmt.executeUpdate(SQL_UPDATE_EMPLOYEE_FROM_TMP);
		stmt.close();
	}

	/**
	 * 将只在tmp_employee中存在的数据全部插入 sys_user
	 */
	private void insertNewSysUser(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_INSERT_SYS_USER_FROM_TMP);
		stmt.executeUpdate(SQL_INSERT_SYS_USER_FROM_TMP);
		stmt.close();
	}
	
	/**
	 * 将只在tmp_employee中存在的数据全部插入 sys_user
	 */
	private void insertNewEmployee(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_INSERT_SYS_USER_FROM_TMP);
		stmt.executeUpdate(SQL_INSERT_SYS_USER_FROM_TMP);
		stmt.close();
	}
}