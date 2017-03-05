package com.shtd.datasyncer.worker.dky;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.datagetter.dky.DataGetWorker;
import com.shtd.datasyncer.dbsaver.dky.sizheng.DataSaveWorker;
import com.shtd.datasyncer.fileparser.dky.FileParseWorker;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * sizheng同步用户数据
 * @author RanWeizheng
 *
 */
public class SizhengWorker  implements IWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public void work() {
		
		// 数据获取
		logger.info("dky sizheng worker 工作开始，从webservice获取数据");
		DataGetWorker getWorker = new DataGetWorker();
		getWorker.pullData();
		
		logger.info("数据获取工作已完成");
		
		
		if ((Constant.INT_TRUE+"").equals(ConfigReader.getInstance().getValue("GET_DATA_ONLY"))) {
			logger.info("当前配置为只获取数据并存储到本地，dky sizheng worker 工作结束。");
			return;
		}

		
		// 数据解析
		logger.info("开始解析数据文件");
		FileParseWorker parseWorker = new FileParseWorker();
		parseWorker.setTeacherFilePath(getWorker.getTeacherFilePath());
//		parseWorker.setTeacherFilePath("D:/Workspaces/java_eclipse/DataSyncer/samp/dky/teacher_data_rwz.txt");
		parseWorker.setClazzFilePath(getWorker.getClazzFilePath());
//		parseWorker.setClazzFilePath("D:/Workspaces/java_eclipse/DataSyncer/samp/dky/clazz_data_rwz.txt");
		parseWorker.setStudentFilePath(getWorker.getStudentFilePath());
//		parseWorker.setStudentFilePath("D:/Workspaces/java_eclipse/DataSyncer/samp/dky/student_data_rwz.txt");

		parseWorker.doParse();
		logger.info("解析数据文件完毕");
		

		// 数据存储到db
		logger.info("开始将数据保存到数据库");
		DataSaveWorker saveWorker = new DataSaveWorker();
		saveWorker.setClazzList(parseWorker.getClazzList());
		saveWorker.setStudentList(parseWorker.getStudentList());
		saveWorker.setTeacherList(parseWorker.getTeacherList());
//		
		saveWorker.doSave();
		logger.info("将数据保存到数据库结束");
		
		logger.info("dky sizheng worker 工作结束");
	}
}
