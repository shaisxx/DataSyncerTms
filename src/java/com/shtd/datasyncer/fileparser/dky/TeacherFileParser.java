package com.shtd.datasyncer.fileparser.dky;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Teacher;
import com.shtd.datasyncer.utils.Constant;

public class TeacherFileParser extends BaseFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private List<Teacher> mTeacherList = new ArrayList<Teacher>();

	
	public TeacherFileParser(String filePathName) {
		super(filePathName);
		
		setDataName("教师数据");
	}
	
	public List<Teacher> getTeacherList() {
		return mTeacherList;
	}
	
	protected int getDataCount() {
		return mTeacherList.size();
	}

	/**
	 * 数据格式
	 * 职工号，姓名，性别
	 * 100909,魏玉辉,男,
	 */
	protected boolean parseLine(String data) {
		if (StringUtils.isBlank(data)) {
			logger.info("本行数据为空，读取文件结束，退出读取。");
			return false;
		}
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(data.split(SEPARATOR)));
		
		Teacher teacher = new Teacher();
		
		// 第一项 教师员工号
		teacher.setEmployeeNo(getItemByIndex(list, 0));

		// 第二项 教师姓名
		teacher.setName(getItemByIndex(list, 1));

		// 第三项 教师性别
		teacher.setGender(Teacher.GENDER_NAME_FEMALE.equals(getItemByIndex(list, 2)) ? Teacher.GENDER_FEMALE : Teacher.GENDER_MALE);
		
		mTeacherList.add(teacher);
		return true;
	}
}
