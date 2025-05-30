package com.ag777.util.db.model;

/**
 * SQLException 返回的错误码
 * 
 * @author ag777
 * @version create on 2018年05月07日,last modify at 2018年08月27日
 */
public class DbErrCode {

	public static class MYSQL {
		// 找不到表(没权限)-Can't find file: '%s.frm' (errno: 22 - Invalid argument)
		public final static int CANT_READ_FILE = 1017;
		// 表已存在(建表时)-Table '%s' already exists
		public final static int TABLE_ALREADY_EXISTS=1050;
		// 缺少列-Unknown column '%s' in 'field list'
		public final static int UNKNOWN_COLUMN= 1054;
		// 列已存在(建列时)-Duplicate column name '%s'
		public final static int COLUMN_ALREADY_EXISTS= 1060;
		// 主键重复-Duplicate entry '%s' for key 'PRIMARY'
		public final static int DUPLICATE_ENTRY = 1062;
		// 表不存在-Table '%s' doesn't exist
		public final static int  TABLE_NOT_EXIST= 1146;
		// 数据太长-Data truncation: Data too long for column '%s' at row %d
		public final static int DATA_TOO_LONG = 1406;
	}

	public static class DM {
		// 查询/插入表记录时
		public final static int TABLE_NOT_EXIST = -1010;
		// 无效的表名 '%s'
		// 创建表时
		public final static int TABLE_NAME_INVALID = -1008;
		// 对象 '%s' 已存在
		// drop表时
		public final static int TABLE_ALREADY_EXISTED = -1032;
		// 违反唯一性约束（INDEX33555574）
		// 插入数据时
		public final static int INDEX_DUPLICATE = -3102;
	}

	// https://www.sqlite.org/rescode.html?spm=5176.28103460.0.0.40f75d27UJk1r6
	public static class SQLITE {

	}
}
