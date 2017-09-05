package com.demo.testng.utils;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SheetUtils {

    public static void clearSheet(XSSFWorkbook wb, String sheetName){
       XSSFSheet sheet = wb.getSheet(sheetName);
       int rowNum = sheet.getLastRowNum();
       if(rowNum > 0) {
           for(int i=1;i<= rowNum;i++) {
               XSSFRow row = sheet.getRow(i);
               sheet.removeRow(row);
           }
       }
    }
}
