package com.shtd.datasyncer.domain.qcjx;

public class AppraisalStudent {

	public static final String ELEMENT_NAME_STUDENT 	 = "BKS_XSJBSJXX";   //学生数据
	public static final String ELEMENT_NAME_NO 		 	 = "XH"; 			 //学号
	public static final String ELEMENT_NAME_NAME 		 = "XM"; 			 //姓名
	public static final String ELEMENT_NAME_GENDER       = "XBM";   		 //性别码
	public static final String ELEMENT_NAME_SHENFENZHENG = "SFZJH"; 		 //身份证号
	
	public static final String GENDER_MALE   = "1"; // 性别 男
	public static final String GENDER_FEMALE = "2"; // 性别 女	
	
	//微课数据表,0代表女,1代表男,2代表性别未知
	public static final String DB_GENDER_MALE      = "1";
	public static final String DB_GENDER_FEMALE    = "0";
	public static final String DB_GENDER_UNDEFINED = "2";
	
	private String stuName;//学生姓名
	private String stuNo;  //学号
	private String stuGender;//性别
	private String stuSenFenZheng; //身份证
	
	
	public String getStuName() {
		return stuName;
	}
	public void setStuName(String stuName) {
		this.stuName = stuName;
	}
	public String getStuNo() {
		return stuNo;
	}
	public void setStuNo(String stuNo) {
		this.stuNo = stuNo;
	}
	public String getStuGender() {
		return stuGender;
	}
	public void setStuGender(String stuGender) {
		this.stuGender = stuGender;
	}
	public String getStuSenFenZheng() {
		return stuSenFenZheng;
	}
	public void setStuSenFenZheng(String stuSenFenZheng) {
		this.stuSenFenZheng = stuSenFenZheng;
	}
	
	
	public String getDBGender() {
		return (GENDER_MALE.equals(this.stuGender) || GENDER_FEMALE.endsWith(this.stuGender)) 
				? (GENDER_MALE.equals(this.stuGender) ? DB_GENDER_MALE : DB_GENDER_FEMALE) 
				: DB_GENDER_UNDEFINED;
	}
}
