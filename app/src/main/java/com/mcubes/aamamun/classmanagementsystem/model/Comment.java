package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 3/1/2019.
 */

public class Comment {

    private String commenter_id,date,comment;

    public Comment() {
    }

    public Comment(String comment, String commenter_id, String date) {
        this.comment = comment;
        this.commenter_id = commenter_id;
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommenter_id() {
        return commenter_id;
    }

    public void setCommenter_id(String commenter_id) {
        this.commenter_id = commenter_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
