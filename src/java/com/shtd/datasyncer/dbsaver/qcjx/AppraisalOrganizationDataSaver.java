package com.shtd.datasyncer.dbsaver.qcjx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.qcjx.AppraisalOrganization;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

/**
 * 教学评价系统组织结构数据保存
 * @author Josh
 */
public class AppraisalOrganizationDataSaver {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	// 删除 临时表 tmp_organization
	private static final String SQL_DROP_TMP_DEPARTMENT = " DROP TABLE IF EXISTS `tmp_organization`";
    // 创建 临时表 tmp_organization
	private static final String SQL_CREATE_TMP_DEPARTMENT = " CREATE TABLE `tmp_organization` ("     
												       + "    `id` int(10) NOT NULL auto_increment ,"
												       + "    `unitno` varchar(32) NOT NULL COMMENT '院系,专业号',"
												       + "    `unitname` varchar(128) NOT NULL COMMENT '院系,专业名称'," 
												       + "    `parentno` varchar(32) default NULL COMMENT '隶属单位号',"
												       + "    PRIMARY KEY  (`id`)"
												       + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	// 批量插入 tmp_organization sql语句的前缀
	private static final String SQL_INSERT_TMP_DEPARTMENT = " INSERT INTO `tmp_organization` (unitno, unitname, parentno) VALUES (?,?,?)";
	
	//将所有 只在 security_organization中存在 而在 tmp_organization中不存在的ID，设置status为屏蔽
	private static final String SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE = 
														" UPDATE security_organization AS a INNER JOIN"
													  + " ("
													  + "  SELECT security_organization.code "
													  + "    FROM security_organization "
													  + "    LEFT JOIN tmp_organization "
													  + "      ON tmp_organization.unitno = security_organization.code "
													  + "   WHERE tmp_organization.unitno IS NULL "
													  + "     AND security_organization.id <> 1 "
													  + "     AND security_organization.`status` = 1 "
													  + " ) AS b "
													  + "    ON a.code = b.code "
													  + "   SET a.`status` = 0";
	
	private static final String SQL_SELECT_EXIST_DEPARTMENT = 
											            " SELECT tmp_organization.unitno,   "
											          + "        tmp_organization.unitname, "
											          + "        tmp_organization.parentno  "
											          + "  FROM  security_organization, tmp_organization"
											          + "  WHERE security_organization.code = tmp_organization.unitno";
	

	// 根据系部no更新系部表
	private static final String SQL_UPDATE_DEPARTMENT = " UPDATE security_organization SET `title` = ? WHERE code = ? ";

	// 根据 tmp_organization中的 工号，查询security_organization中不存在的记录
	private static final String SQL_SELECT_ONLY_EXIST_BY_DEPARTMENT = " SELECT to2.id parentId,to1.* "
																  	+ "   FROM tmp_organization to1 "
																  	+ "   LEFT JOIN tmp_organization to2 "
																  	+ "     ON to2.unitno = to1.parentno "
																  	+ "   LEFT JOIN security_organization so"
																  	+ "     ON to1.unitno = so.code "
																  	+ "  WHERE so.code IS NULL ";

	private static final String SQL_INSERT_DEPARTMENT = " INSERT INTO security_organization (code, name, parent_id, path) " 
														 + " VALUES (?, ?, ?, 1)";	

	private List<AppraisalOrganization> mOrganizationList;
	
	public AppraisalOrganizationDataSaver(List<AppraisalOrganization> departments) {
		mOrganizationList = departments;
	}
	
