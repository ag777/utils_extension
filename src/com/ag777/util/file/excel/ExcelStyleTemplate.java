package com.ag777.util.file.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * excel表格基础样式模板构造工具类
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
 * @version create on 2017年09月06日,last modify at 2017年09月06日
 */
public class ExcelStyleTemplate {
	
	private ExcelStyleTemplate() {}
	
	/**
	 * 一级标题
	 * @param workBook
	 * @return
	 */
	public static CellStyle h1(Workbook workBook) {
		CellStyle style = basic(workBook);
		style.setAlignment(HorizontalAlignment.LEFT);
		
		Font font = basicFont(workBook);
		font.setFontHeightInPoints((short) 22);//设置字体大小
		font.setBold(true);
		
		style.setFont(font);
		return style;
	}
	
	/**
	 * 二级标题
	 * @param workBook
	 * @return
	 */
	public static CellStyle h2(Workbook workBook) {
		return hn(workBook,2);
	}
	
	/**
	 * 三级标题
	 * @param workBook
	 * @return
	 */
	public static CellStyle h3(Workbook workBook) {
		return hn(workBook,3);
	}
	
	/**
	 * 四级标题
	 * @param workBook
	 * @return
	 */
	public static CellStyle h4(Workbook workBook) {
		return hn(workBook,4);
	}
	
	
	/**
	 * n(n>1)级标题样式
	 * @param workBook
	 * @param n
	 * @return
	 */
	public static CellStyle hn(Workbook workBook, int n) {
		CellStyle style = basic(workBook);
		style.setAlignment(HorizontalAlignment.LEFT);
		
		Font font = basicFont(workBook);
		font.setFontHeightInPoints((short) 15);//设置字体大小
		font.setBold(true);
		
		style.setFont(font);
		style.setIndention((short) (n-1));	//缩进
		return style;
	}
	
	/**
	 * 横表标题
	 * @param workBook
	 * @return
	 */
	public static CellStyle titleHorizontal(Workbook workBook) {
		CellStyle style = basic(workBook);
		
		fillBackGroundColor(style, IndexedColors.GREY_25_PERCENT.getIndex());
		Font font = basicFont(workBook);
		
		font.setFontHeightInPoints((short) 9);//设置字体大小
		font.setBold(true);
		
		style.setFont(font);
		
		return style;
	}
	
	public static CellStyle titleVertical(Workbook workBook) {
		CellStyle style = basic(workBook);
		fillBackGroundColor(style, IndexedColors.GREY_25_PERCENT.getIndex());
		
		Font font = basicFont(workBook);
		
		font.setFontHeightInPoints((short) 9);//设置字体大小
		font.setBold(true);
		
		style.setFont(font);
		
		return style;
	}
	
	/**
	 * 表格内容格式(居中)
	 * @param workBook
	 * @return
	 */
	public static CellStyle tableContent(Workbook workBook) {
		CellStyle style = basic(workBook);
		style.setWrapText(true);	//\r\n换行
		
		Font font = basicFont(workBook);
		style.setFont(font);
		return style;
	}
	
	/**
	 * 表格内容格式(居中,带颜色),全部颜色:http://jlcon.iteye.com/blog/1122538
	 * @param workBook
	 * @param color
	 * @return
	 */
	public static CellStyle tableContent(Workbook workBook, IndexedColors color) {
		CellStyle style = basic(workBook);
		style.setWrapText(true);	//\r\n换行
		
		Font font = basicFont(workBook);
		font.setColor(color.getIndex());
		style.setFont(font);
		return style;
	}
	
	/**
	 * 表格内容格式(居左)
	 * @param workBook
	 * @return
	 */
	public static CellStyle tableContentLeft(Workbook workBook) {
		CellStyle style = basic(workBook);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setWrapText(true);	//\r\n换行
		
		Font font = basicFont(workBook);
		style.setFont(font);
		return style;
	}
	
	/**
	 * 基础单元格样式
	 * @param workBook
	 * @return
	 */
	public static CellStyle basic(Workbook workBook) {
		CellStyle style = workBook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);			//水平居中 
		style.setVerticalAlignment(VerticalAlignment.CENTER);	//垂直居中 
		return style;
	}
	
	/**
	 * 基础字体
	 * @param workBook
	 * @return
	 */
	private static Font basicFont(Workbook workBook) {
		Font font = workBook.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 9);//设置字体大小
		return font;
	}

	/**
	 * 填充单元格背景色
	 * @param style
	 * @param indexColor
	 */
	private static void fillBackGroundColor(CellStyle style, short indexColor) {
		style.setFillForegroundColor(indexColor);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}
}
