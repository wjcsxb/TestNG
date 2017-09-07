package com.timerchina.utils;

public class ValidateUtils {

    public static boolean notEmpty(String str){
        return str != null && str.trim().length() > 0;
    }
    public static boolean notEmpty(Object object){
        return object != null;
    }
    public static boolean isEmpty(String str){
        return str == null || str.trim().length() == 0;
    }
    public static boolean isEmpty(Object object){
        return object == null;
    }
}
