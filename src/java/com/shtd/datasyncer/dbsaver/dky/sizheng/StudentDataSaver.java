package com.shtd.datasyncer.dbsaver.dky.sizheng;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYin;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * sizheng 学生数据保存
 * 原始状态    现在状态    操作
 * 有              有               更新，将用户状态置为可用
 * 有              无               更新，将用户状态置为不可用
 * 无              有               插入新的用户数据
 * @author RanWeizheng
 *
 */
public class StudentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 用户类型(User_Group 1=老师，2=学生, 3=后台管理员)
	private static final int USER_GROUP_STUDENT = 2;
	
	// 用户状态（State 0=屏蔽，1=正常）
	private static final int STATE_SHIELDED   = 0;
	private static final int STATE_NORMAL = 1;
		
	private static final String DEFAULT_PWD = "12345";
	
	// 删除 临时表 tmp_student
	private static final String SQL_DROP_TMP_STUDENT = " DROP TABLE IF EXISTS `tmp_student`";
	
    // 创建 临时表 tmp_student
	private static final String SQL_CREATE_TMP_STUDENT = " CREATE TABLE `tmp_student` (" //TEMPORARY 
			                                           + "    `id` int(10) NOT NULL auto_increment ,"
			                                           + "    `loginname` varchar(50) NOT NULL COMMENT '登录名,同学号',"
			                                           + "    `username` varchar(100) NOT NULL COMMENT '真实姓名',"
			                                           + "    `sex` varchar(2) default NULL,"
			                                           //+ "    `birthday` datetime default NULL,"
			                                           //+ "    `number` varchar(50) default NULL COMMENT '学号',"
			                                           //+ "    `email` varchar(200) default NULL COMMENT '邮箱',"
			                                           //+ "    `status` int(2) unsigned NOT NULL COMMENT '帐号状态 1-可用 0-不可用',"
			                                           + "    `class_code` varchar(50) NOT NULL COMMENT '所属班级编号',"//班号 or 编号
			                                           + "    `class_name` varchar(100) default '' COMMENT '所属班级名称',"
			                                           + "    `major_name` varchar(100) default '' COMMENT '专业名称',"
			                                           + "    `pinyin` varchar(100) NOT NULL,"
				                                       + "    `pinyinheadchar` varchar(20) NOT NULL,"
			                                           + "    PRIMARY KEY  (`id`),"
			                                           + "    UNIQUE KEY `idx_student_loginname` (`loginname`)" 
			                                           + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_student sql语句的前缀
	private static final String SQL_INSERT_TMP_STUDENT = " INSERT INTO tmp_student (loginname, username, sex, class_code, class_name, major_name, pinyin, pinyinheadchar) "
                                                       + "value( ?, ?, ?, ?, ?, ?, ?, ?)";
	
	// User中可用的，但是在tmp_student中不存在的学生帐号，置为屏蔽状态;
		private static final String SQL_UPDATE_ONLY_IN_PUBUSER = " UPDATE `user`"
															   + "  LEFT JOIN tmp_student "
															   + "				ON `user`.login_name = tmp_student.loginname "
				                                               + "    set `user`.user_status = " + STATE_SHIELDED
				                                               + "  where "
				                                               + "    `user`.type = " + USER_GROUP_STUDENT
				                                               + "		AND `user`.user_status = " + STATE_NORMAL
				                                               + "		AND	tmp_student.loginname IS NULL";
		
		// User中不可用的，但是在tmp_teacher中存在的学生帐号，置为正常状态, 
		private static final String SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT = " update "
																	   + "		`user`, tmp_student "
				                                                       + "    set `user`.user_status = " + STATE_NORMAL
				                                                       + "   where "
				                                                       + "         `user`.user_status = " + STATE_SHIELDED
				                                                       + "            and `user`.type = " + USER_GROUP_STUDENT
				                                                       + "            and `user`.login_name = tmp_student.loginname";
		
		// 根据tmp_teacher中数据,更新所有可用帐号信息
		//loginname, username, sex, class_code, class_name, major_name, pinyin, pinyinheadchar
		private static final String SQL_UPDATE_USED_ACCOUNT_FROM_TMP = " update `user`, tmp_student, dict_sys as clazz"
				                                                     + "    set `user`.username = tmp_student.username, "
				                                                     + "         `user`.nickname = tmp_student.username, "
				                                                     + "         `user`.gender = tmp_student.sex, "
				                                                     + "         `user`.pinyin = tmp_student.pinyin, "
				                                                     + "         `user`.pinyinheadchar = tmp_student.pinyinheadchar, "
				                                                     + "         `user`.clazz_id = clazz.id, "
				                                                     + "         `user`.major_name = tmp_student.major_name "
				                                                     + "  where "
				                                                     + "		tmp_student.loginname = `user`.login_name "
				                                                     + "    and  tmp_student.class_code = clazz.code"
				                                                     + "    and `user`.user_status = " + STATE_NORMAL
				                                                     + "    and `user`.type = " + USER_GROUP_STUDENT;


		// 将只在tmp_teacher中存在的数据全部插入 pub_user
		private static final String SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP = " insert into `user` "
				                                                         + "(`login_name`, `password`, `type`, `user_status`, "
				                                                         + " `username`, `gender`, `nickname`, "
				                                                         + " clazz_id, major_name,"
				                                                         + " `pinyin`, `pinyinheadchar`) "
				                                                         + " select "
				                                                         + "        tmp_student.loginname, "
				                                                         + "		 " + DEFAULT_PWD + ", "
				                                                         + " 		 " + USER_GROUP_STUDENT + ", "
				                                                         + " 		 " + STATE_NORMAL +  ", "
				                                                         + "		 tmp_student.username, "
				                                                         + "		 tmp_student.sex, tmp_student.username, "
				                                                         + "        clazz.id, tmp_student.major_name,"
				                                                         + "		 tmp_student.pinyin, tmp_student.pinyinheadchar "
				                                                         + "   from tmp_student "
				                                                         + "   left join `user` "
				                                                         + "     on tmp_student.loginname = `user`.login_name, "
				                                                         + "     dict_sys as clazz"
				                                                         + "  where `user`.login_name is null"
				                                                         + "           and clazz.code = tmp_student.class_code";
	
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
			
			logger.info("user表中可用，但不存在于tmp_student表, 状态置为不可用");
			updateAccountOnlyInPUBUser(dbConn);
			
			logger.info("将user表中不可用，tmp_student中存在的, 状态置为可用 ");
			updateAccountValidExistInTmp(dbConn);

			logger.info("根据tmp_teacher中数据,更新所有可用帐号信息");
			updateUsedAccount(dbConn);

			logger.info("将只在tmp_teacher中存在的数据全部插入 user表中");
			insertNewAccount(dbConn);
