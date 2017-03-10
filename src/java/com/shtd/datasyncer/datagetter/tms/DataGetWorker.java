package com.shtd.datasyncer.datagetter.tms;

public class DataGetWorker {

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