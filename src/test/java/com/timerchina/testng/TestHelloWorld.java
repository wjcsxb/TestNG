package com.timerchina.testng;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestHelloWorld {

    @Test
    public void runOtherTest1() {
        System.out.println("@Test - runOtherTest1");
    }

    @Test
    public void runOtherTest2() {
        System.out.println("@Test - runOtherTest2");
    }

    @Test
    public void testUrl(){
        HttpResponse httpResponse = new HttpResponse();
        try {
            String s = httpResponse.getHttpRespone();
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
