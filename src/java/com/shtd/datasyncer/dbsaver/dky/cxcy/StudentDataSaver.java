package com.shtd.datasyncer.dbsaver.dky.cxcy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.SqlserverDb;

public class StudentDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	 
	// 默认生日
	private static final String DEFAULT_BIRTHDAY = "19800101";

	// 用户类型(User_Group 1=老师，0=学生)
	private static final int USER_GROUP_STUDENT = 0;
	
	// 用户状态（State 0=未初始化，1=审批不通过，2=正常，3=结业）
	@SuppressWarnings("unused")
	private static final int STATE_UNINIT   = 0;
	@SuppressWarnings("unused")
	private static final int STATE_UNPROVED = 1;
	private static final int STATE_NORMAL   = 2;
	private static final int STATE_GRADUATE = 3;
	
	private static final String DEFAULT_PWD = "12345";
	
	// 性别 （Sex 男、女）
	private static final String GENDER_NAME_MALE   = "男";
	private static final String GENDER_NAME_FEMALE = "女";
	
	// 删除 临时表 tmp_student
	private static final String SQL_DROP_TMP_STUDENT = " if exists(select * from sysobjects where name='tmp_student' and xtype='U')  "
			                                         + " DROP TABLE tmp_student";
	
    // 创建 临时表 tmp_student
	private static final String SQL_CREATE_TMP_STUDENT = " CREATE TABLE tmp_student ("
			                                         + "    id int identity (1,1) primary key ,"
			                                         + "    Name varchar(100) NOT NULL ,"
			                                         + "    User_Name varchar(100) NOT NULL ,"
			                                         + "    Class_ID bigint,"
			                                         + "    User_Pass varchar(100) NOT NULL,"
			                                         + "    User_Group int NOT NULL,"
			                                         + "    State int NOT NULL,"
			                                         + "    Sex varchar(2) NOT NULL,"
			                                         + "    Birthday varchar(50) NOT NULL"
			                                         + " ) ";
	 
	// 批量插入 tmp_student  
	private static final String SQL_INSERT_TMP_STUDENT = " INSERT INTO tmp_student "
			                                           + " (Name, User_Name, Sex, Class_ID, User_Pass, User_Group, State, Birthday) "
			                                           + " SELECT ?, ?, ?, PUB_Class.ID, '" + DEFAULT_PWD + "', " + USER_GROUP_STUDENT + ", " + STATE_NORMAL + ", ?"
			                                           + "   FROM PUB_Class, PUB_College"
			                                           + "  WHERE pub_class.College_ID = PUB_College.ID"
			                                           + "    AND PUB_Class.Name = ?"
			                                           + "    AND PUB_College.Name = ?";

	// PUB_User中可用的，但是在tmp_student中不存在的帐号，置为结业状态;
	private static final String SQL_UPDATE_ONLY_IN_PUBUSER = " update PUB_User"
			                                               + "    set State = " + STATE_GRADUATE
			                                               + "  where ID in ("
			                                               + "        select PUB_User.ID"
			                                               + "          from PUB_User"
			                                               + "          left join tmp_student"
			                                               + "            on PUB_User.User_Name = tmp_student.User_Name"
			                                               + "           and PUB_User.State != " + STATE_GRADUATE
			                                               + "         where PUB_User.User_Group = " + USER_GROUP_STUDENT
			                                               + "           and tmp_student.User_Name is null"
			                                               + "              ) ";
	
	// PUB_User中不可用的，但是在tmp_student中存在的帐号，置为正常状态
	private static final String SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT = " update PUB_User "
			                                                       + "    set State = " + STATE_NORMAL
			                                                       + "   where ID in ("
			                                                       + "         select PUB_User.ID"
			                                                       + "           from PUB_User, tmp_student"
			                                                       + "          where PUB_User.State = " + STATE_GRADUATE
			                                                       + "            and PUB_User.User_Group = " + USER_GROUP_STUDENT
			                                                       + "            and PUB_User.User_Name = tmp_student.User_Name"
			                                                       + "               )";

	// 根据tmp_student中数据,更新所有可用帐号信息
	private static final String SQL_UPDATE_USED_ACCOUNT_FROM_TMP = " update PUB_User "
			                                                     + "    set PUB_User.Name = tmp_student.Name,"
			                                                     + "        PUB_User.Class_ID = tmp_student.Class_ID,"
			                                                     + "        PUB_User.User_Pass = tmp_student.User_Pass,"
			                                                     + "        PUB_User.User_Group = tmp_student.User_Group,"
			                                                     + "        PUB_User.State = tmp_student.State,"
			                                                     + "        PUB_User.Sex = tmp_student.Sex,"
			                                                     + "        PUB_User.Birthday = tmp_student.Birthday"
			                                                     + "   from PUB_User, tmp_student"
			                                                     + "  where tmp_student.User_Name = PUB_User.User_Name"
			                                                     + "    and PUB_User.State != " + STATE_GRADUATE
			                                                     + "    and PUB_User.User_Group = " + USER_GROUP_STUDENT;


	// 将只在tmp_student中存在的数据全部插入 pub_user
	private static final String SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP = " insert into PUB_User "
			                                                         + "        (Name, User_Name, Class_ID, User_Pass, User_Group, State, Sex, Birthday)"
			                                                         + " select tmp_student.Name, "
			                                                         + "        tmp_student.User_Name, "
			                                                         + "        tmp_student.Class_ID, "
			                                                         + "        tmp_student.User_Pass, "
			                                                         + "        tmp_student.User_Group, "
			                                                         + "        tmp_student.State, "
			                                                         + "        tmp_student.Sex, "
			                                                         + "        tmp_student.Birthday "
			                                                         + "   from tmp_student"
			                                                         + "   left join PUB_User"
			                                                         + "     on tmp_student.User_Name = PUB_User.User_Name"
			                                                         + "  where PUB_User.User_Name is null";
	
	private List<Student> mStudentList;
	
	public StudentDataSaver(List<Student> students) {
		mStudentList = students;
	}

	
	/**
	 * @return
	 * @author zhanggn
	 */
	public boolean doSave() {
		if (mStudentList == null || mStudentList.size() <= 0) {
			logger.error("不存在待保存数据，操作结束");
			return true;
		}
		
		SqlserverDb db = new SqlserverDb();
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
			 * 创建临时表 -- tmp_student  
			 */
			logger.info("创建临时表 -- tmp_student");
			recreateTmpTable(dbConn);
			
			logger.info("将学生数据插入临时表");
			batchInsertTmpTable(dbConn, mStudentList);
			

			logger.info("PUB_User中可用的，但是在tmp_student中不存在的帐号，置为结业状态");
			updateAccountOnlyInPUBUser(dbConn);

			logger.info("PUB_User中不可用的，但是在tmp_student中存在的帐号，置为正常状态");
			updateAccountValidExistInTmp(dbConn);

			logger.info("根据tmp_student中数据,更新所有可用帐号信息");
			updateUsedAccount(dbConn);

			logger.info("将只在tmp_student中存在的数据全部插入 pub_user");
			insertNewAccount(dbConn);
			
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
					e.printStackTrace();
				}
			}
			
			db.closeAll();
		}
		
		return false;
	}

	// 重新创建临时表 tmp_student
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
		
		String genderName = "", birthday = "";
		for (Student student : students) {
			
			// 只处理可登录用户
			if (student.getStatus() != Student.STATUS_ON) {
				continue;
			}
			
			prepStmt.setString(1,  student.getName());
			prepStmt.setString(2,  student.getStudentNo());
			
			genderName = (student.getGender()==Student.GENDER_FEMALE) ? GENDER_NAME_FEMALE : GENDER_NAME_MALE;
			prepStmt.setString(3, genderName);

			birthday = getFromattedDate(student.getBirthday());
			prepStmt.setString(4, birthday);

			prepStmt.setString(5, student.getClazzName());
			prepStmt.setString(6, student.getCollegeName());

			logger.info(prepStmt.toString() + ";" 
			          + student.getName() + ","
					  + student.getStudentNo() + ","
			          + genderName + ","
			          + birthday + ","
			          + student.getClazzName() + ","
			          + student.getCollegeName());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		
		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	// PUB_User中可用的，但是在tmp_student中不存在的帐号，置为结业状态
	private void updateAccountOnlyInPUBUser(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_ONLY_IN_PUBUSER);
		stmt.executeUpdate(SQL_UPDATE_ONLY_IN_PUBUSER);
		stmt.close();
	}

	// PUB_User中不可用的，但是在tmp_student中存在的帐号，置为正常状态
	private void updateAccountValidExistInTmp(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT);
		stmt.executeUpdate(SQL_UPDATE_VALID_EXIST_TMP_ACCOUNT);
		stmt.close();
	}

	/**
	 * 根据tmp_student中数据,更新所有可用帐号信息
	 */
	private void updateUsedAccount(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_UPDATE_USED_ACCOUNT_FROM_TMP);
		stmt.executeUpdate(SQL_UPDATE_USED_ACCOUNT_FROM_TMP);
		stmt.close();
	}

	/**
	 * 将只在tmp_student中存在的数据全部插入 pub_user
	 */
	private void insertNewAccount(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP);
		stmt.executeUpdate(SQL_INSERT_NEW_USED_ACCOUNT_FROM_TMP);
		stmt.close();
	}

	/** 
	 * 将给定日期字符串 格式化成指定格式
	 * 
	 * @param  dateStr  yyyyMMdd 格式
	 * @return dateStr  yyyy-MM-dd 格式
	 * @author zhanggn
	 */
	private String getFromattedDate(String dateStr) {
		SimpleDateFormat fromDateFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat toDateFormat   = new SimpleDateFormat("yyyy-MM-dd");
        try {
        	return toDateFormat.format(fromDateFormat.parse(dateStr));
        } catch (Exception e) {
            return DEFAULT_BIRTHDAY;
        }
	}
}
