package com.shtd.datasyncer.worker.qcjx;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.datagetter.qcjx.DataGetWorker;
import com.shtd.datasyncer.dbsaver.qcjx.TeacherDataSaver;
import com.shtd.datasyncer.fileparser.qcjx.TeacherFileParser;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * 
 * @author zhanggn
 *
 */
public class Worker implements IWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public void work() {
		
		/* server端会返回     "未查询到数据！"
		 * 教师数据，有的 当前状态码 或者 性别编码 为空
		 * 组织结构数据，隶属单位号 有可能为空
		 * 
		 * 三个字典表暂不获取  组织结构/性别/教师当前状态
		 */
		
		logger.info("qcjx worker 工作开始，从webservice获取数据");
		DataGetWorker getWorker = new DataGetWorker();
		getWorker.pullData();
		logger.info("数据获取工作已完成");

		
		if ((Constant.INT_TRUE+"").equals(ConfigReader.getInstance().getValue("GET_DATA_ONLY"))) {
			logger.info("当前配置为只获取数据并存储到本地，qcjx worker 工作结束。");
			return;
		}
		
		
		// 只处理教师数据
		logger.info("解析教师数据");
		TeacherFileParser parser = new TeacherFileParser(getWorker.getTeacherFilePath());
		if (!parser.doParse()) {
			// 解析失败
			logger.info("教师数据解析失败");
		}
		

		
		logger.info("教师数据解析完成，将教师数据更新到数据库");
		TeacherDataSaver saver = new TeacherDataSaver(parser.getTeacherList());
		if (!saver.doSave()) {
			// 保存失败
			logger.info("教师数据更新到数据库失败");
			return;
		}
		

		logger.info("qcjx worker 工作结束");
	}
}
