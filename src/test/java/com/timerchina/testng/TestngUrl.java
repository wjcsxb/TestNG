package com.timerchina.testng;

import com.timerchina.utils.JsonUtils;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestngUrl {

    @Test
    public void TestJson() throws IOException {
        String size = "20";
        Reporter.log("期望用例size:"+size);
        HttpResponse httpResponse = new HttpResponse();
        String s = httpResponse.getHttpRespone();
        Reporter.log("请求接口路径:"+httpResponse.getUrl());
        Reporter.log("请求结果:"+s);
        String actualSize = JsonUtils.getJsonValue(s,"size");
        Reporter.log("用例结果: resultSize=>expected: " + size + " ,actual: "+ actualSize);
        Assert.assertEquals(actualSize,size);
    }
}

