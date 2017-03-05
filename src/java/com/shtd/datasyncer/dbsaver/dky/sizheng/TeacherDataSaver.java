package com.shtd.datasyncer.dbsaver.dky.sizheng;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Teacher;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYin;
import com.shtd.datasyncer.utils.db.MysqlDb;
/**
 * sizheng 教师数据保存
 * 原始状态    现在状态    操作
 * 有              有               更新，将用户状态置为可用
 * 有              无               更新，将用户状态置为不可用
 * 无              有               插入新的用户数据
 * @author RanWeizheng
 *
 */
public class TeacherDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	// 用户类型(User_Group 1=老师，2=学生, 3=后台管理员)
	private static final int USER_GROUP_TEACHER = 1;
	
	// 用户状态（State 0=屏蔽，1=正常）
	private static final int STATE_SHIELDED   = 0;
	private static final int STATE_NORMAL = 1;
	
	private static final String DEFAULT_PWD = "12345";
	 
	// 删除 临时表 tmp_teacher
	private static final String SQL_DROP_TMP_TEACHER =  "DROP TABLE IF EXISTS `tmp_teacher`";
	
    // 创建 临时表 tmp_teacher
	private static final String SQL_CREATE_TMP_TEACHER = " CREATE TABLE tmp_teacher ("
			                                         + "    `id` int(10) NOT NULL auto_increment  ,"
			                                         + "    `loginname` varchar(50) NOT NULL  COMMENT '职工号，登录名',"
			                                         + "    `username` varchar(100) NOT NULL COMMENT '真实姓名',"
			                                         + "    `sex` varchar(2) NOT NULL,"                   //性别
			                                         + "    `password` varchar(100) NOT NULL,"       //密码    
			                                         + "    `pinyin` varchar(100) NOT NULL,"
			                                         + "    `pinyinheadchar` varchar(20) NOT NULL,"
			                                         + "    PRIMARY KEY  (`id`),"
			                                         + "    UNIQUE KEY `idx_temp_teacher_loginname` (`loginname`)" 
			                                         + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	 
	// 批量插入 tmp_teacher  
	private static final String SQL_INSERT_TMP_TEACHER = " INSERT INTO tmp_teacher "
	                                                   + " (`loginname`, `username`, `sex`, `password`, `pinyin`, `pinyinheadchar`) "
	                                                   + " VALUES (?, ?,  ?,  '" + DEFAULT_PWD + "', ?, ?)";//TODO密码加密

	// User中可用的，但是在tmp_teacher中不存在的教师帐号，置为屏蔽状态;
	private static final String SQL_UPDATE_ONLY_IN_PUBUSER = " UPDATE `user`"
														   + "  LEFT JOIN tmp_teacher "
														   + "				ON `user`.login_name = tmp_teacher.loginname "
			                                               + "    set `user`.user_status = " + STATE_SHIELDED
			                                               + "  where "
			                                               + "    `user`.type = " + USER_GROUP_TEACHER
			                                               + "		AND `user`.user_status = " + STATE_NORMAL
			                                               + "		AND	tmp_teacher.loginname IS NULL";
	
	// User中不可用的，但是在tmp_teacher中存在的教师帐号，置为正常状态, 
	private static final String SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT = " update "
																   + "		`user`, tmp_teacher "
			                                                       + "    set `user`.user_status = " + STATE_NORMAL
			                                                       + "   where "
			                                                       + "         `user`.user_status = " + STATE_SHIELDED
			                                                       + "            and `user`.type = " + USER_GROUP_TEACHER
			                                                       + "            and `user`.login_name = tmp_teacher.loginname";
	
	// 根据tmp_teacher中数据,更新所有可用帐号信息
	private static final String SQL_UPDATE_USED_ACCOUNT_FROM_TMP = " update `user`, tmp_teacher "
			                                                     + "    set `user`.username = tmp_teacher.username, "
			                                                     + "         `user`.nickname = tmp_teacher.username, "
			                                                     + "         `user`.gender = tmp_teacher.sex, "
			                                                     + "         `user`.pinyin = tmp_teacher.pinyin, "
			                                                     + "         `user`.pinyinheadchar = tmp_teacher.pinyinheadchar "
			                                                     + "  where "
			                                                     + "		tmp_teacher.loginname = `user`.login_name "
			                                                     + "    and `user`.user_status = " + STATE_NORMAL
			                                                     + "    and `user`.type = " + USER_GROUP_TEACHER;


	// 将只在tmp_teacher中存在的数据全部插入 pub_user
	private static final String SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP = " insert into `user` "
			                                                         + "(`login_name`, `password`, `type`, `user_status`, "
			                                                         + " `username`, `gender`, `nickname`, "
			                                                         + " `pinyin`, `pinyinheadchar`) "
			                                                         + " select "
			                                                         + "        tmp_teacher.loginname, "
			                                                         + "		 " + DEFAULT_PWD + ", "
			                                                         + " 		 " + USER_GROUP_TEACHER + ", "
			                                                         + " 		 " + STATE_NORMAL +  ", "
			                                                         + "		 tmp_teacher.username, "
			                                                         + "		 tmp_teacher.sex, tmp_teacher.username, "
			                                                         + "		 tmp_teacher.pinyin, tmp_teacher.pinyinheadchar "
			                                                         + "   from tmp_teacher "
			                                                         + "   left join `user` "
			                                                         + "     on tmp_teacher.loginname = `user`.login_name "
			                                                         + "  where `user`.login_name is null";
	
	private List<Teacher> mTeacherList;
	
	public TeacherDataSaver(List<Teacher> teachers) {
		mTeacherList = teachers;
	}
	
	/**
	 * @return
	 * @author zhanggn
	 */
	public boolean doSave() {
		if (mTeacherList == null || mTeacherList.size() <= 0) {
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
			 * 创建临时表 -- tmp_teacher  
			 */
			logger.info("创建临时表 -- tmp_teacher");
			recreateTmpTable(dbConn);
			
			logger.info("将教师数据插入临时表");
			batchInsertTmpTable(dbConn, mTeacherList);

			logger.info("User中可用的，但是在tmp_teacher中不存在的教师帐号，置为不可用状态");
			updateAccountOnlyInPUBUser(dbConn);

			logger.info("User中不可用的，但是在tmp_teacher中存在的教师帐号，置为正常状态");
			updateAccountValidExistInTmp(dbConn);

			logger.info("根据tmp_teacher中数据,更新所有可用帐号信息");
			updateUsedAccount(dbConn);

			logger.info("将只在tmp_teacher中存在的数据全部插入 user");
			insertNewAccount(dbConn);
			
			logger.info("删除临时表 tmp_teacher");
//			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将教师数据保存到数据库 操作失败: " + e);
			
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
		
		logger.info(SQL_CREATE_TMP_TEACHER);
		
		stmt.executeUpdate(SQL_CREATE_TMP_TEACHER);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_TEACHER);
		
		stmt.executeUpdate(SQL_DROP_TMP_TEACHER);
		stmt.close();
	}
	
	// 将所有teacher数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<Teacher> teachers) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_TEACHER);
		
		for (Teacher teacher : teachers) {
			/*
			 (`loginname`, `username`, `sex`, `password`, `pinyin`, `pinyinheadchar`) 
			*/
			prepStmt.setString(1,  teacher.getEmployeeNo());
			prepStmt.setString(2,  teacher.getName());
			prepStmt.setObject(3, teacher.getGender());
			
			String pinyin = PinYin.getPingYin(teacher.getName());
			String pinyinHeader = PinYin.getPinYinHeadChar(teacher.getName());
			prepStmt.setString(4,  pinyin);
			prepStmt.setString(5,  pinyinHeader);

			logger.info(prepStmt.toString() + ";" + teacher.getName() + ","+ teacher.getEmployeeNo() + ","+ teacher.getGender());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		
		prepStmt.executeBatch();
		prepStmt.close();
	}
	

	// PUB_User中可用的，但是在tmp_teacher中不存在的教师帐号，置为结业状态
	private void updateAccountOnlyInPUBUser(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_ONLY_IN_PUBUSER);
		stmt.executeUpdate(SQL_UPDATE_ONLY_IN_PUBUSER);
		stmt.close();
	}

	// PUB_User中不可用的，但是在tmp_teacher中存在的教师帐号，置为正常状态
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
}
