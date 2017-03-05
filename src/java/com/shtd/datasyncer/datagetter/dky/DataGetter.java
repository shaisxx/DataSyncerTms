package com.shtd.datasyncer.datagetter.dky;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.shtd.datasyncer.utils.ConfigReader;
import com.shtd.datasyncer.utils.Constant;
import com.shtd.datasyncer.wsdl.dky.MetadataColumn;
import com.shtd.datasyncer.wsdl.dky.Param;
import com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImpl;
import com.shtd.datasyncer.wsdl.dky.WebServiceInterfaceImplServiceLocator;

public class DataGetter {

	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);
	
	private static String SUBFOLDER = "";
	
	// 单次最多获取数据条数
	private static final int MAX_SINGLE_GET_COUNT = 2000;

	
	// 调用接口，用户名
	protected String mUserName;
	
	// 调用接口，密码
	protected String mPwd;

	// 调用接口，数据服务Id	
	protected String mDataSvrId;

	// 返回数据 保存的xml文件名
	protected String mFileName;
	
	// xml的路径+文件名
	private String mFilePathName;
	
	public DataGetter() {
		initSubFolder();
	}
	
	/**
	 * 
	 * @param userName 用户名
	 * @param pwd 密码
	 * @param dataSvrId 数据服务Id	
	 * @param fileName 保存的xml文件名
	 */
	public DataGetter(String userName, String pwd, String dataSvrId, String fileName) {
		initSubFolder();
		
		this.mUserName  = userName;
		this.mPwd       = pwd;
		this.mDataSvrId = dataSvrId;
		this.mFileName  = fileName;
	}
	
	public String getFilePathName() {
		return mFilePathName;
	}
	 
	// 由webservice端 拉取数据 并保存在本地
	public boolean pullData() {
		try {
			//获得数据服务Service
			WebServiceInterfaceImplServiceLocator locator = new WebServiceInterfaceImplServiceLocator();
			WebServiceInterfaceImpl service = locator.getdataService();

			logger.info("由webservice拉取数据，用户名:" + mUserName + ", 服务号:" + mDataSvrId);
			
			//获得数据服务的列的元信息
			MetadataColumn[] columns = service.getDataServerColumns(mUserName, mPwd, mDataSvrId);
			
			String dataContent = "";
			for(int index=0; index<columns.length; index++) {
				//在这里可以获得列的元信息
				// String colname = columns[index].getColumn_name();
				// String colcomment = columns[index].getComments();
				dataContent += columns[index].getColumn_name() + ",";
			}
			
			logger.info("获取栏目名称成功。");
			
			dataContent += "\n";
			
			//无参数情况
			Param[] params = new Param[0]; 
			
			//获得数据行数
			int totlaCount = service.getDataCount(mUserName, mPwd, mDataSvrId, params);
			logger.info("获取数据数量成功，共 " + totlaCount + " 条数据。");
			
			
			// 指定每次最多获取数据条数，分批获取所有数据
			int beginPos = 0;
			int count = 0;
			while (beginPos <= totlaCount) {

				count = (beginPos+MAX_SINGLE_GET_COUNT > totlaCount) 
						? (totlaCount - beginPos)
						: MAX_SINGLE_GET_COUNT;
				
				if (count == 0) {
					break;
				}
				
				// 获取N条数据 返回的数据按照元信息的列的顺序
				String[][] datas = service.getData(mUserName, 
						                           mPwd, 
						                           mDataSvrId, 
						                           params, 
						                           beginPos, 
						                           count);
				
				logger.info("获取从第 " +beginPos+ " 条开始的   " +count+ "  条数据");
				
				for(int rindex = 0, rlen = datas.length; rindex <  rlen; rindex ++) {
					// 一行数据
					for(int cindex = 0, clen = columns.length; cindex < clen; cindex ++) {
						// 获得某行某列的数据
						dataContent += datas[rindex][cindex] + ",";
					}
					
					dataContent += "\n";
				}
				
				beginPos += count;
			}
			
			logger.info("获取数据成功。");
			
			mFilePathName = getFilePathName(mFileName);
			logger.info("拉取数据完毕，将数据写入：" + mFilePathName);
			
			if (writeToFile(mFilePathName, dataContent)) {
				logger.info("数据写入本地文件 " + mFilePathName + " 成功");
				return true;
			} 

			logger.info("数据写入本地文件 " + mFilePathName + " 失败，数据内容为：\n" + count);
			
		} catch (Exception e) {
			logger.error("由webservice端拉取数据失败：" + e);
		}
		
		return false;
	}
	
	// 生成文件路径 ./data/yyyyMMddHHmmss/xmlfile
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
