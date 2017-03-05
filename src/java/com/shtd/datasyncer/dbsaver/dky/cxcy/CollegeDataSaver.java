package com.shtd.datasyncer.dbsaver.dky.cxcy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.SqlserverDb;

public class CollegeDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_clazz
	private static final String SQL_DROP_TMP_CLAZZ   = " if exists(select * from sysobjects where name='tmp_clazz' and xtype='U')  "
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
	

	// 将 tmp_clazz中有的 college ，而college表中没有的记录，插入到college
	private static final String SQL_INSERT_NEW_COLLEGE = " INSERT PUB_College (Name, Description) "
			                                           + " SELECT DISTINCT(tmp_clazz.college_name), tmp_clazz.college_name"
			                                           + " FROM tmp_clazz"
			                                           + " LEFT JOIN PUB_College"
			                                           + " ON tmp_clazz.college_name = PUB_College.Name"
			                                           + " WHERE PUB_College.Name IS NULL ";
	
	private List<Clazz> mClazzList;
	
	public CollegeDataSaver(List<Clazz> clazzs) {
		mClazzList = clazzs;
	}
	
	/**
	 * 1.将班级数据插入临时表 tmp_teacher
	 * 2.将原有表 PUB_College 中不存在的数据插入db
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
			
			logger.info("将班级数据插入到 college 表中");
			insertToCollege(dbConn);

			logger.info("删除临时表 tmp_clazz");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将班级数据保存到数据库 操作失败: " + e);
			
		} finally {
			if(dbConn != null) {
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
	
	// 将班级数据插入到 college 表中
	private void insertToCollege(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_COLLEGE);
		
		stmt.executeUpdate(SQL_INSERT_NEW_COLLEGE);
		stmt.close();
	}
}
