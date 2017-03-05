package com.shtd.datasyncer.worker.qcjx;

import org.apache.log4j.Logger;

import com.shtd.datasyncer.datagetter.qcjx.AppraisalDataGetWorker;
import com.shtd.datasyncer.dbsaver.qcjx.AppraisalClazzDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.AppraisalDepartmentDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.AppraisalOrganizationDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.AppraisalStudentDataSaver;
import com.shtd.datasyncer.dbsaver.qcjx.AppraisalTeacherDataSaver;
import com.shtd.datasyncer.fileparser.qcjx.AppraisalClazzFileParser;
import com.shtd.datasyncer.fileparser.qcjx.AppraisalDepartmentFileParser;
import com.shtd.datasyncer.fileparser.qcjx.AppraisalOrganizationFileParser;
import com.shtd.datasyncer.fileparser.qcjx.AppraisalStudentFileParser;
import com.shtd.datasyncer.fileparser.qcjx.AppraisalStudentSchoolRollFileParser;
import com.shtd.datasyncer.fileparser.qcjx.AppraisalTeacherFileParser;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.worker.IWorker;

/**
 * 汽车学校接口同步--教学评价系统
 * @author Josh	 
 */
public class AppraisalWorker implements IWorker{
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	public void work() {
		
		/* server端会返回     "未查询到数据！"
		 * 教师数据，有的 当前状态码 或者 性别编码 为空
		 * 组织结构数据，隶属单位号 有可能为空
		 * 三个字典表暂不获取  组织结构/性别/教师当前状态
		 */
		
		logger.info("qcjx appraisal worker 工作开始，从webservice获取数据");
		AppraisalDataGetWorker getWorker = new AppraisalDataGetWorker();
		getWorker.pullData();
		logger.info("数据获取工作已完成");

		
		if ((Constant.INT_TRUE+"").equals(ConfigReader.getInstance().getValue("GET_DATA_ONLY"))) {
			logger.info("当前配置为只获取数据并存储到本地，qcjx appraisal worker 工作结束。");
			return;
		}
		
		
		logger.info("解析教师数据");
		AppraisalTeacherFileParser teacherParser = new AppraisalTeacherFileParser(getWorker.getTeacherFilePath());
		if (!teacherParser.doParse()) {
			// 解析失败
			logger.info("教师数据解析失败");
		}
		
		logger.info("解析组织结构数据");
		AppraisalOrganizationFileParser organizationFileParser = new AppraisalOrganizationFileParser(getWorker.getOrganizationFilePath());
		
		if (!organizationFileParser.doParse()) {
			// 解析失败
			logger.info("组织结构数据解析失败");
		}
		
		logger.info("解析院系，专业数据");
		AppraisalDepartmentFileParser departmentFileParser = new AppraisalDepartmentFileParser(getWorker.getDepartmentXmlFilePathName());
		
		if (!departmentFileParser.doParse()) {
			// 解析失败
			logger.info("院系，专业数据解析失败");
		}
		
		//处理班级数据
		logger.info("解析班级数据");
		AppraisalClazzFileParser clazzParser = new AppraisalClazzFileParser(getWorker.getClazzFilePath());
		if (!clazzParser.doParse()) {
			// 解析失败
			logger.info("班级数据解析失败");
		}
		
		logger.info("解析学生信息数据");
		//处理学生信息数据
		AppraisalStudentFileParser stuFileParser = new AppraisalStudentFileParser(getWorker.getStudentXmlFilePathName());
		if (!stuFileParser.doParse()) {
			// 解析失败
			logger.info("学生信息数据解析失败");
		}
		
		logger.info("解析学籍数据");
		//处理学生学籍数据
		AppraisalStudentSchoolRollFileParser  stuSchoolRollParser =  new  AppraisalStudentSchoolRollFileParser(getWorker.getStudentSchoolRollXmlFilePathName());
		if (!stuSchoolRollParser.doParse()) {
			// 解析失败
			logger.info("学籍数据解析失败");
		}
		
		logger.info("组织机构数据解析完成，将组织机构数据更新到数据库");
		AppraisalOrganizationDataSaver organizationDataSaver = new AppraisalOrganizationDataSaver(organizationFileParser.getmDepartmentList());
		if (!organizationDataSaver.doSave()) {
			// 保存失败
			logger.info("组织机构数据更新到数据库失败");
			return;
		}
		
		logger.info("教师数据解析完成，将教师数据更新到数据库");
		AppraisalTeacherDataSaver teacherSaver = new AppraisalTeacherDataSaver(teacherParser.getTeacherList());
		if (!teacherSaver.doSave()) {
			// 保存失败
			logger.info("教师数据更新到数据库失败");
			return;
		}
		
		logger.info("院系、专业数据解析完成，将院系、专业数据更新到数据库");
		AppraisalDepartmentDataSaver departmentDataSaver = new AppraisalDepartmentDataSaver(departmentFileParser.getmDepartmentList());
		if (!departmentDataSaver.doSave()) {
			// 保存失败
			logger.info("院系、专业数据更新到数据库失败");
			return;
		}
		
		logger.info("班级数据解析完成，将班级数据更新到数据库");
		AppraisalClazzDataSaver clazzDataSaver = new AppraisalClazzDataSaver(clazzParser.getmClazzList());
		if (!clazzDataSaver.doSave()) {
			// 保存失败
			logger.info("班级数据解析更新到数据库失败");
			return;
		}
		
		logger.info("班级数据解析完成，将班级数据更新到数据库");
		AppraisalStudentDataSaver studentDataSaver = new AppraisalStudentDataSaver(stuFileParser.getStudentList(),stuSchoolRollParser.getStudentSchoolRollList());
		if (!studentDataSaver.doSave()) {
			// 保存失败
			logger.info("班级数据解析更新到数据库失败");
			return;
		}
		logger.info("qcjx worker 工作结束");
	}
}