//
//
			logger.info("删除临时表 tmp_student");
//			dropTmpTable(dbConn);
//			
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
			
//			// 检查生日是否合法，如果不合法就插入默认值 1990-01-01
//			if (!isValidDate(student.getBirthday())) {
//				student.setBirthday(DEFAULT_BIRTHDAY);
//			}
			
			//loginname, username, sex, class_code, major_name, pinyin, pinyinheadchar
			
			prepStmt.setString(1,  student.getStudentNo());
			prepStmt.setString(2,  student.getName());
			prepStmt.setString(3,  student.getGender() + "");
			prepStmt.setString(4,  student.getSerialNo());
			prepStmt.setString(5,  student.getClazzName());
			prepStmt.setString(6,  student.getMajorName());
			
			String pinyin = PinYin.getPingYin(student.getName());
			String pinyinHeader = PinYin.getPinYinHeadChar(student.getName());
			prepStmt.setString(7,  pinyin);
			prepStmt.setString(8,  pinyinHeader);
			
			logger.info(prepStmt.toString() + ";" 
			          + student.getStudentNo() + ","
					  + student.getName() + ","
			          + student.getGender() + ","
			          + student.getSerialNo() + ","
			          + student.getClazzName() + ","
			          + student.getMajorName() + ","
			          + pinyin + ","
			          + pinyinHeader
			);
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}

		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	// PUB_User中可用的，但是在tmp_student中不存在的学生帐号，置为不可用状态
		private void updateAccountOnlyInPUBUser(Connection conn) throws SQLException {
			Statement stmt = conn.createStatement();

			logger.info(SQL_UPDATE_ONLY_IN_PUBUSER);
			stmt.executeUpdate(SQL_UPDATE_ONLY_IN_PUBUSER);
			stmt.close();
		}
		
		// PUB_User中不可用的，但是在tmp_student中存在的学生帐号，置为正常状态
		private void updateAccountValidExistInTmp(Connection conn) throws SQLException {
			Statement stmt = conn.createStatement();

			logger.info(SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT);
			stmt.executeUpdate(SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT);
			stmt.close();
		}
		
		/**
		 * 根据tmp_teacher中数据,更新所有可用帐号信息
		 */
		private void updateUsedAccount(Connection conn) throws SQLException {
			Statement stmt = conn.createStatement();

			logger.info(SQL_UPDATE_USED_ACCOUNT_FROM_TMP);
			stmt.executeUpdate(SQL_UPDATE_USED_ACCOUNT_FROM_TMP);
			stmt.close();
		}
		
		/**
		 * 将只在tmp_teacher中存在的数据全部插入 pub_user
		 */
		private void insertNewAccount(Connection conn) throws SQLException {
			Statement stmt = conn.createStatement();

			logger.info(SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP);
			stmt.executeUpdate(SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP);
			stmt.close();
		}
	
	
	/*
	 * 将   student中存在，tmp_student中不存在 
	 * 或  student中存在，tmp_student中也存在 但status为0
	 * 
	 * 将这些数据从 student中删除，插入到student_hist表中。
	 */
