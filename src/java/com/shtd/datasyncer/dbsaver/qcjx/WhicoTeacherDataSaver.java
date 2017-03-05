package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.WhicoTeacher;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.PinYin;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * whico平台教师数据保存
 * 
 * 
 *<ul>
 *	<pre>教师接口的操作说明</pre>
 *	<li>创建教师临时表。字段(教师姓名,状态,性别,职工号,身份证号,单位号)</li>
 *	<li>将所获得接口的教师数据插入临时表中。</li>
 *	<li>临时表与教师表(info_teacher和sys_user)进行对比：
 *		<p>1.如果临时表存在的职工号是教师表中未存在的，将进行新增操作，将教师数据插入到教师表中</p>
 *		<p>2.如果临时表存在的职工号是教师表中已存在的，将进行更新操作，以职工号为条件更新到教师表中</p>
 *	    <p style="color:red">3.如果教师表存在的职工号是临时表中未存在的，将教师表中该数据进行逻辑删除。（原因：因微课平台教师数据已CAS数据为准）</p>
 *	</li>
 *	<li>清空并删除临时表中。</li>
 *</ul>	
 * @author jiangnan
 * @date 2015.11.12
 */
public class WhicoTeacherDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_teacher
	private static final String SQL_DROP_TMP_TEACHER = " DROP TABLE IF EXISTS `tmp_teacher`";
	
    // 创建 临时表 tmp_teacher TEMPORARY
	private static final String SQL_CREATE_TMP_TEACHER = " CREATE TEMPORARY TABLE `tmp_teacher` ("     
												       + "    `id` int(10) NOT NULL auto_increment ,"
												       + "    `username` varchar(32) NOT NULL COMMENT '教师姓名',"
												       + "    `status` varchar(16) NOT NULL default '1' COMMENT '状态(0-屏蔽，1-正常)'," 
												       + "    `gender` varchar(2) default NULL COMMENT '性别 性别 1-男  2-女',"
												       + "    `job_no` varchar(20) NOT NULL COMMENT '职工号 用于系统登录帐号',"
												       + "    `id_card` varchar(20) default NULL COMMENT '身份证号',"
												       + "    `unit_code` varchar(20) default NULL COMMENT '单位号',"
												       + "    PRIMARY KEY  (`id`),"
												       + "    UNIQUE KEY `IDXU_job_no` (`job_no`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_teacher sql语句的前缀
	private static final String SQL_INSERT_TMP_TEACHER = " INSERT INTO `tmp_teacher` (username, status, gender, job_no, id_card,unit_code) VALUES (?,?,?,?,?,?)";
	
	//将所有 只在 sys_user中存在 而在 tmp_teacher中不存在的教师帐号，设置logic_delete为逻辑删除
	private static final String SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE = 
														" UPDATE sys_user AS a INNER JOIN"
													  + " ("
													  + "  SELECT sys_user.id "
													  + "    FROM sys_user "
													  + "    LEFT JOIN tmp_teacher "
													  + "      ON tmp_teacher.job_no = sys_user.code "
													  + "   WHERE tmp_teacher.job_no IS NULL "
													  + "     AND sys_user.type = 2 "
													  + "     AND sys_user.`logic_delete`=0 "
													  + " ) AS b "
													  + "    ON a.id = b.id "
													  + "   SET a.`logic_delete` = 1 ";	
	
	//将所有在sys_user表中逻辑删除的老师，在info_teacher表中进行删除
	private static final String SQL_UPDATE_INFO_TEACHER_ONLY_UNAVAILABLE = " UPDATE info_teacher AS a INNER JOIN ( "
													 + " SELECT sys_user.id FROM sys_user "
													 + " INNER JOIN info_teacher ON info_teacher.user_id = sys_user.id "
													 + " WHERE sys_user.type = 2 AND sys_user.logic_delete = 1 " 
													 + " AND info_teacher.logic_delete = 0 "
													 + " ) AS b ON a.user_id = b.id "
													 + " SET a.logic_delete = 1 ";
	
	
	// 根据 tmp_teacher中的 工号，查询sys_user和info_teacher中相关数据
	private static final String SQL_SELECT_EXIST_USER_ID_AND_VALUE = 
											            " SELECT sys_user.id AS user_id," 
											           //+ "        teacher.id       AS teacher_id,"
											           + "        tmp_teacher.job_no, "
											           + "        tmp_teacher.gender, "
											           + "        tmp_teacher.`status`, "
											           + "        tmp_teacher.username," 
											           + "        tmp_teacher.id_card,"
											           + "        tmp_teacher.unit_code"
											           + "   FROM sys_user, info_teacher, tmp_teacher"
											           + "  WHERE sys_user.id = info_teacher.user_id "
											           + "    AND sys_user.code = tmp_teacher.job_no"
											           + "    AND sys_user.type = 2";
	
	
	// 根据教师工号 更新 sys_user表，工号为code
	private static final String SQL_UPDATE_SYS_USER = " UPDATE sys_user SET `username` = ?,`logic_delete`=? WHERE code = ? and type = 2";

	// 根据教师 user_id 更新info_teacher表
	private static final String SQL_UPDATE_TEACHER = " UPDATE info_teacher SET pinyin=?,jianpin=?,gender=?,unit_code=?,logic_delete=? WHERE user_id=?";	
		
	// 根据 tmp_teacher中的 工号，查询sys_user中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_TMP_TEACHER = " SELECT tmp_teacher.* "
																  	+ "   FROM tmp_teacher "
																  	+ "   LEFT JOIN sys_user "
																  	+ "     ON tmp_teacher.job_no = sys_user.code "
																  	+ "    AND sys_user.type=2 "
																  	+ "  WHERE sys_user.code IS NULL ";

	// 插入 sys_user表
	private static final String SQL_INSERT_SYS_USER = " INSERT INTO sys_user (code, username, password, salt, type, last_modify_user_id, create_date, modify_date, logic_delete) " 
														 + " VALUES (?, ?,'', '', '2', 1, NOW(), NOW(), ?)";	

	// 插入 info_teacher表 type = 2 代表教师
	private static final String SQL_INSERT_TEACHER = " INSERT INTO info_teacher (user_id, pinyin, jianpin, gender, staff_no,unit_code, last_modify_user_id, create_date, modify_date, logic_delete) " 
													  + " SELECT sys_user.id, ?, ?, ?, ?, ?, 1, NOW(), NOW(), ?"
													  + "   FROM sys_user  "
													  + "  WHERE sys_user.code = ?" 
													  + "    AND type = 2 ";		
		
	//插入 bbs_user_info 表数据
	private static final String SQL_INSERT_BBS_USER_INFO = " INSERT INTO bbs_info_user (user_id, user_status, gender) SELECT"
													  + " sys_user.id, ?, ? FROM sys_user WHERE sys_user.CODE = ? AND type = 2";
	
	private List<WhicoTeacher> mTeacherList;
	
	public WhicoTeacherDataSaver(List<WhicoTeacher> teachers) {
		mTeacherList = teachers;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_teacher
	 * 2.left join sys_user表，查出所有 tmp_teacher中不存在的记录，更新status为不可用
	 * 3.将tmp_teacher中数据 插入/更新到 sys_user表中
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
			logger.info("将所有只在sys_user中存在的教师帐号，置为屏蔽状态");
			updateNotExistTeacherUnavailable(dbConn);
			
			/*
			 * 需要更新 sys_user 表的 status,username字段   和 info_teacher 表的  pinyin,jianpin,gender
			 */
			logger.info("更新所有 在 tmp_teacher 和 sys_user中都存在的教师信息 ");
			updateExistTeacherData(dbConn);
			
			logger.info("将只在 tmp_teacher 中存在的数据 插入 sys_user 和  info_teacher 表 ");
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
	private void batchInsertTmpTable(Connection conn, List<WhicoTeacher> teachers) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_TEACHER);
		
		for (WhicoTeacher teacher : teachers) {
			prepStmt.setString(1, teacher.getName());
			prepStmt.setString(2, teacher.getDBStatus());
			prepStmt.setString(3, teacher.getDBGender());
			prepStmt.setString(4, teacher.getGongHao());
			prepStmt.setString(5, teacher.getSenFenZheng());
			prepStmt.setString(6, teacher.getDanWei());
			logger.info(prepStmt.toString() + ";" 
			          + teacher.getName() + ","
					  + teacher.getDBStatus() + ","
			          + teacher.getDBGender() + ","
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
	 * 将所有 只在 sys_user中存在 而在 tmp_teacher中不存在的教师帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistTeacherUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_SECURITY_USER_ONLY_UNAVAILABLE);
		
		logger.info(SQL_UPDATE_INFO_TEACHER_ONLY_UNAVAILABLE);
		stmt.executeUpdate(SQL_UPDATE_INFO_TEACHER_ONLY_UNAVAILABLE);
		
		stmt.close();
		return true;
	}

	/*
	 * 需要更新sys_user 表的 status,username 字段
	 * info_user 表的 pinyin,jianpin,gender 字段
	 */
	private void updateExistTeacherData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_UPDATE_SYS_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_UPDATE_TEACHER);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_USER_ID_AND_VALUE); 
		if (rs != null) {
			
			String jobNo = "", username = "", pinyin = "", pinyinHeader = "", logicDelete="";
			while(rs.next()) {				
				/*
				 * 循环中每一条记录 需要更新 sys_user 和 info_teacher 两个表
				 */
//				securityUserPst.setString(1, rs.getString("status"));
				
				username = rs.getString("username");
				securityUserPst.setString(1, username);
				
				logicDelete = Integer.parseInt(rs.getString("status"))==0?"1":"0";
				securityUserPst.setString(2, logicDelete);
				
				jobNo = rs.getString("job_no");
				securityUserPst.setString(3, jobNo);

				logger.info(securityUserPst.toString() + ";" + username +","+ logicDelete +","+jobNo);
				
				securityUserPst.addBatch();

				pinyin = PinYin.getPingYin(username);
				teacherPst.setString(1, pinyin);
				
				pinyinHeader = PinYin.getPinYinHeadChar(username);
				teacherPst.setString(2, pinyinHeader);
				teacherPst.setString(3, rs.getString("gender"));
				teacherPst.setString(4, rs.getString("unit_code"));
				teacherPst.setString(5, logicDelete);
				teacherPst.setString(6, rs.getString("user_id"));
				
				logger.info(teacherPst.toString() + ";" 
						  + pinyin + ","
						  + pinyinHeader + ","
						  + rs.getString("gender") + ","
						  + rs.getString("unit_code") + ","
						  + logicDelete + ","
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
	 * 将只在 tmp_teacher 中存在的数据 插入 sys_user ,info_teacher 和 bbs_user_info表插入数据
	 */
	private void insertNewTeacherData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement securityUserPst = conn.prepareStatement(SQL_INSERT_SYS_USER);
		PreparedStatement teacherPst      = conn.prepareStatement(SQL_INSERT_TEACHER);
		PreparedStatement bbsUserPst      = conn.prepareStatement(SQL_INSERT_BBS_USER_INFO);//bbs_user_info同步数据
		
//		SQL_INSERT_BBS_USER_INFO
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_TMP_TEACHER); 
		if (rs != null) {
			String jobNo = "", username = "", pinyin = "", pinyinHeader = "", logicDelete="";
			while(rs.next()) {
				// 循环中每一条记录 需要更新 sys_user 和 teacher 两个表
				jobNo = rs.getString("job_no");
				securityUserPst.setString(1, jobNo);
				
				username = rs.getString("username");
				securityUserPst.setString(2, username);
					
//				securityUserPst.setString(3, rs.getString("status"));
				
				logicDelete = Integer.parseInt(rs.getString("status"))==0?"1":"0";
				securityUserPst.setString(3, logicDelete);
				
				logger.info(securityUserPst.toString() + ";" + jobNo + "," + username +","+ logicDelete);
				
				securityUserPst.addBatch();

				 
				pinyin = PinYin.getPingYin(username);
				teacherPst.setString(1, pinyin);
				
				pinyinHeader = PinYin.getPinYinHeadChar(username);
				teacherPst.setString(2, pinyinHeader);
				teacherPst.setString(3, rs.getString("gender"));
				teacherPst.setString(4, jobNo);
				teacherPst.setString(5, rs.getString("unit_code"));
				teacherPst.setString(6, logicDelete);
				teacherPst.setString(7, jobNo);
				
				logger.info(teacherPst.toString() + ";" 
						  + pinyin + ","
						  + pinyinHeader + ","
						  + rs.getString("gender") + ","
						  + jobNo + ","
						  + rs.getString("unit_code")+","
						  + logicDelete + ","
						  + jobNo );
				
				teacherPst.addBatch();
				
				//bbs_user_info 同步数据
				bbsUserPst.setString(1, rs.getString("status"));
				bbsUserPst.setString(2, rs.getString("gender"));
				bbsUserPst.setString(3, jobNo);
				
				logger.info(bbsUserPst.toString() + ";" + rs.getString("status") + "," + rs.getString("gender") +","+ jobNo);
				bbsUserPst.addBatch();
			};

			securityUserPst.executeBatch();
			teacherPst.executeBatch();
			bbsUserPst.executeBatch();
		}
		
		stmt.close();
		securityUserPst.close();
		teacherPst.close();
		bbsUserPst.close();
	}
	
}
