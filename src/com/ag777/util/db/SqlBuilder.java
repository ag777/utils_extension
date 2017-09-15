package com.ag777.util.db;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ag777.util.db.model.ColumnPojo;
import com.ag777.util.lang.model.Pair;

public class SqlBuilder {
	private String tableName;
	private List<Pair<ColumnPojo, String>> columnPairList;	//first为数据库中的字段信息，second为javabean中的变量名
	private Sql insert;
	private Sql update;
	private Sql create;
	

	public SqlBuilder(String tableName, List<Pair<ColumnPojo, String>> columnPairList) {
		super();
		this.tableName = tableName;
		this.columnPairList = columnPairList;
	}

	public Sql getInsert() {
		if(insert == null) {
			synchronized (SqlBuilder.class) {
				if(insert == null) {
					initInsertSql();
				}
			}
		}
		return insert;
	}
	
	public Sql getUpdate() {
		if(update == null) {
			synchronized (SqlBuilder.class) {
				if(update == null) {
					initUpdateSql();
				}
			}
		}
		return update;
	}
	
	public Sql getCreate() {
		if(create == null) {
			synchronized (SqlBuilder.class) {
				if(create == null) {
					initCreateTableSql();
				}
				
			}
		}
		return create;
	}
	
	/**
	 * 构造插入语句
	 * @param tableName
	 * @param columnNameList
	 */
	private void initInsertSql() {
		List<String> insertParamList = new ArrayList<>();
		StringBuilder sb_insert = new StringBuilder();
		sb_insert.append("INSERT INTO ").append(tableName).append(" (");
		for (Pair<ColumnPojo, String> item : columnPairList) {
			String colName = item.first.getName();
			sb_insert.append(' ').append(colName).append(',');
			insertParamList.add(item.second);
		}
		sb_insert.setLength(sb_insert.length()-1);
		sb_insert.append(" ) VALUES (");
		for(int i=0;i<columnPairList.size();i++) {
			sb_insert.append(" ?,");
		}
		sb_insert.setLength(sb_insert.length()-1);
		sb_insert.append(" );");
		insert =  new Sql(sb_insert.toString(), insertParamList);
	}
	
	/**
	 * 构造update语句
	 */
	private void initUpdateSql() {
		List<String> updateParamList = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		List<Pair<ColumnPojo, String>> pkList = new ArrayList<>();	//主键列表
		sb.append("UPDATE ").append(tableName).append(" SET");
		
		for (Pair<ColumnPojo, String> pair : columnPairList) {
			ColumnPojo columnPojo = pair.first;
			if(columnPojo.isPK()) {
				pkList.add(pair);
			} else {
				sb.append(" ").append(columnPojo.getName()).append("=?,");
				updateParamList.add(pair.second);
			}
		}
		if(",".equals(sb.charAt(sb.length()-1))) {
			sb.setLength(sb.length()-1);
		}
		sb.append(" WHERE");
		if(!pkList.isEmpty()) {
			for (Pair<ColumnPojo, String> pk : pkList) {
				sb.append(" ").append(pk.first.getName()).append("=? and");
				updateParamList.add(pk.second);
			}
			sb.setLength(sb.length()-4);
		}
		
		update = new Sql(sb.toString(), updateParamList);
	}
	
	/**
	 * 构造建表语句
	 */
	private void initCreateTableSql() {
		StringBuilder sb = new StringBuilder();
		List<String> pkList = new ArrayList<>();	//主键列表
		sb.append("CREATE TABLE ")
		.append(tableName)
		.append(" ( ");
		for (Pair<ColumnPojo, String> pair : columnPairList) {
			ColumnPojo columnPojo = pair.first;
			Long size = columnPojo.getSize();
			int sqlType = columnPojo.getSqlType();
			
			String sqlTypeStr = DbHelper.toString(sqlType);
			sb.append(columnPojo.getName());
			sb.append(" ").append(sqlTypeStr);
			if(size<=0) {
				if(DbHelper.isSqlTypeVarchar(sqlType)) {
					size = 255l;
				}
			}
			
			if(size>0) {
				sb.append("(").append(size).append(")");
			}
			if(columnPojo.isNotNull() || columnPojo.isPK()) {
				sb.append(" NOT NULL ");
			}
			
			if(columnPojo.isPK()) {
				if(Types.INTEGER == sqlType) {	//数值类型的主键自动增长
					sb.append(" AUTO_INCREMENT ");
				}
				pkList.add(columnPojo.getName());
			}
			sb.append(',');
		}
		if(!pkList.isEmpty()) {
			sb.append("PRIMARY KEY (");
			for (String pkName : pkList) {
				sb.append(pkName).append(",");
			}
			sb.setLength(sb.length()-1);
			sb.append(")");
		} else {
			sb.setLength(sb.length()-1);
		}
		sb.append(");");
		create =  new Sql(sb.toString(), null);
	}
	
	public class Sql {
		public String sql;
		public List<String> paramList;
		public Sql(String sql, List<String> paramList) {
			super();
			this.sql = sql;
			this.paramList = paramList;
		}
	}
}
