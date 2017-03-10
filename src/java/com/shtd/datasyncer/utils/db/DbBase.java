package com.shtd.datasyncer.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;

public abstract class DbBase {
	private static Logger logger    = Logger.getLogger(Constant.LOGGER_NAME);

	protected String DB_DRIVER = null;
	protected String USER      = null;
	protected String PASSWD    = null;
	protected String DB_LINK   = null;
	
	private Connection conn = null;
	private Statement  stmt = null;      
	private ResultSet  rs   = null;

	
	public Connection getConn() {
		return conn;
	}
	
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	
	public Statement getStmt() {
		return stmt;
	}

	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}

	public ResultSet getRs() {
		return rs;
	}

	public void setRs(ResultSet rs) {
		this.rs = rs;
	}

	/**
	 * 封装数据库底层操作
	 * @throws SQLException
	 * @author Josh
	 */
	public void initConn() throws SQLException {
		Connection conn = null;
		try {   
			logger.info("尝试获取DB驱动");
			Class.forName(DB_DRIVER);  
			logger.info("获取DB驱动成功");
		} catch (ClassNotFoundException e) {
			throw new SQLException("获取DB驱动失败", e);
		}
		
		try {
			conn = DriverManager.getConnection(DB_LINK, USER, PASSWD);
		} catch (SQLException e) {
			throw new SQLException("获取DB连接（connection）失败", e);
		}
		
		this.setConn(conn);
	}
	
	public void closeAll(Connection conn, Statement stmt, ResultSet rs) {
		
		if (rs != null) {
			try {
				rs.close();
            } catch (SQLException e) {
            	logger.error("关闭rs时出现异常");
            }
		}
		
		if (stmt != null) {
			try {
				stmt.close();
            } catch (SQLException e) {
            	logger.error("关闭stmt时出现异常");
            }
		}
		
		if (conn != null) {
			try {
				conn.close();
            } catch (SQLException e) {
            	logger.error("关闭conn时出现异常");
            }
		}
	}
	
	
	public void closeAll() {
		closeAll(this.conn, this.stmt, this.rs);
	}
}
