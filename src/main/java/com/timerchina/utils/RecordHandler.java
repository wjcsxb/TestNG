package com.timerchina.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecordHandler {

    private enum RecordType {
        VALUE, NAMED_MAP, INDEXED_LIST
    }

    private String singleValue = "";
    private HashMap<String, String> namedValueMap = new HashMap<>();
    private List<String> indexedValueList = new ArrayList<>();
    private RecordType myType;

    public RecordHandler() {
        this("");
    }

    public RecordHandler(String value) {

        this.myType = RecordType.VALUE;
        this.singleValue = value;

    }

    public RecordHandler(HashMap<String, String> map) {

        this.myType = RecordType.NAMED_MAP;
        this.namedValueMap = map;

    }

    public RecordHandler(List<String> list) {

        this.myType = RecordType.INDEXED_LIST;
        this.indexedValueList = list;

    }

    public HashMap<String, String> getMap() {
        return namedValueMap;
    }

    public int size() {
        int result = 0;

        if(myType.equals(RecordType.VALUE)) {
            result = 1;
        } else if(myType.equals(RecordType.NAMED_MAP)) {
            result = namedValueMap.size();
        } else if(myType.equals(RecordType.INDEXED_LIST)) {
            result = indexedValueList.size();
        }

        return result;
    }

    public String get() {
        String result = "";

        if(myType.equals(RecordType.VALUE)) result = singleValue;
        else {
            System.out.println("Called get() on wrong type:" + myType.toString());
        }

        return result;
    }

    public String get(String key) {
        String result = "";

        if(myType.equals(RecordType.NAMED_MAP)) result = namedValueMap.get(key);

        return result;
    }

    public String get(Integer index) {
        String result = "";

        if(myType.equals(RecordType.INDEXED_LIST)) result = indexedValueList.get(index);

        return result;
    }

    public Boolean set(String value) {
        Boolean result = false;

        if(myType.equals(RecordType.VALUE)) {
            this.singleValue = value;
            result = true;
        } else if(myType.equals(RecordType.INDEXED_LIST)) {
            this.indexedValueList.add(value);
            result = true;
        }

        return result;
    }

    public Boolean set(String key, String value) {
        Boolean result = false;

        if(myType.equals(RecordType.NAMED_MAP)) {
            this.namedValueMap.put(key, value);
            result = true;
        }

        return result;
    }

    public Boolean set(Integer index, String value) {
        Boolean result = false;

        if(myType.equals(RecordType.INDEXED_LIST)) {
            if(this.indexedValueList.size() > index) this.indexedValueList.set(index, value);

            result = true;
        }

        return result;
    }

    public Boolean has(String value) {
        Boolean result = false;

        if(myType.equals(RecordType.VALUE) && this.singleValue.equals(value)) {
            result = true;
        } else if(myType.equals(RecordType.NAMED_MAP) && this.namedValueMap.containsKey(value)) {
            result = true;
        } else if(myType.equals(RecordType.INDEXED_LIST) && this.indexedValueList.contains(value)) {
            result = true;
        }

        return result;
    }

    public Boolean remove(String value) {
        Boolean result = false;

        if(myType.equals(RecordType.VALUE) && this.singleValue.equals(value)) {
            this.singleValue = "";
            result = true;
        }
        if(myType.equals(RecordType.NAMED_MAP) && this.namedValueMap.containsKey(value)) {
            this.namedValueMap.remove(value);
            result = true;
        } else if(myType.equals(RecordType.INDEXED_LIST) && this.indexedValueList.contains(value)) {
            this.indexedValueList.remove(value);
            result = true;
        }

        return result;
    }

    public Boolean remove(Integer index) {
        Boolean result = false;

        if(myType.equals(RecordType.INDEXED_LIST) && this.indexedValueList.contains(index)) {
            this.indexedValueList.remove(index);
            result = true;
        }

        return result;
    }
}
