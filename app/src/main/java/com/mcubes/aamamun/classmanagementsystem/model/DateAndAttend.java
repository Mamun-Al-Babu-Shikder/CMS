package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 2/21/2019.
 */

public class DateAndAttend {

    private String date;
    private boolean bol;

    public DateAndAttend(String date, boolean bol) {
        this.bol = bol;
        this.date = date;
    }

    public boolean isBol() {
        return bol;
    }

    public void setBol(boolean bol) {
        this.bol = bol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
