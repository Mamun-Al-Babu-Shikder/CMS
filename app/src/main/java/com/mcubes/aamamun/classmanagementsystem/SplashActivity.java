package com.mcubes.aamamun.classmanagementsystem;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {


    private static Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        storage = new Storage(this);
        new Handler().postDelayed(delay,2000);


    }

    private Runnable delay = new Runnable() {
        @Override
        public void run() {

            if(storage.isLogin()){
                Intent next = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(next);
            }else{
                Intent next = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(next);
            }

            SplashActivity.this.finish();
        }
    };


}
