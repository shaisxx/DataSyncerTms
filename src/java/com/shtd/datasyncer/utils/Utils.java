package com.shtd.datasyncer.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class Utils {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private static String SUBFOLDER = "";
	
	// 单次最多获取数据条数
	public static final int MAX_SINGLE_GET_COUNT = 2000;
	
	// 生成SQL文件备份路径 ./data/yyyyMMddHHmmss/
	public static String getBackFilePathName(String fileName) {
		return ConfigReader.getInstance().getValue("BACK_FOLDER") + getSubFolder() + fileName;
	}
	
	/**
     * 逐层创建文件夹
     * @param path 文件路径
     * @author Josh
     */
    public static boolean mkPathFolders(String filePath) {
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
    			logger.error("根据 " +filePath+ " 逐层创建目录失败" + e);
    			return false;
            }
        }
        
        return true;
    }
    
	public static String getSubFolder(){
		if (StringUtils.isBlank(SUBFOLDER)) {
			Calendar calender = Calendar.getInstance();
	        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");    
			 
			SUBFOLDER = format.format(calender.getTime()) + "/";
		}
		return SUBFOLDER;
	}
	
	public static String formatDateToString(Date date, String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(date);
	}
}