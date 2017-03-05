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

import com.shtd.datasyncer.domain.qcjx.WhicoClazz;
import com.shtd.datasyncer.utils.Constant;

/**
 * 未完全处理，只是解析一下xml文件，并print每条内容
 * @author zhanggn
 *
 */
public class ClazzFileParser {
	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
//	private static final String PARSED_FILE_EXT_NAME = ".parsed";

	private String mFilePathName = "";
	
	private List<WhicoClazz> mClazzList = new ArrayList<WhicoClazz>();
	
	public ClazzFileParser(String filePathName) {
		mFilePathName = filePathName;
	}
	
	public boolean doParse() {
		logger.info("开始解析xml数据。");

		mClazzList.clear();
		
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
	          
	        // 得到文档名称为JZGJCSJXX的元素的节点列表  
	        NodeList list = doc.getElementsByTagName("BJSJXX");
	        
	        if (list == null || list.getLength() <= 0) {
				logger.info(" 数据为空。");
				return true;
	        }
	        
			logger.info("共有" + list.getLength() + "条 数据.");
	        	          
	        // 遍历该集合，显示结合中的元素及其子元素的名字  
            System.out.println("\tno\t所属单位\t班号\t班级名称\t");
	        for(int i=0; i<list.getLength(); i++) {  
	        	WhicoClazz clazz = new WhicoClazz();
	            Element element = (Element) list.item(i); 
	            
	            clazz.setClazzNo(getElementValueByTagName(element, "BH"));
	            clazz.setClazzName(getElementValueByTagName(element, "BJ"));
	            clazz.setDepartmentNo(getElementValueByTagName(element, "DWH"));
	            
	            mClazzList.add(clazz);
	            System.out.println("\t"+(i+1)
      		          			  +"\t"+getElementValueByTagName(element, "DWH")
	            		          +"\t"+getElementValueByTagName(element, "BH")
	            		          +"\t"+getElementValueByTagName(element, "BJ")
	            		);
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

	public List<WhicoClazz> getmClazzList() {
		return mClazzList;
	}
}
