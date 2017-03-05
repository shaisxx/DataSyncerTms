package com.shtd.datasyncer.domain.dky;

public class Student {
	public static final int STATUS_ON       = 1; // 在籍
	public static final int STATUS_NONE     = 2; // 无
	public static final int STATUS_UNCREATE = 3; // 学籍未建立
	
	public static final String STATUS_NAME_ON       = "在籍";
	public static final String STATUS_NAME_NONE     = "无";
	public static final String STATUS_NAME_UNCREATE = "学籍未建立";
	
	
	public static final int GENDER_MALE   = 1;
	public static final int GENDER_FEMALE = 2;

	public static final String GENDER_NAME_MALE   = "男";
	public static final String GENDER_NAME_FEMALE = "女";
	
	// 学号
	private String studentNo;
	
	// 姓名
	private String name;
	
	// 性别
	private int gender;
	
	// 出生日期
	private String birthday;
	
	// 编号
	private String serialNo;
	
	// 班级名称
	private String clazzName;
	
	// 专业名称
	private String majorName;
	
	// 学院名称
	private String collegeName;
	
	// email addr
	private String emailAddr;
	
	// 毕业年份
	private String graduateYear;
	
	// 学籍状态 帐号状态
	private int status;

	public String getStudentNo() {
		return studentNo;
	}

	public void setStudentNo(String studentNo) {
		this.studentNo = studentNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getClazzName() {
		return clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}

	public String getMajorName() {
		return majorName;
	}

	public void setMajorName(String majorName) {
		this.majorName = majorName;
	}

	public String getCollegeName() {
		return collegeName;
	}

	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getGraduateYear() {
		return graduateYear;
	}

	public void setGraduateYear(String graduateYear) {
		this.graduateYear = graduateYear;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
