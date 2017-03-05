package com.shtd.datasyncer.fileparser.dky;

import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.domain.dky.Teacher;
import com.shtd.datasyncer.utils.Constant;

public class FileParseWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private String mTeacherFilePath;
	private String mStudentFilePath;
	private String mClazzFilePath;
	
	private List<Teacher> mTeacherList;
	private List<Student> mStudentList;
	private List<Clazz>   mClazzList;

	public void setTeacherFilePath(String teacherFilePath) {
		this.mTeacherFilePath = teacherFilePath;
	}
	public void setStudentFilePath(String studentFilePath) {
		this.mStudentFilePath = studentFilePath;
	}
	public void setClazzFilePath(String clazzFilePath) {
		this.mClazzFilePath = clazzFilePath;
	}

	public List<Teacher> getTeacherList() {
		return mTeacherList;
	}
	public List<Student> getStudentList() {
		return mStudentList;
	}
	public List<Clazz> getClazzList() {
		return mClazzList;
	}
	
	public void doParse() {
		TeacherFileParser tparser = new TeacherFileParser(mTeacherFilePath);
		if (tparser.doParse()) {
			mTeacherList = tparser.getTeacherList();
		}
		logger.info("解析教师数据完毕");
		
		
		ClazzFileParser cparser = new ClazzFileParser(mClazzFilePath);
		if (cparser.doParse()) {
			mClazzList = cparser.getClazzList();
		}
		logger.info("解析班级数据完毕");

		
		StudentFileParser sparser = new StudentFileParser(mStudentFilePath);
		if (sparser.doParse()) {
			mStudentList = sparser.getStudentList();
		}
		logger.info("解析学生数据完毕");
	}
}
