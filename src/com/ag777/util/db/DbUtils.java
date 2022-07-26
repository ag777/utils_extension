package com.ag777.util.db;

import com.ag777.util.db.model.ColumnPojo;
import com.ag777.util.db.model.DBIPojo;
import com.ag777.util.lang.collection.ListUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数据库操作工具类
 * 
 * @author ag777
 * @version create on 2017年12月01日,last modify at 2022年07月26日
 */
public class DbUtils {

	private DbUtils() {}

	private volatile static Consumer<String> onLog;

	public static void logOn(Consumer<String> onLog) {
		DbUtils.onLog = onLog;
	}

	public static void logOff() {
		DbUtils.onLog = null;
	}
	
	/**
	 * 复制数据库
	 * <p>
	 * 	数据源支持mysql数据库和sqlite数据库
	 * 	目标数据库仅支持mysql数据库
	 * </p>
	 * 
	 * @param source 数据源
	 * @param target 目标数据库
	 * @return 是否成功
	 * @throws Exception 异常
	 */
	public static boolean copyDb(DbHelper source, DbHelper target) throws Exception {
		/*
		 * 思路:
		 * 1.取出数据库中所有的表(排除系统表)
		 * 2.在目标数据库中创建这些表(原来存在的表直接删掉)
		 * 3.取出原表数据,插入新表
		 */
		List<String> tableList = source.tableNameList();
		tableList.remove("meta");	//排除系统表
		
		for (String tableName : tableList) {	//创建表
			log("创建表:"+tableName);
			List<ColumnPojo> colList = source.columnList(tableName);
			List<DBIPojo>dbiList = source.dbiList(tableName);
			if(source.isSqlite()) {
				colList = DbUtils.columnList_sqlite2Mysql(colList, dbiList);
			}
			
			SqlBuilder sb = new SqlBuilder(tableName, colList, false);
			sb.setDbiList(dbiList);
			target.update("DROP TABLE IF EXISTS "+tableName);
			log(sb.getCreateSql().sql);
			sb.doCreate(target);
		}
		System.out.println("创建表结束");
		
		for (String tableName : tableList) {	//插入数据
			List<ColumnPojo> colList = source.columnList(tableName);
			SqlBuilder sb = new SqlBuilder(tableName, colList, false);
			if(!copyData(tableName, sb, source, target)) {
				System.out.println(tableName+"异常");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 复制表内数据
	 * @param tableName 表名
	 * @param sb SqlBuilder
	 * @param source 数据源
	 * @param target 目标数据库
	 * @return 是否成功
	 * @throws Exception 异常
	 */
	private static boolean copyData(String tableName, SqlBuilder sb, DbHelper source, DbHelper target) throws Exception {
		System.out.println("开始复制表:"+tableName);
		
		List<Map<String, Object>> list = source.queryList("select * from "+tableName);
				
		return target.doTransaction(h->{
			for (Map<String, Object> map : list) {
				if(sb.doInsert(map, h) < 0) {
					return false;
				}
			}
			return true;
		});
	}
	
	public static List<ColumnPojo> columnList_sqlite2Mysql(List<ColumnPojo> columnList, List<DBIPojo> dbiList) {
		if(columnList == null) {
			return null;
		}
		List<ColumnPojo> result = ListUtils.newArrayList();
		List<String> dbiFieldList =  ListUtils.newArrayList();
		
		for (DBIPojo dbi : dbiList) {
			List<String> colNameList = dbi.getColumnNameList();
			for (String colName : colNameList) {
				if(!dbiFieldList.contains(colName)) {
					dbiFieldList.add(colName);
				}
			}
		}
		
		for (ColumnPojo col : columnList) {
			ColumnPojo item = col.clone();
			if(item.getDef() != null && item.getDef() instanceof String) {
				item.setDef(((String)item.getDef()).replaceAll("(?<!\\\\)'", ""));	//去除单引号(前面没跟转义符\  <-不知道有没卵用)
			}
			String typeName = item.getTypeName();
			if(typeName != null) {
				typeName = typeName.replaceAll("(?<=\\s).*$", "");	//chrome数据库出现了INTEGER NON类型，并不属于基础类型，后来发现sqlite的类型可以乱输..emm...
				switch(typeName) {
					case "INTEGER":
						typeName = "BIGINT";
						break;
					case "LONGVARCHAR":
						//BLOB/TEXT column 'term' used in key specification without a key length
						typeName = "text";
						break;
					case "VARCHAR":
						if(item.isPK() != null && item.isPK()) {	//主键//Specified key was too long; max key length is 767 bytes
							typeName = "VARCHAR(255)";
						} else {
							typeName = "VARCHAR(1024)";
						}
						break;
					case "BLOB":
						item.setDef(null);
						break;
					default:
						break;
				}
			}
			item.setTypeName(typeName);
			if(item.getTypePojo() != null) {
				item.getTypePojo().setType(typeName);
			}
			
			result.add(item);
		}
		return result;
	}
	
	private static void log(String msg) {
		if(onLog != null) {
			onLog.accept(msg);
		}
	}
}
