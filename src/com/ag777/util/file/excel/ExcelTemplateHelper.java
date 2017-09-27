package com.ag777.util.file.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;

/**
 * excel内容替换工具
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
 * @version create on 2017年09月27日,last modify at 2017年09月27日
 */
public class ExcelTemplateHelper {
	
	private Workbook workBook;
	private Sheet curSheet;
	private String filePath;
	private InputStream inputStream;
	
	private ExcelTemplateHelper(Workbook workBook, InputStream inputStream, String filePath) {
		this.workBook = workBook;
		this.inputStream = inputStream;
		this.filePath = filePath;
		curSheet = workBook.getSheetAt(0);
	}
	
	public static ExcelTemplateHelper load(String filePath) throws EncryptedDocumentException, InvalidFormatException, IOException {
		FileInputStream inputStream = new FileInputStream(new File(filePath));
		Workbook workBook = ExcelReadUtils.getWorkBook(inputStream);
		return new ExcelTemplateHelper(workBook, inputStream, filePath);
	}
	
	public void write(String filePath) throws FileNotFoundException, Exception {
		IOUtils.close(inputStream);
		ExcelWriteHelper.write(FileUtils.getOutputStream(filePath), workBook);
	}
	
	public void write() throws FileNotFoundException, Exception {
		IOUtils.close(inputStream);
		ExcelWriteHelper.write(FileUtils.getOutputStream(filePath), workBook);
	}
	
	public ExcelTemplateHelper replaceAll(Map<String, Object> params) {
		Iterator<Row> itor = curSheet.rowIterator();
		while(itor.hasNext()) {
			Row row = itor.next();
			Iterator<Cell> itorCell = row.cellIterator();
			while(itorCell.hasNext()) {
				Cell cell = itorCell.next();
				String content = cell.getStringCellValue();
				Iterator<String> itorMap = params.keySet().iterator();
				while(itorMap.hasNext()) {
					String key = itorMap.next();
					String value = params.get(key)!=null?params.get(key).toString():"";
					content = content.replace(key, value);	//不用正则
					cell.setCellValue(content);
				}
			}
		}
		return this;
	}
	
}
