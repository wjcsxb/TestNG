package com.timerchina.utils;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.Date;

public class DataWriter {
    protected static final Logger logger = Logger.getLogger(DataReader.class);

    public DataWriter(){
    }

    public static void writeData(XSSFSheet sheet,String data,String ID,String testCase){
        if(sheet != null){
            int rowNum = sheet.getLastRowNum();
            int colNum = sheet.getRow(0).getLastCellNum();
            XSSFRow row = sheet.createRow(rowNum+1);
            XSSFCell cell1 = row.createCell(0);
            XSSFCell cell2 = row.createCell(1);
            XSSFCell cell3 = row.createCell(2);

            cell1.setCellValue(ID);
            cell2.setCellValue(testCase);
            cell3.setCellValue(data);
        }
    }
    public static void writeData(XSSFWorkbook wb,XSSFSheet sheet,boolean flag,JSONCompareResult result,String ID,String testCase){
        if(wb != null && sheet != null){
            int rowNum = sheet.getLastRowNum();
            int colNum = sheet.getRow(0).getLastCellNum();
            XSSFRow row = sheet.createRow(rowNum+1);
            XSSFCell cell1 = row.createCell(0);
            XSSFCell cell2 = row.createCell(1);
            XSSFCell cell3 = row.createCell(2);
            XSSFCell cell4 = row.createCell(3);

            XSSFFont headFont = wb.createFont();
            headFont.setColor(IndexedColors.RED.getIndex());

            XSSFCellStyle cellStyle = wb.createCellStyle();
//            cellStyle.setFillPattern(XSSFCellStyle.FINE_DOTS);
            cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
            cellStyle.setFont(headFont);
            cell1.setCellValue(ID);
            cell2.setCellValue(testCase);
            if(flag) {
                cell3.setCellValue("PASSED");
            }else{
                cell3.setCellStyle(cellStyle);
                cell3.setCellValue("FAILED");
                cell4.setCellValue(result.toString());
            }
        }
    }
    public static void writeData(XSSFWorkbook wb,XSSFSheet sheet,String ID,String testCase,String msg){
        if(wb!=null && sheet != null){
            int rowNum = sheet.getLastRowNum();
            XSSFRow row = sheet.createRow(rowNum+1);
            XSSFCell cell1 = row.createCell(0);
            XSSFCell cell2 = row.createCell(1);
            XSSFCell cell3 = row.createCell(2);
            XSSFCell cell4 = row.createCell(3);

            XSSFFont headFont = wb.createFont();
            headFont.setColor(IndexedColors.RED.getIndex());

            XSSFCellStyle cellStyle = wb.createCellStyle();
            //            cellStyle.setFillPattern(XSSFCellStyle.FINE_DOTS);
            cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
            cellStyle.setFont(headFont);
            cell1.setCellValue(ID);
            cell2.setCellValue(testCase);

            if(ValidateUtils.isEmpty(msg)){
                cell3.setCellValue("PASSED");
            }else{
                cell3.setCellValue("FAILED");
                cell3.setCellStyle(cellStyle);
            }
            cell4.setCellValue(msg);
        }
    }

    public static void writeData(XSSFSheet sheet,String expectStr,String actualStr,String ID ,String testCase){
        if(sheet != null){
            int rowNum = sheet.getLastRowNum();
            int colNum = sheet.getRow(0).getLastCellNum();
            XSSFRow row1 = sheet.createRow(rowNum+1);
            XSSFRow row2 = sheet.createRow(rowNum+2);
            XSSFCell cell1 = row1.createCell(0);
            XSSFCell cell2 = row1.createCell(1);
            XSSFCell cell3 = row1.createCell(2);
            XSSFCell cell4 = row1.createCell(3);

            cell1.setCellValue(ID);
            cell2.setCellValue(testCase);
            cell3.setCellValue("Actual");
            cell4.setCellValue(actualStr);

            XSSFCell cell5 = row2.createCell(2);
            XSSFCell cell6 = row2.createCell(3);


            cell5.setCellValue("Expect");
            cell6.setCellValue(expectStr);
        }
    }
    public static void writeDataForTestLog(XSSFSheet resultSheet,int totalCaseNum, int failedCaseNum,String startTime,String endTime){
        if(ValidateUtils.notEmpty(resultSheet)){
            int rowNum = resultSheet.getLastRowNum();
            int colNum = resultSheet.getRow(0).getLastCellNum();
            XSSFRow row1 = resultSheet.createRow(rowNum+1);
            XSSFRow row2 = resultSheet.createRow(rowNum+2);
            XSSFRow row3 = resultSheet.createRow(rowNum+3);
            XSSFRow row4 = resultSheet.createRow(rowNum+4);
            XSSFCell cell1 = row1.createCell(1);
            XSSFCell cell2 = row1.createCell(2);
            XSSFCell cell3 = row2.createCell(1);
            XSSFCell cell4 = row2.createCell(2);
            XSSFCell cell5 = row3.createCell(1);
            XSSFCell cell6 = row3.createCell(2);
            XSSFCell cell7 = row4.createCell(1);
            XSSFCell cell8 = row4.createCell(2);

            cell1.setCellValue("TotalCaseNum");
            cell2.setCellValue(totalCaseNum);
            cell3.setCellValue("FailedCaseNum");
            cell4.setCellValue(failedCaseNum);
            cell5.setCellValue("Start Time");
            cell6.setCellValue(startTime);
            cell7.setCellValue("End Time");
            cell8.setCellValue(endTime);
        }
    }
}
