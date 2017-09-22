package com.timerchina.testng;

import com.jayway.restassured.response.Response;
import com.timerchina.utils.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseTest implements ITest {
    private Logger logger = Logger.getLogger(BaseTest.class);
    Response   response;
    DataReader myInputData;
    DataReader baseLineData;
    DataReader myBaselineData;
    String     template;
    private int              totalCaseNum  = 0;
    int              failedCaseNum = 0;
    static final String IS_SUCCESS    = "0";
    static final String EQUAL_TO      = "1";
    static final String HAS_ITEMS     = "2";
    static final String ALL_MATCH     = "3";

    //    String filePath = "D:\\project\\TestNG\\src\\test\\resources\\api-test.xlsx";

    XSSFWorkbook wb              = null;
    XSSFSheet    inputSheet      = null;
    XSSFSheet    baselineSheet   = null;
    XSSFSheet    outputSheet     = null;
    XSSFSheet    comparsionSheet = null;
    XSSFSheet    resultSheet     = null;

    private String startTime = "";
    private String endTime   = "";

    public String getTestName() {
        return "API Test";
    }

    @BeforeTest
    @Parameters("filePath")
    public void setup(String filePath) {
        try {
            wb = new XSSFWorkbook(new FileInputStream(filePath));
        } catch (Exception e) {
            logger.info(filePath + "路径错误或者excel文件损坏！");
        }

        inputSheet = wb.getSheet("Input");
        baselineSheet = wb.getSheet("Baseline");
        outputSheet = wb.getSheet("Output");
        comparsionSheet = wb.getSheet("Comparison");
        resultSheet = wb.getSheet("Result");
        SheetUtils.clearSheet(wb, "Output");
        SheetUtils.clearSheet(wb, "Comparison");
        SheetUtils.clearSheet(wb, "Result");


        try {
            InputStream is = HTTPReqGenTest.class.getClassLoader().getResourceAsStream("http_request_template.txt");
            template = IOUtils.toString(is, Charset.defaultCharset());
        } catch (Exception e) {
            Assert.fail("Problem fetching data from input file:" + e.getMessage());
        }
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTime = sf.format(new Date());
    }

    @DataProvider(name = "WorkBookData" ,parallel = false)
    public Iterator<Object[]> testProvider(ITestContext context) {
        List<Object[]> testIDs = new ArrayList<>();

        myInputData = new DataReader(inputSheet, true, true, 0);
        Map<String, RecordHandler> myInput = myInputData.getMap();
        List<Map.Entry<String, RecordHandler>> sortMap = Utils.sortMap(myInput);

        for (Map.Entry<String,RecordHandler> entry : sortMap) {
            String testID = entry.getKey();
            //            System.out.println(test_ID);
            String testCase = entry.getValue().get("TestCase");
            //            System.out.println(test_case);
            if (!testID.equals("") && !testCase.equals("")) {
                testIDs.add(new Object[]{testID, testCase});
            }
            totalCaseNum++;
        }
        myBaselineData = new DataReader(baselineSheet, true, true, 0);
        return testIDs.iterator();
    }

    @AfterTest
    @Parameters("filePath")
    public void afterTest(String filePath) {
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        endTime = sf.format(new Date());
        DataWriter.writeDataForTestLog(resultSheet, totalCaseNum, failedCaseNum, startTime, endTime);
        //统计时间
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            wb.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
