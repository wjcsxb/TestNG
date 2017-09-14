package com.timerchina.utils;

import java.util.*;

public class Utils {

    public static List<Map.Entry<String, RecordHandler>> sortMap(Map<String, RecordHandler> map){
        List<Map.Entry<String, RecordHandler>> list = new ArrayList<>(map.entrySet());  // 通过ArrayList构造函数把map.entrySet()转换成list
        Collections.sort(list, new Comparator<Map.Entry<String, RecordHandler>>() {        // 通过比较器实现比较排序
            public int compare(Map.Entry<String, RecordHandler> mapping1, Map.Entry<String, RecordHandler> mapping2) {
                return Integer.valueOf(mapping1.getKey()).compareTo(Integer.valueOf(mapping2.getKey()));
            }
        });

        Map<String,RecordHandler> sortMap = new HashMap<>();
        for(Map.Entry<String,RecordHandler> mapping:list){
            sortMap.put(mapping.getKey(),mapping.getValue());
        }
        return list;
    }

}
