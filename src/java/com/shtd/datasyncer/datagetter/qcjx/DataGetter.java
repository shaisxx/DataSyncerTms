package com.shtd.datasyncer.datagetter.qcjx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.AESEncrypt;
import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.wsdl.qcjx.DcmDataInfService;

public class DataGetter {

	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private static String SUBFOLDER = "";

	// 调用接口，第一个参数：数据库的用户名
	protected String mUserName;

	// 调用接口，第二个参数：表名
	protected String mTableName;

	// 返回数据 保存的xml文件名
	protected String mXmlFileName;
	
	// xml的路径+文件名
	private String mXmlFilePathName;
	
	public DataGetter() {
		initSubFolder();
	}
	
	public DataGetter(String userName, String tableName, String xmlFileName) {
		initSubFolder();
		
		this.mUserName = userName;
		this.mTableName = tableName;
		this.mXmlFileName = xmlFileName;
	}
	
	public String getXmlFilePathName() {
		return mXmlFilePathName;
	}
	 
	// 由webservice端 拉取数据 并保存在本地
	public boolean pullData() {
		try {
			DcmDataInfService service = new DcmDataInfService();
			
			logger.info("由webservice拉取数据，用户名:" + mUserName + ", 表名:" + mTableName);
			String data = service.getDcmDataInfPort().getDcmDbCInfor(getAESEncryptStr(mUserName), getAESEncryptStr(mTableName));
	
			mXmlFilePathName = getFilePathName(mXmlFileName);
			logger.info("拉取数据完毕，将数据写入：" + mXmlFilePathName);
			
			if (writeToFile(mXmlFilePathName, data)) {
				logger.info("数据写入本地文件 " + mXmlFilePathName + " 成功");
				return true;
			} 

			logger.info("数据写入本地文件 " + mXmlFilePathName + " 失败，数据内容为：\n" + data);
			
			
		} catch (Exception e) {
			logger.error("由webservice端拉取数据失败：" + e);
		}
		
		return false;
		
	}
	
	// 将指定字符串通过AES加密
	private String getAESEncryptStr(String srcStr) {
		String ret = "";
		
		try {
			ret = AESEncrypt.Encrypt(srcStr, AESEncrypt.PASSWORD_CRYPT_KEY);
		} catch (Exception e) {
			logger.error("AES加密字符串：" + srcStr + "失败，" + e);
		}
		
		return ret;
	}
	
	// 生成文件路径 ./xml_data/yyyymmdd/xmlfile
	private String getFilePathName(String xmlFileName) {
		return ConfigReader.getInstance().getValue("DATA_FOLDER") + SUBFOLDER + xmlFileName;
	}
	
	/**
     * 逐层创建文件夹
     * @param path 文件路径
     * 
     */
    private boolean mkPathFolders(String filePath) {
        String paths[] = filePath.split("/");
        String dir = paths[0];
        for (int i = 0; i < paths.length - 2; i++) {
            try {
                dir = dir + "/" + paths[i + 1];
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }
            } catch (Exception e) {
    			logger.error("根据 " +filePath+ " 逐层创建目录失败" + e);
    			return false;
            }
        }
        
        return true;
    }
    
	private void initSubFolder() {
		if (StringUtils.isBlank(SUBFOLDER)) {
			Calendar calender = Calendar.getInstance();
	        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");    
			 
			SUBFOLDER = format.format(calender.getTime()) + "/";
		}
	}
	
	
	// 将数据写入xml文件
	private boolean writeToFile(String filePathName, String content) {
		if (StringUtils.isBlank(filePathName)
              || StringUtils.isBlank(content)) {
			logger.error("数据写入本地文件，传入文件路径：" + filePathName + ", 待写入内容：" + content + ". 写入失败");
			return false;
		}

		if (!mkPathFolders(filePathName)) {
			logger.error("创建文件目录失败！");
			return false;
		}
		
		FileOutputStream   fos = null;
		OutputStreamWriter osw = null;
		try {
			File file = new File(filePathName);
			
			if(!file.exists()) {
				// 不存在则创建
				file.createNewFile();
			}
			
			fos = new FileOutputStream(file);  
	        osw = new OutputStreamWriter(fos, "UTF-8");  
	        osw.write(content);  
	        osw.close();
	        
	        return true;
	        
		} catch (Exception e) {
			logger.error("写入文件失败：" + e);
			
		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (Exception e) {
					logger.error("关闭OutputStreamWriter失败：" + e);
				}
			}
			
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					logger.error("关闭FileOutputStream失败：" + e);
				}
			}
		}

		return false;
	}
}
