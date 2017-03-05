package com.shtd.datasyncer.domain.qcjx;

public class WhicoStudentSchoolRoll {
	
	public static final String ELEMENT_NAME_STUDENT_SCHOOL_ROLL 	 = "BKS_XJJBSJXX";   //学生学籍数据
	public static final String ELEMENT_NAME_NO 		 	 			 = "XH"; 			 //学号
	public static final String ELEMENT_NAME_DEPARTMENT               = "YXSH"; 			 //院系
	public static final String ELEMENT_NAME_MAJOR                    = "ZYM";            //专业
	public static final String ELEMENT_NAME_STUTAS                   = "XSDQZTM";        //学生当前状态，关联字典HB_XSDQZT
	public static final String ELEMENT_NAME_CLAZZ                    = "BH";             //班级，关联字典BJSJXX
	public static final String ELEMENT_NAME_ENROLL_DATE              = "RXSJ";           //入学时间
	public static final String ELEMENT_NAME_GRADE					 = "SZNJ";			 //所在年级
	
	public static final String STATUS_NORMAL_AT_SCHOOL = "01"; // 学生状态	在读
	public static final String STATUS_NORMAL_BACK_SCHOOL = "05"; //学生状态   复学
	
	public static final String DB_STATUS_NORMAL      = "1";
	public static final String DB_STATUS_UNAVAILABLE = "0";
	
	private String stuNo;		 //学号
	private String department;   //院系
	private String major;        //专业
	private String status;       //学生当前状态
	private String clazz;        //班级
	private String enrollDate;   //入学时间
	private String grade; 		 //所在年级
	
	public String getStuNo() {
		return stuNo;
	}
	public void setStuNo(String stuNo) {
		this.stuNo = stuNo;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public String getEnrollDate() {
		return enrollDate;
	}
	public void setEnrollDate(String enrollDate) {
		this.enrollDate = enrollDate;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	public String getDBStatus() {
		return (STATUS_NORMAL_AT_SCHOOL.equals(this.status) || STATUS_NORMAL_BACK_SCHOOL.equals(this.status)) ? 
				DB_STATUS_NORMAL : DB_STATUS_UNAVAILABLE;
	}
	
	
}
