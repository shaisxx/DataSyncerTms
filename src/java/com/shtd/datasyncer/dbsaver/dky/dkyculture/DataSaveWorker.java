package com.shtd.datasyncer.dbsaver.dky.dkyculture;

import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.utils.Constant;

public class DataSaveWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private List<Clazz> mClazzList = null;
	private List<Student> mStudentList = null;

	public void setClazzList(List<Clazz> clazzList) {
		this.mClazzList = clazzList;
	}
	public void setStudentList(List<Student> studentList) {
		this.mStudentList = studentList;
	}

	public void doSave() {
		// 要首先更新 班级数据，再更新 学生数据， dkyculture 不需要教师数据
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
