package com.ag777.util.file.excel;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.util.Optional;

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
	 * 修改指定路径的Excel文件。此方法通过传入的ExcelModifyHelper辅助对象，对Workbook进行遍历修改。
	 * ExcelModifyHelper提供了几个重载的walk方法，允许在不同级别（Workbook、Sheet、Row、Cell）上自定义操作。
	 *
	 * @param filePath Excel文件的路径。
	 * @param helper ExcelModifyHelper对象，定义了在遍历Excel时各级别的自定义操作。
	 * @throws EncryptedDocumentException 如果尝试读取的Excel文档被加密。
	 * @throws IllegalArgumentException 如果文件路径不正确或无法解析。
	 * @throws IOException 如果发生I/O错误。
	 */
	static void modify(String filePath,ExcelModifyHelper helper) throws EncryptedDocumentException, IllegalArgumentException, IOException {
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
						Optional<Object> val = ExcelReadUtils.readCellForObj(cell);
						helper.walk(val, cell, k, row, j, sheet, i);
					}
				}
				
			}
			ExcelWriteHelper.write(FileUtils.getOutputStream(filePath), wb);
		} finally {
			// 关闭流，防止文件被占用
			IOUtils.close(wb);
		}
		
	}
	
	default boolean walk(Workbook wb) {
		return true;
	}
	
	public boolean walk(Sheet sheet, int index);
	public boolean walk(Row row, int rowNum, Sheet sheet, int sheetNum);
	public void walk(Optional<Object> val, Cell cell, int cellNum, Row row, int rowNum, Sheet sheet, int sheetNum);
	
}
