package com.shtd.datasyncer.dbsaver.dky.cxcy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.SqlserverDb;

public class SquadDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_clazz
	private static final String SQL_DROP_TMP_CLAZZ   = " if exists(select * from sysobjects where name='tmp_clazz' and xtype='U') "
			                                         + " DROP TABLE tmp_clazz";
	
    // 创建 临时表 tmp_clazz
	private static final String SQL_CREATE_TMP_CLAZZ = " CREATE TABLE tmp_clazz ("
			                                         + "    id int identity (1,1) primary key ,"
			                                         + "    squad_name varchar(100) NOT NULL ,"
			                                         + "    major_name varchar(50) NOT NULL ,"
			                                         + "    college_name varchar(100) NOT NULL"
			                                         + " ) ";
	
	// 批量插入 tmp_clazz sql语句的前缀
	private static final String SQL_INSERT_TMP_CLAZZ = " INSERT INTO tmp_clazz "
	                                                 + " (squad_name, major_name, college_name) VALUES (?, ?, ?)";
	
	// 获取的一个可用的教师帐号
	private static final String SQL_GET_FIRST_VALID_TEACHER_ID = " select top 1 ID"
			                                                   + "   from PUB_User"
			                                                   + "  where User_Group = 1"
			                                                   + "    and State = 2  ";

	// 获取最后一个班级的id
	private static final String SQL_GET_LAST_CLASS_ID = " select top 1 ID"
			                                          + "   from PUB_Class"
			                                          + "  order by ID desc";
	
	// 将所有新的班级信息插入 表 pub_class
	private static final String SQL_INSERT_NEW_CLASS_TO_PUBCLASS = " insert into PUB_Class (College_ID, Name, Description, Teacher_ID)"
			                                                     + " select PUB_College.ID, tmp_clazz.squad_name, PUB_College.Name , ?"
			                                                     + "   from tmp_clazz"
			                                                     + "   left join ("
			                                                     + "              select PUB_College.Name as college_name, "
			                                                     + "                     PUB_Class.Name as class_name"
			                                                     + "                from PUB_College, PUB_Class"
			                                                     + "               where PUB_Class.College_ID = PUB_College.ID"
			                                                     + "             ) as college_class"
			                                                     + "     on  tmp_clazz.college_name = college_class.college_name"
			                                                     + "    and tmp_clazz.squad_name = college_class.class_name"
			                                                     + "  inner join PUB_College"
			                                                     + "     on tmp_clazz.college_name = PUB_College.Name"
			                                                     + "  where college_class.college_name is null ";
	
	// 取出所有新插入的 class 的 id 信息
	private static final String SQL_SELECT_NEW_INSERT_CLASS_ID_FROM_PUBCLASS = " select ID"
			                                                                 + " from PUB_Class"
			                                                                 + " where ID > ? ";
	
	
	private List<Clazz> mClazzList;
	
	public SquadDataSaver(List<Clazz> clazzs) {
		mClazzList = clazzs;
	}
	
	/**
	 * 因为调用存储,耗时比较多,所以这里就只将新数据插入,不做删除等操作
	 * 
	 * Pub_Class
	 * 	学院ID College_ID 不可为空
	 * 	老师ID Teacher_ID 可空，但为空不能进后台 写死用 teacher 帐号,可随机选一个可以登录的教师帐号
	 * 	班级名称 Name
	 * 
	 * 班级添加完后，调用存储过程：InitClassParams 班级ID 1
	 * 
	 * @return
	 * @author zhanggn
	 */
	public boolean doSave() {
		if (mClazzList == null || mClazzList.size() <= 0) {
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
			 * 创建临时表 -- tmp_clazz  
			 */
			logger.info("创建临时表 -- tmp_clazz");
			recreateTmpTable(dbConn);
			
			logger.info("将班级数据插入临时表");
			batchInsertTmpTable(dbConn, mClazzList);
			
			
			logger.info("将班级数据插入到 pub_class 表中, 并执行初始化存储过程");
			insertNewSquad(dbConn);

			logger.info("删除临时表 tmp_clazz");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将班级数据保存到数据库 操作失败: " + e);
			
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

	// 重新创建临时表 tmp_clazz
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		logger.info(SQL_CREATE_TMP_CLAZZ);
		
		stmt.executeUpdate(SQL_CREATE_TMP_CLAZZ);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_CLAZZ);
		
		stmt.executeUpdate(SQL_DROP_TMP_CLAZZ);
		stmt.close();
	}
	
	// 将所有clazz数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<Clazz> clazzs) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_CLAZZ);
		
		for (Clazz clazz : clazzs) {
			prepStmt.setString(1, clazz.getClazzName());
			
			// 专业 和 学院 插入相同数据
			prepStmt.setString(2, clazz.getCollegeName());
			prepStmt.setString(3, clazz.getCollegeName());
			
			logger.info(prepStmt.toString() + ";" 
			          + clazz.getClazzName() + ","
					  + clazz.getCollegeName() + ","
			          + clazz.getCollegeName());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		
		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	/* 将班级数据插入到 college 表中
	 * 
	 * Pub_Class
	 * 	学院ID College_ID 不可为空
	 * 	老师ID Teacher_ID 可空，但为空不能进后台 可默认一个能正常登录的老师ID
	 * 	班级名称 Name
	 * 班级添加完后，调用存储过程：InitClassParams 班级ID 1
	 */
	private void insertNewSquad(Connection conn) throws SQLException {
		// 获取第一个可用的教师id
		String teacherID = getFirstValidTeacherID(conn);
		
		// 获取当前 pub_class 中最后一项数据的id
		String currentClassID = getLastClassID(conn);
		
		// 将所有新班级数据插入 表 pub_class
		insertNewSquadToPUBClass(conn, teacherID);

		// 获取到所有新插入到 pub_class的 class的id
		List<String> newClassIds = getNewInsertClassID(conn, currentClassID);
		
		// 调用 新建班级的 存储过程
		if (newClassIds != null && newClassIds.size() > 0) {

			logger.info("需要初始化 " + newClassIds.size() + " 个班的数据。");
			
			for (String classID : newClassIds) {
				callNewClassProc(conn, classID);
			}
		} else {
			logger.info("未向PUB_Class添加新班级。");
		}
	}
	
	// 调用 新建班级的 存储过程
	private void callNewClassProc(Connection conn, String classID) throws SQLException {
        CallableStatement proc = conn.prepareCall("{call InitClassParams (?, 1)}");
        proc.setString(1, classID);

		logger.info(proc.toString() + ";" + classID);
		
        proc.execute();
        proc.close();
	}
	
	
	
	/**
	 * 
	 * @param conn
	 * @param lastClassID
	 * @return
	 * @author zhanggn
	 */
	private List<String> getNewInsertClassID(Connection conn, String lastClassID) throws SQLException {
		Statement stmt = conn.createStatement();

		List<String> ids = new ArrayList<String>();
		
		PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_NEW_INSERT_CLASS_ID_FROM_PUBCLASS);
		pstmt.setString(1, lastClassID);

		logger.info(pstmt.toString() + ";" + lastClassID);
		
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			ids.add(rs.getString("ID"));
		}

		rs.close();
		stmt.close();
		
		return ids;
	}
	
	// 将所有新的班级信息插入 表 pub_class
	private void insertNewSquadToPUBClass(Connection conn, String teacherID) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_NEW_CLASS_TO_PUBCLASS);
		prepStmt.setString(1, teacherID);
		
		logger.info(prepStmt.toString() + ";" + teacherID);
		
		// 把一个SQL命令加入命令列表  
		prepStmt.addBatch();

		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	// 获取 第一个 可用的 教师id
	private String getFirstValidTeacherID(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		String teacherID = "1";
		
		logger.info(SQL_GET_FIRST_VALID_TEACHER_ID);
		ResultSet rs = stmt.executeQuery(SQL_GET_FIRST_VALID_TEACHER_ID);
		while(rs.next()) {
			teacherID = rs.getString("ID");
			break;
		}

		rs.close();
		stmt.close();
		
		return teacherID;
	}

	
	// 获取 当前最后一个 班级的id 
	private String getLastClassID(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		String classID = "1";
		
		logger.info(SQL_GET_LAST_CLASS_ID);
		ResultSet rs = stmt.executeQuery(SQL_GET_LAST_CLASS_ID);
		while(rs.next()) {
			classID = rs.getString("ID");
			break;
		}

		rs.close();
		stmt.close();
		
		return classID;
	}
}
