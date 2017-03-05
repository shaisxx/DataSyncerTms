package com.shtd.datasyncer.dbsaver.dky.sizheng;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;
/**
 * Sizheng 学院、专业、班级 字段同步 （均在dict_sys中）
 * @author RanWeizheng
 *
 */
public class ClazzDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private static final int DEFAULT_PARENT_ID = 0;
	private static final String DEAFULT_PATH = "0";
	private static final String TEMP_CLAZZ_NAME = "tmp_clazz";
	
	// 删除 临时表 tmp_clazz
	private static final String SQL_DROP_TMP_CLAZZ = " DROP TABLE IF EXISTS `" + TEMP_CLAZZ_NAME + "`";
	
    // 创建 临时表 tmp_clazz
	private static final String SQL_CREATE_TMP_CLAZZ = " CREATE  TABLE `" + TEMP_CLAZZ_NAME + "` ("
												     + "    `id` int(10) NOT NULL auto_increment ,"
			                                         + "     `clazz_num`  varchar(30) NOT NULL COMMENT '班级编号,保存在dict_sys.code中', "
												     + "    `squad_name` varchar(100) NOT NULL COMMENT '班级名称',"
												     + "    `major_name` varchar(50) NOT NULL COMMENT '专业名称',"
												     + "    `college_name` varchar(100) NOT NULL COMMENT '学院名称',"
												     + "    PRIMARY KEY  (`id`)"
												     + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_clazz sql语句的前缀
	private static final String SQL_INSERT_TMP_CLAZZ = " INSERT INTO `" + TEMP_CLAZZ_NAME + "` "
	                                                 + " (clazz_num, squad_name, major_name, college_name) VALUES (?, ?, ?, ?)";
	
		
	// 将 tmp_clazz中有的 college ，而college表中没有的记录，插入到college
	private static final String SQL_INSERT_NEW_COLLEGE = " INSERT dict_sys (title, parent_id, path) "
                                                       + " SELECT DISTINCT(" + TEMP_CLAZZ_NAME + ".college_name), " + DEFAULT_PARENT_ID + ", " + DEAFULT_PATH
                                                       + " FROM " + TEMP_CLAZZ_NAME + " "
                                                       + " LEFT JOIN dict_sys "
                                                       + " ON " + TEMP_CLAZZ_NAME +".college_name = dict_sys.title and dict_sys.parent_id=" + DEFAULT_PARENT_ID
                                                       + " WHERE dict_sys.title IS NULL ";
	
	// 将 tmp_clazz中有的major，而major表中没有的记录，插入到major
	private static final String SQL_INSERT_NEW_MAJOR = " INSERT dict_sys (title, parent_id, path) "
                                                     + " SELECT majors.major_name as title , dict_sys.id as parent_id,  CONCAT('0_', dict_sys.id) AS major_path"
                                                     + "   FROM dict_sys, "
                                                     + " ( "
                                                     + " 	SELECT DISTINCT(" + TEMP_CLAZZ_NAME + ".major_name) as major_name  "
                                                     + "    FROM " + TEMP_CLAZZ_NAME + " "
                                                     + " 	 LEFT JOIN dict_sys "
                                                     + "    ON "
                                                     + "			" + TEMP_CLAZZ_NAME + ".college_name = dict_sys.title "
                                                     + "		AND dict_sys.parent_id!=0 "
                                                     + "		AND dict_sys.path = CONCAT('0_', dict_sys.parent_id)"
                                                     + "	 WHERE dict_sys.title IS NULL"
                                                     + " ) AS majors "
                                                     + "  WHERE dict_sys.title = majors.major_name";

	// 将temp_clazz、dict_sys表中均存在的班级信息进行更新操作： 班级数据符合 parentid != 0, path形如 0_xxx的形式 TODO 需要再次确认
	private static final String SQL_UPDATE_SQUAD = "UPDATE dict_sys , "
																		+ " ( "
																		+ "   SELECT  clazz.id, "
																		+ "				" + TEMP_CLAZZ_NAME + ".clazz_num, "
																		+ "				" + TEMP_CLAZZ_NAME + ".squad_name, "
																		+ "				majors.id as majors_id, "
																		+ "				CONCAT(majors.path, '_', majors.id) as clazz_path "
																		+ "     FROM"
																		+ "          dict_sys as majors, dict_sys as clazz, " + TEMP_CLAZZ_NAME + ""
																		+ "     WHERE"
																		+ "          clazz.`code` = " + TEMP_CLAZZ_NAME + ".clazz_num "
																		+ "		AND majors.title = " + TEMP_CLAZZ_NAME + ".major_name "
																		+ "		AND majors.path = CONCAT('0_', majors.parent_id) "
																		+ ") as update_clazz "
																		+ " SET "
																		+ "	dict_sys.title = update_clazz.squad_name, "
																		+ "	dict_sys.`code` = update_clazz.clazz_num, "
																		+ "	dict_sys.parent_id = update_clazz.majors_id, "
																		+ "	dict_sys.path = update_clazz.clazz_path "
																		+ " WHERE "
																		+ "	dict_sys.id = update_clazz.id"; 
	
	// 将 tmp_clazz中有的squad，而squad表中没有的记录，插入到squad
	private static final String SQL_INSERT_NEW_SQUAD = "INSERT dict_sys (title, `code`, parent_id, path) "
                                                     + " ( "
                                                     + "	SELECT "
                                                     + "		" + TEMP_CLAZZ_NAME + ".squad_name as title, " + TEMP_CLAZZ_NAME + ".clazz_num as code ,  "
                                                     + "		majors.id as majors_id,  "
                                                     + "		CONCAT(majors.path, '_', majors.id) as clazz_path "    
                                                     + "   FROM "
                                                     + "			dict_sys as majors, "
                                                     + "			" + TEMP_CLAZZ_NAME + " LEFT JOIN dict_sys as clazz ON " + TEMP_CLAZZ_NAME + ".clazz_num = clazz.`code` "
                                                     + "	WHERE"
                                                     + "		majors.title = " + TEMP_CLAZZ_NAME + ".major_name "
                                                     + "		AND majors.path = CONCAT('0_', majors.parent_id) "
                                                     + "		AND clazz.`code` is NULL "
                                                     + ")";
                                                 
	
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

			
			logger.info("补全学院、专业信息， 更新班级数据");
			initCollegeMajorSquad(dbConn);
			
			logger.info("删除临时表 tmp_clazz");
