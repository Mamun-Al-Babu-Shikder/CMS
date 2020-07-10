package com.mcubes.aamamun.classmanagementsystem.service;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by A.A.MAMUN on 3/22/2019.
 */

public class AdmobAdService {

    private InterstitialAd mInterstitialAd;

    private Context ctx;

    public AdmobAdService(Context ctx){
        this.ctx = ctx;
        reloadAd();
    }

    public void reloadAd(){
        MobileAds.initialize(ctx,"ca-app-pub-3578920841741120~6856771711");
        mInterstitialAd = new InterstitialAd(ctx);
        mInterstitialAd.setAdUnitId("ca-app-pub-3578920841741120/1576524878");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public boolean isAdLoaded(){
        return mInterstitialAd!=null && mInterstitialAd.isLoaded();
    }

    public void showAd(){
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
            reloadAd();
        }
    }


}
