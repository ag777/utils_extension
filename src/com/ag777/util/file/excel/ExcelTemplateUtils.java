package com.ag777.util.file.excel;

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
public class ExcelTemplateUtils {
	
	private ExcelTemplateUtils(){}
	
	/**
	 * 遍历每一行每一列,替换excel文件中的内容
	 * @param filePath
	 * @param params
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 */
	public static void replaceAll(String filePath, Map<String, Object> params) throws IOException, EncryptedDocumentException, InvalidFormatException {
		InputStream in = FileUtils.getInputStream(filePath);
		Workbook workBook = ExcelReadUtils.getWorkBook(in);
		Sheet sheet = workBook.getSheetAt(0);
		
		Iterator<Row> itor = sheet.rowIterator();
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
		IOUtils.close(in);
		ExcelWriteHelper.write(FileUtils.getOutputStream(filePath), workBook);
		IOUtils.close(workBook);
	}
	
}
