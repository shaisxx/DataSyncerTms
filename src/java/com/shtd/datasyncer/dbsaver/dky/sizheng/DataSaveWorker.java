package com.shtd.datasyncer.dbsaver.dky.sizheng;

import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.domain.dky.Teacher;
import com.shtd.datasyncer.utils.Constant;

public class DataSaveWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private List<Clazz> mClazzList = null;
	private List<Student> mStudentList = null;
	private List<Teacher> mTeacherList = null;

	public void setClazzList(List<Clazz> clazzList) {
		this.mClazzList = clazzList;
	}
	public void setStudentList(List<Student> studentList) {
		this.mStudentList = studentList;
	}
	public void setTeacherList(List<Teacher> teacherList){
		this.mTeacherList = teacherList;
	}

	public void doSave() {
		//先更新教师数据，这个与其他两个没有先后顺序的问题
		logger.info("将教师数据更新到数据库");
		TeacherDataSaver tSaver = new TeacherDataSaver(mTeacherList);
		if (!tSaver.doSave()) {
			// 保存失败
			logger.info("教师数据更新到数据库失败");
			return;
		}
		
		// 要首先更新 班级数据，再更新 学生数据
		logger.info("将班级数据更新到数据库");
		ClazzDataSaver cSaver = new ClazzDataSaver(mClazzList);
		if (!cSaver.doSave()) {
			// 保存失败
			logger.info("班级数据更新到数据库失败");
			return;
		}
		
		
		logger.info("将学生数据更新到数据库");
		StudentDataSaver sSaver = new StudentDataSaver(mStudentList);
		if (!sSaver.doSave()) {
			// 保存失败
			logger.info("学生数据更新到数据库失败");
			return;
		}
		
		

		
	}
}
