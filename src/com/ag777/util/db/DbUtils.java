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
		ArrayList<String> tableList = source.tableList();
		for (String tableName : tableList) {	//创建表
			if("meta".equals(tableName)) {
				continue;
			}
			List<ColumnPojo> colList = source.columnList(tableName);
			SqlBuilder sb = new SqlBuilder(tableName, colList, false);
			target.update("DROP TABLE IF EXISTS "+tableName);
			sb.doCreate(target);
		}
		for (String tableName : tableList) {	//插入数据
			if("meta".equals(tableName)) {
				continue;
			}
			List<ColumnPojo> colList = source.columnList(tableName);
			SqlBuilder sb = new SqlBuilder(tableName, colList, false);
			if(!copyData(tableName, sb, source, target)) {
				System.out.println("异常");
				return false;
			}
		}
		return true;
	}
	
	private static boolean copyData(String tableName, SqlBuilder sb, DbHelper helper, DbHelper helper2) throws Exception {
		System.out.println("开始复制表:"+tableName);
		
		List<Map<String, Object>> list = helper.queryList("select * from "+tableName);
				
		return helper2.doTransaction(h->{
			for (Map<String, Object> map : list) {
				if(sb.doInsert(map, h) < 0) {
					return false;
				}
			}
			return true;
		});
	}
}
