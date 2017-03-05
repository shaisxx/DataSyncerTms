package com.shtd.datasyncer.dbsaver.dky.cxcy;

import java.util.List;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.domain.dky.Student;
import com.shtd.datasyncer.domain.dky.Teacher;
import com.shtd.datasyncer.utils.Constant;

/**
 * 易润(easyrun) 开发的创新创业平台 数据保存
 * @author zhanggn
 */
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
	public void setTeacherList(List<Teacher> teacherList) {
		this.mTeacherList = teacherList;
	}

	/**
	 * ！！按照此顺序插入数据
	 * 学院 ->老师 ->班级 ->学生  
	 * 
	 * 学院信息：
	 * 表名：PUB_College
	 * 	名称(Name)、描述(Description)
	 * 
	 * 老师、学生信息：
	 * PUB_User
	 * 	姓名(Name)
	 * 	登录名（User_Name）
	 * 	班级ID （Class_ID 老师为空）
	 * 	登录密码（User_Pass） 不能为空随便写个
	 * 	用户类型(User_Group 1=老师，0=学生)
	 * 	用户状态（State 0=未初始化，1=审批不通过，2=正常，3=结业）
	 * 	性别 （Sex 男、女，不填默认男）
	 * 	生日 （Birthday 格式yyyy-MM-dd）
	 * 删除老师、学生，调用存储过程：deleteUser 用户ID；删除老师会同时删除老师管理的班级，班级下的学生
	 * 
	 * 班级信息：
	 * Pub_Class
	 * 	学院ID College_ID 不可为空
	 * 	老师ID Teacher_ID 可空，但为空不能进后台 可默认一个能正常登录的老师ID
	 * 	班级名称 Name
	 * 班级添加完后，调用存储过程：InitClassParams 班级ID 1
	 * 删除班级，调用存储过程：deleteClass 班级ID，删除班级会删除班级下的学生
	 * 
	 * @author zhanggn
	 */
	public void doSave() {
		logger.info("将学院数据更新到数据库");
		CollegeDataSaver cSaver = new CollegeDataSaver(mClazzList);
		if (!cSaver.doSave()) {
			// 保存失败
			logger.info("学院数据更新到数据库失败");
			return;
		}
		

		logger.info("将老师数据更新到数据库");
		TeacherDataSaver tSaver = new TeacherDataSaver(mTeacherList);
		if (!tSaver.doSave()) {
			// 保存失败
			logger.info("老师数据更新到数据库失败");
			return;
		}

		
		logger.info("将班级数据更新到数据库");
		SquadDataSaver sqSaver = new SquadDataSaver(mClazzList);
		if (!sqSaver.doSave()) {
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
