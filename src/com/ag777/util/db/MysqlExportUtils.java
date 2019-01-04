package com.ag777.util.db;

import java.util.List;
import com.ag777.util.lang.collection.ListUtils;

/**
 * mysql数据库导入导出工具
 * 
 * @author ag777
 * @version create on 2019年01月04日,last modify at 2019年01月04日
 */
public class MysqlExportUtils {
	
	private MysqlExportUtils() {}
	
	/**
	 * 获取导入数据库命令
	 * @param user
	 * @param password
	 * @param dbName
	 * @param ignoreTableList
	 * @param filePath
	 * @return
	 */
	public static String getImportCommand(String user, String password, String dbName, List<String> ignoreTableList, String filePath) {  
		return getImportCommand(null, null, null, user, password, dbName, ignoreTableList, filePath);
	}
	
	/**
	 * 获取导入数据库命令
	 * @param mysqlPath
	 * @param user
	 * @param password
	 * @param dbName
	 * @param ignoreTableList
	 * @param filePath
	 * @return
	 */
	public static String getImportCommand(String mysqlPath, String user, String password, String dbName, List<String> ignoreTableList, String filePath) {  
		return getImportCommand(mysqlPath, null, null, user, password, dbName, ignoreTableList, filePath);
	}
	
	/**
	 * 获取导入数据库命令
	 * @param mysqlPath
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @param dbName
	 * @param ignoreTableList
	 * @param filePath
	 * @return
	 */
	public static String getImportCommand(String mysqlPath, String host, Integer port, String user, String password, String dbName, List<String> ignoreTableList, String filePath) {  
		StringBuilder sb = new StringBuilder();
		if(mysqlPath == null) {
			mysqlPath = "mysql";
		}
		if(host == null) {
			host = "127.0.0.1";
		}
		if(port == null) {
			port = 3306;
		}
		sb.append(mysqlPath)
			.append(" -u").append(user)
			.append(" -p").append(password)
			.append(" -h").append(host)
			.append(" -P").append(port)
			.append(' ').append(dbName)
			.append(" <").append(filePath)
			;
		
		return sb.toString();
	}
	
	/**
	 * 获取备份数据库命令
	 * @param user
	 * @param password
	 * @param dbName
	 * @param ignoreTableList
	 * @param filePath
	 * @return
	 */
	public static String getExportCommand(String user, String password, String dbName, List<String> ignoreTableList, String filePath) {
		return getExportCommand(null, null, null, user, password, dbName, ignoreTableList, filePath);
	}

	/**
	 * 获取备份数据库命令
	 * @param mysqlDumpPath
	 * @param user
	 * @param password
	 * @param dbName
	 * @param ignoreTableList
	 * @param filePath
	 * @return
	 */
	public static String getExportCommand(String mysqlDumpPath, String user, String password, String dbName, List<String> ignoreTableList, String filePath) {
		return getExportCommand(mysqlDumpPath, null, null, user, password, dbName, ignoreTableList, filePath);
	}
	
	/**
	 * 获取备份数据库命令
	 * @param mysqlDumpPath
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @param dbName
	 * @param ignoreTableList
	 * @param filePath
	 * @return
	 */
	public static String getExportCommand(String mysqlDumpPath, String host, Integer port, String user, String password, String dbName, List<String> ignoreTableList, String filePath) {  
		StringBuilder sb = new StringBuilder();
		if(mysqlDumpPath == null) {
			mysqlDumpPath = "mysqldump";
		}
		if(host == null) {
			host = "127.0.0.1";
		}
		if(port == null) {
			port = 3306;
		}
		sb.append(mysqlDumpPath)
			.append(" -u").append(user)
			.append(" -p").append(password)
			.append(" -h").append(host)
			.append(" -P").append(port)
			.append(' ').append(dbName)
			;
		
		if(ignoreTableList != null) {
			for (String tableName : ignoreTableList) {
				sb.append(" --ignore-table=").append(dbName).append('.').append(tableName);
			}
		}
		
		sb.append(" >").append(filePath).append(" --skip-lock-tables");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		String cmd = getExportCommand("/usr/local/mysql/bin/mysqldump", "root", "123456", "wrvas", 
				ListUtils.of("t_user1", "t_user2", "t_user3"), "/usr/local/database_bak/sss/backup-nnvas.sql");
		System.out.println(cmd);
	}
}
