package com.shtd.datasyncer.worker.tms;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.datagetter.tms.DataGetWorker;
import com.shtd.datasyncer.dbsaver.tms.EmployeeDataSaver;
import com.shtd.datasyncer.fileparser.tms.FileParseWorker;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * 教务系统  同步用户数据
 * @author Josh
 */
public class TmsWorker implements IWorker{
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	@Override
	public void work() {
		
		// tms worker 工作开始
		logger.info("tms worker 工作开始");
		
		logger.info("开始备份数据库表");
		DbTableBack.getDbTableBackUp().backup();
		logger.info("备份数据库表完毕");
		
		// 数据获取
		logger.info("开始从数据库获取数据");
		DataGetWorker getWorker = new DataGetWorker();
		getWorker.pullData();
		logger.info("获取数据工作完毕");
		
		if ((Constant.INT_TRUE+"").equals(ConfigReader.getInstance().getValue("GET_DATA_ONLY"))) {
			logger.info("当前配置为只获取数据并存储到本地，tms worker 工作结束。");
			return;
		}

		// 数据解析
		logger.info("开始解析数据文件");
		FileParseWorker parseWorker = new FileParseWorker();
		parseWorker.setEmployeeFilePath(getWorker.getEmployeeFilePath());
		parseWorker.doParse();
		logger.info("解析数据文件完毕");
		
		// 数据存储到db
		logger.info("开始将数据保存到数据库");
		EmployeeDataSaver saveWorker = new EmployeeDataSaver(parseWorker.getEmployeeList());
		saveWorker.doSave();
		logger.info("将数据保存到数据库完毕");
		
		logger.info("tms worker 工作结束");
	}
}