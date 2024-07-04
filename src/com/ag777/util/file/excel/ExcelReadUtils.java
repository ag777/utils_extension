package com.ag777.util.file.excel;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.Assert;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

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
 * @version last modify at 2024年07月04日
 */
public class ExcelReadUtils {

	/**
	 * 从Excel的第一个工作表中读取数据，并返回一个包含数据的Map列表。
	 *
	 * 此方法专门处理Excel文件的第一个工作表，将其中的数据转换为Map形式的列表。
	 * 每个Map代表一行数据，键值对对应于列标题和对应的单元格值。
	 *
	 * @param file 要读取的Excel文件，可以是任何版本的Excel文件。
	 * @param keys 表示Excel列的标题数组，用于映射数据行中的值到Map的键。
	 * @param skipRowCount 要跳过的行数，通常用于忽略Excel中的标题行或其他不需要处理的行。
	 * @return 返回一个List，其中每个元素是一个Map，代表Excel中的一行数据。
	 * @throws IOException 如果读取文件时发生错误。
	 * @throws EncryptedDocumentException 如果尝试读取加密的Excel文件。
	 */
	public static List<Map<String, String>> readFirstSheet(File file, String[] keys, int skipRowCount) throws IOException, EncryptedDocumentException {
		// 根据文件获取Workbook对象，支持多种Excel格式。
		Workbook workBook = getWorkBook(file);
		// 从第一个工作表中读取数据，跳过指定的行数，并使用指定的列标题映射数据。
		return readRows(workBook.getSheetAt(0), keys, skipRowCount);
	}

