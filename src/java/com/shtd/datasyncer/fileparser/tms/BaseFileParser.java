package com.shtd.datasyncer.fileparser.tms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;

public class BaseFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private static final String PARSED_FILE_EXT_NAME = ".parsed";
	
	public static final String SEPARATOR = ","; // 数据的分隔符 
	
	private String mFilePathName = "";
	
	private String mDataName = "基本数据";

	public BaseFileParser(String filePathName) {
		mFilePathName = filePathName;
	}
	
	protected void setDataName(String dataName) {
		this.mDataName = dataName;
	}

	@SuppressWarnings("resource")
	public boolean doParse() {
		logger.info("开始解析"+mDataName+"数据。");
		
		if (StringUtils.isBlank(mFilePathName)) {
			logger.info("文件名：" + mFilePathName + " 为空，处理失败。");
			return false;
		}
		
		File dataFile = new File(mFilePathName);
		if (dataFile == null || !dataFile.exists() || !dataFile.isFile() || !dataFile.canRead()) {
			logger.info("文件异常，请检查：" + mFilePathName + " 文件。");
			return false;
		}

		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(dataFile), "utf-8");
			BufferedReader bufferedReader = new BufferedReader(reader);
			String lineTxt = null;
			
			// 第一行是 column name 行， 跳过
			lineTxt = bufferedReader.readLine();
			if (lineTxt == null) {
				return true;
			}
			
			while((lineTxt = bufferedReader.readLine()) != null) {
				if (!parseLine(lineTxt)) {
					return false;
				}
			}
			reader.close();
			
			logger.info("共读取 " + getDataCount() + " 条 " + mDataName + "数据");
	        
	        // 文件处理结束，将文件改名
	        try {
	        	FileUtils.moveFile(dataFile, new File(mFilePathName+PARSED_FILE_EXT_NAME));
	        } catch (Exception e) {
				logger.error("数据文件解析完毕,改名失败：" + e);
			}
	        
	        return true;
		} catch (Exception e) {
			logger.error("数据文件解析失败：" + e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("reader.close()失败：" + e);
				}
			}
		}
		
		return false;
	}
	
	protected boolean parseLine(String data) {
		System.out.println(data);
		return true;
	}
	
	protected int getDataCount() {
		return 0;
	}
	
	/**
	 * 通过 String.split 分隔字符串时，若字符串尾出现连续的空内容，只有分隔符的情况，
	 * 则字符串尾那些数据字段不会保存在list中
	 * @param itemList  字符串 通过 String.split 分割成都 list
	 * @param index 需要获取的元素在第几项
	 * @return
	 * @author zhanggn
	 */
	protected String getItemByIndex(List<String> itemList, int index) {
		if (itemList == null || itemList.size() <= index) {
			return "";
		}
		return itemList.get(index);
		
	}
}