//			dropTmpTable(dbConn);
			
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
			prepStmt.setString(1, clazz.getClazzNo());
			prepStmt.setString(2, clazz.getClazzName());
			
			// 专业 和 学院 插入相同数据
			prepStmt.setString(3, clazz.getCollegeName());
			prepStmt.setString(4, clazz.getCollegeName());
			
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
	
	/**
	 * 补全学院、专业信息， 更新班级数据
	 * @param conn
	 * @throws SQLException
	 */
	private void initCollegeMajorSquad(Connection conn) throws SQLException {
		// 插入college
		insertToCollege(conn);
		
		// 插入 major
		insertToMajor(conn);
		
		// 更新 已有的班级 -- 当存在重复的班级号时，只会更新第一个符合条件的，具体原因似乎与mysql的机制有关系
		updateSquad(conn);
		
		//插入之前不存在的班级
		insertToSquad(conn);
	}
	
	/**
	 * 插入之前不存在的学院信息
	 * @param conn
	 * @throws SQLException
	 */
	private void insertToCollege(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_COLLEGE);
		
		stmt.executeUpdate(SQL_INSERT_NEW_COLLEGE);
		stmt.close();
	}
	
	/**
	 * 插入之前不存在的专业信息（学院 与 专业同名）
	 * @param conn
	 * @throws SQLException
	 */
	private void insertToMajor(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_MAJOR);
		
		stmt.executeUpdate(SQL_INSERT_NEW_MAJOR);
		stmt.close();
	}
	
	/**
	 * 更新已有的班级信息
	 * @param conn
	 * @throws SQLException
	 */
	private void updateSquad(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_SQUAD);
		
		stmt.executeUpdate(SQL_UPDATE_SQUAD);
		stmt.close();
	}
	
	/**
	 * 插入之前不存在的班级
	 * @param conn
	 * @throws SQLException
	 */
	private void insertToSquad(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_INSERT_NEW_SQUAD);
		
		stmt.executeUpdate(SQL_INSERT_NEW_SQUAD);
		stmt.close();
	}	
}