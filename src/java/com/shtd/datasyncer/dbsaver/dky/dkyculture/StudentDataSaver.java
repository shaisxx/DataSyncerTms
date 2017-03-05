package com.shtd.datasyncer.dbsaver.dky.dkyculture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

public class StudentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	@SuppressWarnings("unused")
	private static final int STATUS_UNAVAILABLE = 0; 
	private static final int STATUS_AVAILABLE   = 1; 
	
	// 删除 临时表 tmp_student
	private static final String SQL_DROP_TMP_STUDENT = " DROP TABLE IF EXISTS `tmp_student`";
	
    // 创建 临时表 tmp_student
	private static final String SQL_CREATE_TMP_STUDENT = " CREATE TEMPORARY TABLE `tmp_student` (" 
			                                           + "    `id` int(10) NOT NULL auto_increment ,"
			                                           + "    `loginname` varchar(50) NOT NULL COMMENT '登录名',"
			                                           + "    `username` varchar(100) NOT NULL COMMENT '真实姓名',"
			                                           + "    `sex` varchar(2) default NULL,"
			                                           + "    `birthday` datetime default NULL,"
			                                           + "    `number` varchar(50) default NULL COMMENT '学号',"
			                                           + "    `email` varchar(200) default NULL COMMENT '邮箱',"
			                                           + "    `status` int(2) unsigned NOT NULL COMMENT '帐号状态 1-可用 0-不可用',"
			                                           + "    `college_id` int(11) unsigned NOT NULL COMMENT '所属学院id',"
			                                           + "    `major_id` int(11) unsigned NOT NULL COMMENT '所属专业id',"
			                                           + "    `class_id` int(11) unsigned NOT NULL COMMENT '所属班级id',"
			                                           + "    PRIMARY KEY  (`id`),"
			                                           + "    UNIQUE KEY `idx_student_loginname` (`loginname`)" 
			                                           + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_student sql语句的前缀
	private static final String SQL_INSERT_TMP_STUDENT = " INSERT INTO tmp_student (loginname, username, sex, birthday, number, email, college_id, major_id, class_id, status)"
                                                       + " SELECT ?, ?, ?, ?, ?, ?, college.id, major.id, squad.id, ?"
			                                           + "   FROM squad, major, college"
                                                       + "  WHERE squad.squad_name = ? "
			                                           + "    AND major.major_name = ? "
                                                       + "    AND college.college_name = ? "
			                                           + "    AND squad.major_id = major.id"
                                                       + "    AND major.college_id = college.id";
	
	// 将   student中存在，tmp_student中不存在 
	// 或  student中存在，tmp_student中也存在 但status为0
	// 这些数据插入到student_hist表中
	private static final String SQL_INSERT_UNAVAILABLE_STUDENT_HISTORY = " INSERT INTO student_hist"
			                                                           + " (studentid, loginname, username, pwd, number, email, lastlogintime, logintimes, score, college_id, major_id, class_id, deletetime)"
			                                                           + " SELECT student.id, student.loginname, student.username, student.pwd, student.number, student.email, student.lastlogintime, student.logintimes, student.score, student.college_id, student.major_id, student.class_id, now()" 
			                                                           + "   FROM student"
			                                                           + "   LEFT JOIN tmp_student"
			                                                           + "     ON tmp_student.loginname = student.loginname"
			                                                           + "  WHERE tmp_student.`status` = 0"
			                                                           + "     OR tmp_student.id IS NULL";

	// 将   student中存在，tmp_student中不存在 
	// 或  student中存在，tmp_student中也存在 但status为0
	// 这些数据从 student表中删除
	private static final String SQL_DELETE_UNAVAILABLE_STUDENT = " DELETE student.*"
                                                               + "  FROM student"
                                                               + "  LEFT JOIN tmp_student"
                                                               + "    ON tmp_student.loginname = student.loginname"
                                                               + " WHERE tmp_student.`status` = 0"
                                                               + "    OR tmp_student.id IS NULL";
	
	// 将 student_hist中存在，tmp_student中也存在 且 status为1
	// 的帐号 插入到 student 中
	private static final String SQL_INSERT_AVAILABLE_STUDENT_HISTORY_TO_STUDENT = 
			                                        " INSERT INTO student "
                                                  + " (id, loginname, username, pwd, number, email, lastlogintime, logintimes, score, college_id, major_id, class_id)"
                                                  + " SELECT student_hist.studentid, student_hist.loginname, student_hist.username, student_hist.pwd, student_hist.number, student_hist.email, student_hist.lastlogintime, student_hist.logintimes, student_hist.score, student_hist.college_id, student_hist.major_id, student_hist.class_id" 
                                                  + "   FROM student_hist, tmp_student"
                                                  + "  WHERE tmp_student.loginname = student_hist.loginname"
                                                  + "    AND tmp_student.`status` = 1";
	
	// 将 student_hist中存在，tmp_student中也存在 且 status为1
	// 的帐号  从 student_hist 中删除
	private static final String SQL_DELETE_AVAILABLE_STUDENT_HISTORY = " DELETE student_hist.* "
                                                                     + "   FROM student_hist, tmp_student  "
                                                                     + "  WHERE tmp_student.loginname =student_hist.loginname "
                                                                     + "    AND tmp_student.`status` = 1 ";
	
	// 将所有 tmp_student中status为1的帐号 insert 或 update 到 student 表中去
	// 因为 loginname 有唯一索引
	// 表 studnet中  pwd不能为空，设置为 12345 
	private static final String SQL_INSERT_OR_UPDATE_STUDENT_FROM_TMP = 
			" INSERT INTO student (loginname, username, sex, birthday, pwd, number, email, college_id, major_id, class_id) "
           +" SELECT tmp_student.loginname, tmp_student.username, tmp_student.sex, tmp_student.birthday, '12345', tmp_student.number, tmp_student.email, tmp_student.college_id, tmp_student.major_id, tmp_student.class_id"
           +"   FROM tmp_student"
           +"  WHERE tmp_student.`status` = 1" 
           +"     ON DUPLICATE KEY "
           +" UPDATE username=tmp_student.username, sex=tmp_student.sex, birthday=tmp_student.birthday, pwd='12345', number=tmp_student.number, email=tmp_student.email, college_id=tmp_student.college_id, major_id=tmp_student.major_id, class_id=tmp_student.class_id";
	
	
	private static final String DEFAULT_BIRTHDAY = "20010101";
	
	
	private List<Student> mStudentList;
	
	public StudentDataSaver(List<Student> students) {
		mStudentList = students;
	}
	
	/**
	 * 只向db添加 可用账户，不可用的账户不插入数据库
	 * 1.将学生数据插入临时表 tmp_student
	 * 2.查出所有 tmp_student中不存在的记录   或者是 tmp_student中为不可用状态的记录，将其从student中删除，插入student_hist表
	 * 3.student_hist表，查出所有  tmp_student中为可用状态的记录，将其从student_hist中删除，插入student表
	 * 4.将tmp_student中数据 插入/更新到 student表中
	 * @return
	 * @author zhanggn
	 */
	public boolean doSave() {
		if (mStudentList == null || mStudentList.size() <= 0) {
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
			 * 创建临时表 -- tmp_student loginname不能为空，且不可重复 要用作登录 
			 *                         username 不能为空
			 */
			logger.info("创建临时表 -- tmp_student");
			recreateTmpTable(dbConn);

			logger.info("将学生数据插入临时表");
			batchInsertTmpTable(dbConn, mStudentList);
			
			logger.info("将所有 只存在于student表而不存在于tmp_student表，或tmp_student表的status为0的数据，"
					+ "从student表删除，插入student_hist表");
			removeUnavailableStudentAccount(dbConn);
			

			logger.info("将 student_hist中存在，tmp_student中也存在 且 status为1 的帐号 "
					+ "插入到 student 中, 并从 student_hist 中删除");
			reuseAvailableStudentAccountInHistory(dbConn);
			

			logger.info("查找所有 tmp_student中 status=1 的账户信息"
					+ " 将其插入或更新到 student表中"
					+ " 如果student表中已有相同 loginname 的账户，则update"
					+ " 没有的话就  insert");
			insertOrUpdateTmpAvailableStudentAccount(dbConn);


			logger.info("删除临时表 tmp_student");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
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
	
	
	// 重新创建临时表 tmp_teacher
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		logger.info(SQL_CREATE_TMP_STUDENT);
		
		stmt.executeUpdate(SQL_CREATE_TMP_STUDENT);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_STUDENT);
		
		stmt.executeUpdate(SQL_DROP_TMP_STUDENT);
		stmt.close();
	}
	
	// 将所有student数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<Student> students) throws SQLException {
		
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_STUDENT);
		
		for (Student student : students) {
			
			if (student.getStatus() != Student.STATUS_ON) {
				continue;
			}
			
			// 检查生日是否合法，如果不合法就插入默认值 1990-01-01
			if (!isValidDate(student.getBirthday())) {
				student.setBirthday(DEFAULT_BIRTHDAY);
			}
			
			prepStmt.setString(1,  student.getStudentNo());
			prepStmt.setString(2,  student.getName());
			prepStmt.setString(3,  student.getGender() + "");
			prepStmt.setString(4,  student.getBirthday());
			prepStmt.setString(5,  student.getSerialNo());
			prepStmt.setString(6,  student.getEmailAddr());
			prepStmt.setString(7,  STATUS_AVAILABLE + "");
			prepStmt.setString(8,  student.getClazzName());
			
			// 此处逻辑需注意，填写的是学院名称 - dky的数据不符合 学院-专业-班级 三层层级关系，所以插数据库时，学院数据和专业数据相同
			prepStmt.setString(9,  student.getCollegeName());
			prepStmt.setString(10, student.getCollegeName());

			logger.info(prepStmt.toString() + ";" 
			          + student.getStudentNo() + ","
					  + student.getName() + ","
			          + student.getGender() + ","
			          + student.getBirthday() + ","
			          + student.getSerialNo() + ","
			          + student.getEmailAddr() + ","
			          + STATUS_AVAILABLE + ","
			          + student.getClazzName() + ","
			          + student.getCollegeName() + ","
			          + student.getCollegeName());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}

		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	/*
	 * 将   student中存在，tmp_student中不存在 
	 * 或  student中存在，tmp_student中也存在 但status为0
	 * 
	 * 将这些数据从 student中删除，插入到student_hist表中。
	 */
	private void removeUnavailableStudentAccount(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		// 插入到student_hist表
		logger.info(SQL_INSERT_UNAVAILABLE_STUDENT_HISTORY);
		stmt.executeUpdate(SQL_INSERT_UNAVAILABLE_STUDENT_HISTORY);

		// 从 student中删除
		logger.info(SQL_DELETE_UNAVAILABLE_STUDENT);
		stmt.executeUpdate(SQL_DELETE_UNAVAILABLE_STUDENT);
		
		stmt.close();
	}
	

	/**
	 * 将 student_hist中存在，tmp_student中也存在 且 status为1的帐号 
	 * 插入到 student 中
	 * 并从 student_hist 中删除
	 */
	private void reuseAvailableStudentAccountInHistory(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		// 将 student_hist中存在，tmp_student中也存在 且 status为1的帐号
		// 插入到 student 中
		logger.info(SQL_INSERT_AVAILABLE_STUDENT_HISTORY_TO_STUDENT);
		stmt.executeUpdate(SQL_INSERT_AVAILABLE_STUDENT_HISTORY_TO_STUDENT);

		// 并从 student_hist 中删除
		logger.info(SQL_DELETE_AVAILABLE_STUDENT_HISTORY);
		stmt.executeUpdate(SQL_DELETE_AVAILABLE_STUDENT_HISTORY);

		stmt.close();
	}
	

	/**
	 * 查找所有 tmp_student中 status=1的账户信息
	 * 将其插入或更新到 student表中
	 * 如果student表中已有相同 loginname 的账户，则update，
	 * 没有的话就  insert
	 */
	private void insertOrUpdateTmpAvailableStudentAccount(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_INSERT_OR_UPDATE_STUDENT_FROM_TMP);
		stmt.executeUpdate(SQL_INSERT_OR_UPDATE_STUDENT_FROM_TMP);
		stmt.close();
	}
	
	
	/** 
	 * 判断日期格式是否合法 
	 * 
	 * @param dateStr  yyyyMMdd 格式
	 * @return
	 * @author zhanggn
	 */
	private boolean isValidDate(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false);
        try {
        	dateFormat.parse(dateStr);
        	return true;
        } catch (Exception e) {
            return false;
        }
	}
}
