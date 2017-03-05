package com.shtd.datasyncer.datagetter.dky;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;


public class DataGetWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private static final String DATA_USERNAME = "twcytb";
	private static final String DATA_PASSWORD = "twcytb20140607";
	
	// 学生
	private static final String STUDENT_DATA_SVRID    = "twcy_xs";
	private static final String STUDENT_DATA_FILENAME = "student_data.txt";

	// 教师
	private static final String TEACHER_DATA_SVRID    = "twcy_rs";
	private static final String TEACHER_DATA_FILENAME = "teacher_data.txt";

	// 班级
	private static final String CLAZZ_DATA_SVRID      = "twcy_bj";
	private static final String CLAZZ_DATA_FILENAME   = "clazz_data.txt";

	private String mTeacherFilePathName = "";
	private String mStudentFilePathName = "";
	private String mClazzFilePathName   = "";
	
	public String getTeacherFilePath() {
		return mTeacherFilePathName;
	}

	public String getStudentFilePath() {
		return mStudentFilePathName;
	}

	public String getClazzFilePath() {
		return mClazzFilePathName;
	}
	
	public void pullData() {
		mTeacherFilePathName = getData("教师数据", new DataGetter(DATA_USERNAME, DATA_PASSWORD, TEACHER_DATA_SVRID, TEACHER_DATA_FILENAME));
		
		mClazzFilePathName = getData("班级数据", new DataGetter(DATA_USERNAME, DATA_PASSWORD, CLAZZ_DATA_SVRID, CLAZZ_DATA_FILENAME));
	
		mStudentFilePathName = getData("学生数据", new DataGetter(DATA_USERNAME, DATA_PASSWORD, STUDENT_DATA_SVRID, STUDENT_DATA_FILENAME));
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
