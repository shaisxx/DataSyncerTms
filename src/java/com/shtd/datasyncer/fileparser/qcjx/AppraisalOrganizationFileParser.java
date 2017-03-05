package com.shtd.datasyncer.fileparser.qcjx;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.shtd.datasyncer.domain.qcjx.AppraisalOrganization;
import com.shtd.datasyncer.utils.Constant;

/**
 * 解析院系及专业的接口
 * @author jiangnan
 *
 */
public class AppraisalOrganizationFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
//	private static final String PARSED_FILE_EXT_NAME = ".parsed";

	private String mFilePathName = "";
	private List<AppraisalOrganization> mDepartmentList = new ArrayList<AppraisalOrganization>();
	
	public AppraisalOrganizationFileParser(String filePathName) {
		mFilePathName = filePathName;
	}
	
	public boolean doParse() {
		logger.info("开始解析xml数据。");
		mDepartmentList.clear();
		
		if (StringUtils.isBlank(mFilePathName)) {
			logger.info("文件名：" + mFilePathName + " 为空，处理失败。");
			return false;
		}
		
		File xmlFile = new File(mFilePathName);
		if (xmlFile == null || !xmlFile.exists() || !xmlFile.isFile() || !xmlFile.canRead()) {
			logger.info("文件异常，请检查：" + mFilePathName + " 文件。");
			return false;
		}
		
		try {
			// 得到DOM解析器的工厂实例  
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
	        // 从DOM工厂中获得DOM解析器  
	        DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();  
	        // 声明为File为了识别中文名  
	        Document doc = dbBuilder.parse(mFilePathName);
	          
	        // 得到文档名称为YXSDWJBSJXX的元素的节点列表  
	        NodeList list = doc.getElementsByTagName(AppraisalOrganization.ELEMENT_NAME_DEPARTMENT);
	        
	        if (list == null || list.getLength() <= 0) {
				logger.info(" 数据为空。");
				return true;
	        }
	        
			logger.info("共有" + list.getLength() + "条 数据.");
	        // 遍历该集合，显示结合中的元素及其子元素的名字  
            System.out.println("\tno\t单位号\t单位名称\t隶属单位号\t失效日期\t");
	        for(int i=0; i<list.getLength(); i++) {  
	            Element element = (Element) list.item(i); 
	            AppraisalOrganization whicoDepartment = new AppraisalOrganization();
	            whicoDepartment.setUnitNo(getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_NO));
	            whicoDepartment.setName(getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_NAME));
	            whicoDepartment.setParentNo(getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_PARAENT_ID));
	            System.out.println("\t"+(i+1)
      		          			  +"\t"+getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_NO)
	            		          +"\t"+getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_NAME)
	            		          +"\t"+getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_PARAENT_ID)
	            		          +"\t"+getElementValueByTagName(element, AppraisalOrganization.ELEMENT_NAME_VALIDATE)
	            		);
	            mDepartmentList.add(whicoDepartment);
	        }
	        
	        return true;
	        
		} catch (Exception e) {
			logger.error("xml文件解析失败：" + e);
		}
		
		return false;
	}
	
	

	private String getElementValueByTagName(Element element, String tagName) {
		String value = "";
		try {
			value = element.getElementsByTagName(tagName).item(0).getFirstChild().getNodeValue();
		} catch (Exception e) {
		}
		
		return value;
	}

	public List<AppraisalOrganization> getmDepartmentList() {
		return mDepartmentList;
	}

 
}
