package com.shtd.datasyncer.fileparser.tms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.tms.Employee;
import com.shtd.datasyncer.utils.Constant;

public class EmployeeFileParser extends BaseFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private List<Employee> mEmployeeList = new ArrayList<Employee>();
	
	public EmployeeFileParser(String filePathName) {
		super(filePathName);
		
		setDataName("教职工");
	}
	
	public List<Employee> getEmployeeList() {
		return mEmployeeList;
	}
	
	protected int getDataCount() {
		return mEmployeeList.size();
	}

	/**
	 * 解析txt每行数据
	 * @author Josh
	 */
	protected boolean parseLine(String data) {
		if (StringUtils.isBlank(data)) {
			logger.info("本行数据为空，读取文件结束，退出读取。");
			return false;
		}
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(data.split(SEPARATOR)));
		
		Employee employee = new Employee();
		
		// 第一项 教师员工号
		employee.setUserNo(getItemByIndex(list, 0));

		// 第二项 教师姓名
		employee.setUsername(getItemByIndex(list, 1));
		
		// 第三项 邮箱
		employee.setEmail(getItemByIndex(list, 2));
		
		// 第四项 手机号码
		employee.setMobile(getItemByIndex(list, 3));
		
		// 第五项 教师性别
		employee.setGender(Employee.GENDER_NAME_FEMALE.equals(getItemByIndex(list, 4))?Employee.GENDER_FEMALE:Employee.GENDER_MALE);
				
		// 第六项 职务名称
		employee.setPostTitle(getItemByIndex(list, 5));
		
		// 第七项 部门
		employee.setDepartment(getItemByIndex(list, 6));
		
		// 第八项 岗位分类
		employee.setPostType(getItemByIndex(list, 7));
		
		// 第九项 编制类别
		employee.setStaffType(getItemByIndex(list, 8));
		
		// 第十项 行政职务等级
		employee.setPostLevel(getItemByIndex(list, 9));
		
		// 第十一项 是否任课
		employee.setTeachFlag(Employee.TEACH_FLAG_NAME_NO.equals(getItemByIndex(list, 10))?Employee.TEACH_FLAG_NO:Employee.TEACH_FLAG_YES);
		
		// 第十二项 是否退休
		employee.setRetireFlag(Employee.RETIRE_FLAG_NAME_YES.equals(getItemByIndex(list, 11))?Employee.RETIRE_FLAG_YES:Employee.RETIRE_FLAG_NO);
		
		// 第十三项 是否具备教师资格
		employee.setCertFlag(Employee.CERT_FLAG_NAME_YES.equals(getItemByIndex(list, 12))?Employee.CERT_FLAG_YES:Employee.CERT_FLAG_NO);
		
		// 第十四项 状态
		employee.setStatus(Employee.STATUS_NAME_YES.equals(getItemByIndex(list, 13))?Employee.STATUS_YES:Employee.STATUS_NO);

		mEmployeeList.add(employee);
		return true;
	}
}