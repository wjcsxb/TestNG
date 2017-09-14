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

public class TestNG extends BaseTest {

    private Logger logger = Logger.getLogger(TestNG.class);
//    private Response   response;
//    private DataReader myInputData;
//    private DataReader baseLineData;
//    private DataReader myBaselineData;
//    private String     template;
//    private int              totalCaseNum  = 0;
//    private int              failedCaseNum = 0;
//    private static final String IS_SUCCESS    = "0";
//    private static final String EQUAL_TO      = "1";
//    private static final String HAS_ITEMS     = "2";
//    private static final String ALL_MATCH     = "3";
//
//    //    String filePath = "D:\\project\\TestNG\\src\\test\\resources\\api-test.xlsx";
//
//    XSSFWorkbook wb              = null;
//    XSSFSheet    inputSheet      = null;
//    XSSFSheet    baselineSheet   = null;
//    XSSFSheet    outputSheet     = null;
//    XSSFSheet    comparsionSheet = null;
//    XSSFSheet    resultSheet     = null;
//
//    private String startTime = "";
//    private String endTime   = "";
//
//    public String getTestName() {
//        return "API Test";
//    }
//
//    @BeforeTest
//    @Parameters("filePath")
//    public void setup(String filePath) {
//        try {
//            wb = new XSSFWorkbook(new FileInputStream(filePath));
//        } catch (Exception e) {
//            logger.info(filePath + "路径错误或者excel文件损坏！");
//        }
//
//        inputSheet = wb.getSheet("Input");
//        baselineSheet = wb.getSheet("Baseline");
//        outputSheet = wb.getSheet("Output");
//        comparsionSheet = wb.getSheet("Comparison");
//        resultSheet = wb.getSheet("Result");
//        SheetUtils.clearSheet(wb, "Output");
//        SheetUtils.clearSheet(wb, "Comparison");
//        SheetUtils.clearSheet(wb, "Result");
//
//
//        try {
//            InputStream is = HTTPReqGenTest.class.getClassLoader().getResourceAsStream("http_request_template.txt");
//            template = IOUtils.toString(is, Charset.defaultCharset());
//        } catch (Exception e) {
//            Assert.fail("Problem fetching data from input file:" + e.getMessage());
//        }
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        startTime = sf.format(new Date());
//    }

//    @DataProvider(name = "WorkBookData")
//    public Iterator<Object[]> testProvider(ITestContext context) {
//        List<Object[]> test_IDs = new ArrayList<>();
//
//        myInputData = new DataReader(inputSheet, true, true, 0);
//        Map<String, RecordHandler> myInput = myInputData.get_map();
//        List<Map.Entry<String, RecordHandler>> sortMap = Utils.sortMap(myInput);
//
//        for (Map.Entry<String,RecordHandler> entry : sortMap) {
//            String test_ID = entry.getKey();
//            //            System.out.println(test_ID);
//            String test_case = entry.getValue().get("TestCase");
//            //            System.out.println(test_case);
//            if (!test_ID.equals("") && !test_case.equals("")) {
//                test_IDs.add(new Object[]{test_ID, test_case});
//            }
//            totalCaseNum++;
//        }
//        myBaselineData = new DataReader(baselineSheet, true, true, 0);
//        return test_IDs.iterator();
//    }

    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = true,threadPoolSize = 3,invocationCount = 22, timeOut = 1000)
    public void restAssured(String ID, String testCase) {

        Long id = Thread.currentThread().getId();
        System.out.println("Test method executing on thread with id: " + id);

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
                Object actualObject = rep.extract().response().path(strings[0]);
                if(actualObject != null && actualObject instanceof ArrayList){
                    String[] results =  strings[1].split(",");
                    if(((ArrayList) actualObject).get(0) instanceof Integer){
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
            return successTest(rep);        //  测试 接口是否有数据
        }else if(type.equals(EQUAL_TO)){
            return equalTest(expectedResponse,rep);   //  测试 EqualField   字段数据匹配
        }else if(type.equals(HAS_ITEMS)){
            return hasItemsTest(expectedResponse,rep);  // 测试 HasItemField  字段数据包含
        }else if(type.equals(ALL_MATCH)){
            return allMatchTest(expectedResponse,rep);    // 测试 AllMatch      完全匹配
        }else{
            failedCaseNum ++;
            return  "Type is not defined!";
        }
    }


    @BeforeMethod
    public void beforeMethod() {
        long id = Thread.currentThread().getId();
        System.out.println("Before test-method. Thread id is: " + id);
    }
//    @AfterTest
//    @Parameters("filePath")
//    public void afterTest(String filePath) {
//        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        endTime = sf.format(new Date());
//        DataWriter.writeDataForTestLog(resultSheet, totalCaseNum, failedCaseNum, startTime, endTime);
//        //统计时间
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(filePath);
//            wb.write(fileOutputStream);
//            fileOutputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
