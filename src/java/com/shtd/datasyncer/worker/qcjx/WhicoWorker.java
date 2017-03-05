package com.shtd.datasyncer.worker.qcjx;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.datagetter.qcjx.WhicoDataGetWorker;
import com.shtd.datasyncer.dbsaver.qcjx.WhicoClazzDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.WhicoDepartmentDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.WhicoStudentDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.WhicoTeacherDataSaver;
import com.shtd.datasyncer.fileparser.qcjx.WhicoClazzFileParser;
import com.shtd.datasyncer.fileparser.qcjx.WhicoDepartmentFileParser;
import com.shtd.datasyncer.fileparser.qcjx.WhicoStudentFileParser;
import com.shtd.datasyncer.fileparser.qcjx.WhicoStudentSchoolRollFileParser;
import com.shtd.datasyncer.fileparser.qcjx.WhicoTeacherFileParser;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * 微课平台-汽车学校接口同步
 * @author jiangnan
 *
 */
public class WhicoWorker implements IWorker {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public void work() {
		
		/* server端会返回     "未查询到数据！"
		 * 教师数据，有的 当前状态码 或者 性别编码 为空
		 * 组织结构数据，隶属单位号 有可能为空
		 * 
		 * 三个字典表暂不获取  组织结构/性别/教师当前状态
		 */
		
		logger.info("qcjx whico worker 工作开始，从webservice获取数据");
		WhicoDataGetWorker getWorker = new WhicoDataGetWorker();
		getWorker.pullData();
		logger.info("数据获取工作已完成");

		
		if ((Constant.INT_TRUE+"").equals(ConfigReader.getInstance().getValue("GET_DATA_ONLY"))) {
			logger.info("当前配置为只获取数据并存储到本地，qcjx whico worker 工作结束。");
			return;
		}
		
		
		logger.info("解析教师数据");
		WhicoTeacherFileParser teacherParser = new WhicoTeacherFileParser(getWorker.getTeacherFilePath());
		if (!teacherParser.doParse()) {
			// 解析失败
			logger.info("教师数据解析失败");
		}
		
		logger.info("解析院系，专业数据");
		WhicoDepartmentFileParser departmentFileParser = new WhicoDepartmentFileParser(getWorker.getDepartmentXmlFilePathName());
		
		if (!departmentFileParser.doParse()) {
			// 解析失败
			logger.info("院系，专业数据解析失败");
		}
		
		//处理班级数据
		logger.info("解析班级数据");
		WhicoClazzFileParser clazzParser = new WhicoClazzFileParser(getWorker.getClazzFilePath());
		if (!clazzParser.doParse()) {
			// 解析失败
			logger.info("班级数据解析失败");
		}
		
		logger.info("解析学生信息数据");
		//处理学生信息数据
		WhicoStudentFileParser stuFileParser = new WhicoStudentFileParser(getWorker.getStudentXmlFilePathName());
		if (!stuFileParser.doParse()) {
			// 解析失败
			logger.info("学生信息数据解析失败");
		}
		
		logger.info("解析学籍数据");
		//处理学生学籍数据
		WhicoStudentSchoolRollFileParser  stuSchoolRollParser =  new  WhicoStudentSchoolRollFileParser(getWorker.getStudentSchoolRollXmlFilePathName());
		if (!stuSchoolRollParser.doParse()) {
			// 解析失败
			logger.info("学籍数据解析失败");
		}
		
		logger.info("教师数据解析完成，将教师数据更新到数据库");
		WhicoTeacherDataSaver teacherSaver = new WhicoTeacherDataSaver(teacherParser.getTeacherList());
		if (!teacherSaver.doSave()) {
			// 保存失败
			logger.info("教师数据更新到数据库失败");
		}
		
		logger.info("院系、专业数据解析完成，将院系、专业数据更新到数据库");
		WhicoDepartmentDataSaver departmentDataSaver = new WhicoDepartmentDataSaver(departmentFileParser.getmDepartmentList());
		if (!departmentDataSaver.doSave()) {
			// 保存失败
			logger.info("院系、专业数据更新到数据库失败");
		}
		
		logger.info("班级数据解析完成，将班级数据更新到数据库");
		WhicoClazzDataSaver clazzDataSaver = new WhicoClazzDataSaver(clazzParser.getmClazzList());
		if (!clazzDataSaver.doSave()) {
			// 保存失败
			logger.info("班级数据解析更新到数据库失败");
		}
		
		logger.info("学生数据解析完成，将学生数据更新到数据库");
		WhicoStudentDataSaver studentDataSaver = new WhicoStudentDataSaver(stuFileParser.getStudentList(),stuSchoolRollParser.getStudentSchoolRollList());
		if (!studentDataSaver.doSave()) {
			// 保存失败
			logger.info("学生数据解析更新到数据库失败");
		}
		
		logger.info("qcjx worker 工作结束");
	}
}
