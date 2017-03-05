package com.shtd.datasyncer.datagetter.qcjx;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;


public class DataGetWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private static final String CLAZZ_DATA_USER_NAME           = "CBDC_XX";
	private static final String CLAZZ_DATA_TABLE_NAME          = "BJSJXX";
	private static final String CLAZZ_DATA_XML_FILENAME        = "clazz_data.xml";

	private static final String GENDER_DATA_USER_NAME          = "CBDC_DM";
	private static final String GENDER_DATA_TABLE_NAME         = "GB_XBDM";
	private static final String GENDER_DATA_XML_FILENAME       = "gender_data.xml";

	private static final String ORGANIZATION_DATA_USER_NAME    = "CBDC_XX";
	private static final String ORGANIZATION_DATA_TABLE_NAME   = "YXSDWJBSJXX";
	private static final String ORGANIZATION_DATA_XML_FILENAME = "organization_data.xml";

	private static final String TEACHER_DATA_USER_NAME         = "CBDC_JG";
	private static final String TEACHER_DATA_TABLE_NAME        = "JZGJCSJXX";
	private static final String TEACHER_DATA_XML_FILENAME      = "teacher_data.xml";

	private static final String USER_STATUS_DATA_USER_NAME     = "CBDC_DM";
	private static final String USER_STATUS_DATA_TABLE_NAME    = "HB_JZGDQZT";
	private static final String USER_STATUS_DATA_XML_FILENAME  = "user_status_data.xml";

	private String mTeacherXmlFilePathName = "";
	private String mOrganizationXmlFilePathName = "";
	private String mGenderXmlFilePathName = "";
	private String mStatusXmlFilePathName = "";
	private String mClazzXmlFilePathName = "";
	
	public String getTeacherFilePath() {
		return mTeacherXmlFilePathName;
	}

	public String getOrganizationFilePath() {
		return mOrganizationXmlFilePathName;
	}

	public String getGenderFilePath() {
		return mGenderXmlFilePathName;
	}

	public String getStatusFilePath() {
		return mStatusXmlFilePathName;
	}

	public String getClazzFilePath() {
		return mClazzXmlFilePathName;
	}
	
	public void pullData() {
		mTeacherXmlFilePathName = getData("教师数据", new DataGetter(TEACHER_DATA_USER_NAME, TEACHER_DATA_TABLE_NAME, TEACHER_DATA_XML_FILENAME));
		
		mOrganizationXmlFilePathName = getData("组织数据", new DataGetter(ORGANIZATION_DATA_USER_NAME, ORGANIZATION_DATA_TABLE_NAME, ORGANIZATION_DATA_XML_FILENAME));
	
		mGenderXmlFilePathName = getData("性别字典数据", new DataGetter(GENDER_DATA_USER_NAME, GENDER_DATA_TABLE_NAME, GENDER_DATA_XML_FILENAME));

		mStatusXmlFilePathName = getData("教师状态字典数据", new DataGetter(USER_STATUS_DATA_USER_NAME, USER_STATUS_DATA_TABLE_NAME, USER_STATUS_DATA_XML_FILENAME));

		mClazzXmlFilePathName = getData("班级数据", new DataGetter(CLAZZ_DATA_USER_NAME, CLAZZ_DATA_TABLE_NAME, CLAZZ_DATA_XML_FILENAME));
	}
	
	private String getData(String dataName, DataGetter getter) {
		String filePathName = "";
		if (getter.pullData()) {
			filePathName = getter.getXmlFilePathName();
			logger.info(dataName + "获取成功，保存在 ：" + filePathName);
		}
		
		return filePathName;
	}
}
