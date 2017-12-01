package com.ag777.util.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ag777.util.db.model.ColumnPojo;

/**
 * 数据库操作工具类
 * 
 * @author ag777
 * @version create on 2017年12月01日,last modify at 2017年12月01日
 */
public class DbUtils {

	private DbUtils() {}
	
	/**
	 * 复制数据库
	 * @param source
	 * @param target
	 * @return
	 * @throws Exception
	 */
	public static boolean copyDb(DbHelper source, DbHelper target) throws Exception {
		/**
		 * 思路:
		 * 1.取出数据库中所有的表(排除系统表)
		 * 2.在目标数据库中创建这些表(原来存在的表直接删掉)
		 * 3.取出原表数据,插入新表
		 */
		ArrayList<String> tableList = source.tableList();
		tableList.remove("meta");	//排除系统表
		
		for (String tableName : tableList) {	//创建表
			List<ColumnPojo> colList = source.columnList(tableName);
			SqlBuilder sb = new SqlBuilder(tableName, colList, false);
			target.update("DROP TABLE IF EXISTS "+tableName);
			sb.doCreate(target);
		}
		for (String tableName : tableList) {	//插入数据
			List<ColumnPojo> colList = source.columnList(tableName);
			SqlBuilder sb = new SqlBuilder(tableName, colList, false);
			if(!copyData(tableName, sb, source, target)) {
				System.out.println("异常");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 复制表内数据
	 * @param tableName
	 * @param sb
	 * @param helper
	 * @param helper2
	 * @return
	 * @throws Exception
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
}
