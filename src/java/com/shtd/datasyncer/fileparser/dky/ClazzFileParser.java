package com.shtd.datasyncer.fileparser.dky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.domain.dky.Clazz;
import com.shtd.datasyncer.utils.Constant;

public class ClazzFileParser extends BaseFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private List<Clazz> mClazzList = new ArrayList<Clazz>();
	
	public ClazzFileParser(String filePathName) {
		super(filePathName);
		
		setDataName("班级数据");
	}
	
	public List<Clazz> getClazzList() {
		return mClazzList;
	}
	
	protected int getDataCount() {
		return mClazzList.size();
	}

	/**
	 * 数据格式
	 * 编号，班级名称，学院名称
	 * ZD03003,中 升学3班,电信工程学院,
	 */
	protected boolean parseLine(String data) {
		if (StringUtils.isBlank(data)) {
			logger.info("本行数据为空，读取文件结束，退出读取。");
			return false;
		}
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(data.split(SEPARATOR)));
		
		Clazz clazz = new Clazz();
		
		// 第一项 编号
		clazz.setClazzNo(getItemByIndex(list, 0));

		// 第二项 班级名称
		clazz.setClazzName(getItemByIndex(list, 1));

		// 第三项 学院名称
		clazz.setCollegeName(getItemByIndex(list, 2));
		
		mClazzList.add(clazz);
		return true;
	}

}
