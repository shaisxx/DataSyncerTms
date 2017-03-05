package com.shtd.datasyncer.domain.qcjx;

public class AppraisalTeacher {

	public static final String ELEMENT_NAME_TEACHER = "JZGJCSJXX";  // 教师数据
	public static final String ELEMENT_NAME_NAME         = "XM";    // 姓名
	public static final String ELEMENT_NAME_SHENFENZHENG = "SFZJH"; // 身份证号
	public static final String ELEMENT_NAME_STATUS_CODE  = "DQZTM"; // 当前状态码
	public static final String ELEMENT_NAME_GONGHAO      = "JGH";   // 教工号
	public static final String ELEMENT_NAME_GENDER       = "XBM";   // 性别码
	public static final String ELEMENT_NAME_DANWEI       = "DWH";   // 单位号
	
	
	public static final String GENDER_MALE   = "1"; // 性别 男
	public static final String GENDER_FEMALE = "2"; // 性别 女
	
	public static final String STATUS_NORMAL = "11"; // 教师帐号 正常
	
	
	public static final String DB_STATUS_NORMAL      = "1";
	public static final String DB_STATUS_UNAVAILABLE = "0";
	
	//微课数据表,0代表女,1代表男,2 未知
	public static final String DB_GENDER_MALE      = "1";
	public static final String DB_GENDER_FEMALE    = "0";
	public static final String DB_GENDER_UNDEFINED = "2";
	
	
	private String mName;
	private String mGender;
	private String mGongHao;
	private String mSenFenZheng;
	private String mStatus;
	private String mDanWei;
	
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public String getGender() {
		return mGender;
	}
	public void setGender(String gender) {
		this.mGender = gender;
	}
	public String getGongHao() {
		return mGongHao;
	}
	public void setGongHao(String gongHao) {
		this.mGongHao = gongHao;
	}
	public String getSenFenZheng() {
		return mSenFenZheng;
	}
	public void setSenFenZheng(String senFenZheng) {
		this.mSenFenZheng = senFenZheng;
	}
	public String getStatus() {
		return mStatus;
	}
	public void setStatus(String status) {
		this.mStatus = status;
	}
	public String getDanWei() {
		return mDanWei;
	}
	public void setDanWei(String danWei) {
		this.mDanWei = danWei;
	}
	
	public String toString() {
		return " 教师信息: \n"
		       + "\t姓名\t:" + getName()
		       + "\t身份证号\t:" + getSenFenZheng()
		       + "\t当前状态码\t:" + getStatus()
		       + "\t教工号\t:" + getGongHao()
		       + "\t单位号\t:" + getDanWei()
		       + "\t性别码\t:" + getGender()
		       + "\n---------------------------";
	}
	
	
	public String getDBStatus() {
		return STATUS_NORMAL.equals(this.mStatus) ? DB_STATUS_NORMAL : DB_STATUS_UNAVAILABLE;
	}
	
	public String getDBGender() {
		return (GENDER_MALE.equals(this.mGender) || GENDER_FEMALE.endsWith(this.mGender)) 
				? (GENDER_MALE.equals(this.mGender) ? DB_GENDER_MALE : DB_GENDER_FEMALE) 
				: DB_GENDER_UNDEFINED;
	}
	
	
	
}
