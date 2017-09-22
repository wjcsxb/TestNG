package com.timerchina.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.log4j.Logger;


/**
 * The type Data reader.
 */
public class DataReader {

    /**
     * The constant logger.
     */
    protected static final Logger logger = Logger.getLogger(DataReader.class);

    private HashMap<String, RecordHandler> map = new HashMap<String, RecordHandler>();

    private Boolean byColumnName = false;
    private Boolean byRowKey = false;
    private List<String> headers = new ArrayList<String>();

    private Integer size = 0;

    /**
     * Instantiates a new Data reader.
     */
    public DataReader() {
    }


    /**
     * Instantiates a new Data reader.
     *
     * @param sheet        the sheet
     * @param hasHeaders   the has headers
     * @param hasKeyColumn the has key column
     * @param keyColumn    the key column
     */
    public DataReader(XSSFSheet sheet, Boolean hasHeaders, Boolean hasKeyColumn, Integer keyColumn) {

        XSSFRow myRow = null;
        HashMap<String, String> myList;
        size = 0;

        this.byColumnName = hasHeaders;
        this.byRowKey = hasKeyColumn;

        try {

            if(byColumnName) {
                myRow = sheet.getRow(0);
                for(Cell cell: myRow) {
                    headers.add(cell.getStringCellValue());
                }
                size = 1;
            }

            for(; (myRow = sheet.getRow(size)) != null; size++ ) {

                myList = new HashMap<String, String>();

                if(byColumnName) {
                    for(int col = 0; col < headers.size(); col++ ) {
                        if(col < myRow.getLastCellNum()) {
                            myList.put(headers.get(col), getSheetCellValue(myRow.getCell(col))); // myRow.getCell(col).getStringCellValue());
                        } else {
                            myList.put(headers.get(col), "");
                        }
                    }
                } else {
                    for(int col = 0; col < myRow.getLastCellNum(); col++ ) {
                        myList.put(Integer.toString(col), getSheetCellValue(myRow.getCell(col)));
                    }
                }

                if(byRowKey) {
                    if(myList.size() == 2 && keyColumn == 0) {
                        map.put(getSheetCellValue(myRow.getCell(keyColumn)), new RecordHandler(myList.get(1)));
                    } else if(myList.size() == 2 && keyColumn == 1) {
                        map.put(getSheetCellValue(myRow.getCell(keyColumn)), new RecordHandler(myList.get(0)));
                    } else {
                        map.put(getSheetCellValue(myRow.getCell(keyColumn)), new RecordHandler(myList));
                    }
                } else {
                    map.put(Integer.toString(size), new RecordHandler(myList));
                }
            }

        } catch (Exception e) {
            logger.error("Exception while loading data from Excel sheet:"+e.getMessage());
        }
    }

    private String getSheetCellValue(XSSFCell cell) {

        String value = "";

        try {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            value = cell.getStringCellValue();
        } catch(NullPointerException npe) {
            return "";
        }

        return value;
    }

    /**
     * Gets map.
     *
     * @return the map
     */
    public HashMap<String, RecordHandler> getMap() {
        return map;
    }

    /**
     * Gets record.
     *
     * @param record the record
     * @return the record
     */
    public RecordHandler getRecord(String record) {

        RecordHandler result = new RecordHandler();

        if(map.containsKey(record)) {
            result = map.get(record);
        }

        return result;
    }

}