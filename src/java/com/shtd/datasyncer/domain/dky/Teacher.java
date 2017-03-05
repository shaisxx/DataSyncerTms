package com.shtd.datasyncer.domain.dky;

public class Teacher {

	public static final int GENDER_MALE   = 1;
	public static final int GENDER_FEMALE = 2;

	public static final String GENDER_NAME_MALE   = "男";
	public static final String GENDER_NAME_FEMALE = "女";
	
	// 职工号
	private String employeeNo;
	
	// 姓名
	private String name;
	
	// 性别
	private int gender;

	public String getEmployeeNo() {
		return employeeNo;
	}

	public void setEmployeeNo(String employeeNo) {
		this.employeeNo = employeeNo;
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
}
