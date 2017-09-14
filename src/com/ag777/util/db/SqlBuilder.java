package com.ag777.util.db;

import java.util.List;

import com.ag777.util.db.model.ColumnPojo;
import com.ag777.util.lang.model.Pair;

public class SqlBuilder {
	private String tableName;
	private List<Pair<ColumnPojo, String>> columnPairList;	//first为数据库中的字段信息，second为javabean中的变量名
	private String insert;
	private String create;

	public SqlBuilder(String tableName, List<Pair<ColumnPojo, String>> columnPairList) {
		super();
		this.tableName = tableName;
		this.columnPairList = columnPairList;
	}

	public String getInsert() {
		if(insert == null) {
			synchronized (SqlBuilder.class) {
				if(insert == null) {
					insert = initInsertSql();
				}
			}
		}
		return insert;
	}
	
	public String getCreate() {
		if(create == null) {
			synchronized (SqlBuilder.class) {
				if(create == null) {
					create = initCreateTableSql();
				}
				
			}
		}
		return create;
	}
	
	/**
	 * 获取插入语句
	 * @param tableName
	 * @param columnNameList
	 * @return
	 */
	public String initInsertSql() {
		StringBuilder sb_insert = new StringBuilder();
		sb_insert.append("INSERT INTO ").append(tableName).append(" (");
		for (Pair<ColumnPojo, String> item : columnPairList) {
			String colName = item.first.getName();
			sb_insert.append(' ').append(colName).append(',');
		}
		sb_insert.setLength(sb_insert.length()-1);
		sb_insert.append(" ) VALUES (");
		for(int i=0;i<columnPairList.size();i++) {
			sb_insert.append(" ?,");
		}
		sb_insert.setLength(sb_insert.length()-1);
		sb_insert.append(" );");
		return sb_insert.toString();
	}
	
	
	public String initCreateTableSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ")
		.append(tableName)
		.append(" ( ");
		for (Pair<ColumnPojo, String> pair : columnPairList) {
			String sqlTypeStr = DbHelper.toString(pair.first.getSqlType());
			sb.append(pair.first.getName());
			if(pair.first.getSize()>0) {
				sb.append("(").append(pair.first.getSize()).append(")");
			}
			
			sb.append(',');
		}
		sb.setLength(sb.length()-1);
		sb.append(");");
		return sb.toString();
	}
}
