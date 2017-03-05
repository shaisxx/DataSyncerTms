package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.AppraisalTeacher;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * 教学评价系统教师数据保存
 * @author Josh
 */
public class AppraisalTeacherDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_teacher
	private static final String SQL_DROP_TMP_TEACHER = " DROP TABLE IF EXISTS `tmp_teacher`";
	
    // 创建 临时表 tmp_teacher
	private static final String SQL_CREATE_TMP_TEACHER = " CREATE TEMPORARY TABLE `tmp_teacher` ("     
												       + "    `id` int(11) NOT NULL auto_increment ,"
												       + "    `username` varchar(32) NOT NULL,"
												       + "    `status` varchar(16) NOT NULL default '1' COMMENT '状态(0-屏蔽，1-正常)'," 
												       + "    `gender` varchar(2) default NULL COMMENT '性别 性别 1-男  2-女',"
												       + "    `job_no` varchar(20) NOT NULL COMMENT '职工号 用于系统登录帐号',"
												       + "    `id_card` varchar(20) default NULL COMMENT '身份证号',"
												       + "    `org_code` varchar(20) default NULL COMMENT '单位号',"
												       + "    PRIMARY KEY  (`id`),"
												       + "    UNIQUE KEY `IDXU_job_no` (`job_no`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_teacher sql语句的前缀
	private static final String SQL_INSERT_TMP_TEACHER = " INSERT INTO `tmp_teacher` (username, status, gender, job_no, id_card, org_code) VALUES (?,?,?,?,?,?)";
	
	//将所有 只在 security_user中存在 而在 tmp_teacher中不存在的教师帐号，设置status为屏蔽
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
													  + "     AND security_user.id <> 1 "
													  + " ) AS b "
													  + "    ON a.id = b.id "
													  + "   SET a.`status` = 0 ";	
		
	// 根据 tmp_teacher中的 工号，查询security_user和teacher中相关数据
	private static final String SQL_SELECT_EXIST_USER_ID_AND_VALUE = 
											            " SELECT security_user.id AS user_id," 
											           + "        tmp_teacher.job_no, "
											           + "        tmp_teacher.gender, "
											           + "        tmp_teacher.`status`, "
											           + "        tmp_teacher.username," 
											           + "        tmp_teacher.id_card"
											           + "   FROM security_user, teacher, tmp_teacher"
											           + "  WHERE security_user.id = teacher.id "
											           + "    AND security_user.login_name = tmp_teacher.job_no"
											           + "    AND security_user.type = 1";
	
	
	// 根据教师工号 更新 security_user表，工号为code
	private static final String SQL_UPDATE_SYS_USER = " UPDATE security_user SET `status` = ? WHERE login_name = ? and type = 1";

	// 根据教师 user_id 更新teacher表
	private static final String SQL_UPDATE_TEACHER = " UPDATE teacher SET gender=? WHERE id=?";	
		
	// 根据 tmp_teacher中的 工号，查询security_user中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_TMP_TEACHER = " SELECT tmp_teacher.*,security_organization.id orgId "
																  	+ "   FROM tmp_teacher "
																  	+ "   LEFT JOIN security_user "
																  	+ "     ON tmp_teacher.job_no = security_user.login_name "
																  	+ "    AND security_user.type=1 "
																  	+ "   LEFT JOIN security_organization "
																  	+ "     ON security_organization.code = tmp_teacher.org_code"
																  	+ "  WHERE security_user.login_name IS NULL ";

	// 插入 security_user表
	private static final String SQL_INSERT_SYS_USER = " INSERT INTO security_user (login_name, password, type, status, create_date, update_date) " 
														 + " VALUES (?, '', '1', ?, NOW(), NOW())";	

	// 插入 teacher表 type = 2 代表教师
	private static final String SQL_INSERT_TEACHER = " INSERT INTO teacher (id, gender, job_no, teacher_name, org_id, create_date, update_date) " 
													  + " SELECT security_user.id, ?, ?, ?, ?, NOW(), NOW()"
													  + "   FROM security_user  "
													  + "  WHERE security_user.login_name = ? " 
													  + "    AND type = 1 ";	
	
	private List<AppraisalTeacher> mTeacherList;
	
	public AppraisalTeacherDataSaver(List<AppraisalTeacher> teachers) {
		mTeacherList = teachers;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_teacher
	 * 2.left join security_user表，查出所有 tmp_teacher中不存在的记录，更新status为不可用
	 * 3.将tmp_teacher中数据 插入/更新到 security_user表中
	 * @return
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
			
			//logger.info("将所有只在security_user中存在的教师帐号，置为屏蔽状态");
			logger.info("将所有只在security_user中存在的教师帐号，置为屏蔽状态");
			updateNotExistTeacherUnavailable(dbConn);
			
			/*
			 * 需要更新 security_user 表的 status,username字段   和 teacher 表的  pinyin,jianpin,gender
			 */
			logger.info("更新所有 在 tmp_teacher 和 security_user中都存在的教师信息 ");
			updateExistTeacherData(dbConn);
			
			logger.info("将只在 tmp_teacher 中存在的数据 插入 security_user 和  teacher 表 ");
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
	private void batchInsertTmpTable(Connection conn, List<AppraisalTeacher> teachers) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_TEACHER);
		
		for (AppraisalTeacher teacher : teachers) {
			prepStmt.setString(1, teacher.getName());
			prepStmt.setString(2, teacher.getDBStatus());
			prepStmt.setString(3, teacher.getGender());
			prepStmt.setString(4, teacher.getGongHao());
			prepStmt.setString(5, teacher.getSenFenZheng());
			prepStmt.setString(6, teacher.getDanWei());

			logger.info(prepStmt.toString() + ";" 
			          + teacher.getName() + ","
					  + teacher.getDBStatus() + ","
			          + teacher.getGender() + ","
			          + teacher.getGongHao() + ","
			          + teacher.getSenFenZheng() + ","
			          + teacher.getDanWei());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}

		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	/* 
	 * 将所有 只在 security_user中存在 而在 tmp_teacher中不存在的教师帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistTeacherUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}

	/*
	 * 需要更新security_user 表的 status,username 字段
	 * info_user 表的 pinyin,jianpin,gender 字段
	 */
	private void updateExistTeacherData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_UPDATE_SYS_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_UPDATE_TEACHER);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_USER_ID_AND_VALUE); 
		if (rs != null) {
			
			String jobNo = "";
			while(rs.next()) {				
				/*
				 * 循环中每一条记录 需要更新 security_user 和 teacher 两个表
				 */
				securityUserPst.setString(1, rs.getString("status"));
				
				jobNo = rs.getString("job_no");
				securityUserPst.setString(2, jobNo);

				logger.info(securityUserPst.toString() + ";" + rs.getString("status") +","+jobNo);
				
				securityUserPst.addBatch();

				teacherPst.setString(1, rs.getString("gender"));
				teacherPst.setString(2, rs.getString("user_id"));
				
				logger.info(teacherPst.toString() + ";" 
						  + rs.getString("gender") + ","
						  + rs.getString("user_id"));
				
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
	 * 将只在 tmp_teacher 中存在的数据 插入 security_user 和 teacher 表
	 */
	private void insertNewTeacherData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_INSERT_SYS_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_INSERT_TEACHER);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_TMP_TEACHER); 
		if (rs != null) {
			String jobNo = "",username = "";
			while(rs.next()) {
				jobNo = rs.getString("job_no");
				securityUserPst.setString(1, jobNo);
				
				securityUserPst.setInt(2, Integer.valueOf(rs.getString("status")));
				
				logger.info(securityUserPst.toString() + ";" + jobNo + "," + rs.getString("status"));
				
				securityUserPst.addBatch();

				teacherPst.setInt(1, StringUtils.isBlank(rs.getString("gender"))?0:Integer.valueOf(rs.getString("gender")));
				teacherPst.setString(2, jobNo);
				teacherPst.setString(3, rs.getString("username"));
				teacherPst.setString(4, rs.getString("orgId"));
				teacherPst.setString(5, jobNo);
				
				logger.info(teacherPst.toString() + ";" 
						  + rs.getString("gender")
						  + jobNo + ","
						  + username + ","
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