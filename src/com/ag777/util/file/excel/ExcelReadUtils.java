package com.ag777.util.file.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.collection.CollectionAndMapUtils;

/**
 * excel文件读取工具类
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
 * @version last modify at 2017年09月27日
 */
public class ExcelReadUtils {

	public static Workbook getWorkBook(String filePath) throws EncryptedDocumentException, InvalidFormatException, IOException {
		if (filePath == null || "".equals(filePath)) {
			throw new IOException("文件名为空");
		} else {
			return WorkbookFactory.create(new File(filePath));
		}
	}
	
	public static Workbook getWorkBook(InputStream inputStream) throws EncryptedDocumentException, InvalidFormatException, IOException {
		return WorkbookFactory.create(inputStream);
	}
	
	
	public static List<List<Map<String, String>>> read(String filePath, boolean isIgnoreFirstRow) throws EncryptedDocumentException, InvalidFormatException, IOException {
		if (filePath == null || "".equals(filePath)) {
			return null;
		} else {
			List<List<Map<String, String>>> sheetList = CollectionAndMapUtils.newArrayList();
			Workbook workBook = WorkbookFactory.create(new File(filePath));
			Iterator<Sheet> itorSheet = workBook.sheetIterator();
			while(itorSheet.hasNext()) {
				List<Map<String, String>> rowList = CollectionAndMapUtils.newArrayList();
				Sheet sheet = itorSheet.next();
				Iterator<Row> itorRow = sheet.rowIterator();
				while(itorRow.hasNext()) {
					Map<String, String> rowMap = CollectionAndMapUtils.newLinkedHashMap();
					Row row = itorRow.next();
					if(isIgnoreFirstRow && row.getRowNum() == 0) {
						continue;
					}
					Iterator<Cell> itorCell = row.cellIterator();
					while(itorCell.hasNext()) {
						Cell cell = itorCell.next();
						char key = (char)('a' + cell.getColumnIndex());
						String value = getValue(cell);
						rowMap.put(String.valueOf(key), value);
					}
					rowList.add(rowMap);
				}
				sheetList.add(rowList);
			}
			
			return sheetList;
		}
	}
	
