package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.AppraisalStudent;
import com.shtd.datasyncer.domain.qcjx.AppraisalStudentSchoolRoll;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * 教学评价系统学生数据保存
 * @author Josh
 */
public class AppraisalStudentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_student
	private static final String SQL_DROP_TMP_STUDENT = " DROP TABLE IF EXISTS `tmp_student`";
	
	//创建临时表 tmp_student
	private static final String SQL_CREATE_TMP_STUDENT = "CREATE TABLE `tmp_student` ("
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
	
	
	//将所有 只在 security_user中存在 而在 tmp_student中不存在的学生帐号，设置status为屏蔽
	private static final String SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE = 
														" UPDATE security_user AS a INNER JOIN"
													  + " ("
													  + "  SELECT security_user.id "
													  + "    FROM security_user "
													  + "    LEFT JOIN tmp_student "
													  + "      ON tmp_student.stu_no = security_user.login_name "
													  + "   WHERE tmp_student.stu_no IS NULL "
													  + "     AND security_user.type = 2 "
													  + "     AND security_user.`status`=1 "
													  + " ) AS b "
													  + "    ON a.id = b.id "
													  + "   SET a.`status` = 0 ";	
	
	// 根据 tmp_student中的 工号，查询security_user和student中相关数据
	private static final String SQL_SELECT_EXIST_USER_ID_AND_VALUE = " SELECT security_user.id userId,"
													  + " tmp_student.stu_name,"
													  + " tmp_student.stu_no,"
													  + " tmp_student.gender,"
												 	  + " tmp_student.id_card,"
													  + " tmp_school_roll.`status`,"
													  + " tmp_school_roll.enroll_date,"
													  + " faculty.id facultyId,"
													  + " major.id majorId,"
													  + " clazz.id clazzId"
												 	  + " FROM"
													  + " security_user"
													  + " INNER JOIN tmp_student ON security_user.login_name = tmp_student.stu_no"
													  + " INNER JOIN tmp_school_roll ON tmp_student.stu_no = tmp_school_roll.stu_no"
													  + " LEFT JOIN clazz ON tmp_school_roll.clazz = clazz.`code`"
													  + " LEFT JOIN faculty ON tmp_school_roll.department = faculty.`code`"
													  + " LEFT JOIN major ON tmp_school_roll.major = major.`code`"
													  + " WHERE security_user.type = 2 ";
	
	//根据学生学号   更新 security_user表, 学号为code
	private static final String SQL_UPDATE_SYS_USER = " UPDATE security_user SET `status` = ? WHERE login_name = ? and type = 2";
	
	//根据学生id 更新 student表
	private static final String SQL_UPDATE_STUDENT = "UPDATE student SET gender=?, clazzId=? WHERE id=?";
	

	// 根据 tmp_student中的 工号，查询security_user中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_TMP_STUDENT = " SELECT tmp_student.stu_name,"
													  +" tmp_student.stu_no,"
													  +" tmp_student.gender,"
											          +" tmp_student.id_card,"
											          +" tmp_school_roll.`status`,"
											          +" tmp_school_roll.enroll_date,"
											          +" faculty.id facultyId,"
											          +" major.id majorId,"
											          +" clazz.id clazzId"
											          +" FROM tmp_student"
											          +" INNER JOIN tmp_school_roll ON tmp_student.stu_no = tmp_school_roll.stu_no"
											          +" LEFT JOIN security_user ON tmp_student.stu_no = security_user.login_name AND security_user.type = 2"
											          +" LEFT JOIN clazz ON tmp_school_roll.clazz = clazz.`code`"
											          +" LEFT JOIN faculty ON tmp_school_roll.department = faculty.`code`"
											          +" LEFT JOIN major ON tmp_school_roll.major = major.`code`"
											          +" WHERE security_user.login_name IS NULL";	
			
	// 插入 security_user表
	private static final String SQL_INSERT_SYS_USER = " INSERT INTO security_user (login_name, password, type, status, create_date, update_date) " 
													  + " VALUES (?, ?, '2', ?, NOW(), NOW())";	

	// 插入 student 表 type = 2 代表学生
	private static final String SQL_INSERT_STUDENT = " INSERT INTO student (id, gender, student_no, student_name, clazz_id,"
													  + " create_date, update_date )"
													  + " SELECT security_user.id, ?, ?, ?, ?, NOW(), NOW() "
													  + " FROM security_user WHERE security_user.login_name = ? AND security_user.type = 2 ";
	
	//password
	private static final String PASSWORD = "805d0ddf5ae8e65d8cf869040586ae3a";
	
	private List<AppraisalStudent> mStudentList; //学生数据
	private List<AppraisalStudentSchoolRoll> mStudentSchoolRollList; //学籍数据
	
	public AppraisalStudentDataSaver(List<AppraisalStudent> students, List<AppraisalStudentSchoolRoll> studentSchoolRolls){
		mStudentList = students;
		mStudentSchoolRollList = studentSchoolRolls;
	} 
		
	/**
	 * 将学生输入插入临时表 tmp_student,学生学籍信息插入临时表tmp_school_roll
	 * left join security_user表, 查出所有tmp_student中不存在的记录,更新status为不可用
	 * 将tmp_student 中输入插入/更新到security_user 表中
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
			
			logger.info("将所有只在security_user中存在的学生帐号，置为屏蔽状态");
			updateNotExistStudentUnavailable(dbConn);
			
			/*
			 * 需要更新 security_user 表的 status,username字段   和 student 表的  pinyin,jianpin,gender
			 */
			logger.info("更新所有 在 tmp_student 和 security_user中都存在的学生信息 ");
			updateExistStudentData(dbConn);
			
			logger.info("将只在 tmp_student 中存在的数据 插入 security_user 和  student 表 ");
			insertNewStudentData(dbConn);

			logger.info("删除临时表 tmp_student,tmp_school_roll");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("将学生数据保存到数据库 操作失败: " + e);
			
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
	private void batchInsertTmpTable(Connection conn, List<AppraisalStudent> students, List<AppraisalStudentSchoolRoll> schoolRolls) 
		throws SQLException {
		
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_STUDENT);
		//将学生数据插入临时表
		for (AppraisalStudent student : students) {
			prepStmt.setString(1, student.getStuName());
			prepStmt.setString(2, student.getStuNo());
			prepStmt.setString(3, student.getStuGender());
			prepStmt.setString(4, student.getStuSenFenZheng());

			logger.info(prepStmt.toString() + ";" 
			          + student.getStuName() + ","
					  + student.getStuNo() + ","
			          + student.getStuGender() + ","
			          + student.getStuSenFenZheng());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		//执行sql
		prepStmt.executeBatch();
		
		//将学生学籍数据插入临时表
		prepStmt = conn.prepareStatement(SQL_INSERT_TMP_SCHOOL_ROLL);
		
		for(AppraisalStudentSchoolRoll schoolRoll : schoolRolls){
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
	 * 将所有 只在 security_user中存在 而在 tmp_student中不存在的学生帐号，设置status为屏蔽 
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
	 * 需要更新security_user 表的 status,username 字段
	 * student 表的 pinyin,jianpin,gender 字段
	 */
	private void updateExistStudentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_UPDATE_SYS_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_UPDATE_STUDENT);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_USER_ID_AND_VALUE); 
		if (rs != null) {
			
			String stuNo = "";
			while(rs.next()) {				
				/*
				 * 循环中每一条记录 需要更新 security_user 和 student 两个表
				 */
				securityUserPst.setString(1, rs.getString("status"));
				
				stuNo = rs.getString("stu_no");
				securityUserPst.setString(2, stuNo);

				logger.info(securityUserPst.toString() + ";" + rs.getString("status")+","+stuNo);
				
				securityUserPst.addBatch();
				
				teacherPst.setString(1, rs.getString("gender"));
				teacherPst.setString(2, rs.getString("clazzId"));
				teacherPst.setString(3, rs.getString("userId"));
				
				logger.info(teacherPst.toString() + ";" 
						  + rs.getString("gender") + ","
						  + rs.getString("clazzId")+","
						  + rs.getString("userId"));
				
				teacherPst.addBatch();				
			};

			securityUserPst.executeBatch();
			teacherPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		teacherPst.close();
	}
	
	/*
	 * 将只在 tmp_student 中存在的数据 插入 security_user 和  student 表
	 */
	private void insertNewStudentData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_INSERT_SYS_USER);
		PreparedStatement studentPst      = conn.prepareStatement(SQL_INSERT_STUDENT);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_TMP_STUDENT); 
		if (rs != null) {
			String stuNo = "",stuName = "";
			while(rs.next()) {
				// 循环中每一条记录 需要更新 security_user 和 student 两个表
				stuNo = rs.getString("stu_no");
				securityUserPst.setString(1, stuNo);
				securityUserPst.setString(2, PASSWORD);
				securityUserPst.setInt(3, StringUtils.isBlank(rs.getString("status"))?0:Integer.valueOf(rs.getString("status")));
				
				logger.info(securityUserPst.toString() + ";" + stuNo + "," + rs.getString("status"));
				
				securityUserPst.addBatch();
				
				studentPst.setInt(1, StringUtils.isBlank(rs.getString("gender"))?0:Integer.valueOf(rs.getString("gender")));
				studentPst.setString(2, stuNo);
				studentPst.setString(3, rs.getString("stu_name"));
				studentPst.setInt(4, StringUtils.isBlank(rs.getString("clazzId"))?0:Integer.valueOf(rs.getString("clazzId")));
				studentPst.setString(5, stuNo);
				
				logger.info(studentPst.toString() + ";" 
						  + rs.getString("gender") + ","
						  + stuNo + ","
						  + stuName + ","
						  + rs.getString("clazzId")+","
						  + stuNo);
				
				studentPst.addBatch();				
				
			};

			securityUserPst.executeBatch();
			studentPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		studentPst.close();
	}
}