package com.shtd.datasyncer.datagetter.tms;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;


public class DataGetWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private static final String TEACHER_DATA_FILENAME = "teacher_data.txt";

	private String mTeacherFilePathName = "";
	
	public String getTeacherFilePath() {
		return mTeacherFilePathName;
	}
	
	public void pullData() {
		mTeacherFilePathName = getData("教师数据", new DataGetter(TEACHER_DATA_FILENAME));
	}
	
	private String getData(String dataName, DataGetter getter) {
		String filePathName = "";
		if (getter.pullData()) {
			filePathName = getter.getFilePathName();
			logger.info(dataName + "获取成功，保存在 ：" + filePathName);
		}
		return filePathName;
	}
}