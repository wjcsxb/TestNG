package com.timerchina.testng;

import com.timerchina.utils.*;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.ValidatableResponseImpl;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.ValidatableResponse;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.*;

import static org.hamcrest.Matchers.*;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;

public class TestNG extends BaseTest {

    private Logger logger = Logger.getLogger(TestNG.class);


    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = true)
//    threadPoolSize = 3,invocationCount = 10, timeOut = 10000 同一个测试方法的并发
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

}
