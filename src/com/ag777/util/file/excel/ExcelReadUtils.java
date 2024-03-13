package com.ag777.util.file.excel;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.exception.Assert;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * excel文件读取工具类
 * <p>
 * <a href="https://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/">官方文档</a>
 * <a href="https://segmentfault.com/a/1190000012165530">3.17版本更新内容</a>
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>poi-xxx.jar</li>
 * <li>commons-codec-xx.jar</li>
 * <li>xmlbeans-2.6.0.jar</li>
 * <li>commons-collections4-4.1.jar</li>
 * </ul>
 * </p>
 * </p>
 *
 * <p>
 * 当我们只要使用xls格式时、只要导入poi-version-yyyymmdd.jar就可以了。<br>
 当我们还要使用xlsx格式、还要导入poi-ooxml-version-yyyymmdd.jar。<br>
 至于poi-ooxml-schemas-version-yyyymmdd.jar这个jar基本不太会用到的。<br>
 当我们需要操作word、ppt、viso、outlook等时需要用到poi-scratchpad-version-yyyymmdd.jar。<br>
 参考:https://www.cnblogs.com/zhangchengbing/p/6340036.html
 * </p>
 *
 * @author ag777
 * @version last modify at 2024年03月13日
 */
public class ExcelReadUtils {

	/**
	 * 读取指定路径的Excel文件，将其内容解析为一个LinkedHashMap，其中键是Sheet的名称，值是包含每个Sheet中所有行数据的列表。
	 * 每行数据被表示为一个列表，其中包含该行中每个单元格的值。此方法支持读取加密的文档。
	 *
	 * @param filePath 要读取的Excel文件的路径。
	 * @return 一个LinkedHashMap，键是Sheet的名称，值是每个Sheet中所有行的数据列表。
	 * @throws EncryptedDocumentException 如果尝试读取的Excel文档被加密。
	 * @throws IllegalArgumentException 如果文件路径不正确或无法解析。
	 * @throws IOException 如果发生I/O错误，例如文件不存在或无法读取。
	 */
	public static LinkedHashMap<String, List<List<Object>>> read(String filePath) throws EncryptedDocumentException, IllegalArgumentException, IOException {
        try (Workbook workbook = getWorkBook(filePath)) {
            return readWorkbook(workbook);
        }
    }

	/**
	 * 从提供的输入流中读取Excel数据，并将其内容解析为一个LinkedHashMap。此LinkedHashMap的键为Sheet的名称，
	 * 值为一个列表，该列表包含每个Sheet中所有行的数据。每行数据再以一个列表的形式表示，其中包含该行中每个单元格的值。
	 * 此方法在读取完毕后自动关闭输入流。
	 *
	 * @param inputStream 要读取的Excel数据的输入流。
	 * @return 一个LinkedHashMap，其键为Sheet的名称，值为每个Sheet中所有行的数据列表。
	 * @throws EncryptedDocumentException 如果尝试读取的Excel文档被加密。
	 * @throws IllegalArgumentException 如果输入流不正确或无法解析。
	 * @throws IOException 如果发生I/O错误，如读取过程中的输入输出异常。
	 */
	public static LinkedHashMap<String, List<List<Object>>> read(InputStream inputStream) throws EncryptedDocumentException, IllegalArgumentException, IOException {
        try (Workbook workbook = getWorkBook(inputStream)) {
            return readWorkbook(workbook);
        }
    }

