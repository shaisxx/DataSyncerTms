package com.shtd.datasyncer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigReader {
	private static Properties PROP ;
	private Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private ConfigReader() {
		PROP = new Properties();
		try {
			FileInputStream fis =  new FileInputStream(new File(System.getProperty("user.dir") + "/config.properties"));
			PROP.load(fis);
		} catch (Exception e) {
			logger.error("读取配置文件 config.properties 失败：" + e);
		}
	}

	private static class ConfigReaderHolder {
		private static ConfigReader instance = new ConfigReader();
	}

	public static ConfigReader getInstance() {
		return ConfigReaderHolder.instance;
	}

	/**
	 * 没有配置,返回""
	 * @param key
	 * @return
	 */
	public String getValue(String key) {
		return PROP.getProperty(key, "");
	}
	
}
