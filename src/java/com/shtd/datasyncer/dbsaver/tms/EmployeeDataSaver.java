package com.shtd.datasyncer.dbsaver.tms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.RestoreAction;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.tms.Employee;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYinUtil;
import com.shtd.datasyncer.utils.SendEmail;
import com.shtd.datasyncer.utils.db.MysqlDb;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

/**
 * 教职工数据保存
 * @author Josh
 */
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
														"SET user_id = (SELECT id FROM sys_user WHERE user_no = tmp_employee.user_no LIMIT 1)," + 
															"department_id = (SELECT id FROM dict_edu_common_department WHERE title = tmp_employee.department LIMIT 1)," +
															"post_type_id = (SELECT id FROM dict_edu_tch_post_type WHERE title = tmp_employee.post_type LIMIT 1)," +
															"staff_type_id = (SELECT id FROM dict_edu_tch_staff_type WHERE title = tmp_employee.staff_type LIMIT 1)," +
															"post_level_id = (SELECT id FROM dict_edu_tch_post_level WHERE title = tmp_employee.post_level LIMIT 1)";
	
	private static final String SQL_SELECT_TMP_EMPLOYEE = " SELECT " +
															" te.user_id, " +
															" te.user_no, " +
															" te.username, " +
															" te.email, " +
															" te.mobile, " +
															" te.gender, " +
															" te.post_title, " +
															" te.department_id, " +
															" te.department, " +
															" te.post_type_id, " +
															" te.post_type, " +
															" te.staff_type_id, " +
															" te.staff_type, " +
															" te.post_level_id, " +
															" te.post_level, " +
															" te.teach_flag, " +
															" te.retire_flag, " +
															" te.cert_flag, " +
															" te.`status` " +
															" FROM " +
															" tmp_employee te";

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

	private static final String SQL_SELECT_CUR_EMPLOYEE = " SELECT " +
														" ete.user_id, " +
														" su.user_no, " +
														" su.username, " +
														" su.email, " +
														" su.mobile, " +
														" ete.gender, " +
														" ete.post_title, " +
														" decd.title department, " +
														" detpt.title post_type, " +
														" detst.title staff_type, " +
														" detpl.title post_level, " +
														" ete.teach_flag, " +
														" ete.retire_flag, " +
														" ete.cert_flag, " +
														" ete.`status` " +
														" FROM " +
														" edu_tch_employee ete " +
														" INNER JOIN sys_user su ON su.id = ete.user_id " +
														" LEFT JOIN dict_edu_common_department decd ON decd.id = ete.department_id " +
														" LEFT JOIN dict_edu_tch_post_type detpt ON detpt.id = ete.post_type_id " +
														" LEFT JOIN dict_edu_tch_staff_type detst ON detst.id = ete.staff_type_id " +
														" LEFT JOIN dict_edu_tch_post_level detpl ON detpl.id = ete.post_level_id ";
	

	// 将只在tmp_employee中存在的数据全部插入 sys_user
	private static final String SQL_INSERT_SYS_USER = " INSERT INTO sys_user(user_no,username,email,mobile) VALUE(?,?,?,?)";
	
	// 将只在tmp_employee中存在的数据全部插入 edu_tch_employee
	private static final String SQL_INSERT_EDU_TCH_EMPLOYEE = " INSERT INTO edu_tch_employee ( " +
																" user_id, " +
																" pinyin, " +
																" jianpin, " +
																" gender, " +
																" post_title, " +
																" department_id, " +
																" post_type_id, " +
																" staff_type_id, " +
																" post_level_id, " +
																" teach_flag, " +
																" retire_flag, " +
																" cert_flag, " +
																" STATUS, " +
																" last_modify_user_id, " +
																" create_date, " +
																" modify_date " +
																" ) " +
																" VALUE " +
																" (?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),now())";
	
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
			
			// 查询当前教职工数据 （ 以备后用）
			List<Employee> curEmployeeList = new ArrayList<Employee>();
			selectCurEmployeeList(dbConn, curEmployeeList);
			
			// 创建临时表 -- tmp_employee
			recreateTmpTable(dbConn);
			
			// 将教师数据插入临时表
			batchInsertTmpTable(dbConn, mEmployeeList);
			
			// 更新临时表tmp_employee
			updateTmpEmployee(dbConn);
			
			// 查询临时表教职工数据（ 以备后用）
			List<Employee> tmpEmployeeList = new ArrayList<Employee>();
			selectTmpEmployeeList(dbConn, tmpEmployeeList);
			
			// 比较需要更新的教职工数据，并发送邮件
			String diffMsgContent = diffEmployeeList(tmpEmployeeList, curEmployeeList);
			
			// 将只在tmp_employee中存在的数据全部插入sys_user、edu_tch_employee
			List<Employee> insertEmployeeList = getInsertEmployeeList(tmpEmployeeList, curEmployeeList);
			String insertMsgContent = insertNewEmployee(db, insertEmployeeList);
			
			if(StringUtils.isNotBlank(diffMsgContent) || StringUtils.isNotBlank(insertMsgContent)){
				logger.info("将同步日志信息发送邮件给管理员");
				SendEmail.Send(insertMsgContent + diffMsgContent);
			}
			
			// 删除临时表 tmp_employee
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
					e.printStackTrace();
				}
			}
			
			db.closeAll();
		}
		
		return false;
	}
	
	private void selectCurEmployeeList(Connection conn, List<Employee> employeeList) throws SQLException {
		Statement stmt = conn.createStatement();
		logger.info("开始查询教职工表edu_tch_employee数据");
		logger.info(SQL_SELECT_CUR_EMPLOYEE);
		ResultSet set = stmt.executeQuery(SQL_SELECT_CUR_EMPLOYEE);

		Employee employee = null;
		while (set.next()) {
			employee = new Employee();
			employee.setUserId(set.getInt(1));
			employee.setUserNo(set.getString(2));
			employee.setUsername(set.getString(3));
			employee.setEmail(set.getString(4));
			employee.setMobile(set.getString(5));
			employee.setGender(set.getInt(6));
			employee.setPostTitle(set.getString(7));
			employee.setDepartment(set.getString(8));
			employee.setPostType(set.getString(9));
			employee.setStaffType(set.getString(10));
			employee.setPostLevel(set.getString(11));
			employee.setTeachFlag(set.getInt(12));
			employee.setRetireFlag(set.getInt(13));
			employee.setCertFlag(set.getInt(14));
			employee.setStatus(set.getInt(15));
			employeeList.add(employee);
		}
		stmt.close();
		logger.info("查询教职工表edu_tch_employee完毕");
	}

	private void selectTmpEmployeeList(Connection conn, List<Employee> employeeList) throws SQLException {
		logger.info("开始查询临时表tmp_employee数据");
		Statement stmt = conn.createStatement();
		logger.info(SQL_SELECT_TMP_EMPLOYEE);
		ResultSet set = stmt.executeQuery(SQL_SELECT_TMP_EMPLOYEE);

		Employee employee = null;
		while (set.next()) {
			employee = new Employee();
			employee.setUserId(set.getInt(1));
			employee.setUserNo(set.getString(2));
			employee.setUsername(set.getString(3));
			employee.setEmail(set.getString(4));
			employee.setMobile(set.getString(5));
			employee.setGender(set.getInt(6));
			employee.setPostTitle(set.getString(7));
			employee.setDepartmentId(set.getInt(8));
			employee.setDepartment(set.getString(9));
			employee.setPostTypeId(set.getInt(10));
			employee.setPostType(set.getString(11));
			employee.setStaffTypeId(set.getInt(12));
			employee.setStaffType(set.getString(13));
			employee.setPostLevelId(set.getInt(14));
			employee.setPostLevel(set.getString(15));
			employee.setTeachFlag(set.getInt(16));
			employee.setRetireFlag(set.getInt(17));
			employee.setCertFlag(set.getInt(18));
			employee.setStatus(set.getInt(19));
			employeeList.add(employee);
		}
		stmt.close();
		logger.info("查询临时表tmp_employee数据完毕");
	}
	
	// 创建临时表 -- tmp_employee
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		logger.info("创建临时表 -- tmp_employee");
		Statement stmt = conn.createStatement();		
		logger.info(SQL_CREATE_TMP_EMPLOYEE);
		stmt.executeUpdate(SQL_CREATE_TMP_EMPLOYEE);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		logger.info("删除临时表 tmp_employee");
		Statement stmt = conn.createStatement();
		logger.info(SQL_DROP_TMP_EMPLOYEE);
		stmt.executeUpdate(SQL_DROP_TMP_EMPLOYEE);
		stmt.close();
	}
	
	// 将所有employee数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<Employee> employeeList) throws SQLException {
		logger.info("开始批量将教职工数据插入临时表tmp_employee");
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
		logger.info("批量插入教职工数据完毕");
	}
	
	
	// 更新临时表tmp_employee
	private void updateTmpEmployee(Connection conn) throws SQLException {
		logger.info("开始批量更新临时表tmp_employee");
		Statement stmt = conn.createStatement();
		logger.info(SQL_UPDATE_TMP_EMPLOYEE);
		stmt.executeUpdate(SQL_UPDATE_TMP_EMPLOYEE);
		stmt.close();
		logger.info("批量更新临时表tmp_employee完毕");
	}

	// sys_user存在，但是在tmp_employee中不存在的教师帐号，置为‘逻辑删除’状态
	private void updateEmployeeOnlyInSysUser(Connection conn) throws SQLException {
		logger.info("sys_user存在，但是在tmp_employee中不存在的教师帐号，置为‘逻辑删除’状态");
		Statement stmt = conn.createStatement();
		logger.info(SQL_UPDATE_ONLY_IN_SYS_USER);
		stmt.executeUpdate(SQL_UPDATE_ONLY_IN_SYS_USER);
		stmt.close();
	}

	/**
	 * 根据tmp_employee中数据,更新sys_user信息
	 */
	private void updateSysUser(Connection conn) throws SQLException {
		logger.info("根据tmp_employee中数据,更新sys_user信息");
		Statement stmt = conn.createStatement();
		logger.info(SQL_UPDATE_SYS_USER_FROM_TMP);
		stmt.executeUpdate(SQL_UPDATE_SYS_USER_FROM_TMP);
		stmt.close();
	}
	
	/**
	 * 根据tmp_employee中数据,更新edu_tch_employee信息
	 */
	private void updateEmployee(Connection conn) throws SQLException {
		logger.info("根据tmp_employee中数据,更新edu_tch_employee信息");
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_EMPLOYEE_FROM_TMP);
		stmt.executeUpdate(SQL_UPDATE_EMPLOYEE_FROM_TMP);
		stmt.close();
	}

	private String insertNewEmployee(MysqlDb db, List<Employee> employeeList) throws SQLException {
		StringBuffer mailMsgSb = new StringBuffer();
		StringBuffer logMsgSb = new StringBuffer();
		Connection dbConn = null;
		try {
			dbConn = db.getConn();
			
			if (dbConn == null) {
				logger.error("未获取有效数据库连接，操作失败");
			}
			
			dbConn.setAutoCommit(false);
			
			logger.info("将只在tmp_employee中存在的数据全部插入sys_user、edu_tch_employee,插入数量:" + employeeList.size());
	        PreparedStatement preparedStatement = null;  
	        ResultSet rs = null;  
	        if(employeeList != null && employeeList.size() > 0){
            	for(Employee employee:employeeList){
    		        preparedStatement = dbConn.prepareStatement(SQL_INSERT_SYS_USER, Statement.RETURN_GENERATED_KEYS);  
    		        preparedStatement.setString(1, employee.getUserNo());  
    		        preparedStatement.setString(2, employee.getUsername());
    		        preparedStatement.setString(3, StringUtils.isNotBlank(employee.getEmail())?employee.getEmail():" ");
    		        preparedStatement.setString(4, StringUtils.isNotBlank(employee.getMobile())?employee.getMobile():" ");
    		        preparedStatement.executeUpdate();  
    		        rs = preparedStatement.getGeneratedKeys();  
    		        
    		        Object retId = (rs.next())?rs.getObject(1):null; 
    		        
    		        employee.setUserId(Integer.valueOf(((Long)retId).toString()));
    		        
    		        preparedStatement = dbConn.prepareStatement(SQL_INSERT_EDU_TCH_EMPLOYEE);  
    		        preparedStatement.setInt(1, employee.getUserId());  
    		        preparedStatement.setString(2, StringUtils.isNotBlank(employee.getUsername())?PinYinUtil.getPingYin(employee.getUsername()):" ");
    		        preparedStatement.setString(3, StringUtils.isNotBlank(employee.getUsername())?PinYinUtil.getPinYinHeadChar(employee.getUsername()):" ");
    		        preparedStatement.setInt(4, employee.getGender());
    		        preparedStatement.setString(5, employee.getPostTitle());
    		        preparedStatement.setInt(6, employee.getDepartmentId());
    		        preparedStatement.setInt(7, employee.getPostTypeId());
    		        preparedStatement.setInt(8, employee.getStaffTypeId());
    		        preparedStatement.setInt(9, employee.getPostLevelId());
    		        preparedStatement.setInt(10, employee.getTeachFlag());
    		        preparedStatement.setInt(11, employee.getRetireFlag());
    		        preparedStatement.setInt(12, employee.getCertFlag());
    		        preparedStatement.setInt(13, employee.getStatus());
    		        preparedStatement.setInt(14, 1);
    		        preparedStatement.executeUpdate();  
    		        
    		        logMsgSb.append("\n [Add Employee] : " + employee.toString());
    		        mailMsgSb.append("</br> [Add Employee] : " + employee.toString());
            	}
            	logger.info(logMsgSb.toString());
	        }
	        
			dbConn.commit();
		} catch (Exception e) {
			logger.error("将教职工数据保存到数据库，操作失败，数据库回滚 : " + e);
			if(dbConn != null){
				dbConn.rollback();
			}
			
		} 
		logger.info("数据插入sys_user、edu_tch_employee完毕");
		
		return mailMsgSb.toString();
	}
	
	private List<Employee> getInsertEmployeeList(List<Employee> employeeList, List<Employee> curEmployeeList){
		List<Employee> insertEmployeeList = new ArrayList<Employee>();
		if(employeeList != null && employeeList.size() > 0
				&& curEmployeeList != null && curEmployeeList.size() > 0){
			
			Map<String, Employee> removeMap = new HashMap<String, Employee>();
			Map<String, Employee> emMap = new HashMap<String, Employee>();
			for(Employee employee:employeeList){
				emMap.put(employee.getUserNo(), employee);
				insertEmployeeList.add(employee);
			}
			
			for(Employee employee:curEmployeeList){
				if(emMap.containsKey(employee.getUserNo())){
					removeMap.put(employee.getUserNo(), emMap.get(employee.getUserNo()));
				}
			}
			
			for(Employee employee: removeMap.values()){
				insertEmployeeList.remove(employee);
			}
		}
		
		return insertEmployeeList;
	}
	
	private String diffEmployeeList(List<Employee> employeeList, List<Employee> curEmployeeList){
		
		StringBuffer mailMsgSb = new StringBuffer();
		StringBuffer logMsgSb = new StringBuffer();
		if(employeeList != null && employeeList.size() > 0
				&& curEmployeeList != null && curEmployeeList.size() > 0){
			
			Map<String, Employee> emMap = new HashMap<String, Employee>();
			for(Employee employee:employeeList){
				emMap.put(employee.getUserNo(), employee);
			}
			for(Employee curEmployee:curEmployeeList){
				if(emMap.containsKey(curEmployee.getUserNo())){
					Employee employee = emMap.get(curEmployee.getUserNo());
					if(curEmployee.getUserNo().equals(employee.getUserNo())){
						String diffStr = diffEmployee(employee, curEmployee);
						if(StringUtils.isNotBlank(diffStr)){
							logMsgSb.append("\n [Update Employee] : [userNo=" + employee.getUserNo() + "," + diffStr + "]");
							mailMsgSb.append("</br> [Update Employee] : [userNo=" + employee.getUserNo() + "," + diffStr + "]");
						}
					}
				}
			}
			logger.info(logMsgSb.toString());
		}
		return mailMsgSb.toString();
	}
	
	private String diffEmployee(Employee employee, Employee curEmployee){
		StringBuffer retSb = new StringBuffer();
		if(StringUtils.isNotBlank(employee.getUsername()) 
				&& StringUtils.isNotBlank(curEmployee.getUsername())
				&& !employee.getUsername().equals(curEmployee.getUsername())){
			
			retSb.append("username=" + employee.getUsername() + "|" + curEmployee.getUsername() + ",");
		}
		if(StringUtils.isNotBlank(employee.getEmail())
				&& StringUtils.isNotBlank(curEmployee.getEmail())
				&& !employee.getEmail().equals(curEmployee.getEmail())){
			
			retSb.append("email=" + employee.getEmail() + "|" + curEmployee.getEmail() + ",");
		}
		if(StringUtils.isNotBlank(employee.getMobile())
				&& StringUtils.isNotBlank(curEmployee.getMobile())
				&& !employee.getMobile().equals(curEmployee.getMobile())){
			
			retSb.append("mobile=" + employee.getMobile() + "|" + curEmployee.getMobile() + ",");
		}
		if(!(employee.getGender().intValue()==curEmployee.getGender().intValue())){
			retSb.append("gender=" + (employee.getGender().intValue()==Employee.GENDER_MALE?Employee.GENDER_NAME_MALE:Employee.GENDER_NAME_FEMALE) + "|" + (curEmployee.getGender().intValue()==Employee.GENDER_MALE?Employee.GENDER_NAME_MALE:Employee.GENDER_NAME_FEMALE) + ",");
		}
		if(StringUtils.isNotBlank(employee.getPostTitle())
				&& StringUtils.isNotBlank(curEmployee.getPostTitle())
				&& !employee.getPostTitle().equals(curEmployee.getPostTitle())){
			
			retSb.append("postTitle=" + employee.getPostTitle() + "|" + curEmployee.getPostTitle() + ",");
		}
		if(!employee.getDepartment().equals(curEmployee.getDepartment())){
			retSb.append("department=" + employee.getDepartment() + "|" + curEmployee.getDepartment() + ",");
		}
		if(!employee.getPostType().equals(curEmployee.getPostType())){
			retSb.append("postType=" + employee.getPostType() + "|" + curEmployee.getPostType() + ",");
		}
		if(!employee.getStaffType().equals(curEmployee.getStaffType())){
			retSb.append("staffType=" + employee.getStaffType() + "|" + curEmployee.getStaffType() + ",");
		}
		if(!employee.getPostLevel().equals(curEmployee.getPostLevel())){
			retSb.append("postLevel=" + employee.getPostLevel() + "|" + curEmployee.getPostLevel() + ",");
		}
		if(!(employee.getTeachFlag().intValue()==curEmployee.getTeachFlag().intValue())){
			retSb.append("teachFlag=" + (employee.getTeachFlag().intValue()==Employee.TEACH_FLAG_YES?Employee.TEACH_FLAG_NAME_YES:Employee.TEACH_FLAG_NAME_NO) + "|" + (curEmployee.getTeachFlag().intValue()==Employee.TEACH_FLAG_YES?Employee.TEACH_FLAG_NAME_YES:Employee.TEACH_FLAG_NAME_NO) + ",");
		}
		if(!(employee.getRetireFlag().intValue()==curEmployee.getRetireFlag().intValue())){
			retSb.append("retireFlag=" + (employee.getRetireFlag().intValue()==Employee.RETIRE_FLAG_YES?Employee.RETIRE_FLAG_NAME_YES:Employee.RETIRE_FLAG_NAME_NO) + "|" + (curEmployee.getRetireFlag().intValue()==Employee.RETIRE_FLAG_YES?Employee.RETIRE_FLAG_NAME_YES:Employee.RETIRE_FLAG_NAME_NO) +  ",");
		}
		if(!(employee.getCertFlag().intValue()==curEmployee.getCertFlag().intValue())){
			retSb.append("certFlag=" + (employee.getCertFlag().intValue()==Employee.CERT_FLAG_YES?Employee.CERT_FLAG_NAME_YES:Employee.CERT_FLAG_NAME_NO) + "|" + (curEmployee.getCertFlag().intValue()==Employee.CERT_FLAG_YES?Employee.CERT_FLAG_NAME_YES:Employee.CERT_FLAG_NAME_NO) + ",");
		}
		if(!(employee.getStatus().intValue()==curEmployee.getStatus().intValue())){
			retSb.append("status=" + (employee.getStatus().intValue()==Employee.STATUS_YES?Employee.STATUS_NAME_YES:Employee.STATUS_NAME_NO) + "|" + (curEmployee.getStatus().intValue()==Employee.STATUS_YES?Employee.STATUS_NAME_YES:Employee.STATUS_NAME_NO) + ",");
		}
		return retSb.toString().lastIndexOf(",")!=-1&&retSb.toString().lastIndexOf(",")==(retSb.toString().length()-1)?retSb.substring(0, retSb.toString().length()-1):"";
	}
}