	/**
	 * 根据文件获取Excel工作簿对象。
	 * 此方法封装了从文件获取输入流并创建工作簿的过程。
	 *
	 * @param file 需要被转换为工作簿的Excel文件。
	 * @return 返回一个代表Excel工作簿的Workbook对象。
	 * @throws IllegalArgumentException 如果文件参数为空或无效。
	 * @throws EncryptedDocumentException 如果文件被加密。
	 * @throws IOException 如果在读取文件时发生IO错误。
     */
	public static Workbook getWorkBook(File file) throws IllegalArgumentException, EncryptedDocumentException, IOException {
		// 通过FileUtils的getInputStream方法获取文件输入流，为创建Workbook准备。
		return getWorkBook(FileUtils.getInputStream(file));
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
	 * 获取sheet名称列表
	 * @param wb 工作簿
	 * @return sheet名称列表
	 */
	public static List<String> getSheetNameList(Workbook wb) {
		List<String> list = ListUtils.newArrayList();
		Iterator<Sheet> itor = wb.sheetIterator();
		while(itor.hasNext()) {
			list.add(itor.next().getSheetName());
		}
		return list;
	}

	/**
	 * 根据指定的条件查找工作簿中的表单。
	 *
	 * 通过迭代工作簿中的所有表单，使用提供的谓词对每个表单进行测试，直到找到满足条件的表单。
	 * 如果找到满足条件的表单，则返回包含该表单的Optional对象；如果没有找到满足条件的表单，则返回空的Optional对象。
	 *
	 * @param workbook 要搜索的工作簿对象，不能为null。
	 * @param predicate 用于测试表单是否满足特定条件的谓词，不能为null。
	 * @return 包含找到的表单的Optional对象，如果没有找到满足条件的表单，则返回空的Optional对象。
	 */
	public static Optional<Sheet> findSheet(Workbook workbook, Predicate<Sheet> predicate) {
		// 初始化工作簿的表单迭代器
		Iterator<Sheet> itor = workbook.sheetIterator();
		// 遍历工作簿中的所有表单
		while(itor.hasNext()) {
			Sheet sheet = itor.next();
			// 使用提供的谓词测试当前表单是否满足条件
			if (predicate.test(sheet)) {
				// 如果满足条件，返回包含该表单的Optional对象
				return Optional.ofNullable(sheet);
			}
		}
		// 如果没有找到满足条件的表单，返回空的Optional对象
		return Optional.empty();
	}

	/**
	 * 从Excel的Sheet中读取数据，并将其转换为List<Map<String, String>>格式。
	 * 每个Map代表一行数据，键值对对应于指定的keys和相应的单元格值。
	 *
	 * @param sheet 要读取数据的Sheet对象。
	 * @param keys 用于映射Excel列和Map键的字符串数组。
	 * @param skipRowCount 要跳过的行数，通常用于忽略标题行或注释行。
	 * @return 返回一个包含所有数据行的List，每行数据是一个Map。
	 */
	public static List<Map<String, String>> readRows(Sheet sheet, String[] keys, int skipRowCount) {
		// 读取sheet中的所有数据行，跳过指定行数的头部
		List<List<String>> rows = readRows(sheet, skipRowCount, true);
		// 初始化一个List来存储所有转换后的数据行
		List<Map<String, String>> datas = new ArrayList<>(rows.size());
		// 遍历每一行数据
		for (List<String> row : rows) {
			// 为每一行数据创建一个Map来存储键值对
			Map<String, String> map = new HashMap<>(keys.length);
			// 遍历keys数组，将每个key和对应位置的cell值添加到map中
			for (int i = 0; i < keys.length; i++) {
				map.put(keys[i], ListUtils.get(row, i));
			}
			// 将填充好的map添加到datas列表中
			datas.add(map);
		}
		return datas;
	}



	/**
	 * 从Excel的Sheet中读取数据，返回一个二维字符串列表。
	 * 每个子列表代表一行数据，子列表中的字符串代表该行的每个单元格值。
	 *
	 * @param sheet 要读取数据的Sheet对象。
	 * @param skipRowCount 跳过的起始行数，用于忽略某些不需要处理的头部行。
	 * @param skipEmptyRow 是否跳过空行。如果设置为true，则空行将被忽略；如果设置为false，则空行会被填充为包含空列表的行。
	 * @return 返回一个二维字符串列表，包含从Sheet中读取的所有数据。
	 */
	public static List<List<String>> readRows(Sheet sheet, int skipRowCount, boolean skipEmptyRow) {
		// 起始读取的行号，跳过前面的skipRowCount行
		int rowNum = skipRowCount;
		// 获取Sheet的总行数
		int totalRowCount = sheet.getLastRowNum();
		// 初始化存储数据的列表，预设总行数以提高性能
		List<List<String>> rows = new ArrayList<>(totalRowCount);
		// 遍历Sheet中的每一行
		for (; rowNum <= totalRowCount; rowNum++) {
			// 获取当前行对象
			Row row = sheet.getRow(rowNum);
			// 读取当前行的数据，返回一个Optional对象
			Optional<List<String>> rowDatas = readRow(row);
			// 如果当前行数据存在，则将其添加到结果列表中
			if (rowDatas.isPresent()) {
				rows.add(rowDatas.get());
			} else if (!skipEmptyRow) {
				// 如果当前行为空但不跳过空行，则将空列表添加到结果中
				rows.add(Collections.emptyList());
			}
		}
		// 返回包含所有行数据的列表
		return rows;
	}

	/**
	 * 从给定的Row对象中读取一行数据，并以字符串列表的形式返回。
	 * 这个方法处理了行中可能存在的空单元格，确保返回的列表中每一项都有值。
	 *
	 * @param row Excel表格中的行对象，可能为null。
	 * @return 包含该行所有单元格数据的字符串列表的Optional对象。
	 *         如果行为空，则返回空的Optional；如果行不为空，但所有单元格都为空，则返回包含空字符串列表的Optional。
	 */
	public static Optional<List<String>> readRow(Row row) {
		// 检查行对象是否为空
		if (row != null) {
			// 初始化标志变量，用于判断当前行是否有非空单元格(排除空行)
			boolean flag = false;
			short colCount = row.getLastCellNum();
			// 初始化用于存储行数据的列表，大小等于当前行的列数
			List<String> rowDatas = new ArrayList<>(colCount);
			// 遍历当前行的每一个单元格
			for (short i = 0; i < colCount; i++) {
				// 获取当前列的单元格对象
				Cell xssfCell = row.getCell(i);
				// 尝试读取单元格的值，并以Optional形式返回
				Optional<String> value = readCellForStr(xssfCell);
				// 如果单元格的值存在
				if (value.isPresent()) {
					// 标记当前行存在非空单元格
					flag = true;
					// 将单元格的值添加到行数据列表中
					rowDatas.add(value.get());
				} else {
					// 如果单元格的值不存在，添加空字符串到行数据列表中
					rowDatas.add("");
				}
			}
			// 如果当前行存在非空单元格，返回包含行数据的Optional
			if (flag) {
				return Optional.of(rowDatas);
			}
		}
		// 如果行对象为空，或所有单元格都为空，返回空的Optional
		return Optional.empty();
	}


	/**
	 * 读取单元格的内容，并根据单元格的类型转换为相应的字符串表示。
	 *
	 * @param cell 要读取的单元格对象，可以为null。
	 * @return 返回包含单元格内容的Optional字符串。如果单元格为null或内容为空，则返回空的Optional。
	 */
	public static Optional<String> readCellForStr(Cell cell) {
		Optional<Object> val = readCellForObj(cell);
		if (!val.isPresent()) {
			return Optional.empty();
		}
		Object o = val.get();
		if (o instanceof FormulaCellValue) {
			o = ((FormulaCellValue) o).getValue();
		} else if (o instanceof Date) {
			o = ((Date) o).getTime();
		}
		String strVal = o.toString().trim();
		// 去除结果字符串的前后空格，并检查是否为空，为空则返回空的Optional，否则返回处理后的结果
		return !StringUtils.isEmpty(strVal)? Optional.of(strVal) : Optional.empty();
	}

	/**
	 * 获取单元格中的数据,可能是字符串类型，也可能是数值型,甚至是boolean类型
	 * @param cell 单元格
	 * @return 单元格
	 */
	public static Optional<Object> readCellForObj(Cell cell) {
		if(cell == null){
			return Optional.empty();
		}
		switch (cell.getCellType()) {
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return Optional.ofNullable(cell.getDateCellValue());
				} else {
					return Optional.of(cell.getNumericCellValue());
				}
			case BOOLEAN:
				return Optional.of(cell.getBooleanCellValue());
			case FORMULA:
				return Optional.of(new FormulaCellValue(cell.getStringCellValue(), cell.getCellFormula()));
			default:
				return Optional.ofNullable(cell.getStringCellValue());
		}
	}

	/**
	 * 判断是否为excel2007及以上
	 * @throws IOException 读取异常
	 * @throws IllegalArgumentException 工作簿格式不正确
	 */
	public static  boolean isExcel2007(String filePath) throws IOException, IllegalArgumentException {
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
	 * @throws IllegalArgumentException 工作簿格式不正确
	 */
	public static  boolean isExcel2007(InputStream is) throws IOException, IllegalArgumentException {
		// If clearly doesn't do mark/reset, wrap up
		if(!is.markSupported()) {  //关于该方法:https://blog.csdn.net/jektonluo/article/details/49588673
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

	/**
	 * 表示公式单元格值的类。
	 * 该类用于存储单元格的计算公式及其对应的显示值。
	 */
	public static class FormulaCellValue {
	    private String value; // 单元格的显示值
	    private String formula; // 单元格的计算公式

	    public FormulaCellValue(String value, String formula) {
	        this.value = value;
	        this.formula = formula;
	    }

	    public String getValue() {
	        return value;
	    }

	    public void setValue(String value) {
	        this.value = value;
	    }

	    public String getFormula() {
	        return formula;
	    }

	    public void setFormula(String formula) {
	        this.formula = formula;
	    }
	}
   
}
