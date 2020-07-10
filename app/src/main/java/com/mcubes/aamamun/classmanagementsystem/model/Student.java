package com.mcubes.aamamun.classmanagementsystem.model;

/**
 * Created by A.A.MAMUN on 2/13/2019.
 */

public class Student
{
    private String uid,name,phone,img;
    private int id,sex;

    public Student() {
    }


    public Student(int id, String img, String name, String phone, int sex, String uid) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.uid = uid;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", img='" + img + '\'' +
                ", sex=" + sex +
                '}';
    }
}
