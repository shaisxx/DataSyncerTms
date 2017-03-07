package com.shtd.datasyncer.domain.tms;

import java.io.Serializable;

public class Employee implements Serializable {

	private static final long serialVersionUID = -2166024237082555238L;
	
	public static final int GENDER_MALE   = 1;
	public static final int GENDER_FEMALE = 0;

	public static final String GENDER_NAME_MALE   = "男";
	public static final String GENDER_NAME_FEMALE = "女";
	
	public static final int TEACH_FLAG_NO = 0;
	public static final int TEACH_FLAG_YES = 1;
	
	public static final String TEACH_FLAG_NAME_NO = "否";
	public static final String TEACH_FLAG_NAME_YES = "是";
	
	public static final int RETIRE_FLAG_NO = 0;
	public static final int RETIRE_FLAG_YES = 1;
	
	public static final String RETIRE_FLAG_NAME_NO = "否";
	public static final String RETIRE_FLAG_NAME_YES = "是";
	
	public static final int CERT_FLAG_NO = 0;
	public static final int CERT_FLAG_YES = 1;
	
	public static final String CERT_FLAG_NAME_NO = "否";
	public static final String CERT_FLAG_NAME_YES = "是";
	
	public static final int STATUS_NO = 0;
	public static final int STATUS_YES = 1;
	
	public static final String STATUS_NAME_NO = "不可用";
	public static final String STATUS_NAME_YES = "可用";	

	private Integer id;
	
	private Integer userId;

    private String userNo;

    private String username;

    private String email;

    private String mobile;
    
    private Integer gender;
    
    private String postTitle;
    
    private String department;
    
    private String postType;
    
    private String staffType;
    
    private String postLevel;
    
    private Integer teachFlag;
    
    private Integer retireFlag;
    
    private Integer certFlag;
    
    private Integer status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getPostType() {
		return postType;
	}

	public void setPostType(String postType) {
		this.postType = postType;
	}

	public String getStaffType() {
		return staffType;
	}

	public void setStaffType(String staffType) {
		this.staffType = staffType;
	}

	public String getPostLevel() {
		return postLevel;
	}

	public void setPostLevel(String postLevel) {
		this.postLevel = postLevel;
	}

	public Integer getTeachFlag() {
		return teachFlag;
	}

	public void setTeachFlag(Integer teachFlag) {
		this.teachFlag = teachFlag;
	}

	public Integer getRetireFlag() {
		return retireFlag;
	}

	public void setRetireFlag(Integer retireFlag) {
		this.retireFlag = retireFlag;
	}

	public Integer getCertFlag() {
		return certFlag;
	}

	public void setCertFlag(Integer certFlag) {
		this.certFlag = certFlag;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Employee [userNo=" + userNo + ", username=" + username
				+ ", email=" + email + ", mobile=" + mobile + ", gender="
				+ gender + ", postTitle=" + postTitle + ", department="
				+ department + ", postType=" + postType + ", staffType="
				+ staffType + ", postLevel=" + postLevel + ", teachFlag="
				+ teachFlag + ", retireFlag=" + retireFlag + ", certFlag="
				+ certFlag + ", status=" + status + "]";
	}
}