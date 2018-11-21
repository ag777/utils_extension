package com.ag777.util.file.excel;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;

/**
 * excel表格修改辅助类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>poi-xxx.jar</li>
 * <li>commons-codec-xx.jar</li>
 * <li>xmlbeans-2.6.0.jar</li>
 * <li>commons-collections4-4.1.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年11月21日,last modify at 2018年11月21日
 */
public interface ExcelModifyHelper {
	
	/**
	 * 
	 * @param filePath
	 * @param helper
	 * @throws EncryptedDocumentException
	 * @throws IllegalArgumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static void modify(String filePath,ExcelModifyHelper helper) throws EncryptedDocumentException, IllegalArgumentException, InvalidFormatException, IOException {
		Workbook wb = null;
		try {
			wb = ExcelReadUtils.getWorkBook(filePath);
			if(!helper.walk(wb)) {
				return;
			}
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				Sheet sheet = wb.getSheetAt(i);
				if(!helper.walk(sheet, i)) {
					continue;
				}
				for(int j=sheet.getFirstRowNum(); j<=sheet.getLastRowNum(); j++) {
					Row row = sheet.getRow(j);
					if(row == null) {
						continue;
					}
					if(!helper.walk(row, j, sheet, i)) {
						continue;
					}
					for(int k=row.getFirstCellNum(); k<=row.getLastCellNum();k++) {
						Cell cell = row.getCell(k);
						Object val = getValue(cell);
						helper.walk(val, cell, k, row, j, sheet, i);
					}
				}
				
			}
			ExcelWriteHelper.write(FileUtils.getOutputStream(filePath), wb);
		} finally {
			IOUtils.close(wb);	//关闭流，防止文件被占用
		}
		
	}
	
	/**
	 * 获取单元格中的数据,可能是字符串类型，也可能是数值型,甚至是boolean类型
	 * @param cell
	 * @return
	 */
	public static Object getValue(Cell cell) {
		if(cell == null){
			return null;
		}	
		if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
			return cell.getBooleanCellValue();
		} else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
			Double num = cell.getNumericCellValue();
			if(num.longValue() == num){
				num.longValue();
			}
			return num;
		} else {
			return cell.getStringCellValue();
		}
	}
	
	default boolean walk(Workbook wb) {
		return true;
	}
	
	public boolean walk(Sheet sheet, int index);
	public boolean walk(Row row, int rowNum, Sheet sheet, int sheetNum);
	public void walk(Object val, Cell cell, int cellNum, Row row, int rowNum, Sheet sheet, int sheetNum);
	
}
