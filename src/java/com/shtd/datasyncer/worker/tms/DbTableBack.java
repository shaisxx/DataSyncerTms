package com.shtd.datasyncer.worker.tms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.Utils;

public class DbTableBack {

	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public static Map<String, String> backUpTableList = new ConcurrentHashMap<String, String>();
	private static DbTableBack backObj = new DbTableBack();

	public static DbTableBack getDbTableBackUp() {
		return backObj;
	}

	/**
	 * 数据库表备份
	 * @author Josh
	 */
	public void backup() {
		String syncerTableNames = ConfigReader.getInstance().getValue("SYNCER_DB_BACKUP_TABLE_NAMES");
		if(StringUtils.isNotBlank(syncerTableNames)){
			String username = ConfigReader.getInstance().getValue("SYNCER_DB_USER");
			String password = ConfigReader.getInstance().getValue("SYNCER_DB_PASSWD");
			String mysqlpaths = ConfigReader.getInstance().getValue("SYNCER_DB_PATH");
			String address = ConfigReader.getInstance().getValue("SYNCER_DB_IP");
			String databaseName = ConfigReader.getInstance().getValue("SYNCER_DB_NAME");
			backUpTables(syncerTableNames, username, password, mysqlpaths, address, databaseName);
		}
		
		String dbTableNames = ConfigReader.getInstance().getValue("MYSQL_DB_BACKUP_TABLE_NAMES");
		if(StringUtils.isNotBlank(dbTableNames)){
			String username = ConfigReader.getInstance().getValue("MYSQL_DB_USER");
			String password = ConfigReader.getInstance().getValue("MYSQL_DB_PASSWD");
			String mysqlpaths = ConfigReader.getInstance().getValue("MYSQL_DB_PATH");
			String address = ConfigReader.getInstance().getValue("MYSQL_DB_IP");
			String databaseName = ConfigReader.getInstance().getValue("MYSQL_DB_NAME");
			backUpTables(dbTableNames, username, password, mysqlpaths, address, databaseName);
		}
	}
	
	/**
	 * 执行数据库表备份
	 * @param backupTableNames
	 * @param dbUser
	 * @param dbPassword
	 * @param dbPaths
	 * @param dbAddress
	 * @param dbName
	 * @author Josh
	 */
	private void backUpTables(String backupTableNames,String dbUser,String dbPassword,
			String dbPaths,String dbAddress,String dbName){
		
		String[] tableArr = backupTableNames.split(",");
		for(String tableName:tableArr){
			if (null != backUpTableList.get(tableName))return;
			logger.info("开始备份 " + tableName);
			try {
				
				String dbSqlPathName = Utils.getBackFilePathName(tableName);
				
				if(!Utils.mkPathFolders(dbSqlPathName)){
					logger.error("创建文件目录失败！");
					return;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(dbPaths);
				sb.append("mysqldump ");
				sb.append("--opt ");
				sb.append("-h ");
				sb.append(dbAddress);
				sb.append(" ");
				sb.append("--user=");
				sb.append(dbUser);
				sb.append(" ");
				sb.append("--password=");
				sb.append(dbPassword);
				sb.append(" ");
				sb.append("--lock-all-tables=true ");
				sb.append("--result-file=");
				sb.append(dbSqlPathName + ".sql");
				sb.append(" ");
				sb.append("--default-character-set=utf8 ");
				sb.append(dbName);
				sb.append(" "); 
				sb.append(tableName);
				Runtime cmd = Runtime.getRuntime();
				Process p = cmd.exec(sb.toString());
				p.waitFor(); // 该语句用于标记，如果备份没有完成，则该线程持续等待
				logger.info(tableName + " 备份完毕，将数据写入：" + dbSqlPathName + ".sql");
			} catch (Exception e) {
				logger.error("备份操作出现问题", e);
			} finally {
				backUpTableList.remove(tableName); // 最终都将解除
			}
		}
	}
}