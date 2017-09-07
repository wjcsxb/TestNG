package com.timerchina.testng;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.timerchina.utils.DataReader;
import com.timerchina.utils.HTTPReqGen;
import com.timerchina.utils.RecordHandler;
import com.jayway.restassured.response.Response;

public class HTTPReqGenTest implements ITest {

    private Response response;
    private DataReader myInputData;
    private DataReader myBaselineData;
    private String template;

    public String getTestName() {
        return "API Test";
    }

    String filePath = "";

    XSSFWorkbook wb = null;
    XSSFSheet inputSheet = null;
    XSSFSheet baselineSheet = null;
    XSSFSheet outputSheet = null;
    XSSFSheet comparsionSheet = null;
    XSSFSheet resultSheet = null;

    private double totalcase = 0;
    private double failedcase = 0;
    private String startTime = "";
    private String endTime = "";


    @BeforeTest
    @Parameters("workBook")
    public void setup(String path) {
        filePath = path;
        try {
            wb = new XSSFWorkbook(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputSheet = wb.getSheet("Input");
        baselineSheet = wb.getSheet("Baseline");


//        SheetUtils.removeSheetByName(wb, "Output");
//        SheetUtils.removeSheetByName(wb, "Comparison");
//        SheetUtils.removeSheetByName(wb, "Result");
        outputSheet = wb.createSheet("Output");
        comparsionSheet = wb.createSheet("Comparison");
        resultSheet = wb.createSheet("Result");

        try {
            InputStream is = HTTPReqGenTest.class.getClassLoader().getResourceAsStream("http_request_template.txt");
            template = IOUtils.toString(is, Charset.defaultCharset());
        } catch (Exception e) {
            Assert.fail("Problem fetching data from input file:" + e.getMessage());
        }

        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTime = sf.format(new Date());
    }



    @Test(dataProvider = "WorkBookData", description = "ReqGenTest")
    public void api_test(String ID, String test_case) {

        HTTPReqGen myReqGen = new HTTPReqGen();

        try {
            myReqGen.generate_request(template, myInputData.get_record(ID));
            response = myReqGen.perform_request();
        } catch (Exception e) {
            Assert.fail("Problem using HTTPRequestGenerator to generate response: " + e.getMessage());
        }

        String baseline_message = myBaselineData.get_record(ID).get("Response");
        if (response.statusCode() == 200)
            try {
//                DataWriter.writeData(outputSheet, response.asString(), ID, test_case);

                JSONCompareResult result = JSONCompare.compareJSON(baseline_message, response.asString(), JSONCompareMode.NON_EXTENSIBLE);
                if (!result.passed()) {
//                    DataWriter.writeData(comparsionSheet, result, ID, test_case);
//                    DataWriter.writeData(resultSheet, "false", ID, test_case, 0);
//                    DataWriter.writeData(outputSheet);
                    failedcase++;
                } else {
//                    DataWriter.writeData(resultSheet, "true", ID, test_case, 0);
                }
            } catch (JSONException e) {
//                DataWriter.writeData(comparsionSheet, "", "Problem to assert Response and baseline messages: "+e.getMessage(), ID, test_case);
//                DataWriter.writeData(resultSheet, "error", ID, test_case, 0);
                failedcase++;
                Assert.fail("Problem to assert Response and baseline messages: " + e.getMessage());
            }
        else {
//            DataWriter.writeData(outputSheet, response.statusLine(), ID, test_case);

            if (baseline_message.equals(response.statusLine())) {
//                DataWriter.writeData(resultSheet, "true", ID, test_case, 0);
            } else {
//                DataWriter.writeData(comparsionSheet, baseline_message, response.statusLine(), ID, test_case);
//                DataWriter.writeData(resultSheet, "false", ID, test_case, 0);
//                DataWriter.writeData(outputSheet);
                failedcase++;
            }
        }
    }

    @AfterTest
    public void teardown() {
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        endTime = sf.format(new Date());
//        DataWriter.writeData(resultSheet, totalcase, failedcase, startTime, endTime);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            wb.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}