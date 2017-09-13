package com.timerchina.testng;

import com.timerchina.utils.*;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.ValidatableResponseImpl;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.*;

import static org.hamcrest.Matchers.*;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;

public class TestNG implements ITest {

    private Logger logger = Logger.getLogger(TestNG.class);
    private Response   response;
    private DataReader myInputData;
    private DataReader baseLineData;
    private DataReader myBaselineData;
    private String     template;
    private int              totalCaseNum  = 0;
    private int              failedCaseNum = 0;
    private static final String IS_SUCCESS    = "0";
    private static final String EQUAL_TO      = "1";
    private static final String HAS_ITEMS     = "2";
    private static final String ALL_MATCH     = "3";

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

    @DataProvider(name = "WorkBookData")
    public Iterator<Object[]> testProvider(ITestContext context) {
        List<Object[]> test_IDs = new ArrayList<>();

        myInputData = new DataReader(inputSheet, true, true, 0);
        Map<String, RecordHandler> myInput = myInputData.get_map();
        List<Map.Entry<String, RecordHandler>> sortMap = Utils.sortMap(myInput);

        for (Map.Entry<String,RecordHandler> entry : sortMap) {
            String test_ID = entry.getKey();
            //            System.out.println(test_ID);
            String test_case = entry.getValue().get("TestCase");
            //            System.out.println(test_case);
            if (!test_ID.equals("") && !test_case.equals("")) {
                test_IDs.add(new Object[]{test_ID, test_case});
            }
            totalCaseNum++;
        }
        myBaselineData = new DataReader(baselineSheet, true, true, 0);

        //        System.out.println(myBaselineData.get_map().get("1").get_map());
        //        System.out.println(totalCaseNum);
        //        System.out.println(Arrays.toString(test_IDs.get(0)));
        //        System.out.println(test_IDs.iterator().next()[1]);
        return test_IDs.iterator();
    }

    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = false)
    public void apiRunTest(String ID, String testCase) {
        HTTPReqGen myReqGen = new HTTPReqGen();

        try {
            myReqGen.generate_request(template, myInputData.getRecord(ID));
            response = myReqGen.perform_request();
        } catch (Exception e) {
            Assert.fail("Problem using HTTPRequestGenerator to generate response: " + e.getMessage());
        }
        String baseline_message = myBaselineData.getRecord(ID).get("ExpectedResponse");
        String type = myBaselineData.getRecord(ID).get("Type");
        if(!type.equals(ALL_MATCH)){
            DataWriter.writeData(wb,resultSheet,ID,testCase,"is not AllMatch");
            return;
        }
        String msg = "";
        if (response.statusCode() == 200) {
            try {
                DataWriter.writeData(outputSheet, response.asString(), ID, testCase);
                JSONCompareResult result = JSONCompare.compareJSON(baseline_message, response.asString(), JSONCompareMode.NON_EXTENSIBLE);
            } catch (Exception e) {
                failedCaseNum ++;
                e.printStackTrace();
            }
        } else {
            failedCaseNum ++;
            msg = "接口请求失败！";
        }
        DataWriter.writeData(wb, resultSheet, ID, testCase, msg);
    }

    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = false)
    public void testAPi(String ID, String testCase) {
        HTTPReqGen myReqGen = new HTTPReqGen();

        try {
            myReqGen.generate_request(template, myInputData.getRecord(ID));
            response = myReqGen.perform_request();
        } catch (Exception e) {
            Assert.fail("Problem using HTTPRequestGenerator to generate response: " + e.getMessage());
        }
        String baseline_message = myBaselineData.getRecord(ID).get("Response");
        if (response.statusCode() == 200) {
            try {
                DataWriter.writeData(outputSheet, response.asString(), ID, testCase);
                JSONCompareResult result = JSONCompare.compareJSON(baseline_message, response.asString(), JSONCompareMode.NON_EXTENSIBLE);

                if (!result.passed()) {
                    DataWriter.writeData(wb, resultSheet, false, result, ID, testCase);
                    DataWriter.writeData(comparsionSheet, baseline_message, response.asString(), ID, testCase);
                } else {
                    DataWriter.writeData(wb, resultSheet, true, result, ID, testCase);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            DataWriter.writeData(outputSheet, response.statusLine(), ID, testCase);
            DataWriter.writeData(wb, resultSheet, ID, testCase, "接口请求失败！");
        }
    }

    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = false)
    public void testField(String ID, String testCase) {
        HTTPReqGen myReqGen = new HTTPReqGen();

        try {
            myReqGen.generate_request(template, myInputData.getRecord(ID));
            response = myReqGen.perform_request();
        } catch (Exception e) {
            Assert.fail("Problem using HTTPRequestGenerator to generate response: " + e.getMessage());
        }

        String expectSize = myBaselineData.getRecord(ID).get("Size");
        String compareField = myBaselineData.getRecord(ID).get("CompareField");
        if (response.statusCode() == 200) {
            DataWriter.writeData(outputSheet, response.asString(), ID, testCase);
            String actualResult = JsonUtils.getJsonValue(response.asString(), "result");
            if (actualResult == null || actualResult.length() == 0) {
                actualResult = JsonUtils.getJsonValue(response.asString(), "list");
            }
            String actualSize = JsonUtils.getJsonValue(response.asString(), "size");
            if (compareField != null && compareField.length() > 0) {
                String[] fields = compareField.split(",");
                String msg = isContains(actualResult, fields, ID, testCase);
                if (!expectSize.equals(actualSize)) {
                    msg = msg + " and size is Unexpect!";
                }
                DataWriter.writeData(wb, resultSheet, ID, testCase, msg);
            } else {
                DataWriter.writeData(wb, resultSheet, ID, testCase, "CompareField未配置！");
            }

        } else {
            DataWriter.writeData(outputSheet, response.statusLine(), ID, testCase);
            DataWriter.writeData(wb, resultSheet, ID, testCase, "接口请求失败！");
        }
    }

    /**
     * RestAssured + TestNG 测试
     * */
    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = true)
    public void restAssured(String ID, String testCase) {
        String expectedResponse = myBaselineData.getRecord(ID).get("ExpectedResponse");
        String type = myBaselineData.getRecord(ID).get("Type");
        String url = myInputData.getRecord(ID).get("host") + myInputData.getRecord(ID).get("call_suff");
        String contentType = myInputData.getRecord(ID).get("Content-Type");

        RestAssured.registerParser(contentType,Parser.JSON);
        String msg = "";

        try {
            ValidatableResponse rep = getResponse(url);
            DataWriter.writeData(outputSheet, ((ValidatableResponseImpl) rep).body().extract().response().asString(), ID, testCase);

            msg = getTestMsg(type, expectedResponse, rep);
//            msg = msg + rep.extract().response().getContentType();
        }catch (Exception e){
            failedCaseNum ++;
            msg = msg + " URL请求失败 " +e.getMessage();
        }
        DataWriter.writeData(wb, resultSheet, ID, testCase, msg);
    }

    private static ValidatableResponse getResponse(String url) {
        return given().when().get(url).then().assertThat();
    }

    private String equalTest(String field,ValidatableResponse rep){
        String msg = "";
        String[] fields = field.split(",");
        for(String s:fields){
            String[] strings = s.split("=");
            try{
                Object actualObject = rep.extract().path(strings[0]);
                if(actualObject instanceof Integer ){
                    Assert.assertEquals((Integer) actualObject,Integer.valueOf(strings[1]));
                }else if(actualObject instanceof String){
                    Assert.assertEquals((String)actualObject,strings[1]);
                }else if(actualObject instanceof ArrayList){
//                    rep.assertThat().body(strings[0], equalTo(Arrays.asList(strings[1])));
                    Assert.assertEquals((ArrayList)actualObject,Arrays.asList(strings[1]));
                }else{
                    failedCaseNum++;
                    msg = "actualObject is not Integer ,String or ArrayList";
                    break;
                }
            }catch(AssertionError e){
                failedCaseNum ++;
                msg = e.toString();
                break;
            }catch (Exception e1){
                failedCaseNum ++;
                msg = e1.toString();
                break;
            }
        }
        return msg;
    }
    private String hasItemsTest(String field,ValidatableResponse rep)  {
        String msg = "";
        String[] fields = field.split("&");
        for( String s : fields ){
            String[] strings = s.split("=");
            try{
                Object actualOject = rep.extract().response().path(strings[0]);
                if(actualOject != null && actualOject instanceof ArrayList){
                    String[] results =  strings[1].split(",");
                    if(((ArrayList) actualOject).get(0) instanceof Integer){
                        for(String s1:results){
                            rep.body(strings[0],hasItems(Integer.valueOf(s1)));
                        }
                    }else {
                        for(String s1:results) {
                            rep.body(strings[0], hasItems(s1));
                        }
                    }
                }else {
                    failedCaseNum++;
                    msg = "Expected " + strings[0] + " is null or not ArrayList.";
                    break;
                }
            }catch (AssertionError e){
                failedCaseNum++;
                msg = e.toString();
                break;
            }catch (Exception e1){
                failedCaseNum ++;
                msg = e1.toString();
                break;
            }
        }
        return msg;
    }

    private String allMatchTest(String expectStr,ValidatableResponse rep){
        String msg = "";
        try{
            rep.body(equalTo(expectStr));
        }catch (AssertionError e){
            failedCaseNum++;
            msg = e.toString();
        }
        return msg;
    }

    private String successTest(ValidatableResponse rep){
        int statusCode = rep.extract().response().getStatusCode();
        if(statusCode == 200){
            return null;
        }else{
            failedCaseNum++;
            return String.valueOf(statusCode);
        }
    }
    private String getTestMsg(String type ,String expectedResponse ,ValidatableResponse rep ){
        if(ValidateUtils.isEmpty(type)){
            failedCaseNum ++;
            logger.info("excel中Type未配置！");
            return  "excel中Type未配置";
        }
        if(ValidateUtils.isEmpty(rep)){
            failedCaseNum ++;
            logger.info("excel中ExpectedResponse未配置！");
            return  "excel中ExpectedResponse未配置";
        }
        if(type.equals(IS_SUCCESS)){
            //TODO 接口是否有数据
            return successTest(rep);
        }else if(type.equals(EQUAL_TO)){
            //TODO  测试 EqualField   字段数据匹配
            return equalTest(expectedResponse,rep);
        }else if(type.equals(HAS_ITEMS)){
            //TODO  测试 HasItemField  字段数据包含
            return hasItemsTest(expectedResponse,rep);
        }else if(type.equals(ALL_MATCH)){
            //TODO  测试 AllMatch      完全匹配
            return allMatchTest(expectedResponse,rep);
        }else{
            failedCaseNum ++;
            return  "Type is not defined!";
        }
    }
    private String isContains(String actualResult, String[] fields, String ID, String testCase) {
        String msg = "";
        for (String s : fields) {
            if (!actualResult.contains(s)) {
                String[] values = s.split(":");
                msg = values[0] + "is Unexpected";
                return msg;
            }
        }
        return null;
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
