package com.shtd.datasyncer.datagetter.tms;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;


public class DataGetWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private static final String TEACHER_DATA_FILENAME = "employee_data.txt";

	private String mEmployeeFilePathName = "";
	
	public String getEmployeeFilePath() {
		return mEmployeeFilePathName;
	}
	
	public void pullData() {
		mEmployeeFilePathName = getData("教职工数据", new DataGetter(TEACHER_DATA_FILENAME));
	}
	
	private String getData(String dataName, DataGetter getter) {
		String filePathName = "";
		if (getter.pullData()) {
			filePathName = getter.getFilePathName();
		}
		return filePathName;
	}
}