//	private void removeUnavailableStudentAccount(Connection conn) throws SQLException {
//		Statement stmt = conn.createStatement();
//
//		// 插入到student_hist表
//		logger.info(SQL_INSERT_UNAVAILABLE_STUDENT_HISTORY);
//		stmt.executeUpdate(SQL_INSERT_UNAVAILABLE_STUDENT_HISTORY);
//
//		// 从 student中删除
////		logger.info(SQL_DELETE_UNAVAILABLE_STUDENT);
////		stmt.executeUpdate(SQL_DELETE_UNAVAILABLE_STUDENT);
//		
//		stmt.close();
//	}
	

	/**
	 * 将 student_hist中存在，tmp_student中也存在 且 status为1的帐号 
	 * 插入到 student 中
	 * 并从 student_hist 中删除
	 */
//	private void reuseAvailableStudentAccountInHistory(Connection conn) throws SQLException {
//		Statement stmt = conn.createStatement();
//
//		// 将 student_hist中存在，tmp_student中也存在 且 status为1的帐号
//		// 插入到 student 中
//		logger.info(SQL_INSERT_AVAILABLE_STUDENT_HISTORY_TO_STUDENT);
//		stmt.executeUpdate(SQL_INSERT_AVAILABLE_STUDENT_HISTORY_TO_STUDENT);
//
//		// 并从 student_hist 中删除
////		logger.info(SQL_DELETE_AVAILABLE_STUDENT_HISTORY);
////		stmt.executeUpdate(SQL_DELETE_AVAILABLE_STUDENT_HISTORY);
//
//		stmt.close();
//	}
	

	/**
	 * 查找所有 tmp_student中 status=1的账户信息
	 * 将其插入或更新到 student表中
	 * 如果student表中已有相同 loginname 的账户，则update，
	 * 没有的话就  insert
	 */
//	private void insertOrUpdateTmpAvailableStudentAccount(Connection conn) throws SQLException {
//		Statement stmt = conn.createStatement();
//
//		logger.info(SQL_INSERT_OR_UPDATE_STUDENT_FROM_TMP);
//		stmt.executeUpdate(SQL_INSERT_OR_UPDATE_STUDENT_FROM_TMP);
//		stmt.close();
//	}
	
	
	/** 
	 * 判断日期格式是否合法 
	 * 
	 * @param dateStr  yyyyMMdd 格式
	 * @return
	 * @author zhanggn
	 */
//	private boolean isValidDate(String dateStr) {
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//        dateFormat.setLenient(false);
//        try {
//        	dateFormat.parse(dateStr);
//        	return true;
//        } catch (Exception e) {
//            return false;
//        }
//	}
}
