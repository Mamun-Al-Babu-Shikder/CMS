package com.mcubes.aamamun.classmanagementsystem;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mcubes.aamamun.classmanagementsystem.service.AdmobAdService;

import java.io.FileFilter;


public class Main3Activity extends AppCompatActivity {

    private Storage storage;
    private Bundle bndl;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View navView;
    private Button closeDrawerBtn;

    private boolean isHome = true;

    private AdmobAdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        getSupportActionBar().hide();
        bndl = getIntent().getExtras();
        //Toast.makeText(getBaseContext(),"STU_ID"+bndl.getString("STU_ID"),Toast.LENGTH_SHORT).show();
        storage = new Storage(this);

        //-------- Ad Service ---------//
        adService = new AdmobAdService(this);

        initialization();

    }

    private void initialization() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navView = navigationView.inflateHeaderView(R.layout.nav_header2);
        closeDrawerBtn = (Button) navView.findViewById(R.id.closeDrawerBtn);

        ((TextView) navView.findViewById(R.id.cls_id)).setText(""+bndl.getString("CLASS_ID"));
        ((TextView) navView.findViewById(R.id.cCode)).setText(""+bndl.getString("CLASS_CODE"));
        ((TextView) navView.findViewById(R.id.cTitle)).setText(""+bndl.getString("CLASS_TITLE"));

        closeDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        homePage();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem mi) {

                navigationView.setCheckedItem(mi.getItemId());

                if(mi.getItemId()==R.id.opt_home){

                    if(!isHome) {
                        homePage();
                    }
                }else if(mi.getItemId()==R.id.opt_post){

                    PostFragment fragment = new PostFragment();
                    bndl.putString("POSTER_TYPE","Student");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    isHome = false;

                }else if(mi.getItemId()==R.id.opt_student) {

                    StudentFragment fragment = new StudentFragment();
                    bndl.putString("ACCESS_PERSON", "STUDENT");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    isHome = false;

                }else if(mi.getItemId()==R.id.opt_attendance_info){

                    MyAttendanceInfo fragment = new MyAttendanceInfo();
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    isHome = false;

                    adService.showAd();

                }else if(mi.getItemId()==R.id.opt_teacher_info){

                    TeacherProfileView fragment = new TeacherProfileView();
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    isHome = false;

                }else if(mi.getItemId()==R.id.opt_files){

                    FilesFragment fragment = new FilesFragment();
                    bndl.putInt("ACCESS_TYPE", FilesFragment.STUDENT);
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    isHome = false;

                }else if(mi.getItemId()==R.id.opt_google_drive){

                    GoogleDrive fragment = new GoogleDrive();
                    bndl.putString("ACCESS_PERSON", "STUDENT");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    isHome = false;

                    adService.showAd();

                }

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

    }

    public void optionMenuBtn(View v) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void homePage()
    {
        NewsFeed fragment = new NewsFeed();
        bndl.putString("POSTER_TYPE","Student");
        fragment.setArguments(bndl);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
        navigationView.setCheckedItem(R.id.opt_home);
        isHome = true;
    }



    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else if(!isHome){
            homePage();
        }else{
            super.onBackPressed();
        }
    }


}
