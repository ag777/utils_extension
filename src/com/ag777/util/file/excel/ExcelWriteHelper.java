package com.ag777.util.file.excel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.ag777.util.lang.IOUtils;

/**
 * @Description excel写入工具(样式请在外部定义)
 * @author wanggz
 * Time: last modify at 2017/08/28.
 */
public class ExcelWriteHelper {
	
	private Workbook workBook;
	private Sheet curSheet;
	private Drawing<?> drawing;	//画图的顶级管理器，一个sheet只能获取一个（一定要注意这点） 
	
	
	private int index;
	
	
	public ExcelWriteHelper() {
		workBook = new HSSFWorkbook();
		createSheet();
	}
	
	public ExcelWriteHelper(String curSheetName) {
		workBook = new HSSFWorkbook();
		createSheet(curSheetName);
	}
	
	/**
	 * 游标置0
	 */
	public void initIndex() {
		index = 0;
	}
	
	/*-----输出------*/
	/**
	 * 将excel文件写出到输出流中,并关闭输出流
	 * @param os
	 * @throws Exception 
	 */
	public void write(OutputStream os) throws Exception {
		try {
			workBook.write(os);
		} catch (Exception e) {
			throw e;
		} finally {
			if(os != null) {
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * 销毁对象
	 */
	public void dispose() {
		IOUtils.close(workBook);
		workBook = null;
	}
	
	/*-----------页面相关-----------------*/
	/**
	 * 创建新页面
	 */
	public void createSheet() {
		createSheet(null);
	}

	/**
	 * 创建新页面
	 * @param curSheetName 页面名称(显示在左下角)
	 */
	public ExcelWriteHelper createSheet(String curSheetName) {
		if(curSheetName == null) {
			curSheet = workBook.createSheet();
		} else {
			curSheet = workBook.createSheet(curSheetName);
		}
		
		drawing = curSheet.createDrawingPatriarch();
		initIndex();		//游标一开始指向第一行
		return this;
	}
	
	/**
	 * 设置列宽
	 * @param colWidths
	 * @return
	 */
	public ExcelWriteHelper columnWidth(int[] colWidths) {
		//设置列宽
		for (short i=0;i<colWidths.length; i++) {
			curSheet.setColumnWidth(i,colWidths[i]); 
		}
		return this;
	}
	
	/*---------------每页内容相关----------------------*/
	
	/**
	 * 横表
	 * @param dataList
	 * @param keys
	 * @param titles
	 * @param titleStryle
	 * @param contentStyle
	 * @return 
	 */
	public ExcelWriteHelper createTableHorizontal(
			List<Map<String, Object>> dataList,
			String[] keys,
			String[] titles,
			CellStyle titleStyle,
			CellStyle[] contentStyles) {
		if(dataList == null) {
			dataList = new ArrayList<>();
		}
		
		if(keys == null && !dataList.isEmpty()) {
			Map<String, Object> firstMap = dataList.get(0);
			if(!firstMap.isEmpty()) {
				keys = new String[firstMap.size()];
				int i=0;
				Iterator<String> itor = firstMap.keySet().iterator();
				while(itor.hasNext()) {
					keys[i] = itor.next();
					i++;
				}
			}
		}
		
		if(titles != null && titles.length == 0) {	//如果标题为空，则令titles和keys相同
			titles = keys;
		}
		
		/*参数处理结束*/
		
		int rowNumStart = index;	//先记录表格初始行的位置
		int rowNumLast = index+dataList.size()-(titles==null?1:0);	//结束行的位置
		int colNumLast = keys==null?(titles==null?0:titles.length-1):keys.length-1;	//结束列数，-1是因为计数从0开始
		
		if (titles != null) {
			// 创建行头
			Row row0 = createRow();
			// 创建第一行的列
			for (int i = 0, length = titles.length; i < length; i++) {
				Cell cell = row0.createCell(i);
				if(titleStyle != null) {
					cell.setCellStyle(titleStyle);
				}
				cell.setCellValue(new HSSFRichTextString(titles[i]));
			}
		}
		// 创建数据行
		if (keys  != null) {
			for (int k=0, size = dataList.size(); k < size; k++) {	//k为dataList的下标,i代表多少行,j代表多少列
				
				Map<String, Object> rowObj = dataList.get(k);
				
				// 创建行
				Row row = createRow();
				// 创建行的列
				if (keys != null) {
					for (int j = 0, length = keys.length; j < length; j++) {
						Cell cell = row.createCell(j);
						
						CellStyle contentStyle = (j<contentStyles.length)?contentStyles[j]:contentStyles[contentStyles.length-1];
						
						if(contentStyle != null) {
							cell.setCellStyle(contentStyle);
						}
						
						if(rowObj.containsKey(keys[j]) && rowObj.get(keys[j])!=null){
							cell.setCellValue(new HSSFRichTextString(rowObj
									.get(keys[j]).toString()));
						}else{
							cell.setCellValue("");
						}
					}

				}
			}
		}
		
		createTableBorder(rowNumStart, rowNumLast, 0, colNumLast);
		return this;
	}
	
	
	
	/**
	 * 横表(仅一条数据行)
	 * @param dataMap
	 * @param keys
	 * @param titles
	 * @param titleStyle
	 * @param contentStyle
	 * @return
	 */
	public ExcelWriteHelper createTableHorizontal(
			Map<String, Object> dataMap,
			String[] keys,
			String[] titles,
			CellStyle titleStyle,
			CellStyle contentStyle) {
		/*参数处理*/
		
		if(dataMap == null) {
			dataMap = new HashMap<>();
		}
		
		if(keys == null && !dataMap.isEmpty()) {
			keys = new String[dataMap.size()];
			int i=0;
			Iterator<String> itor = dataMap.keySet().iterator();
			while(itor.hasNext()) {
				keys[i] = itor.next();
				i++;
			}
		}
		
		if(titles != null && titles.length == 0) {	//如果标题为空，则令titles和keys相同
			titles = keys;
		}
		
		/*参数处理结束*/
		
		int rowNumStart = index;	//先记录表格初始行的位置
		int rowNumLast = index+1-(titles==null?1:0);	//结束行的位置
		int colNumLast = keys==null?(titles==null?0:titles.length-1):keys.length-1;	//结束列数，-1是因为计数从0开始
		
		if (titles != null) {
			// 创建行头
			Row row0 = createRow();
			// 创建第一行的列
			for (int i = 0, length = titles.length; i < length; i++) {
				Cell cell = row0.createCell(i);
				if(titleStyle != null) {
					cell.setCellStyle(titleStyle);
				}
				cell.setCellValue(new HSSFRichTextString(titles[i]));
			}
		}
		// 创建数据行
		if (keys  != null) {
			
				
			Map<String, Object> rowObj = dataMap;
			
			// 创建行
			Row row = createRow();
			// 创建行的列
			if (keys != null) {
				for (int j = 0, length = keys.length; j < length; j++) {
					Cell cell = row.createCell(j);
					
					if(contentStyle != null) {
						cell.setCellStyle(contentStyle);
					}
					
					if(rowObj.containsKey(keys[j]) && rowObj.get(keys[j])!=null){
						cell.setCellValue(new HSSFRichTextString(rowObj
								.get(keys[j]).toString()));
					}else{
						cell.setCellValue("");
					}
				}

			}

				
		}
		
		createTableBorder(rowNumStart, rowNumLast, 0, colNumLast);
		return this;
	}
	
	/**
	 * 纵表
	 * @param dataList
	 * @param keys
	 * @param titles
	 * @param titleStryle
	 * @param contentStyle
	 * @return 
	 */
	public ExcelWriteHelper createTableVertical(
			List<Map<String, Object>> dataList,
			String[] keys,
			String[] titles,
			CellStyle titleStyle,
			CellStyle[] contentStyles) {
		/*参数处理*/
		if(dataList == null) {
			dataList = new ArrayList<>();
		}
		
		if(keys == null && !dataList.isEmpty()) {
			Map<String, Object> firstMap = dataList.get(0);
			if(!firstMap.isEmpty()) {
				keys = new String[firstMap.size()];
				int i=0;
				Iterator<String> itor = firstMap.keySet().iterator();
				while(itor.hasNext()) {
					keys[i] = itor.next();
					i++;
				}
			}
		}
		
		if(titles != null && titles.length == 0) {	//如果标题为空，则令titles和keys相同
			titles = keys;
		}
		
		/*参数处理结束*/
		
		int rowNumStart = index;	//先记录表格初始行的位置
		int rowNumLast = keys==null?index:index+keys.length-1;	//结束行的位置
		
		int k = 0;	//每列的游标
		
		if(titles != null) {	//写标题
			for (int i = 0; i < titles.length; i++) {
				Row row = createRow();
				createCell(row, 0, titles[i], titleStyle);
			}
			k++;	//标题占了一列
		}
		
		//生成列
		if(keys != null) {
			//遍历数据列表，再通过键取出对应的值，获取对应的行生成对应的列，按列插入数据
			for (int i = 0; i < dataList.size(); i++) {	//生成列数
				Map<String, Object> rowObj = dataList.get(i);
				
				for (int j = 0; j < keys.length; j++) {	//行数
					Row row = curSheet.getRow(rowNumStart+j);	//如果当前行没被创建则返回null
					if(row == null ) {	
						row = createRow();
					}
					
					CellStyle contentStyle = (j<contentStyles.length)?contentStyles[j]:contentStyles[contentStyles.length-1];
					
					if(rowObj.containsKey(keys[j])) {
						createCell(row, k+i, rowObj.get(keys[j]), contentStyle);
					} else {
						createCell(row, k+i, "", contentStyle);
					}
				}
	
			}
		}
		
		createTableBorder(rowNumStart, rowNumLast, 0, k+dataList.size()-1);
		return this;
		
	}
	
	/**
	 * 纵表
	 * @param dataList
	 * @param keys
	 * @param titles
	 * @param titleStryle
	 * @param contentStyle
	 * @return 
	 */
	public ExcelWriteHelper createTableVertical(
			List<Map<String, Object>> dataList,
			String[] keys,
			String[] titles,
			CellStyle titleStyle,
			CellStyle contentStyle) {
		
		return createTableVertical(dataList,
				keys,
				titles,
				titleStyle,
				new CellStyle[]{contentStyle});
	}
	
	/**
	 * 纵表(一个数据列)
	 * @param dataMap
	 * @param keys
	 * @param titles
	 * @param titleStyle
	 * @param contentStyle
	 * @return
	 */
	public ExcelWriteHelper createTableVertical(
			Map<String, Object> dataMap,
			String[] keys,
			String[] titles,
			CellStyle titleStyle,
			CellStyle contentStyle) {
		/*参数处理*/
		
		if(dataMap == null) {
			dataMap = new HashMap<>();
		}
		
		if(keys == null) {
			Map<String, Object> firstMap = dataMap;
			if(!firstMap.isEmpty()) {
				keys = new String[firstMap.size()];
				int i=0;
				Iterator<String> itor = firstMap.keySet().iterator();
				while(itor.hasNext()) {
					keys[i] = itor.next();
					i++;
				}
			}
		}
		
		if(titles != null && titles.length == 0) {	//如果标题为空，则令titles和keys相同
			titles = keys;
		}
		
		/*参数处理结束*/
		
		int rowNumStart = index;	//先记录表格初始行的位置
		int rowNumLast = keys==null?index:index+keys.length-1;
		
		int k = 0;	//每列的游标
		
		if(titles != null) {	//写标题
			for (int i = 0; i < titles.length; i++) {
				Row row = createRow();
				createCell(row, 0, titles[i], titleStyle);
			}
			k++;	//标题占了一列
		}
		
		
		//生成列
		if(keys != null) {
			//遍历数据列表，再通过键取出对应的值，获取对应的行生成对应的列，按列插入数据
			Map<String, Object> rowObj = dataMap;
			
			for (int j = 0; j < keys.length; j++) {	//行数
				
				Row row = curSheet.getRow(rowNumStart+j);	//如果当前行没被创建则返回null
				if(row == null ) {	
					row = createRow();
				}
				
				if(rowObj.containsKey(keys[j])) {
					createCell(row, k, rowObj.get(keys[j]), contentStyle);
				} else {
					createCell(row, k, "", contentStyle);
				}
			}
	
		}
		
		createTableBorder(rowNumStart, rowNumLast, 0, k);
		return this;
	}
	
	/**
	 * 纵表(仅一条数据行)
	 * @param datas
	 * @param titles
	 * @param titleStyle
	 * @param contentStyle
	 * @return
	 */
	public ExcelWriteHelper createTableVertical(
			Object[] datas,
			String[] titles,
			CellStyle titleStyle,
			CellStyle[] contentStyles) {
		/*参数处理*/
		
		if(datas == null) {
			datas = new Object[]{};
		}
		
		/*参数处理结束*/
		
		int rowNumStart = index;	//先记录表格初始行的位置
		int rowNumLast = index+datas.length-1;
		
		int k = 0;	//每列的游标
		
		if(titles != null) {	//写标题
			for (int i = 0; i < titles.length; i++) {
				Row row = createRow();
				createCell(row, 0, titles[i], titleStyle);
			}
			k++;	//标题占了一列
		}
		
		//写数据列
		if(datas.length>0) {
			for (int i = 0; i < datas.length; i++) {
				Row row = curSheet.getRow(rowNumStart+i);	//如果当前行没被创建则返回null
				if(row == null ) {	
					row = createRow();
				}
				CellStyle contentStyle = (i<contentStyles.length)?contentStyles[i]:contentStyles[contentStyles.length-1];
				createCell(row, k, datas[i], contentStyle);
			}
		}
		
		createTableBorder(rowNumStart, rowNumLast, 0, k);
		return this;
	}
	
	/**
	 * 插入图片,执行失败不会报错，只会输出错误信息
	 * @param filePath	图片路径
	 * @param width	宽度占多少列
	 * @param height	高度占多少行
	 */
	public void loadImage(String filePath, int width, int height) {
		try {
			InputStream is = new FileInputStream(filePath);  
//			byte[] bytes = IOUtils.toByteArray(is);  
			  
			BufferedImage img = javax.imageio.ImageIO.read(is);
			
            //imageIO转byte数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
			javax.imageio.ImageIO.write(img, "png", out);//IOUtils.toByteArray(img.);  
			byte[] bytes = out.toByteArray();
//			System.out.println(bytes.length);
			
			// 增加图片到 Workbook  
			int pictureIdx = workBook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);  
			is.close();
			  
			CreationHelper helper = workBook.getCreationHelper();
			//add a picture shape  
			ClientAnchor anchor = helper.createClientAnchor() ; 
			//set top-left corner of the picture,  
			//subsequent call of Picture#resize() will operate relative to it  
			// 设置图片位置  
			anchor.setCol1(0);  
			anchor.setRow1(index);
			anchor.setDx1(0);
			anchor.setDy1(0);
			anchor.setCol2(width);
			anchor.setRow2(index+height);
			anchor.setDx2(0);
			anchor.setDy2(0);
	
			drawing.createPicture(anchor, pictureIdx);   
			index += height;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 创建带sheet内超链接的行
	 * @param content
	 * @param style
	 * @param link
	 * @return
	 */
	public Row createLinkRow(Object content, CellStyle style, Hyperlink link) {
		Row row = createRow();
		createCell(row, 0, content, style, link);
		return row;
	}
	
	/**
	 * 获取sheet内超链接
	 * @param column	A-Z
	 * @param row	>=0
	 * @return
	 */
	public Hyperlink getLink(char column, int row) {
		HyperlinkType type = HyperlinkType.DOCUMENT;
		CreationHelper createHelper = workBook.getCreationHelper();
		Hyperlink link = createHelper.createHyperlink(type);
		link.setAddress(
				new StringBuilder()
					.append("#")
					.append(curSheet.getSheetName())
					.append("!")
					.append(column)
					.append(row)
					.toString());  
		return link;
	}
	
	/**
	 * 创建只有一列的行
	 * @param content
	 * @param style
	 * @return 
	 */
	public Row createRow(Object content, CellStyle style) {
		Row row = createRow();
		createCell(row, 0, content, style);
		return row;
	}
	
	public Row createRowMerged(Object content, CellStyle style, int col) {
		Row row = createRowMerged(col);
		createCell(row, 0, content, style);
		return row;
	}
	
	/**
	 * 创建一个数据行,一行都采用相同的样式
	 * @param contentList
	 * @param style
	 */
	public void createRow(List<Object> contentList, CellStyle style) {
		Row row = createRow();
		for(int i=0; i<contentList.size(); i++) {
			createCell(
					row,
					i,
					contentList.get(i),
					style);
		}
	}
	
	/**
	 * 创建一个数据行,一行都采用相同的样式
	 * @param contents
	 * @param style
	 */
	public void createRow(Object[] contents, CellStyle style) {
		Row row = createRow();
		for(int i=0; i<contents.length; i++) {
			createCell(
					row,
					i,
					contents[i],
					style);
		}
	}
	
	/**
	 * 空1行
	 */
	public void skipRow() {
		index++;
	}
	
	/**
	 * 空rowNum行
	 * @param rowNum
	 */
	public void skipRow(int rowNum) {
		index += rowNum;
	}
	
	/**
	 * 创建空行并合并0-col列
	 * @param col
	 * @return
	 */
	public Row createRowMerged(int col) {
		Row row = createRow();
		CellRangeAddress range = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, col);
		curSheet.addMergedRegion(range);
		return row;
	}
	
	/**
	 * 创建空行
	 * @return 
	 */
	public Row createRow() {
		Row row = curSheet.createRow(index);
//		row.setHeightInPoints(ExcelConstant.ROW_HEIGHT);	//行高
		index++;
		return row;
	}
	
	
	/*---------------内部用方法------------------------*/
	
	
	/**
	 * 创建单个单元格
	 * @param cell
	 * @param content
	 * @param style
	 */
	public static Cell createCell(Row row, int colNum, Object content, CellStyle style) {
		Cell cell = row.createCell(colNum);
		if(style != null) {
			cell.setCellStyle(style);
		}
		if(content != null) {
			cell.setCellValue(new HSSFRichTextString(content.toString()));
		} else {
			cell.setCellValue("");
		}
		return cell;
	}
	
	/**
	 * 创建一个带超链接的单元格
	 * @param row
	 * @param colNum
	 * @param content
	 * @param style
	 * @param link
	 * @return
	 */
	private static Cell createCell(Row row, int colNum, Object content, CellStyle style, Hyperlink link) {
		Cell cell = createCell(row, colNum, content, style);
		if(link != null) {
			cell.setHyperlink(link);
		}
		
		return cell;
	}
	
	/**
	 * 为表格创建边框
	 * @param firstRow	第一行行数
	 * @param lastRow	最后一行行数
	 * @param firstCol	第一列列数
	 * @param lastCol	最后一列列数
	 */
	private void createTableBorder(int firstRow, int lastRow, int firstCol, int lastCol) {
		if(lastRow < firstRow || lastCol < firstCol) {
			return;
		}
		for(int row = firstRow; row<=lastRow; row++) {
			CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, row, firstCol, lastCol);
			RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, curSheet); 
		}
		
		for(int col = firstCol; col<=lastCol; col++) {
			CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, col);
			RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, curSheet);  
		}
		CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		
		RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress, curSheet);  
//		RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, curSheet);  
		RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, curSheet); 
//		RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, curSheet);  
	}
	
	
	
}
