package com.shtd.datasyncer.domain.qcjx;

public class WhicoClazz {

	public static final String ELEMENT_NAME_CLAZZ	 	 	= "BJSJXX";  	// 学生数据
	public static final String ELEMENT_NAME_NO         		= "BH";    		// 班号
	public static final String ELEMENT_NAME_NAME			= "BJ"; 		// 班级
	public static final String ELEMENT_NAME_CREATE_DATE  	= "JBNY";   	// 建班年月
	public static final String ELEMENT_NAME_TEACHER_NO  	= "BZRJGH"; 	// 班主任教工号
	public static final String ELEMENT_NAME_LEADER_NO    	= "BZXH";   	// 班长学号
	public static final String ELEMENT_NAME_INSTRUCTOR_NO	= "BZXH";   	// 辅导员号
	public static final String ELEMENT_NAME_DEP_NO       	= "DWH";   		// 专业号
	// 班号
	private String clazzNo;
	
	// 班级名称
	private String clazzName;
	
	// 专业号
	private String departmentNo;
	
	// 年级
	private String grade;


	public String getClazzNo() {
		return clazzNo;
	}

	public void setClazzNo(String clazzNo) {
		this.clazzNo = clazzNo;
	}

	public String getClazzName() {
		return clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}

	public String getDepartmentNo() {
		return departmentNo;
	}

	public void setDepartmentNo(String departmentNo) {
		this.departmentNo = departmentNo;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	 
}