	/**
	 * 1.将教师数据插入临时表 tmp_organization
	 * 2.left join sys_user表，查出所有 tmp_organization中不存在的记录，更新status为不可用
	 * 3.将tmp_organization中数据 插入/更新到 sys_user表中
	 * @return
	 */
	public boolean doSave() {
		if (mOrganizationList == null || mOrganizationList.size() <= 0) {
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
			 * 创建临时表 -- tmp_organization
			 */
			logger.info("创建临时表 -- tmp_organization");
			recreateTmpTable(dbConn);
			
			logger.info("将组织机构数据插入临时表");
			batchInsertTmpTable(dbConn, mOrganizationList);
			
			logger.info("将所有 只在 security_organization中存在 而在 tmp_organization中不存在的ID，设置status为屏蔽");
			updateNotExistOrganizationUnavailable(dbConn);

			logger.info("更新所有 在 tmp_organization 和 security_organization中都存在的信息 ");
			updateExistOrganizationData(dbConn);
			
			logger.info("将只在 tmp_organization 中存在的数据 插入 security_organization 表 ");
			insertNewOrganizationData(dbConn);

			logger.info("删除临时表 tmp_organization");
			dropTmpTable(dbConn);
			
			dbConn.commit();
			dbConn.setAutoCommit(true);

			logger.info("DB操作结束 ");
			return true;
			
		} catch (Exception e) {
			logger.error("将组织结构数据保存到数据库 操作失败: " + e);
			e.printStackTrace();
			
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
	
	
	// 重新创建临时表 tmp_organization
	private void recreateTmpTable(Connection conn) throws SQLException {
		dropTmpTable(conn);
		
		Statement stmt = conn.createStatement();		
		
		logger.info(SQL_CREATE_TMP_DEPARTMENT);
		
		stmt.executeUpdate(SQL_CREATE_TMP_DEPARTMENT);
		stmt.close();
	}
	
	private void dropTmpTable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		logger.info(SQL_DROP_TMP_DEPARTMENT);
		
		stmt.executeUpdate(SQL_DROP_TMP_DEPARTMENT);
		stmt.close();
	}
	
	// 将所有department数据插入临时表
	private void batchInsertTmpTable(Connection conn, List<AppraisalOrganization> departments) throws SQLException {
		PreparedStatement prepStmt = conn.prepareStatement(SQL_INSERT_TMP_DEPARTMENT);
		
		//unitno, unitname, parentno
		for (AppraisalOrganization department : departments) {
			prepStmt.setString(1, department.getUnitNo());
			prepStmt.setString(2, department.getName());
			prepStmt.setString(3, department.getParentNo());

			logger.info(prepStmt.toString() + ";" 
			          + department.getUnitNo() + ","
					  + department.getName() + ","
			          + department.getParentNo());
			
			// 把一个SQL命令加入命令列表  
			prepStmt.addBatch();
		}
		prepStmt.executeBatch();
		prepStmt.close();
	}
	
	
	/* 将所有只存在于 tmp_organization 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	private boolean updateNotExistOrganizationUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_DEPARTMENT_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}


	/* 将所有只存在于 tmp_organization 表 而在 security表中不存在的帐号，置为不可用状态
	 * 可能update行数为0，所以此处不判断操作返回值，直接返回 true 
	 */
	/*private boolean updateNotExistMajorUnavailable(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		logger.info(SQL_UPDATE_MAJOR_ONLY_UNAVAILABLE);
		
		stmt.executeUpdate(SQL_UPDATE_MAJOR_ONLY_UNAVAILABLE);
		stmt.close();
		return true;
	}*/
	
	/*
	 * 需要更新title
	 */
	private void updateExistOrganizationData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_UPDATE_DEPARTMENT);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_DEPARTMENT); 
		if (rs != null) {
			
			String unitno = "", unitname = "";
			while(rs.next()) {
				unitname = rs.getString("unitname");
				departmentPst.setString(1, unitname);
				
				unitno = rs.getString("unitno");
				departmentPst.setString(2, unitno);
				
				

				logger.info(departmentPst.toString() + ";" + unitname +","+unitno);
				departmentPst.addBatch();
			};
			departmentPst.executeBatch();
		}
		
		stmt.close();
		departmentPst.close();
	}
	
	/*private void updateExistMajorData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_UPDATE_MAJOR);
		//修改
		ResultSet rs = stmt.executeQuery(SQL_SELECT_EXIST_MAJOR); 
		if (rs != null) {
			
			String unitno = "", unitname = "";
			while(rs.next()) {		
				
				unitname = rs.getString("unitname");
				departmentPst.setString(1, unitname);
				
				unitno = rs.getString("unitno");
				departmentPst.setString(2, unitno);
				
				logger.info(departmentPst.toString() + ";" + unitname +","+unitno);
				departmentPst.addBatch();
			};
			departmentPst.executeBatch();
		}
		
		stmt.close();
		departmentPst.close();
	}*/
	
	/*
	 * 将只在 tmp_organization 中存在的数据 插入 edu_p 表
	 */
	private void insertNewOrganizationData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement departmentPst = conn.prepareStatement(SQL_INSERT_DEPARTMENT);
		
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_BY_DEPARTMENT); 
		if (rs != null) {
			String unitno = "", unitname = "";
			Integer parentId;
			while(rs.next()) {
				unitno = rs.getString("unitno");
				departmentPst.setString(1, unitno);
				
				unitname = rs.getString("unitname");
				departmentPst.setString(2, unitname);
				
				parentId = StringUtils.isBlank(rs.getString("parentId"))?1:Integer.valueOf(rs.getString("parentId"));
				departmentPst.setInt(3, parentId);

				logger.info(departmentPst.toString() + ";" + unitname +","+unitno + "," + parentId);
				
				departmentPst.addBatch();
			};

			departmentPst.executeBatch();
		}
		
		stmt.close();
		departmentPst.close();
	}
	
	/*private void insertNewMajorData(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		PreparedStatement majorPst = conn.prepareStatement(SQL_INSERT_MAJOR);
		ResultSet rs = stmt.executeQuery(SQL_SELECT_ONLY_EXIST_BY_MAJOR); 
		if (rs != null) {
			String unitno = "", unitname = "";
			int depid = 0;
			while(rs.next()) {
				unitno = rs.getString("unitno");
				majorPst.setString(1, unitno);
				
				unitname = rs.getString("unitname");
				majorPst.setString(2, unitname);
				
				depid = rs.getInt("depid");
				majorPst.setInt(3, depid);
				
				logger.info(majorPst.toString() + ";" + unitname +","+unitno+"," + depid);
				
				majorPst.addBatch();
			};

			majorPst.executeBatch();
		}
		
		stmt.close();
		majorPst.close();
	}*/
}