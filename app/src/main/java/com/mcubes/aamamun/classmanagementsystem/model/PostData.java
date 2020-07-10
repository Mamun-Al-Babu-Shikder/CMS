package com.mcubes.aamamun.classmanagementsystem.model;



public class PostData
{
    private String poster_id, post_img, img_name, post_time, post_stext, post_btext ,post_link;

    public PostData() {
    }

    public PostData(String img_name, String post_btext, String post_img, String post_link, String post_stext, String post_time, String poster_id) {
        this.img_name = img_name;
        this.post_btext = post_btext;
        this.post_img = post_img;
        this.post_link = post_link;
        this.post_stext = post_stext;
        this.post_time = post_time;
        this.poster_id = poster_id;
    }

    public String getImg_name() {
        return img_name;
    }

    public void setImg_name(String img_name) {
        this.img_name = img_name;
    }

    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }

    public String getPost_btext() {
        return post_btext;
    }

    public void setPost_btext(String post_btext) {
        this.post_btext = post_btext;
    }

    public String getPost_img() {
        return post_img;
    }

    public void setPost_img(String post_img) {
        this.post_img = post_img;
    }

    public String getPost_link() {
        return post_link;
    }

    public void setPost_link(String post_link) {
        this.post_link = post_link;
    }

    public String getPost_stext() {
        return post_stext;
    }

    public void setPost_stext(String post_stext) {
        this.post_stext = post_stext;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

}
