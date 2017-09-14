package com.timerchina.testng;

import com.timerchina.utils.*;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.Assert;
import org.testng.annotations.*;


public class TestByJSONCompare extends BaseTest{


    private Logger logger = Logger.getLogger(TestByJSONCompare.class);
    public String getTestName() {
        return "API Test";
    }

    @Test(dataProvider = "WorkBookData", description = "ReqGenTest", enabled = true)
    public void apiRunTest(String ID, String testCase) {
        HTTPReqGen myReqGen = new HTTPReqGen();

        try {
            myReqGen.generateRequest(template, myInputData.getRecord(ID));
            response = myReqGen.performRequest();
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
            myReqGen.generateRequest(template, myInputData.getRecord(ID));
            response = myReqGen.performRequest();
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

}
