package com.shtd.datasyncer.fileparser.dky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.utils.Constant;

public class StudentFileParser extends BaseFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private List<Student> mStudentList = new ArrayList<Student>();

	public StudentFileParser(String filePathName) {
		super(filePathName);
		
		setDataName("学生数据");
	}
	
	public List<Student> getStudentList() {
		return mStudentList;
	}
	
	protected int getDataCount() {
		return mStudentList.size();
	}

	/**
	 * 数据格式 、
	 * 性别有可能有不同， 毕业年份也有可能不同 
	 * 学号，姓名，性别，出生日期，编号，班级名称，专业，学院，email地址，毕业年份，学籍状态
	 * 20080059024,姜倩,女,19900117,20080059,08电信,电子信息工程技术(电信),电信工程学院,,2011,无,
	 * 20143001001,刘欣馨,2-女,19950505,20143001,14机械制造,机械制造与自动化,机械工程学院,,2017,学籍未建立,
	 * 20136004003,孙璐,女,19950123,20136004,13国际商务,国际商务(物流管理),经济管理学院,,20160701,在籍,

	 * 职工号，姓名，性别
	 */
	protected boolean parseLine(String data) {
		if (StringUtils.isBlank(data)) {
			logger.info("本行数据为空，读取文件结束，退出读取。");
			return false;
		}
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(data.split(SEPARATOR)));
		
		Student student = new Student();
		
		// 学号
		student.setStudentNo(getItemByIndex(list, 0));

		// 姓名
		student.setName(getItemByIndex(list, 1));

		// 性别 女 \ 2-女
		String genderName = getItemByIndex(list, 2);
		if (genderName != null && genderName.length() == 3) {
			genderName = genderName.substring(2);
		}
		student.setGender(Student.GENDER_NAME_FEMALE.equals(genderName) ? Student.GENDER_FEMALE : Student.GENDER_MALE);
		
		// 出生日期
		student.setBirthday(getItemByIndex(list, 3));
		
		// 编号
		student.setSerialNo(getItemByIndex(list, 4));

		// 班级名称
		student.setClazzName(getItemByIndex(list, 5));

		// 专业
		student.setMajorName(getItemByIndex(list, 6));

		// 学院
		student.setCollegeName(getItemByIndex(list, 7));

		// email地址 
		student.setEmailAddr(getItemByIndex(list, 8));

		// 毕业年份 2017 \ 20160701
		String graduateYear = getItemByIndex(list, 9);
		if (graduateYear != null && graduateYear.length() > 4) {
			graduateYear = graduateYear.substring(0, 4);
		}
		student.setGraduateYear(graduateYear);

		// 学籍状态
		String statusName = getItemByIndex(list, 10);
		int status = Student.STATUS_NONE;
		if (Student.STATUS_NAME_ON.equals(statusName)) {
			status = Student.STATUS_ON;
		} else if (Student.STATUS_NAME_UNCREATE.equals(statusName)) {
			status = Student.STATUS_UNCREATE;
		}
		student.setStatus(status);

		mStudentList.add(student);
		return true;
	}

}
