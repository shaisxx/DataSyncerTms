package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.Teacher;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYin;
import com.shtd.datasyncer.utils.db.MysqlDb;

public class TeacherDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_teacher
	private static final String SQL_DROP_TMP_TEACHER = " DROP TABLE IF EXISTS `tmp_teacher`";
	
    // 创建 临时表 tmp_teacher
	private static final String SQL_CREATE_TMP_TEACHER = " CREATE TEMPORARY TABLE `tmp_teacher` ("     
												       + "    `id` int(10) NOT NULL auto_increment ,"
												       + "    `username` varchar(32) NOT NULL,"
												       + "    `status` varchar(16) NOT NULL default '1' COMMENT '状态(0-屏蔽，1-正常)'," 
												       + "    `gender` varchar(2) default NULL COMMENT '性别 性别 1-男  2-女',"
												       + "    `job_no` varchar(20) NOT NULL COMMENT '职工号 用于系统登录帐号',"
												       + "    `id_card` varchar(20) default NULL COMMENT '身份证号',"
												       + "    PRIMARY KEY  (`id`),"
												       + "    UNIQUE KEY `IDXU_job_no` (`job_no`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_teacher sql语句的前缀
	private static final String SQL_INSERT_TMP_TEACHER = " INSERT INTO `tmp_teacher` (username, status, gender, job_no, id_card) VALUES (?,?,?,?,?)";
	
	// 将所有 只在security_user中存在 而在 tmp_teacher中不存在的教师帐号，设置status为屏蔽
	private static final String SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE = 
		                                                 " UPDATE security_user AS a INNER JOIN"
		                                               + " ("
		                                               + "  SELECT security_user.id "
		                                               + "    FROM security_user "
		                                               + "    LEFT JOIN tmp_teacher "
		                                               + "      ON tmp_teacher.job_no = security_user.login_name "
		                                               + "   WHERE tmp_teacher.job_no IS NULL "
		                                               + "     AND security_user.type = 1 "
		                                               + "     AND security_user.`status`=1 "
		                                               + " ) AS b "
		                                               + "    ON a.id = b.id "
		                                               + "   SET a.`status` = 0 ";
	
	// 根据 tmp_teacher中的 工号，查询security_user和teacher中相关数据
	private static final String SQL_SELECT_EXIST_USER_ID_AND_VALUE = 
		                                               " SELECT security_user.id AS security_user_id," 
                                                     + "        teacher.id       AS teacher_id,"
                                                     + "        tmp_teacher.job_no, "
                                                     + "        tmp_teacher.gender, "
                                                     + "        tmp_teacher.`status`, "
                                                     + "        tmp_teacher.username," 
                                                     + "        tmp_teacher.id_card"
                                                     + "   FROM security_user, teacher, tmp_teacher"
                                                     + "  WHERE security_user.login_name = teacher.job_no "
                                                     + "    AND security_user.login_name = tmp_teacher.job_no"
                                                     + "    AND security_user.type = 1";
	
	// 根据教师工号 更新 security_user表，工号为login_name
	private static final String SQL_UPDATE_SECURITY_USER = " UPDATE security_user SET `status` = ? WHERE login_name = ? and type = 1";

	// 根据教师工号 更新 teacher表，工号为job_no
	private static final String SQL_UPDATE_TEACHER = " UPDATE teacher SET teacher_name=?, spell_name=?, spell_short_name=?, gender=?  WHERE job_no = ?";
		
	// 根据 tmp_teacher中的 工号，查询security_user中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_TMP_TEACHER = " SELECT tmp_teacher.* "
                                                                  + "   FROM tmp_teacher "
                                                                  + "   LEFT JOIN security_user "
                                                                  + "     ON tmp_teacher.job_no = security_user.login_name "
                                                                  + "    AND security_user.type=1 "
                                                                  + "  WHERE security_user.login_name IS NULL ";

	// 插入 security_user表
	private static final String SQL_INSERT_SECURITY_USER = " INSERT INTO security_user (login_name, password, type, status) " 
			                                             + " VALUES (?, '', '1', ?)";

	// 插入 teacher表
	private static final String SQL_INSERT_TEACHER = " INSERT INTO teacher (id, job_no, teacher_name, spell_name, spell_short_name, gender) " 
                                                   + " SELECT security_user.id, ?, ?, ?, ?, ? "
                                                   + "   FROM security_user  "
                                                   + "  WHERE security_user.login_name=?" 
                                                   + "    AND type=1 ";
		
		
	
	
	private List<Teacher> mTeacherList;
	
	public TeacherDataSaver(List<Teacher> teachers) {
		mTeacherList = teachers;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_teacher
	 * 2.left join security_user表，查出所有 tmp_teacher中不存在的记录，更新status为不可用
	 * 3.将tmp_teacher中数据 插入/更新到 security_user表中
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
			 * 创建临时表 -- tmp_teacher 教师工号 不能为空，且不可重复 要当作登录login name
			 *                         教师姓名 不能为空
			 */
			logger.info("创建临时表 -- tmp_teacher");
			recreateTmpTable(dbConn);
			
			logger.info("将教师数据插入临时表");
			batchInsertTmpTable(dbConn, mTeacherList);
			
			logger.info("将所有只在security_user中存在的教师帐号，置为屏蔽状态");
			updateNotExistTeacherUnavailable(dbConn);
			
			// 需要更新 security_user 表的 status字段   和 teacher 表的 teacher_name、gender、spell_name、spell_short_name
			logger.info("更新所有 在 tmp_teacher 和 security_user中都存在的教师信息 ");
			updateExistTeacherData(dbConn);
			
			logger.info("将只在 tmp_teacher 中存在的数据 插入 security_user 和 teacher 表 ");
			insertNewTeacherData(dbConn);

			logger.info("删除临时表 tmp_teacher");
			dropTmpTable(dbConn);
			
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
			prepStmt.setString(1, teacher.getName());
			prepStmt.setString(2, teacher.getDBStatus());
			prepStmt.setString(3, teacher.getDBGender());
			prepStmt.setString(4, teacher.getGongHao());
			prepStmt.setString(5, teacher.getSenFenZheng());

			logger.info(prepStmt.toString() + ";" 
			          + teacher.getName() + ","
					  + teacher.getDBStatus() + ","
			          + teacher.getDBGender() + ","
			          + teacher.getGongHao() + ","
			          + teacher.getSenFenZheng());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}

		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	/* 将所有只存在于 tmp_teacher 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistTeacherUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}
	
	
	
	// 更新所有 在 tmp_teacher 和 security_user中都存在的教师信息 
	// 需要更新 security_user 表的 status字段 和 teacher 表的 teacher_name、gender、spell_name、spell_short_name
	private void updateExistTeacherData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_UPDATE_SECURITY_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_UPDATE_TEACHER);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_USER_ID_AND_VALUE); 
		if (rs != null) {
			
			String jobNo = "", username = "", pinyin = "", pinyinHeader = "";
			while(rs.next()) {
				
				// 循环中每一条记录 需要更新 security_user 和 teacher 两个表
				securityUserPst.setString(1, rs.getString("status"));
				
				jobNo = rs.getString("job_no");
				securityUserPst.setString(2, jobNo);

				logger.info(securityUserPst.toString() + ";" + rs.getString("status") + ","+ jobNo);
				
				securityUserPst.addBatch();

				username = rs.getString("username");
				teacherPst.setString(1, username);
				
				pinyin = PinYin.getPingYin(username);
				teacherPst.setString(2, pinyin);
				
				pinyinHeader = PinYin.getPinYinHeadChar(username);
				teacherPst.setString(3, pinyinHeader);
				teacherPst.setString(4, rs.getString("gender"));
				teacherPst.setString(5, jobNo);
				
				logger.info(teacherPst.toString() + ";" 
				          + username + ","
						  + pinyin + ","
						  + pinyinHeader + ","
						  + rs.getString("gender") + ","
						  + jobNo);
				
				teacherPst.addBatch();
			};

			securityUserPst.executeBatch();
			teacherPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		teacherPst.close();
	}
	
	// 将只在 tmp_teacher 中存在的数据 插入 security_user 和 teacher 表
	private void insertNewTeacherData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_INSERT_SECURITY_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_INSERT_TEACHER);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_TMP_TEACHER); 
		if (rs != null) {
			String jobNo = "", username = "", pinyin = "", pinyinHeader = "";
			while(rs.next()) {
				
				// 循环中每一条记录 需要更新 security_user 和 teacher 两个表
				jobNo = rs.getString("job_no");
				securityUserPst.setString(1, jobNo);
				securityUserPst.setString(2, rs.getString("status"));
				
				logger.info(securityUserPst.toString() + ";" + jobNo + "," + rs.getString("status"));
				
				securityUserPst.addBatch();

				teacherPst.setString(1, jobNo);
				
				username = rs.getString("username");
				teacherPst.setString(2, username);

				pinyin = PinYin.getPingYin(username);
				teacherPst.setString(3, pinyin);
				
				pinyinHeader = PinYin.getPinYinHeadChar(username);
				teacherPst.setString(4, pinyinHeader);
				teacherPst.setString(5, rs.getString("gender"));
				teacherPst.setString(6, jobNo);
				
				logger.info(teacherPst.toString() + ";" 
				          + jobNo + ","
						  + username + ","
						  + pinyin + ","
						  + pinyinHeader + ","
						  + rs.getString("gender") + ","
						  + jobNo);
				
				teacherPst.addBatch();
			};

			securityUserPst.executeBatch();
			teacherPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		teacherPst.close();
	}
	
}
