package com.shtd.datasyncer.fileparser.tms;

import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.tms.Employee;
import com.shtd.datasyncer.utils.Constant;

public class FileParseWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private String mEmployeeFilePath;
	
	private List<Employee> mEmployeeList;

	public void setEmployeeFilePath(String employeeFilePath) {
		this.mEmployeeFilePath = employeeFilePath;
	}

	public List<Employee> getEmployeeList() {
		return mEmployeeList;
	}
	
	public void doParse() {
		EmployeeFileParser tparser = new EmployeeFileParser(mEmployeeFilePath);
		if (tparser.doParse()) {
			mEmployeeList = tparser.getEmployeeList();
		}
		logger.info("解析教师数据完毕");
	}
}