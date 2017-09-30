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

/**
 * The type Test ng.
 */
public class TestNG extends BaseTest {

    private Logger logger = Logger.getLogger(TestNG.class);

    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = true)
    //    threadPoolSize = 3,invocationCount = 10, timeOut = 10000 同一个测试方法的并发
    public void apiTest(String ID, String testCase) {

        String expectedResponse = myBaselineData.getRecord(ID).get("ExpectedResponse");
        String type = myBaselineData.getRecord(ID).get("Type");
        String url = myInputData.getRecord(ID).get("host") + myInputData.getRecord(ID).get("requestPath");
        String contentType = myInputData.getRecord(ID).get("Content-Type");

        RestAssured.registerParser(contentType, Parser.JSON);   //contentType有必要配置？
        String msg = "";

        try {
            ValidatableResponse rep = getResponse(url);
            if(isSuccess(rep)){   //  url是否 404、500等

                int statusCode = rep.extract().response().path("statusCode");
                if(EXCEPTION_CODE == statusCode){     //接口是否报异常 （通用接口中程序异常信息也包装在API结果中）
                    msg = rep.extract().response().path("desc").toString() + rep.extract().response().path("result").toString();
                    failedCaseNum ++;
                }else{
                    DataWriter.writeData(outputSheet, ((ValidatableResponseImpl) rep).body().extract().response().asString(), ID, testCase);
                    msg = getTestMsg(type, expectedResponse, rep);
                }
            }else{
                msg = successTest(rep);
            }

            //            msg = msg + rep.extract().response().getContentType();
        } catch (Exception e) {
            failedCaseNum++;
            msg = msg + " URL请求失败 " + e.getMessage();
        }
        DataWriter.writeData(wb, resultSheet, ID, testCase, msg);
    }

    /**
     *  获取Response
     * */
    private static ValidatableResponse getResponse(String url) {
        return given().when().get(url).then().assertThat();
    }

    /**
     * 测试 字段数据匹配
     * */
    private String equalTest(String field, ValidatableResponse rep) {
        String msg = "";
        String[] fields = field.split(",");
        for (String s : fields) {
            String[] strings = s.split("=");
            try {
                Object actualObject = rep.extract().path(strings[0]);
                if (actualObject instanceof Integer) {
                    Assert.assertEquals((Integer) actualObject, Integer.valueOf(strings[1]));
                } else if (actualObject instanceof String) {
                    Assert.assertEquals((String) actualObject, strings[1]);
                } else if (actualObject instanceof ArrayList) {
                    //                    rep.assertThat().body(strings[0], equalTo(Arrays.asList(strings[1])));
                    Assert.assertEquals((ArrayList) actualObject, Arrays.asList(strings[1]));
                } else {
                    failedCaseNum++;
                    msg = "actualObject is not Integer ,String or ArrayList";
                    break;
                }
            } catch (AssertionError e) {
                failedCaseNum++;
                msg = e.toString();
                break;
            } catch (Exception e1) {
                failedCaseNum++;
                msg = e1.toString();
                break;
            }
        }
        return msg;
    }

    /**
     *  测试 字段数据包含
     * */
    private String hasItemsTest(String field, ValidatableResponse rep) {
        String msg = "";
        String[] fields = field.split("&");
        for (String s : fields) {
            String[] strings = s.split("=");
            try {
                Object actualObject = rep.extract().response().path(strings[0]);
                if (actualObject != null && actualObject instanceof ArrayList) {
                    String[] results = strings[1].split(",");
                    if (((ArrayList) actualObject).get(0) instanceof Integer) {
                        List<Integer> intList = new ArrayList<>();
                        for (String s1 : results) {
                            intList.add(Integer.valueOf(s1));
                        }
                        rep.body(strings[0], hasItems(intList.toArray()));
                    } else {
//                        rep.body(strings[0], hasItems(results));
                        for(String string : results){
                            rep.body(strings[0],hasItems(string));
                        }
                    }
                } else {
                    failedCaseNum++;
                    msg = "Expected " + strings[0] + " is null or not ArrayList.";
                    break;
                }
            } catch (AssertionError e) {
                failedCaseNum++;
                msg = e.toString();
                break;
            } catch (Exception e1) {
                failedCaseNum++;
                msg = e1.toString();
                break;
            }
        }
        return msg;
    }

    /**
     *  API结果完全匹配
     * */
    private String allMatchTest(String expectStr, ValidatableResponse rep) {
        String msg = "";
        try {
            rep.body(equalTo(expectStr));
        } catch (AssertionError e) {
            failedCaseNum ++;
            msg = e.toString();
        }
        return msg;
    }

    /**
     *  API是否有结果 statusCode = 200 ？
     * */
    private String successTest(ValidatableResponse rep) {
        int statusCode = rep.extract().response().getStatusCode();
        if (statusCode == 200) {
            return null;
        } else {
            failedCaseNum++;
            return String.valueOf(statusCode);
        }
    }

    /**
     *
     * */
    private boolean isSuccess(ValidatableResponse rep){
        return rep.extract().response().getStatusCode() == 200;
    }
    private String getTestMsg(String type, String expectedResponse, ValidatableResponse rep) {
        if (ValidateUtils.isEmpty(type)) {
            failedCaseNum++;
            logger.info("excel中Type未配置！");
            return "excel中Type未配置";
        }
        if (ValidateUtils.isEmpty(rep)) {
            failedCaseNum++;
            logger.info("excel中ExpectedResponse未配置！");
            return "excel中ExpectedResponse未配置";
        }
        if (type.equals(IS_SUCCESS)) {
            return successTest(rep);        //  测试 接口是否有数据
        } else if (type.equals(EQUAL_TO)) {
            return equalTest(expectedResponse, rep);   //  测试 EqualField   字段数据匹配
        } else if (type.equals(HAS_ITEMS)) {
            return hasItemsTest(expectedResponse, rep);  // 测试 HasItemField  字段数据包含
        } else if (type.equals(ALL_MATCH)) {
            return allMatchTest(expectedResponse, rep);    // 测试 AllMatch      完全匹配
        } else {
            failedCaseNum++;
            return "Type is not defined!";
        }
    }

}
