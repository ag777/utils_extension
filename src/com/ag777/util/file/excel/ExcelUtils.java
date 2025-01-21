package com.ag777.util.file.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

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

    /**
     * 复制一个Excel工作表（Sheet）的所有内容到另一个工作表。
     * 包括列宽、行高、单元格内容、单元格样式和合并单元格等属性。
     *
     * @param sourceSheet 源工作表，从中复制所有内容
     * @param targetSheet 目标工作表，将源工作表的内容复制到这里
     */
    public static void copySheet(Sheet sourceSheet, Sheet targetSheet) {
        // 复制列宽：遍历第一行的每个单元格，设置目标Sheet的列宽与源Sheet相同
        for (int i = 0; i < sourceSheet.getRow(0).getLastCellNum(); i++) {
            targetSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
        }

        // 复制行高和内容：遍历源Sheet的所有行
        for (int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row targetRow = targetSheet.createRow(i);

            if (sourceRow != null) {
                // 复制行高：如果源行不为空，则设置目标行的高度与源行相同
                targetRow.setHeight(sourceRow.getHeight());

                // 遍历源行的每个单元格，复制内容到目标行
                for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                    Cell sourceCell = sourceRow.getCell(j);
                    Cell targetCell = targetRow.createCell(j);

                    if (sourceCell != null) {
                        // 调用copyCell方法复制单元格内容和样式
                        copyCell(sourceCell, targetCell);
                    }
                }
            }
        }

        // 复制合并单元格：遍历源Sheet的所有合并区域，并在目标Sheet中添加相同的合并区域
        for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sourceSheet.getMergedRegion(i);
            targetSheet.addMergedRegion(mergedRegion);
        }
    }


    /**
     * 复制单个单元格的内容和样式。
     *
     * @param sourceCell 源单元格，从中复制内容和样式
     * @param targetCell 目标单元格，将源单元格的内容和样式复制到这里
     */
    public static void copyCell(Cell sourceCell, Cell targetCell) {
        // 复制单元格内容：根据单元格类型设置目标单元格的值
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            default:
                targetCell.setCellValue("");
        }

        // 复制单元格样式：创建新的CellStyle并从源单元格克隆样式，应用到目标单元格
        CellStyle sourceStyle = sourceCell.getCellStyle();
        CellStyle targetStyle = targetCell.getSheet().getWorkbook().createCellStyle();
        targetStyle.cloneStyleFrom(sourceStyle);
        targetCell.setCellStyle(targetStyle);
    }
}
