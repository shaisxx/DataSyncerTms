package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.WhicoStudent;
import com.shtd.datasyncer.domain.qcjx.WhicoStudentSchoolRoll;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYin;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * whico平台学生数据保存
 * 
 * 
 *<ul>
 *	<pre>学生接口的操作说明</pre>
 *	<li>创建学生临时表。字段(学生姓名,学号,性别,身份证号)</li>
 *	<li>创建学生学籍临时表。字段(学号,院系,专业,状态,班级,入学时间,所在年级)</li>
 *	<li>将所获得接口的学生及学生学籍数据插入临时表中。</li>
 *	<li>临时表与学生表(info_student和sys_user)进行对比：
 *		<p>1.如果临时表存在的学号是学生表中未存在的，将进行新增操作，将学生、学生学籍数据插入到学生表中</p>
 *		<p>2.如果临时表存在的学号是学生表中已存在的，将进行更新操作，以学号为条件更新到学生表中</p>
 *		<p>3.如果学生表存在的学号是临时表中未存在的，已学生表为主，不进行操作</p>
 *	</li>
 *	<li>清空并删除临时表中。</li>
 *</ul>	
 * @author jiangnan
 * @date 2015.11.12
 */
public class WhicoStudentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_student
	private static final String SQL_DROP_TMP_STUDENT = " DROP TABLE IF EXISTS `tmp_student`";
	
	//创建临时表 tmp_student
	private static final String SQL_CREATE_TMP_STUDENT = "CREATE TEMPORARY TABLE `tmp_student` ("
													  +"	`id` int(11) NOT NULL auto_increment,"
													  +"	`stu_name` varchar(128) NOT NULL COMMENT '学生姓名',"
													  +"	`stu_no` varchar(32) NOT NULL COMMENT '学号',"
													  +"	`gender` varchar(2) default NULL COMMENT '性别 1-男 2-女',"
													  +"	`id_card` varchar(32) default NULL COMMENT '身份证号',"
													  +"	PRIMARY KEY  (`id`),"
													  +"	UNIQUE KEY `IDXU_stu_no` (`stu_no`)"
													  +"	) ENGINE=InnoDB DEFAULT CHARSET=utf8";

	// 批量插入 tmp_student sql语句
	private static final String SQL_INSERT_TMP_STUDENT = "INSERT INTO tmp_student(stu_name, stu_no ,gender, id_card) VALUES(?, ?, ?, ?)"; 
	
	//删除临时表 tmp_school_roll
	private static final String SQL_DROP_TMP_SCHOOL_ROLL = " DROP TABLE IF EXISTS `tmp_school_roll`";
	
	//创建临时表 tmp_school_roll
	private static final String SQL_CREATE_TMP_SCHOOL_ROLL = "CREATE TABLE `tmp_school_roll` ("
													  +"	`id` int(11) NOT NULL auto_increment,"
													  +"	`stu_no` varchar(32) NOT NULL COMMENT '学号',"
													  +"	`department` varchar(32) default NULL COMMENT '院系',"
												      +"	`major` varchar(32) default NULL COMMENT '专业',"
												      +"	`status` varchar(32) default NULL COMMENT '状态',"
												      +"	`clazz` varchar(32) default NULL COMMENT '班级',"
												      +"	`enroll_date` varchar(32) default NULL COMMENT '入学时间',"
												      +"	`grade` varchar(32) default NULL COMMENT '所在年级',"
												      +"	PRIMARY KEY  (`id`),"
												      +"	UNIQUE KEY `IDXU_stu_no` (`stu_no`)"
												      +"	) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8";
	
	//批量插入 tmp_school_roll sql语句
	private static final String SQL_INSERT_TMP_SCHOOL_ROLL = "INSERT INTO tmp_school_roll(stu_no, department, major, status, clazz, enroll_date, grade) VALUES (?, ?, ?, ?, ?, ?,?)";
	
	
	//将所有 只在 sys_user中存在 而在 tmp_student中不存在的学生帐号，设置status为屏蔽
	private static final String SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE = 
														" UPDATE sys_user AS a INNER JOIN"
													  + " ("
													  + "  SELECT sys_user.id "
													  + "    FROM sys_user "
													  + "    LEFT JOIN tmp_student "
													  + "      ON tmp_student.stu_no = sys_user.code "
													  + "   WHERE tmp_student.stu_no IS NULL "
													  + "     AND sys_user.type = 3 "
													  + "     AND sys_user.`status`=1 "
													  + " ) AS b "
													  + "    ON a.id = b.id "
													  + "   SET a.`status` = 0 ";	
	
	// 根据 tmp_student中的 工号，查询sys_user和info_student中相关数据
	private static final String SQL_SELECT_EXIST_USER_ID_AND_VALUE = " SELECT sys_user.id user_id,"
													  + " tmp_student.stu_name,"
													  + " tmp_student.stu_no,"
													  + " tmp_student.gender,"
												 	  + " tmp_student.id_card,"
													  + " tmp_school_roll.`status`,"
													  + " tmp_school_roll.enroll_date,"
													  +	" tmp_school_roll.grade,"
													  + " edu_department.id department_id,"
													  + " edu_major.id major_id,"
													  + " edu_clazz.id clazz_id"
												 	  + " FROM"
													  + " sys_user"
													  + " INNER JOIN tmp_student ON sys_user.`code` = tmp_student.stu_no"
													  + " INNER JOIN tmp_school_roll ON tmp_student.stu_no = tmp_school_roll.stu_no"
													  + " LEFT JOIN edu_clazz ON tmp_school_roll.clazz = edu_clazz.`code`"
													  + " LEFT JOIN edu_department ON tmp_school_roll.department = edu_department.`code`"
													  + " LEFT JOIN edu_major ON tmp_school_roll.major = edu_major.`code`"
													  + " WHERE sys_user.type = 3 ";
	
	//根据学生学号   更新 sys_user表, 学号为code
	private static final String SQL_UPDATE_SYS_USER = " UPDATE sys_user SET `status` = ?,`username` = ?,`logic_delete`=? WHERE code = ? and type = 3";
	
	//根据学生 user_id 更新 info_student表
	private static final String SQL_UPDATE_STUDENT = "UPDATE info_student SET pinyin=?,jianpin=?,gender=?,department_id=?,major_id=?,clazz_id=?,enroll_year=?,logic_delete=? WHERE user_id=?";
	

	// 根据 tmp_student中的 工号，查询sys_user中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_TMP_STUDENT = " SELECT tmp_student.stu_name,"
													  +" tmp_student.stu_no,"
													  +" tmp_student.gender,"
											          +" tmp_student.id_card,"
											          +" tmp_school_roll.`status`,"
											          +" tmp_school_roll.enroll_date,"
											          +" tmp_school_roll.grade,"
											          +" edu_department.id department_id,"
											          +" edu_major.id major_id,"
											          +" edu_clazz.id clazz_id"
											          +" FROM tmp_student"
											          +" INNER JOIN tmp_school_roll ON tmp_student.stu_no = tmp_school_roll.stu_no"
											          +" LEFT JOIN sys_user ON tmp_student.stu_no = sys_user.`code` AND sys_user.type = 3"
											          +" LEFT JOIN edu_clazz ON tmp_school_roll.clazz = edu_clazz.`code`"
											          +" LEFT JOIN edu_department ON tmp_school_roll.department = edu_department.`code`"
											          +" LEFT JOIN edu_major ON tmp_school_roll.major = edu_major.`code`"
											          +" WHERE sys_user.CODE IS NULL";	
			
	// 插入 sys_user表
	private static final String SQL_INSERT_SYS_USER = " INSERT INTO sys_user (code, username, password, salt, type, status, last_modify_user_id, create_date, modify_date, logic_delete) " 
													  + " VALUES (?, ?, ?, ?, '3', ?, 1, NOW(), NOW(), ?)";	

	// 插入 info_student 表 type = 3 代表学生	
	private static final String SQL_INSERT_STUDENT = " INSERT INTO info_student (user_id, pinyin, jianpin, gender, student_no, department_id, major_id, clazz_id, enroll_year,"
													  + " last_modify_user_id, create_date, modify_date, logic_delete)"
													  + " SELECT sys_user.id, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW(), NOW(), ? "
													  + " FROM sys_user WHERE sys_user.CODE = ? AND type = 3 ";
	
	//插入 bbs_user_info 表数据
	private static final String SQL_INSERT_BBS_USER_INFO = " INSERT INTO bbs_info_user (user_id, user_status, gender) SELECT"
													  + " sys_user.id, ?, ? FROM sys_user WHERE sys_user.CODE = ? AND type = 3";
	
	//password
	private static final String PASSWORD = "xQk9aYmMQcB82cKLXWy0jTIxLM4IW/AFxwM6aimptOEtKx3G11MiIccdnXKtG0qwQCsUuaAG+0EPN0+75NHMNQ==";
	//salt
	private static final String SALT = "+LNv9ZAosEuKyGwROKAHHA==";
	
	private List<WhicoStudent> mStudentList; //学生数据
	private List<WhicoStudentSchoolRoll> mStudentSchoolRollList; //学籍数据
	
	public WhicoStudentDataSaver(List<WhicoStudent> students, List<WhicoStudentSchoolRoll> studentSchoolRolls){
		mStudentList = students;
		mStudentSchoolRollList = studentSchoolRolls;
	} 
		
	/**
	 * 将学生输入插入临时表 tmp_student,学生学籍信息插入临时表tmp_school_roll
	 * left join sys_user表, 查出所有tmp_student中不存在的记录,更新status为不可用
	 * 将tmp_student 中输入插入/更新到sys_user 表中
	 * @return
	 */
	public boolean doSave() {
		if (mStudentList == null || mStudentList.size() <= 0 
				|| mStudentSchoolRollList == null || mStudentSchoolRollList.size() <= 0) {
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
			 * 创建临时表 -- tmp_student 学生学号 不能为空，且不可重复 要当作登录login name
			 * 学生姓名 不能为空
			 */
			logger.info("创建临时表 -- tmp_student,创建临时表 -- tmp_school_roll");
			recreateTmpTable(dbConn);
			
			logger.info("将学生数据插入临时表,将学生学籍信息插入临时表");
			batchInsertTmpTable(dbConn, mStudentList, mStudentSchoolRollList);
			
//			logger.info("将所有只在sys_user中存在的学生帐号，置为屏蔽状态");
//			updateNotExistStudentUnavailable(dbConn);
			
			/*
			 * 需要更新 sys_user 表的 status,username字段   和 info_student 表的  pinyin,jianpin,gender
			 */
			logger.info("更新所有 在 tmp_student 和 sys_user中都存在的学生信息 ");
			updateExistStudentData(dbConn);
			
			logger.info("将只在 tmp_student 中存在的数据 插入 sys_user 和  info_student 表 ");
			insertNewStudentData(dbConn);

			logger.info("删除临时表 tmp_student,tmp_school_roll");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("将教师数据保存到数据库 操作失败: " + e);
			
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
	
	
	// 重新创建临时表 tmp_student, tmp_school_roll
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		String[] tmpTableSql = {SQL_CREATE_TMP_STUDENT, SQL_CREATE_TMP_SCHOOL_ROLL};
		
		for(String tableSql : tmpTableSql){
			logger.info(tableSql);
			stmt.executeUpdate(tableSql);
		}
		
		stmt.close();
	}
	
	//删除临时表 tmp_student, tmp_school_roll
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		String[] dropTableSql = {SQL_DROP_TMP_STUDENT, SQL_DROP_TMP_SCHOOL_ROLL};
		
		for(String tableSql : dropTableSql){
			logger.info(tableSql);
			
			stmt.executeUpdate(tableSql);
		}
		stmt.close();
	}
	
	// 将所有student 数据插入临时表,学生学籍数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<WhicoStudent> students, List<WhicoStudentSchoolRoll> schoolRolls) 
		throws SQLException {
		
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_STUDENT);
		//将学生数据插入临时表
		for (WhicoStudent student : students) {
			prepStmt.setString(1, student.getStuName().trim());
			prepStmt.setString(2, student.getStuNo());
			prepStmt.setString(3, student.getDBGender());
			prepStmt.setString(4, student.getStuSenFenZheng());

			logger.info(prepStmt.toString() + ";" 
			          + student.getStuName() + ","
					  + student.getStuNo() + ","
			          + student.getDBGender() + ","
			          + student.getStuSenFenZheng());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		//执行sql
		prepStmt.executeBatch();
		
		//将学生学籍数据插入临时表
		prepStmt = conn.prepareStatement(SQL_INSERT_TMP_SCHOOL_ROLL);
		
		for(WhicoStudentSchoolRoll schoolRoll : schoolRolls){
			prepStmt.setString(1, schoolRoll.getStuNo());
			prepStmt.setString(2, schoolRoll.getDepartment());
			prepStmt.setString(3, schoolRoll.getMajor());
			prepStmt.setString(4, schoolRoll.getDBStatus());
			prepStmt.setString(5, schoolRoll.getClazz());
			prepStmt.setString(6, schoolRoll.getEnrollDate());
			prepStmt.setString(7, schoolRoll.getGrade());
			
			logger.info(prepStmt.toString() + ";" 
			          + schoolRoll.getStuNo() + ","
					  + schoolRoll.getDepartment() + ","
			          + schoolRoll.getMajor() + ","
			          + schoolRoll.getDBStatus() + ","
			          + schoolRoll.getClazz() + ","
			          + schoolRoll.getEnrollDate() + ","
			          + schoolRoll.getGrade() );
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		//执行sql
		prepStmt.executeBatch();
		
		prepStmt.close();
	}
	
	/*
	 * 将所有 只在 sys_user中存在 而在 tmp_student中不存在的学生帐号，设置status为屏蔽 
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistStudentUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}

	/*
	 * 需要更新sys_user 表的 status,username 字段
	 * info_student 表的 pinyin,jianpin,gender 字段
	 */
	private void updateExistStudentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_UPDATE_SYS_USER);
		PreparedStatement studentPst      = conn.prepareStatement(SQL_UPDATE_STUDENT);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_USER_ID_AND_VALUE); 
		if (rs != null) {
			
			String stuNo = "", username = "", pinyin = "", pinyinHeader = "", logicDelete="";
			while(rs.next()) {				
				/*
				 * 循环中每一条记录 需要更新 sys_user 和 info_student 两个表
				 */
				securityUserPst.setString(1, rs.getString("status"));
				
				username = rs.getString("stu_name");
				securityUserPst.setString(2, username);
				
				logicDelete = Integer.parseInt(rs.getString("status"))==0?"1":"0";
				securityUserPst.setString(3, logicDelete);
				
				stuNo = rs.getString("stu_no");
				securityUserPst.setString(4, stuNo);
				
				logger.info(securityUserPst.toString() + ";" + rs.getString("status") +","+ username +","+ logicDelete +","+stuNo);
				
				securityUserPst.addBatch();
				
				pinyin = PinYin.getPingYin(username);
				studentPst.setString(1, pinyin);
				
				pinyinHeader = PinYin.getPinYinHeadChar(username);
				studentPst.setString(2, pinyinHeader);
				studentPst.setString(3, rs.getString("gender"));
				studentPst.setString(4, rs.getString("department_id"));
				studentPst.setString(5, rs.getString("major_id"));
				studentPst.setString(6, rs.getString("clazz_id"));
//				studentPst.setString(7, rs.getString("enroll_date"));
				studentPst.setString(7, rs.getString("grade"));
				studentPst.setString(8, logicDelete);
				studentPst.setString(9, rs.getString("user_id"));
				
				logger.info(studentPst.toString() + ";" 
						  + pinyin + ","
						  + pinyinHeader + ","
						  + rs.getString("gender") + ","
						  + rs.getString("department_id")+","
						  + rs.getString("major_id")+","
						  + rs.getString("clazz_id")+","
						  + rs.getString("grade")+","
						  + logicDelete+","
						  + rs.getString("user_id"));
				
				studentPst.addBatch();				
			};

			securityUserPst.executeBatch();
			studentPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		studentPst.close();
	}
	
	/*
	 * 将只在 tmp_student 中存在的数据 插入 sys_user , info_student 表 和 bbs_user_info表插入数据
	 */
	private void insertNewStudentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_INSERT_SYS_USER);
		PreparedStatement studentPst      = conn.prepareStatement(SQL_INSERT_STUDENT);
		//bbs_user_info同步数据
		PreparedStatement bbsUserPst      = conn.prepareStatement(SQL_INSERT_BBS_USER_INFO);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_TMP_STUDENT); 
		if (rs != null) {
			String stuNo = "", username = "", pinyin = "", pinyinHeader = "", logicDelete="";
			while(rs.next()) {
				// 循环中每一条记录 需要更新 sys_user 和 info_student 两个表
				stuNo = rs.getString("stu_no");
				securityUserPst.setString(1, stuNo);
				
				username = rs.getString("stu_name");
				securityUserPst.setString(2, username);
				securityUserPst.setString(3, PASSWORD);
				securityUserPst.setString(4, SALT);
				securityUserPst.setString(5, rs.getString("status"));
				
				logicDelete = Integer.parseInt(rs.getString("status"))==0?"1":"0";
				securityUserPst.setString(6, logicDelete);
				
				logger.info(securityUserPst.toString() + ";" + stuNo + "," + username +","+ rs.getString("status")+","+ logicDelete);
				
				securityUserPst.addBatch();
				
				pinyin = PinYin.getPingYin(username);
				studentPst.setString(1, pinyin);
				pinyinHeader = PinYin.getPinYinHeadChar(username);
				studentPst.setString(2, pinyinHeader);
				studentPst.setString(3, rs.getString("gender"));
				studentPst.setString(4, stuNo);
				studentPst.setString(5, rs.getString("department_id"));
				studentPst.setString(6, rs.getString("major_id"));
				studentPst.setString(7, rs.getString("clazz_id"));
//				studentPst.setString(8, rs.getString("enroll_date"));
				studentPst.setString(8, rs.getString("grade"));
				studentPst.setString(9, logicDelete);
				studentPst.setString(10, stuNo);
				
				logger.info(studentPst.toString() + ";" 
						  + pinyin + ","
						  + pinyinHeader + ","
						  + rs.getString("gender") + ","
						  + stuNo + ","
						  + rs.getString("department_id")+","
						  + rs.getString("major_id")+","
						  + rs.getString("clazz_id")+","
						  + rs.getString("grade")+","
						  + logicDelete+","
						  + stuNo);
				
				studentPst.addBatch();				
				
				//bbs_user_info 同步数据
				bbsUserPst.setString(1, rs.getString("status"));
				bbsUserPst.setString(2, rs.getString("gender"));
				bbsUserPst.setString(3, stuNo);
				
				logger.info(bbsUserPst.toString() + ";" + rs.getString("status") + "," + rs.getString("gender") +","+ stuNo);
				bbsUserPst.addBatch();				
			};

			securityUserPst.executeBatch();
			studentPst.executeBatch();
			bbsUserPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		studentPst.close();
		bbsUserPst.close();
	}
	
}
