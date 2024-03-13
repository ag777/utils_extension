package com.ag777.util.file.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/3/12 16:01
 */
public class ExcelUtils {

    private ExcelUtils() {}

    /**
     * 遍历工作簿的所有行，根据每行内容的最大长度动态调整行高
     * 。
     * @param workbook 工作簿
     */
    public static void adjustAllRowHeightsBasedOnContent(Workbook workbook) {
        if (workbook == null) {
            return;
        }
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            adjustAllRowHeightsBasedOnContent(workbook.getSheetAt(i));
        }
    }

    /**
     * 遍历工作表的所有行，根据每行内容的最大长度动态调整行高。
     *
     * @param sheet 需要调整行高的工作表
     */
    public static void adjustAllRowHeightsBasedOnContent(Sheet sheet) {
        if (sheet == null) {
            return;
        }
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            adjustRowHeightBasedOnContent(row);
        }
    }

    /**
     * 根据行内容的最大长度动态调整行高。
     * 如果单元格内容的最大长度超过35个字符，行高将根据内容长度增加。
     *
     * @param row 需要调整行高的行
     */
    public static void adjustRowHeightBasedOnContent(Row row) {
        if (row == null) {
            return;
        }
        // 根据内容长度设置行高
        int maxContentLength = 0;
        for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
            Cell cell = row.getCell(j);
            if (cell == null) {
                continue;
            }
            int cellContentLength = cell.toString().length();
            // 找出每一行中最长的单元格内容
            if (cellContentLength > maxContentLength) {
                maxContentLength = cellContentLength;
            }
        }
        // 设置默认行高为35
        row.setHeightInPoints(35);
        // 如果字符长度大于35，根据内容长度增加行高
        if (maxContentLength > 35) {
            float multiplier = maxContentLength / 35f;
            float newHeight = 35 * multiplier;
            /*if (d>2 && d<4){
                f = 35*2;
            }else if(d>=4 && d<6){
                f = 35*3;
            }else if (d>=6 && d<8){
                f = 35*4;
            }*/
            row.setHeightInPoints(newHeight);
        }
    }

    /**
     * 调整指定工作表中所有列的宽度以适应其内容。
     *
     * @param sheet 需要调整列宽的工作表
     */
    public static void autoAdjustColumnsWidth(Sheet sheet) {
        if (sheet == null) {
            return;
        }

        // 获取工作表中的列数
        int numberOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();

        // 遍历所有列，自动调整其宽度
        for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);

            // 为了避免列宽过窄，可以在autoSizeColumn之后加一点额外宽度
            int currentWidth = sheet.getColumnWidth(columnIndex);
            // 以字符为单位的额外宽度
            int extraWidth = 512;
            sheet.setColumnWidth(columnIndex, currentWidth + extraWidth);
        }
    }
}
