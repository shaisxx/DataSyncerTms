package com.shtd.datasyncer.utils.db;

import com.shtd.datasyncer.utils.ConfigReader;

public class SyncerMysqlDb extends DbBase {

	public SyncerMysqlDb() {
		super();
		
		this.DB_DRIVER = "com.mysql.jdbc.Driver";
		this.USER    = ConfigReader.getInstance().getValue("SYNCER_DB_USER");
		this.PASSWD  = ConfigReader.getInstance().getValue("SYNCER_DB_PASSWD");
		this.DB_LINK = ConfigReader.getInstance().getValue("SYNCER_DB_LINK");
	}
}
