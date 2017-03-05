package com.shtd.datasyncer.domain.qcjx;

public class WhicoDepartment {
	/**
	 *  DWH '单位号';
		DWMC  '单位名称';
		DWYWMC  '单位英文名称';
		DWJC '单位简称';
		DWYWJC '单位英文简称';
		DWJP  '单位简拼';
		DWDZ  '单位地址';
		LSDWH '隶属单位号';
		DWLBM '单位类别码';
		DWYXBS '单位有效标识';
		SXRQ '失效日期';
		STBS '实体标识';
		DWBBM '单位办别码';
		JLNY  '建立年月';
		DWFZRH '单位负责人号';
	 */
	public static final String ELEMENT_NAME_DEPARTMENT	 	= "YXSDWJBSJXX";  	// 院系/专业
	public static final String ELEMENT_NAME_NO         		= "DWH";    		// 单位号
	public static final String ELEMENT_NAME_NAME			= "DWMC"; 			// 单位名称
	public static final String ELEMENT_NAME_PARAENT_ID  	= "LSDWH";   		// 隶属单位号
	public static final String ELEMENT_NAME_VALIDATE	  	= "SXRQ"; 			// 失效日期
	public static final String ELEMENT_NAME_UNIT_TYPE	  	= "DWLBM"; 			// 单位类别码
	
	// 院系、专业ID
	private String unitNo;
	
	// 名称
	private String name;
	
	// 隶属单位号
	private String parentNo;
	
	//失效时间
	private String validate;
	
	//单位类别
	private String unitType;
	
	public String getUnitNo() {
		return unitNo;
	}

	public void setUnitNo(String unitNo) {
		this.unitNo = unitNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentNo() {
		return parentNo;
	}

	public void setParentNo(String parentNo) {
		this.parentNo = parentNo;
	}

	public String getValidate() {
		return validate;
	}

	public void setValidate(String validate) {
		this.validate = validate;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}
	 

	 
}
