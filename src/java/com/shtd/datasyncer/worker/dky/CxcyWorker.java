package com.shtd.datasyncer.worker.dky;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.datagetter.dky.DataGetWorker;
import com.shtd.datasyncer.dbsaver.dky.cxcy.DataSaveWorker;
import com.shtd.datasyncer.fileparser.dky.FileParseWorker;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * 创新创业
 * @author zhanggn
 *
 */
public class CxcyWorker implements IWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public void work() {
		
		// 数据获取
		logger.info("dky cxcy worker 工作开始，从webservice获取数据");
		DataGetWorker getWorker = new DataGetWorker();
		getWorker.pullData();
		
		logger.info("数据获取工作已完成");
		
		
		if ((Constant.INT_TRUE+"").equals(ConfigReader.getInstance().getValue("GET_DATA_ONLY"))) {
			logger.info("当前配置为只获取数据并存储到本地，dky cxcy worker 工作结束。");
			return;
		}
		
		// 数据解析
		logger.info("开始解析数据文件");
//		FileParseWorker parseWorker = new FileParseWorker();
//		parseWorker.setTeacherFilePath(getWorker.getTeacherFilePath());
//		parseWorker.setClazzFilePath(getWorker.getClazzFilePath());
//		parseWorker.setStudentFilePath(getWorker.getStudentFilePath());
//
//		parseWorker.doParse();
		logger.info("解析数据文件完毕");
		

		// 数据存储到db
		logger.info("开始将数据保存到数据库");
//		DataSaveWorker saveWorker = new DataSaveWorker();
//		saveWorker.setClazzList(parseWorker.getClazzList());
//		saveWorker.setTeacherList(parseWorker.getTeacherList());
//		saveWorker.setStudentList(parseWorker.getStudentList());
//		
//		saveWorker.doSave();
		logger.info("将数据保存到数据库结束");
		
		logger.info("dky cxcy worker 工作结束");
	}

}