	/**
	 * 读取给定的Workbook对象，将其内容转换为一个LinkedHashMap，其中键是Sheet的名称，值是包含每个Sheet中所有行数据的列表。
	 * 每行数据被表示为一个列表，其中包含该行中每个单元格的值。
	 *
	 * @param workbook 要读取的Workbook对象。
	 * @return 一个LinkedHashMap，键是Sheet的名称，值是每个Sheet中所有行的数据列表。
	 */
	public static LinkedHashMap<String, List<List<Object>>> readWorkbook(Workbook workbook) {
		LinkedHashMap<String, List<List<Object>>> sheetDataMap = new LinkedHashMap<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			Sheet sheet = workbook.getSheetAt(i);
			List<List<Object>> rowDataList = readSheet(sheet);
			sheetDataMap.put(sheet.getSheetName(), rowDataList);
		}
		return sheetDataMap;
	}

	/**
	 * 读取给定的Sheet对象，将其内容转换为一个列表，该列表包含了Sheet中每一行的数据。
	 * 每行数据被表示为一个列表，其中包含该行中每个单元格的值。
	 *
	 * @param sheet 要读取的Sheet对象。
	 * @return 一个列表，其中包含了给定Sheet中每一行的数据，每行数据也是一个列表，包含了该行中每个单元格的值。
	 */
	public static List<List<Object>> readSheet(Sheet sheet) {
		List<List<Object>> rowDataList = new ArrayList<>();
		for (Row row : sheet) {
			List<Object> cellDataList = new ArrayList<>();
			for (Cell cell : row) {
				Object cellValue = getStrValue(cell);
				cellDataList.add(cellValue);
			}
			rowDataList.add(cellDataList);
		}
		return rowDataList;
	}

	/**
	 * 从给定的Sheet读取数据，将每行数据映射到一个Map中，其中键是列标题，值是对应单元格的数据。
	 * 可以选择是否忽略第一行（通常是标题行）。
	 *
	 * @param sheet 要读取的Excel工作表对象。
	 * @param titles 列标题数组，用于作为Map中的键。
	 * @param isIgnoreFirstRow 是否忽略第一行数据。如果为true，则跳过第一行；如果为false，则从第一行开始读取。
	 * @return 一个List，包含Map，每个Map代表一行数据，键为列标题，值为对应的单元格数据。
	 */
	public static List<Map<String, String>> readSheet(Sheet sheet, String[] titles, boolean isIgnoreFirstRow) {
		List<Map<String, String>> rowDataList = new ArrayList<>(sheet.getLastRowNum());
		for (Row row : sheet) {
			// 跳过第一行
			if (isIgnoreFirstRow) {
				isIgnoreFirstRow = false;
				continue;
			}
			//每行中的内容
			Map<String, String> item = new HashMap<>();
			//排除空行
			boolean flag = false;
			int maxColNum = row.getLastCellNum();
			for(int index=0; index<titles.length; index++) {
				String title = titles[index];
				// 标题项目数大于excel中当前行的列数
				if(index >= maxColNum) {
					item.put(title, null);
				}else {
					Cell xssfCell = row.getCell(index);
					String value = getStrValue(xssfCell);
					if(value != null) {
						value = value.trim();
						//一行当中只要有一个单元格数据不为空则视为有效行(非空行)
						if(!value.isEmpty()) {
							flag = true;
						}
					}
					item.put(title, value);

				}
			}
			// 每个标题(titles)或者说列(col)遍历完成
			// 排除空行
			if(flag) {
				rowDataList.add(item);
			}
		}
		return rowDataList;
	}

	/**
	 * 获取工作簿对象
	 * @param filePath 文件路径
	 * @return 工作簿
	 * @throws IllegalArgumentException 参数异常
	 * @throws EncryptedDocumentException 解密工作簿异常
	 * @throws IOException 读取异常
	 */
	public static Workbook getWorkBook(String filePath) throws IllegalArgumentException, EncryptedDocumentException, IOException {
		Assert.notBlank(filePath, "文件路径不能为空");
		return getWorkBook(FileUtils.getInputStream(filePath));
	}

	/**
	 * 获取工作簿对象
	 * @param inputStream 流
	 * @return 工作簿
	 * @throws IllegalArgumentException 参数异常
	 * @throws EncryptedDocumentException 解密工作簿异常
	 * @throws IOException 读取异常
	 */
	public static Workbook getWorkBook(InputStream inputStream) throws IllegalArgumentException, EncryptedDocumentException, IOException {
		Assert.notNull(inputStream, "文件流不能为空");
		try {
			return WorkbookFactory.create(inputStream);
		} finally {	//测试直接关闭输入流是不影响后续读取的,而且如果想保存回源文件必须先关闭输入流停止文件占用
			IOUtils.close(inputStream);
		}
	}

	/**
	 * 移除角标对应的sheet
	 * @param wb 工作簿
	 * @param index sheet下标
	 * @throws IllegalArgumentException 角标超出范围等等
	 */
	public static void removeSheet(Workbook wb, int index) throws IllegalArgumentException {
		wb.removeSheetAt(index);
	}

	/**
	 * 获取单元格里面的值
	 * @param cell 单元格
	 * @return 单元格中的内容
	 */
	public static String getStrValue(Cell cell) {
		if(cell == null){
			return null;
		}
		String result;
		if (cell.getCellType() == CellType.BOOLEAN) {
			result =  String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == CellType.NUMERIC || cell.getCellTypeEnum() == CellType.FORMULA) {
			Double num = cell.getNumericCellValue();
			if(num.longValue() == num){
				result = String.valueOf(num.longValue());
			} else {
				result = String.valueOf(num);
			}
		} else {
			result =  cell.getStringCellValue();
		}
		return !StringUtils.isEmpty(result)?result:null;
	}

	/**
	 * 获取单元格中的数据,可能是字符串类型，也可能是数值型,甚至是boolean类型
	 * @param cell 单元格
	 * @return 单元格
	 */
	public static Object getObjectValue(Cell cell) {
		if(cell == null){
			return null;
		}
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue();
				} else {
					return cell.getNumericCellValue();
				}
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case FORMULA:
				return cell.getCellFormula();
			default:
				return null;
		}
	}
	
	/**
     * 判断是否为excel2007及以上
	 * @throws IOException 读取异常
     */
    public static boolean isExcel2007(String filePath) throws IOException {

    	InputStream is = null;
        try {
        	is = Files.newInputStream(Paths.get(filePath));
        	return isExcel2007(is);
        } finally {
        	IOUtils.close(is);
        }

    }

    /**
     * @param is 输入流
     * @return 是否为excel2007及以上(是否是xlsx)
     * @throws IOException 读取异常
     */
	public static  boolean isExcel2007(InputStream is) throws IOException {
    	// If clearly doesn't do mark/reset, wrap up
		// 关于该方法:https://blog.csdn.net/jektonluo/article/details/49588673
		if(!is.markSupported()) {
        	is = new PushbackInputStream(is, 8);
        }
        /*
         * 官方文档:
         * Get the file magic of the supplied InputStream (which MUST support mark and reset).
			If unsure if your InputStream does support mark / reset, use prepareToCheckMagic(InputStream) to wrap it and make sure to always use that, and not the original!
			Even if this method returns UNKNOWN it could potentially mean, that the ZIP stream has leading junk bytes
         */
        FileMagic fileMagic = FileMagic.valueOf(FileMagic.prepareToCheckMagic(is));
        /*
         * 官方文档:
         * 	hasOOXMLHeader(java.io.InputStream inp)
			Deprecated.
         * in 3.17-beta2, use FileMagic.valueOf(InputStream) == FileMagic.OOXML instead
         */
        if(fileMagic == FileMagic.OOXML) {
    		//Excel版本为excel2007及以上
    		return true;
        } else if(fileMagic == FileMagic.OLE2) {
        	return false;
        }

    	throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
    }
   
}
