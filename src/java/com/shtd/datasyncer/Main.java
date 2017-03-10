package com.shtd.datasyncer;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * 数据同步主函数
 * @author Josh
 */
public class Main {

	/**
	 * @author Josh
	 */
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public static void main(String[] args) {
		logger.info("同步程序启动");
		
		String workerClassName = "";
		try {
			workerClassName = ConfigReader.getInstance().getValue("WORKER_CLASS");
			IWorker worker = (IWorker) (Class.forName(workerClassName).newInstance());
			worker.work();
		} catch (Exception e) {
			logger.error("生成worker失败，worker class = " + workerClassName , e);
		}
		
		logger.info("同步程序结束");
	}
}