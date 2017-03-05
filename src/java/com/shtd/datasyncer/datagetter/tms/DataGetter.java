package com.shtd.datasyncer.datagetter.tms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.utils.db.MysqlDb;

public class DataGetter {

	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private static String SUBFOLDER = "";

	private static final String SELECT_DATA_SQL = "SELECT "
			+ "user_no, username, email, mobile, gender, post_title, "
			+ "department, post_type, staff_type, post_level, teach_flag, "
			+ "retire_flag, cert_flag, `status` " + "FROM sys_user_syncer ";

	// 返回数据 保存的txt文件名
	protected String mFileName;

	// txt的路径+文件名
	private String mFilePathName;

	public DataGetter() {
		initSubFolder();
	}

	/**
	 * @param fileName
	 *            保存的txt文件名
	 */
	public DataGetter(String fileName) {
		initSubFolder();
		this.mFileName = fileName;
	}

	public String getFilePathName() {
		return mFilePathName;
	}

	// 查询数据表获取数据 并保存在本地
	public boolean pullData() {
		try {
			logger.info("查询数据库表获取数据，数据库表名：sys_user_syncer");

			Integer count = 0;

			// 设置标记
			boolean flag = true;
			// 外层循环，每次加4
			for (int j = 0;; j += 4) {
				StringBuffer dataContentSb = new StringBuffer();
				for (int i = j; i < j + 4; i++) {
					int h = i * 500;
					Integer dataCount = getDataContent(dataContentSb, h, 500);
					count += dataCount;
					if (dataCount < 500) {
						flag = false;
					}
				}
				logger.info("获取数据成功。");

				mFilePathName = getFilePathName(mFileName);
				logger.info("获取数据完毕，将数据写入：" + mFilePathName);

				if (writeToFile(mFilePathName, dataContentSb.toString())) {
					logger.info("数据写入本地文件 " + mFilePathName + " 成功，数量：" + count);
					System.out.println("数据写入本地文件 " + mFilePathName + " 成功，数量：" + count);
				}

				if (!flag) {
					break;
				}
			}

		} catch (Exception e) {
			logger.error("查询数据库表获取数据失败：" + e);
			e.printStackTrace();
		}

		return false;
	}

	// 生成文件路径 ./data/yyyyMMddHHmmss/txtfile
	private String getFilePathName(String xmlFileName) {
		return ConfigReader.getInstance().getValue("DATA_FOLDER") + SUBFOLDER
				+ xmlFileName;
	}

	/**
	 * 逐层创建文件夹
	 * 
	 * @param path
	 *            文件路径
	 * 
	 */
	private boolean mkPathFolders(String filePath) {
		String paths[] = filePath.split("/");
		String dir = paths[0];
		for (int i = 0; i < paths.length - 2; i++) {
			try {
				dir = dir + "/" + paths[i + 1];
				File dirFile = new File(dir);
				if (!dirFile.exists()) {
					dirFile.mkdir();
				}
			} catch (Exception e) {
				logger.error("根据 " + filePath + " 逐层创建目录失败" + e);
				return false;
			}
		}

		return true;
	}

	private void initSubFolder() {
		if (StringUtils.isBlank(SUBFOLDER)) {
			Calendar calender = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

			SUBFOLDER = format.format(calender.getTime()) + "/";
		}
	}

	// 将数据写入xml文件
	private boolean writeToFile(String filePathName, String content) {
		if (StringUtils.isBlank(filePathName) || StringUtils.isBlank(content)) {
			logger.error("数据写入本地文件，传入文件路径：" + filePathName + ", 待写入内容："
					+ content + ". 写入失败");
			return false;
		}

		if (!mkPathFolders(filePathName)) {
			logger.error("创建文件目录失败！");
			return false;
		}

		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			File file = new File(filePathName);

			if (!file.exists()) {
				// 不存在则创建
				file.createNewFile();
			}

			fos = new FileOutputStream(file);
			osw = new OutputStreamWriter(fos, "UTF-8");
			osw.write(content);
			osw.close();

			return true;

		} catch (Exception e) {
			logger.error("写入文件失败：" + e);

		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (Exception e) {
					logger.error("关闭OutputStreamWriter失败：" + e);
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					logger.error("关闭FileOutputStream失败：" + e);
				}
			}
		}

		return false;
	}

	public Integer getDataContent(StringBuffer dataContentSb, int start,
			int end) {

		MysqlDb db = new MysqlDb();
		Connection dbConn = null;
		Integer dataCount = 0;
		try {
			db.initConn();
			dbConn = db.getConn();

			if (dbConn == null) {
				logger.error("未获取有效数据库连接，操作失败");
			}

			dbConn.setAutoCommit(false);

			Statement stm = dbConn.createStatement();
			ResultSet set = stm.executeQuery(
					SELECT_DATA_SQL + " limit " + start + "," + end);

			while (set.next()) {
				dataContentSb.append(set.getString(1)).append(",");
				dataContentSb.append(set.getString(2)).append(",");
				dataContentSb.append(set.getString(3)).append(",");
				dataContentSb.append(set.getString(4)).append(",");
				dataContentSb.append(set.getString(5)).append(",");
				dataContentSb.append(set.getString(6)).append(",");
				dataContentSb.append(set.getString(7)).append(",");
				dataContentSb.append(set.getString(8)).append(",");
				dataContentSb.append(set.getString(9)).append(",");
				dataContentSb.append(set.getString(10)).append(",");
				dataContentSb.append(set.getString(11)).append(",");
				dataContentSb.append(set.getString(12)).append(",");
				dataContentSb.append(set.getString(13)).append(",");
				dataContentSb.append(set.getString(14)).append(", \n");
				dataCount++;
			}

			dbConn.commit();// 事务提交
			dbConn.close();
		} catch (SQLException e) {
			try {
				dbConn.rollback();
				logger.error("操作失败，数据库回滚");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();

		}
		return dataCount;
	}
}