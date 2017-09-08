package com.ag777.util.file.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 表格基础样式构造工具类
 * @author ag777
 * Time: last modify at 2017/09/06.
 */
public class ExcelStyleUtils {
	
	/**
	 * 自定义单元格样式
	 * @param workBook
	 * @param centerInHorizontal	水平居中对齐
	 * @param centerInVertical		垂直居中对齐
	 * @param backgroundColor	背景色
	 * @param indention				缩进
	 * @param isWrapText				内容遇到\r\n换行
	 * @return
	 */
	public static CellStyle customStyle(
			Workbook workBook,
			boolean centerInHorizontal,
			boolean centerInVertical,
			IndexedColors backgroundColor,
			Short indention,
			boolean isWrapText) {
		CellStyle style = workBook.createCellStyle();
		
		if(centerInHorizontal) {
			style.setAlignment(HorizontalAlignment.CENTER);			//水平居中 
		}
		
		if(centerInVertical) {
			style.setVerticalAlignment(VerticalAlignment.CENTER);	//垂直居中 
		}
		
		if(indention != null) {
			style.setIndention((short) (indention-1));	//缩进
		}
		
		if(backgroundColor != null) {
			fillBackGroundColor(style, backgroundColor.getIndex());
		}
		
		if(isWrapText) {
			style.setWrapText(true);	//\r\n换行
		}
		return style;
	}

	/**
	 * 自定义字体
	 * @param workBook
	 * @param fontName	比如"宋体"
	 * @param fontSize 		字体大小
	 * @param color			颜色
	 * @param isBold			粗体
	 * @return
	 */
	public static Font customFont(Workbook workBook,String fontName,Short fontSize, IndexedColors color,  boolean isBold) {
		Font font = workBook.createFont();
		if(fontName != null) {
			font.setFontName(fontName);
		}
		if(fontSize != null) {
			font.setFontHeightInPoints(fontSize);//设置字体大小
		}
		
		if(color != null) {
			font.setColor(color.getIndex());
		}
		
		if(isBold) {
			font.setBold(true);
		}
		
		return font;
	}
	
	/**
	 * 填充背景色
	 * @param style
	 * @param indexColor
	 */
	private static void fillBackGroundColor(CellStyle style, short indexColor) {
		style.setFillForegroundColor(indexColor);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}
	
}
