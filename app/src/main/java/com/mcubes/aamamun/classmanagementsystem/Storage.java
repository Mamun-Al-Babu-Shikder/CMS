package com.mcubes.aamamun.classmanagementsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by A.A.MAMUN on 2/7/2019.
 */

public class Storage {

    private static Context ctx;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor spe;

    public Storage(Context ctx){

        this.ctx = ctx;
        sp = ctx.getSharedPreferences("CMS_STORAGE",Context.MODE_PRIVATE);
        spe = sp.edit();
    }


    public void setLogin(boolean val){
        spe.putBoolean("LOGIN",val);
        c();
    }

    public boolean isLogin(){
        return sp.getBoolean("LOGIN", false);
    }

    public void setRemember(boolean val){
        spe.putBoolean("REMEMBER",val);
        c();
    }

    public boolean isRemember(){
        return sp.getBoolean("REMEMBER",false);
    }

    public void setUID(String uid){
        spe.putString("UID",uid);
        c();
    }

    public String getUID(){
        return sp.getString("UID",null);
    }

    public void setEmail(String email){
        spe.putString("EMAIL",email);
        c();
    }

    public String getEmail(){
        return sp.getString("EMAIL",null);
    }

    public void setName(String name){
        spe.putString("NAME",name);
        c();
    }

    public String getName(){
        return sp.getString("NAME",null);
    }

    public void setImgUrl(String url){
        spe.putString("IMAGE_URL",url);
        c();
    }

    public String getImageUrl(){
        return sp.getString("IMAGE_URL","unknown");
    }

    public void setPassword(String pass){
        spe.putString("PASSWORD",pass);
        c();
    }

    public String getPassword(){
        return sp.getString("PASSWORD",null);
    }

    public boolean isImageViewAtStudentList() {
        return sp.getBoolean("ImageViewAtStudentList", true);
    }

    public void setImageViewAtStudentList(boolean val) {
        spe.putBoolean("ImageViewAtStudentList", val);
        c();
    }

    public boolean isImageViewAtAttendanceSheet(){
        return  sp.getBoolean("ImageViewAtAttendanceSheet", true);
    }

    public void setImageViewAtAttendanceSheet(boolean val){
        spe.putBoolean("ImageViewAtAttendanceSheet", val);
        c();
    }


    public static boolean isNetConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info!=null && info.isConnected();
    }

    private void c(){
        spe.commit();
    }
}
