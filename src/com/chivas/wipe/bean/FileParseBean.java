package com.chivas.wipe.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileParseBean {

    private String type;            // TextView
    private String name;            // tvTitle
    private String value;           // R.id.tv_title

    private List<String> resList;   // R.id.tv_title, R.id.edt_name
    private String method;          // onClickTitle
    private boolean hasParameter;   // View view

    public FileParseBean() {
        //
    }

    public FileParseBean(String type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public FileParseBean(List<String> resList, String method, boolean hasParameter) {
        this.method = method;
        this.hasParameter = hasParameter;
        this.resList = resList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getResList() {
        return resList == null ? new ArrayList<String>() : resList;
    }

    public void setResList(List<String> resList) {
        this.resList = resList;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isHasParameter() {
        return hasParameter;
    }

    public void setHasParameter(boolean hasParameter) {
        this.hasParameter = hasParameter;
    }

    public String formatBindView(String format) {
        return String.format(Locale.CHINA, format, name, type, value);
    }

    public List<String> formatOnClick(String format) {
        List<String> result = new ArrayList<>();
        if (resList == null) {
            return result;
        }

        for (String resId : resList) {
            result.add(String.format(format, resId, method, hasParameter ? "v" : ""));
        }
        return result;
    }
}
