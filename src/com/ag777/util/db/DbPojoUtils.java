package com.ag777.util.db;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ag777.util.db.model.ColumnPojo;
import com.ag777.util.db.model.interf.Column;
import com.ag777.util.db.model.interf.Id;
import com.ag777.util.db.model.interf.Table;
import com.ag777.util.lang.model.Pair;
import com.ag777.util.lang.reflection.PackageUtils;
import com.ag777.util.lang.reflection.ReflectionUtils;


/**
 * 
 * @author ag777
 * 共存亡: SqlBuilder.java, Column.java,Id.java, Table.java
 */
public class DbPojoUtils {
	
	/**
	 * 遍历包，获取包下的所有带table注释的类，并配套对应的sqlbuilder
	 * @param packageName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Map<Class<?>, SqlBuilder>  getSqlMapByPackage(String packageName) throws IOException, ClassNotFoundException {
		Map<Class<?>, SqlBuilder> result = new HashMap<>();
		List<String> list = PackageUtils.getClassesInPackage(packageName);
		for (String className : list) {
			Class<?> clazz = Class.forName(className);
			if(clazz.isAnnotationPresent(Table.class)) {
				result.put(clazz, getSqlBuilder(clazz));
			}
		}
		return result;
	}
	
	/**
	 * 通过解析类中的数据库注释，构造sqlBuilder对象并返回,如果对象不含table注释则返回null
	 * @param clazz
	 * @return
	 */
	public static SqlBuilder getSqlBuilder(Class<?> clazz) {
		if(clazz.isAnnotationPresent(Table.class)) {
			Table table = clazz.getAnnotation(Table.class);
			
			List<Field> fieldList = ReflectionUtils.getFieldListByAnnotation(clazz, Column.class);
			List<Pair<ColumnPojo, String>> colPairList = new ArrayList<>();
			for (Field field : fieldList) {
				Pair<ColumnPojo, String> pair = new Pair<>();
				Column column = field.getAnnotation(Column.class);
				pair.first = new ColumnPojo();
				pair.first.setName(column.name());
				pair.first.setSqlType(DbHelper.toSqlType(field.getType()));	//将字段类型转为数据类型
				pair.first.setSize(column.size());
				if(field.isAnnotationPresent(Id.class)) {
					pair.first.isNotNull(true);
					pair.first.isPK(true);
				}
				
				pair.second = field.getName();
				colPairList.add(pair);
			}
			
			
			return new SqlBuilder(table.name(), colPairList);
		}
		return null;
	}
}
