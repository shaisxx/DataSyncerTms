package com.shtd.datasyncer.datagetter.qcjx;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.Constant;


public class WhicoDataGetWorker {
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
	
	/*学生基本信息表*/
	private static final String STUDENT_DATA_USER_NAME     = "CBDC_XS";
	private static final String STUDENT_DATA_TABLE_NAME    = "BKS_XSJBSJXX";
	private static final String STUDENT_DATA_XML_FILENAME  = "student_data.xml";
	
	/*学生学籍表*/
	private static final String STUDENT_SCHOOL_ROLL_DATA_USER_NAME     = "CBDC_XS";
	private static final String STUDENT_SCHOOL_ROLL_DATA_TABLE_NAME    = "BKS_XJJBSJXX";
	private static final String STUDENT_SCHOOL_ROLL_DATA_XML_FILENAME  = "student_school_roll_data.xml";
	/*专业表*/
	private static final String DEPARTMENT_DATA_USER_NAME     = "CBDC_XX";
	private static final String DEPARTMENT_DATA_TABLE_NAME    = "YXSDWJBSJXX";
	private static final String DEPARTMENT_DATA_XML_FILENAME  = "departement_data.xml";
 
	
	private String mTeacherXmlFilePathName = "";
	private String mOrganizationXmlFilePathName = "";
	private String mGenderXmlFilePathName = "";
	private String mStatusXmlFilePathName = "";
	private String mClazzXmlFilePathName = "";
	//2015-10-28 by:jiangnan new add
	private String mStudentXmlFilePathName = "";
	private String mStudentSchoolRollXmlFilePathName = "";
	private String mDepartmentXmlFilePathName = "";
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
	
	public String getStudentXmlFilePathName() {
		return mStudentXmlFilePathName;
	}

	public String getStudentSchoolRollXmlFilePathName() {
		return mStudentSchoolRollXmlFilePathName;
	}

	public String getDepartmentXmlFilePathName() {
		return mDepartmentXmlFilePathName;
	}
	
	public void pullData() {
		mTeacherXmlFilePathName = getData("教师数据", new DataGetter(TEACHER_DATA_USER_NAME, TEACHER_DATA_TABLE_NAME, TEACHER_DATA_XML_FILENAME));
		
		mOrganizationXmlFilePathName = getData("组织数据", new DataGetter(ORGANIZATION_DATA_USER_NAME, ORGANIZATION_DATA_TABLE_NAME, ORGANIZATION_DATA_XML_FILENAME));
	
		mGenderXmlFilePathName = getData("性别字典数据", new DataGetter(GENDER_DATA_USER_NAME, GENDER_DATA_TABLE_NAME, GENDER_DATA_XML_FILENAME));

		mStatusXmlFilePathName = getData("教师状态字典数据", new DataGetter(USER_STATUS_DATA_USER_NAME, USER_STATUS_DATA_TABLE_NAME, USER_STATUS_DATA_XML_FILENAME));

		mClazzXmlFilePathName = getData("班级数据", new DataGetter(CLAZZ_DATA_USER_NAME, CLAZZ_DATA_TABLE_NAME, CLAZZ_DATA_XML_FILENAME));
		
		mStudentXmlFilePathName = getData("学生数据", new DataGetter(STUDENT_DATA_USER_NAME, STUDENT_DATA_TABLE_NAME, STUDENT_DATA_XML_FILENAME));
		
		mStudentSchoolRollXmlFilePathName = getData("学生学籍数据", new DataGetter(STUDENT_SCHOOL_ROLL_DATA_USER_NAME, STUDENT_SCHOOL_ROLL_DATA_TABLE_NAME, STUDENT_SCHOOL_ROLL_DATA_XML_FILENAME));
		
		mDepartmentXmlFilePathName = getData("院系专业数据", new DataGetter(DEPARTMENT_DATA_USER_NAME, DEPARTMENT_DATA_TABLE_NAME, DEPARTMENT_DATA_XML_FILENAME));
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
