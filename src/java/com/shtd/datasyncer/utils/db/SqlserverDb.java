package com.shtd.datasyncer.utils.db;

import com.shtd.datasyncer.utils.ConfigReader;

public class SqlserverDb extends DbBase {


	public SqlserverDb() {
		super();
		
		this.DB_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
		this.USER    = ConfigReader.getInstance().getValue("SQLSERVER_DB_USER");
		this.PASSWD  = ConfigReader.getInstance().getValue("SQLSERVER_DB_PASSWD");
		this.DB_LINK = ConfigReader.getInstance().getValue("SQLSERVER_DB_LINK");
	}
}
