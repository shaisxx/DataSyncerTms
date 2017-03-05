package com.shtd.datasyncer.utils.db;

import com.shtd.datasyncer.utils.ConfigReader;

public class MysqlDb extends DbBase {

	public MysqlDb() {
		super();
		
		this.DB_DRIVER = "com.mysql.jdbc.Driver";
		this.USER    = ConfigReader.getInstance().getValue("MYSQL_DB_USER");
		this.PASSWD  = ConfigReader.getInstance().getValue("MYSQL_DB_PASSWD");
		this.DB_LINK = ConfigReader.getInstance().getValue("MYSQL_DB_LINK");
	}
}
