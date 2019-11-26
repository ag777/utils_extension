package com.ag777.util.file.excel.model;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * excel逐行读取辅助工具类
 * 
 * @author ag777
  * @version create on 2019年11月20日,last modify at 2019年11月20日
 */
@FunctionalInterface
public interface ExcelLineReader {

	public void readLine(Sheet sheet, int sheetNo, Row row, int rowNo, List<String> colList);
}
