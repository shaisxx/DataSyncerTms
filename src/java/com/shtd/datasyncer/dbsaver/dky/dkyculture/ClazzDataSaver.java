package com.shtd.datasyncer.dbsaver.dky.dkyculture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

public class ClazzDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_clazz
	private static final String SQL_DROP_TMP_CLAZZ = " DROP TABLE IF EXISTS `tmp_clazz`";
	
    // 创建 临时表 tmp_clazz
	private static final String SQL_CREATE_TMP_CLAZZ = " CREATE TEMPORARY TABLE `tmp_clazz` ("
												     + "    `id` int(10) NOT NULL auto_increment ,"
												     + "    `squad_name` varchar(100) NOT NULL COMMENT '班级名称',"
												     + "    `major_name` varchar(50) NOT NULL COMMENT '专业名称',"
												     + "    `college_name` varchar(100) NOT NULL COMMENT '学院名称',"
												     + "    PRIMARY KEY  (`id`)"
												     + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_clazz sql语句的前缀
	private static final String SQL_INSERT_TMP_CLAZZ = " INSERT INTO `tmp_clazz` "
	                                                 + " (squad_name, major_name, college_name) VALUES (?,?,?)";
	
		
	// 将 tmp_clazz中有的 college ，而college表中没有的记录，插入到college
	private static final String SQL_INSERT_NEW_COLLEGE = " INSERT college (college_name, directions) "
                                                       + " SELECT DISTINCT(tmp_clazz.college_name), '' "
                                                       + " FROM tmp_clazz "
                                                       + " LEFT JOIN college "
                                                       + " ON tmp_clazz.college_name = college.college_name "
                                                       + " WHERE college.college_name IS NULL ";
	
	// 将 tmp_clazz中有的major，而major表中没有的记录，插入到major
	private static final String SQL_INSERT_NEW_MAJOR = " INSERT INTO major (major_name, college_id, directions) "
                                                     + " SELECT DISTINCT(tmp_clazz.major_name),college.id AS collegeid, '' "
                                                     + "   FROM tmp_clazz "
                                                     + "   LEFT JOIN "
                                                     + " ( "
                                                     + " 	SELECT college.college_name, major.major_name "
                                                     + "    FROM college, major  "
                                                     + " 	 WHERE college.id = major.college_id "
                                                     + " ) AS clazz_data "
                                                     + "     ON tmp_clazz.college_name = clazz_data.college_name "
                                                     + "    AND tmp_clazz.major_name = clazz_data.major_name "
                                                     + "  INNER JOIN college "
                                                     + "     ON college.college_name = tmp_clazz.college_name " 
                                                     + "  WHERE clazz_data.major_name IS NULL";

	
	// 将 tmp_clazz中有的squad，而squad表中没有的记录，插入到squad
	private static final String SQL_INSERT_NEW_SQUAD = " INSERT INTO squad (squad_name, major_id, directions) "
                                                     + " SELECT DISTINCT(tmp_clazz.squad_name), college_major.majorid , ''  "    
                                                     + "   FROM tmp_clazz "
                                                     + "   LEFT JOIN "
                                                     + "        ( "
                                                     + "          SELECT college.college_name, major.major_name, squad.squad_name "
                                                     + "            FROM college, major, squad "
                                                     + "           WHERE college.id = major.college_id "
                                                     + "             AND major.id = squad.major_id "
                                                     + "        ) AS squad_data "
                                                     + "     ON tmp_clazz.college_name = squad_data.college_name "
                                                     + "    AND tmp_clazz.major_name = squad_data.major_name "
                                                     + "    AND tmp_clazz.squad_name = squad_data.squad_name "
                                                     + "  INNER JOIN " 
                                                     + "        ( "
                                                     + "          SELECT major.id AS majorid, major.major_name, college.college_name "
                                                     + "            FROM major, college "
                                                     + "           WHERE major.college_id = college.id "
                                                     + "        ) AS college_major "
                                                     + "     ON college_major.college_name = tmp_clazz.college_name " 
                                                     + "    AND college_major.major_name = tmp_clazz.major_name  "
                                                     + "  WHERE squad_data.squad_name IS NULL ";
	
	private List<Clazz> mClazzList;
	
	public ClazzDataSaver(List<Clazz> clazzs) {
		mClazzList = clazzs;
	}
	
	/**
	 * 1.将班级数据插入临时表 tmp_teacher
	 * 2.根据 学院  专业  班级 的顺序，逐条插入到相关表中 
	 *   其中，学院  和  专业 的名称一样！
	 * @return
	 * @author zhanggn
	 */
	public boolean doSave() {
		if (mClazzList == null || mClazzList.size() <= 0) {
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
			 * 创建临时表 -- tmp_clazz  
			 */
			logger.info("创建临时表 -- tmp_clazz");
			recreateTmpTable(dbConn);
			
			logger.info("将班级数据插入临时表");
			batchInsertTmpTable(dbConn, mClazzList);

			
			logger.info("将班级数据插入到 college  major 和 squad 表中");
			insertToCollegeMajorSquad(dbConn);

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
	
	// 将班级数据插入到 college  major 和 squad 表中
	private void insertToCollegeMajorSquad(Connection conn) throws SQLException {
		
		// 插入 college
		insertToCollege(conn);
		
		// 插入 major
		insertToMajor(conn);
		
		// 插入 squad
		insertToSquad(conn);
	}
	
	private void insertToCollege(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_COLLEGE);
		
		stmt.executeUpdate(SQL_INSERT_NEW_COLLEGE);
		stmt.close();
	}
	
	private void insertToMajor(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_MAJOR);
		
		stmt.executeUpdate(SQL_INSERT_NEW_MAJOR);
		stmt.close();
	}
	
	private void insertToSquad(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_SQUAD);
		
		stmt.executeUpdate(SQL_INSERT_NEW_SQUAD);
		stmt.close();
	}
	
}
