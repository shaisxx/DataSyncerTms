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

	public void backup() {
		String tableNames = ConfigReader.getInstance().getValue("tableNames");
		if(StringUtils.isNotBlank(tableNames)){
			String[] tableArr = tableNames.split(",");
			for(String tableName:tableArr){
				if (null != backUpTableList.get(tableName))return;
				backUpTableList.put(tableName, tableName); // 标记已经用于备份
				new Thread(new DbBackUpThread(tableName)).start();
			}
		}
	}
	
	/**
	 * 用于执行某表的备份
	 */
	class DbBackUpThread implements Runnable {
		String tableName = null;

		public DbBackUpThread(String tableName) {
			this.tableName = tableName;
		}

		@Override
		public void run() {
			try {
				logger.info("开始备份 " + tableName);
				System.out.println("开始备份 " + tableName);

				String username = ConfigReader.getInstance().getValue("username");
				String password = ConfigReader.getInstance().getValue("password");
				String mysqlpaths = ConfigReader.getInstance().getValue("mysqlpath");
				String address = ConfigReader.getInstance().getValue("dbAddress");
				String databaseName = ConfigReader.getInstance().getValue("databaseName");
				String sqlpathName = Utils.getBackFilePathName(tableName);
				
				if(!Utils.mkPathFolders(sqlpathName)){
					logger.error("创建文件目录失败！");
					return;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(mysqlpaths);
				sb.append("mysqldump ");
				sb.append("--opt ");
				sb.append("-h ");
				sb.append(address);
				sb.append(" ");
				sb.append("--user=");
				sb.append(username);
				sb.append(" ");
				sb.append("--password=");
				sb.append(password);
				sb.append(" ");
				sb.append("--lock-all-tables=true ");
				sb.append("--result-file=");
				sb.append(sqlpathName + ".sql");
				sb.append(" ");
				sb.append("--default-character-set=utf8 ");
				sb.append(databaseName);
				sb.append(" "); 
				sb.append(tableName);
				Runtime cmd = Runtime.getRuntime();
				Process p = cmd.exec(sb.toString());
				p.waitFor(); // 该语句用于标记，如果备份没有完成，则该线程持续等待
				logger.info(tableName + " 备份完毕，将数据写入：" + sqlpathName + ".sql");
				System.out.println(tableName + " 备份完毕，将数据写入：" + sqlpathName + ".sql");
			} catch (Exception e) {
				logger.error("备份操作出现问题", e);
			} finally {
				backUpTableList.remove(tableName); // 最终都将解除
			}
		}
	}
}