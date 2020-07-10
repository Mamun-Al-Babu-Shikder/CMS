package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 2/9/2019.
 */

public class User {

    private String name,phone,email, imgUrl, dob, address, education, work;
    private int sex;

    public User(){

    }

    public User(String address, String dob, String education, String email, String imgUrl, String name, String phone, int sex, String work) {
        this.address = address;
        this.dob = dob;
        this.education = education;
        this.email = email;
        this.imgUrl = imgUrl;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.work = work;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }
}
