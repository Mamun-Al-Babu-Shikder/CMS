package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 2/17/2019.
 */

public class JoinReqStatus {

    private String id,status;

    public JoinReqStatus() {
    }

    public JoinReqStatus(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
