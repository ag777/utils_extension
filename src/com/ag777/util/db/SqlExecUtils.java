package com.ag777.util.db;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.ag777.util.db.DbHelper;
import com.ag777.util.db.model.DbErrCode;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.RegexUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;

/**
 * sql执行辅助类
 * <p>
 * 目前支持插入一个map结构的数据
 * </p>
 * 
 * @author ag777
 * @version create on 2018年08月24日,last modify at 2018年08月27日
 */
public class SqlExecUtils {

	private SqlExecUtils() {
	}
	
	/**
	 * 批量插入,异常终止
	 * <p>
	 * 不带事务
	 * </p>
	 * @param list
	 * @param tableName
	 * @param db
	 * @throws SQLException
	 */
	public static <K, V>void insert(List<Map<K, V>> list, String tableName, DbHelper db) throws SQLException {
		if(ListUtils.isEmpty(list)) {
			return;
		}
		for (Map<K, V> map : list) {
			insert(map, tableName, db);
		}
	}
	
	/**
	 * 插入一行记录
	 * @param map
	 * @param tableName
	 * @param db
	 * @throws SQLException
	 */
	public static <K, V>void insert(Map<K, V> map, String tableName, DbHelper db) throws SQLException {
		if(MapUtils.isEmpty(map) || StringUtils.isEmpty(tableName) || db == null) {
			return;
		}
		StringBuilder sql = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
		List<String> titleList = ListUtils.newArrayList();	//列头
		Object[] params = new Object[map.size()];		//数据
		int index = 0;
		Iterator<K> itor = map.keySet().iterator();
		while(true) {
			K key = itor.next();
			V value = map.get(key);
			String keyStr = key.toString();
			//填充列头
			titleList.add(keyStr);
			//填充参数数组
			params[index] = value;
			index++;
			//填充sql
			sql.append('`').append(keyStr).append('`');
			if(itor.hasNext()) {
				sql.append(',');
			} else {
				break;
			}
			
		}
		
		sql.append(")VALUES(").append(StringUtils.stack("?,", index)).deleteCharAt(sql.length()-1).append(");");
		update(sql.toString(), tableName, params, titleList, db);
	}
	
	private static void update(String sql, String tableName, Object[] params, List<String> titleList, DbHelper db) throws SQLException {
		try {
			db.updateWithException(sql.toString(), params);
		} catch (SQLException e) {
			boolean flag = false;
			switch(e.getErrorCode()) {
				case 1017:
				case DbErrCode.MYSQL.TABLE_NOT_EXIST:
					flag = createTable(tableName, titleList, db);
					break;
				case DbErrCode.MYSQL.UNKNOWN_COLUMN:
					flag = createColumn(db, e, tableName);
					break;
				case DbErrCode.MYSQL.DUPLICATE_ENTRY:
					break;
				default:
					System.out.println(e.getErrorCode());
					break;
			}
			if(flag) {
				update(sql, tableName, params, titleList, db);
			} else {
				throw e;
			}
		}
	}
	
	/**
	 * 创建表
	 * @param tableName
	 * @param titleList
	 * @param db
	 * @return
	 */
	private static synchronized boolean createTable(String tableName, List<String> titleList, DbHelper db) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE `").append(tableName).append("` (");
		titleList.remove("id");
		sb.append("`id` int(11) NOT NULL AUTO_INCREMENT,");
		for (String title : titleList) {
			sb.append("`").append(title).append("` TEXT DEFAULT NULL,");
		}
		//多一个逗号没关系，因为还得设置主键
		sb.append(" PRIMARY KEY (`id`) ");
		sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		
		try {
			db.updateWithException(sb.toString());
			Console.log(StringUtils.concat("创建表[", tableName, "]"));
			return true;
		} catch (SQLException ex) {
			if(ex.getErrorCode() == DbErrCode.MYSQL.TABLE_ALREADY_EXISTS) {	//表已存在
				return true;
			}
			ex.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 创建字段
	 * @param db
	 * @param e sql异常,该方法会解析并取出其中的列名
	 * @param tableName
	 * @param titleList
	 * @return
	 */
	private static boolean createColumn(DbHelper db, SQLException e, String tableName) {
		String columnName = RegexUtils.find(e.getMessage(), "Unknown column\\s'(.+)'\\sin 'field list'","$1");
		return createColumn(db, columnName, tableName, null);
	}
	
	/**
	 * 创建字段
	 * @param db 
	 * @param columnName 列名
	 * @param tableName
	 * @param afterColName放在该字段后
	 * @return
	 */
	private static synchronized  boolean createColumn(DbHelper db, String columnName, String tableName, String afterColName) {
		if(columnName != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("ALTER TABLE `")
				.append(tableName)
				.append("` ADD COLUMN `")
				.append(columnName)
				.append("`  text NULL");
			if(afterColName != null) {
//				sb.append(" FIRST");
				sb.append(" AFTER `").append(afterColName).append("`");
			}
			sb.append(';');
			
			try {
				db.updateWithException(sb.toString());
				Console.log(StringUtils.concat("表[", tableName,"]增加列[",columnName,"]"));
				return true;
			} catch (SQLException ex) {
				if(ex.getErrorCode() == DbErrCode.MYSQL.COLUMN_ALREADY_EXISTS) {
					return true;
				}
				ex.printStackTrace();
			}
			
		}
		return false;
	}
	
}
