package com.ag777.util.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ag777.util.db.model.ColumnPojo;
import com.ag777.util.db.model.DBIPojo;
import com.ag777.util.db.model.TypePojo;
import com.ag777.util.lang.RegexUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.model.Pair;

public class SqlBuilder {
	private static String TAIL_CREATE = " ENGINE=InnoDB DEFAULT CHARSET=utf8";
	
	private String tableName;
	private List<Pair<ColumnPojo, String>> columnPairList;	//first为数据库中的字段信息，second为javabean中的变量名
	private List<DBIPojo> dbiList;	//索引列表
	private Sql insert;
	private Sql update;
	private Sql create;
	
	public SqlBuilder(String tableName, List<Pair<ColumnPojo, String>> columnPairList) {
		this.tableName = tableName;
		this.columnPairList = columnPairList;
		dbiList = ListUtils.newArrayList();
	}
	
	public SqlBuilder(String tableName, List<ColumnPojo> column, boolean isCamel) {
		List<Pair<ColumnPojo, String>> columnPairList = new ArrayList<>();
		for (ColumnPojo columnPojo : column) {
			Pair<ColumnPojo, String> pair = new Pair<>();
			String name = columnPojo.getName();
			if(isCamel) {
				name = StringUtils.underline2Camel(name, true);
			}
			pair.first = columnPojo;
			pair.second = name;
			columnPairList.add(pair);
		}
		this.tableName = tableName;
		this.columnPairList = columnPairList;
		dbiList = ListUtils.newArrayList();
	}

	public SqlBuilder setDbiList(List<DBIPojo> dbiList) {
		this.dbiList = dbiList;
		return this;
	}

	public Sql getInsertSql() {
		if(insert == null) {
			synchronized (SqlBuilder.class) {
				if(insert == null) {
					initInsertSql();
				}
			}
		}
		return insert;
	}
	
	public Sql getUpdateSql() {
		if(update == null) {
			synchronized (SqlBuilder.class) {
				if(update == null) {
					initUpdateSql();
				}
			}
		}
		return update;
	}
	
	public Sql getCreateSql() {
		if(create == null) {
			synchronized (SqlBuilder.class) {
				if(create == null) {
					initCreateTableSql();
				}
				
			}
		}
		return create;
	}
	
	public void doCreate(DbHelper helper) {
		String sql = getCreateSql().sql;
		helper.update(sql);
	}
	
	public int doInsert(Map<String, Object> paramsMap, DbHelper helper) {
		
		String sql = getInsertSql().sql;
		List<String> keyList = getInsertSql().paramList;
		Object[] params = new Object[keyList.size()];
		for(int i=0; i<keyList.size(); i++) {
			String key = keyList.get(i);
			params[i] = MapUtils.get(paramsMap, key);
		}
		return helper.update(sql, params);
	}
	
	public int[] doBatchInsert(List<Map<String, Object>> paramsMapList, DbHelper helper) {
		String sql = getInsertSql().sql;
		List<String> keyList = getInsertSql().paramList;
		List<Object[]> paramsList = new ArrayList<>();
		for (Map<String, Object> item : paramsMapList) {
			Object[] params = new Object[keyList.size()];
			for(int i=0; i<keyList.size(); i++) {
				String key = keyList.get(i);
				params[i] = MapUtils.get(item, key);
			}
			paramsList.add(params);
		}
		
		return helper.batchUpdate(sql, paramsList);
	}
	
	/*============内部方法=============*/
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
		Map<String, String> nameTypeMap = MapUtils.newHashMap();	//字段名:对应数据库类型
		
		for (Pair<ColumnPojo, String> pair : columnPairList) {
			ColumnPojo columnPojo = pair.first;
			TypePojo type = columnPojo.getTypePojo();
			sb.append(columnPojo.getName())
				.append(' ').append(type.getType()).append(' ');
			
			/*填充默认值*/
			if(columnPojo.getDef() != null) {
				if(DbHelper.isSqlTypeDate(columnPojo.getSqlType()) && "CURRENT_TIMESTAMP".equals(columnPojo.getDef())) {
					sb.append(" DEFAULT ")
						.append(columnPojo.getDef())
						.append(' ');
				} else {
					sb.append(" DEFAULT '")
						.append(columnPojo.getDef())
						.append("' ");
				}
			} else if(!columnPojo.isNotNull() && !columnPojo.isPK()) {
				sb.append(" DEFAULT NULL ");
			}
			
			/*填充[NOT NULL]*/
			if(columnPojo.isNotNull() || columnPojo.isPK()) {
				sb.append(" NOT NULL ");
			}
			
			/*填充自动其他信息,如自动增长*/
			if(type.getExtra() != null) {
				sb.append(type.getExtra().toUpperCase());
			} else if(columnPojo.isAutoIncrement()) {
				sb.append(" AUTO_INCREMENT ");
			}
			
			
			/*填充描述*/
			if(columnPojo.getRemarks() != null) {
				sb.append(" COMMENT '")
					.append(columnPojo.getRemarks())
					.append("' ");
			}
			
			/*添加至主键列表*/
			if(columnPojo.isPK()) {
				pkList.add(columnPojo.getName());
			}
			
			sb.append(',');
			
			nameTypeMap.put(columnPojo.getName(), type.getType().toUpperCase());
		}
		
		if(!pkList.isEmpty()) {
			sb.append("PRIMARY KEY (");
			for (String pkName : pkList) {
				sb.append(pkName).append(",");
			}
			sb.setLength(sb.length()-1);
			sb.append(")").append(",");
		} 
		
		if(!ListUtils.isEmpty(dbiList)) {
			for (DBIPojo dbi : dbiList) {
				String name = dbi.getName();
				if("PRIMARY".equals(name)) {
					continue;
				} else {
					List<String> columnNameList = dbi.getColumnNameList();
					for(int i=0; i<columnNameList.size(); i++) {
						String columnName = columnNameList.get(i);
						String type = nameTypeMap.get(columnName);
						if("TEXT".equals(type)) {
							columnNameList.set(i, columnName+"(255)");
						} else if(type.startsWith("VARCHAR")) {
							Integer size = RegexUtils.findInteger(type, "\\((\\d+)\\)", "$1");
							if(size > 255) {
								columnNameList.set(i, columnName+"(255)");
							}
						}
					}
					
					sb.append(" key ")
						.append(dbi.getName())
						.append(" (")
						.append(ListUtils.toString(dbi.getColumnNameList(), ","));
					if(dbi.getTypeName() != null) {
						sb.append(") USING ")
							.append(dbi.getTypeName());
					}
					sb.append(',');
				}
				
			}
		}
		
		sb.setLength(sb.length()-1);	//删除最后一个逗号
		
		sb.append(")");
		sb.append(TAIL_CREATE);
		sb.append(';');
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
