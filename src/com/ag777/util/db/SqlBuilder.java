package com.ag777.util.db;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ag777.util.db.model.ColumnPojo;
import com.ag777.util.db.model.DBIPojo;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.model.Pair;

public class SqlBuilder {
	private final static Long sqlLiteColumnSize = 2000000000l;
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
		for (Pair<ColumnPojo, String> pair : columnPairList) {
			ColumnPojo columnPojo = pair.first;
			Integer size = columnPojo.getSize();
			int sqlType = columnPojo.getSqlType();
			
			
			sb.append(columnPojo.getName());
			
			if(size != null && !size.equals(sqlLiteColumnSize)) {
				String sqlTypeStr = DbHelper.toString(sqlType, size);
				sb.append(" ").append(sqlTypeStr);
				if(Types.LONGVARCHAR != sqlType && !DbHelper.isSqlTypeDate(sqlType)) {	//text和日期类型类型不加大小限制
					if(size<=0) {
						if(DbHelper.isSqlTypeVarchar(sqlType)) {
							size = 255;
						}
					}
					
					if(size>0) {
						Integer decimalDigits = columnPojo.getDecimalDigits();	//小数精度
						sb.append("(").append(size);
						if(decimalDigits != null && 0 != decimalDigits) {
							sb.append(",").append(decimalDigits);
						}
						sb.append(")");
					}
				}
				
			} else {	//sqllite数据库导出的数据
				
				switch(sqlType) {
					case Types.VARCHAR:
						sb.append(" text");
						break;
					case Types.INTEGER:
						sb.append(" bigint");
						break;
					default:
						String sqlTypeStr = DbHelper.toString(sqlType, 0);
						sb.append(" ").append(sqlTypeStr);
						break;
				}
				
			}

			if(columnPojo.isNotNull() || columnPojo.isPK()) {
				sb.append(" NOT NULL ");
			}
			
			if(columnPojo.isAutoIncrement()) {
				sb.append(" AUTO_INCREMENT ");
			}
			if(columnPojo.isPK()) {
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
			sb.append(")").append(",");
		} 
		
		if(!ListUtils.isEmpty(dbiList)) {
			for (DBIPojo dbi : dbiList) {
				String name = dbi.getName();
				if("PRIMARY".equals(name)) {
					continue;
				} else {
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
