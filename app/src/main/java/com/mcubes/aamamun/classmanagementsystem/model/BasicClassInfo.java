package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 2/10/2019.
 */

public class BasicClassInfo {

    private String code, title, founder;

    public BasicClassInfo(){

    }

    public BasicClassInfo(String code, String founder, String title) {
        this.code = code;
        this.founder = founder;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFounder() {
        return founder;
    }

    public void setFounder(String founder) {
        this.founder = founder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