	/**
	 * 读取excel文件
	 * @param filePath 				excel文件路径
	 * @param sheetTitles			转为map时对应的key
	 * @param isIgnoreFirstRow	是否忽略第一行(有时候第一行是标题栏)
	 * @return 路径为空或者为null时返回null
	 * @throws IOException
	 * @throws InvalidFormatException 
	 * @throws EncryptedDocumentException 
	 */
	public static List<List<Map<String, String>>> read(String filePath, List<List<String>> sheetTitles, boolean isIgnoreFirstRow) throws IOException, EncryptedDocumentException, InvalidFormatException {//读取excel,不论什么版本
		Workbook workBook = WorkbookFactory.create(new File(filePath));
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	public static List<List<Map<String, String>>> read(String filePath, String[][] sheetTitles, boolean isIgnoreFirstRow) throws IOException, EncryptedDocumentException, InvalidFormatException {//读取excel,不论什么版本
		Workbook workBook = WorkbookFactory.create(new File(filePath));
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	public static List<Map<String, String>> read(String filePath, String[] titles, boolean isIgnoreFirstRow) throws IOException, EncryptedDocumentException, InvalidFormatException {//读取excel,不论什么版本
		Workbook workBook = WorkbookFactory.create(new File(filePath));
		List<List<Map<String, String>>> list = readWorkBook(workBook, titles, isIgnoreFirstRow);
		if(CollectionAndMapUtils.isEmpty(list)) {
			return CollectionAndMapUtils.newArrayList();
		}
		return list.get(0);
	}
	
	/**
	 * 读取工作簿,用的是抽象类的方法,不区分版本(已去除空数据行)
	 * @param workBook
	 * @param sheetTitleList
	 * @param isIgnoreFirstRow
	 * @return
	 */
	public static List<List<Map<String, String>>> readWorkBook(Workbook workBook, List<List<String>> sheetTitleList, boolean isIgnoreFirstRow) {
		/**
		 * 二维列表转二维数组
		 */
		String[][] sheetTitles = null;
		if(sheetTitleList != null) {
			sheetTitles = new String[sheetTitleList.size()][];
			for (int i = 0; i < sheetTitleList.size(); i++) {
				List<String> titleList = sheetTitleList.get(i);
				String[] titles = new String[titleList.size()];
				for (int j = 0; j < titleList.size(); j++) {
					String title = titleList.get(j);
					titles[j] = title;
				}
				sheetTitles[i] = titles;
			}
			
		}
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	public static List<List<Map<String, String>>> readWorkBook(Workbook workBook, String[] titles, boolean isIgnoreFirstRow) {
		String[][] sheetTitles = new String[][] {titles};
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	/**
	 * 读取工作簿,用的是抽象类的方法,不区分版本
	 * @param workBook
	 * @param sheetTitles
	 * @param isIgnoreFirstRow
	 * @return
	 */
	public static List<List<Map<String, String>>> readWorkBook(Workbook workBook, String[][] sheetTitles, boolean isIgnoreFirstRow) {
		List<List<Map<String, String>>> sheetList = new ArrayList<List<Map<String, String>>>();	//最终结果集
		// Read the Sheet
		for (int numSheet=0; numSheet < workBook.getNumberOfSheets(); numSheet++) {
			if(sheetTitles.length < numSheet+1) {	//定义标题超出页数则结束遍历
				break;
			}
			String[] titles = sheetTitles[numSheet];
			List<Map<String, String>> rows = new ArrayList<Map<String,String>>();	//每个sheet中的内容
			
			Sheet sheet = workBook.getSheetAt(numSheet);
			if (sheet == null) {
				continue;
			}
			
			int rowNum = 0;
			if(isIgnoreFirstRow) {	//忽略第一行则从第二行开始读
				rowNum = 1;
			}
			
			// Read the Row
			for (; rowNum <= sheet.getLastRowNum(); rowNum++) {
				
				Row row = sheet.getRow(rowNum);
				if (row != null) {	
					Map<String, String> item = new HashMap<String, String>();	//每行中的内容
					boolean flag = false;	//排除空行
					int maxColNum = row.getLastCellNum();
					for(int index=0; index<titles.length; index++) {
						String title = titles[index];
						if(index >= maxColNum) {	//标题项目数大于excel中当前行的列数
							item.put(title, null);
						}else {
							Cell xssfCell = row.getCell(index);
							String value = getValue(xssfCell);
							if(value != null) {
								value = value.trim();
								if(!value.isEmpty()) {	//一行当中只要有一个单元格数据不为空则视为有效行(非空行)
									flag = true;
								}
							}
							item.put(title, value);
							
						}
					}//每个标题(titles)或者说列(col)遍历完成
					if(flag) {	//排除空行
						rows.add(item);
					}
				}
				
			}	//行(row)遍历完成
			sheetList.add(rows);
		}	//sheet遍历完成
		
		IOUtils.close(workBook);	//关闭流，防止文件被占用
		
		return sheetList;
	}
	
	/**
	 * 获取单元格里面的值
	 * @param cell
	 * @return
	 */
	public static String getValue(Cell cell) {
		if(cell == null){
			return null;
		}	
		if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
			Double num = cell.getNumericCellValue();
			if(num.longValue() == num){
				return String.valueOf(num.longValue());
			}
			return String.valueOf(num);
		} else {
			return String.valueOf(cell.getStringCellValue());
		}
	}
	
	/** 
     * 判断是否为excel2007及以上 
	 * @throws IOException 
	 * @throws InvalidFormatException 
     */  
    public static  boolean isExcel2007(String filePath) throws IOException, InvalidFormatException {
    	
    	InputStream is = null;
        try {  
        	is = new FileInputStream(filePath);
        	return isExcel2007(is);
        } catch (IOException ex) {  
        	throw ex; 
        } catch (InvalidFormatException ex) {
        	throw ex; 
		}  finally {
        	IOUtils.close(is);
        }
        
    }  
    
    /**
     * 判断是否为excel2007及以上 
     * @param is
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    @SuppressWarnings("deprecation")
	public static  boolean isExcel2007(InputStream is) throws IOException, InvalidFormatException {
    	// If clearly doesn't do mark/reset, wrap up  
        if(! is.markSupported()) {  
        	is = new PushbackInputStream(is, 8);  
        }  
    	POIFSFileSystem.hasPOIFSHeader(is);
    	if(POIFSFileSystem.hasPOIFSHeader(is)) {  
            return false;  
        }  
    	if(POIXMLDocument.hasOOXMLHeader(is)) {  
    		//Excel版本为excel2007及以上  
    		return true;
        }  
    	throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream"); 
    }
}
