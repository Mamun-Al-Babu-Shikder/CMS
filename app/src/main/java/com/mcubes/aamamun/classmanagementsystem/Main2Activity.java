package com.mcubes.aamamun.classmanagementsystem;

import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mcubes.aamamun.classmanagementsystem.service.AdmobAdService;

public class Main2Activity extends AppCompatActivity //implements NavigationView.OnNavigationItemSelectedListener
{

    private Storage storage;
    private Bundle bndl;
    private boolean iSHome = false;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View navView;
    private Button closeDrawerBtn;

    private AdmobAdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        getSupportActionBar().hide();
        bndl = getIntent().getExtras();
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
                    if(!iSHome) {
                        homePage();
                    }
                }else if(mi.getItemId()==R.id.opt_post){

                    PostFragment fragment = new PostFragment();
                    bndl.putString("POSTER_TYPE","Teacher");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    iSHome = false;

                }else if(mi.getItemId()==R.id.opt_student){

                    StudentFragment fragment = new StudentFragment();
                    bndl.putString("ACCESS_PERSON","TEACHER");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    iSHome = false;

                }else if(mi.getItemId()==R.id.opt_request){

                    RequestFragment fragment = new RequestFragment();
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    iSHome = false;

                }else if(mi.getItemId()==R.id.opt_attendance){

                    AttendanceFragment fragment = new AttendanceFragment();
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    iSHome = false;

                    adService.showAd();

                }else if(mi.getItemId()==R.id.opt_files){

                    FilesFragment fragment = new FilesFragment();
                    bndl.putString("POSTER_TYPE","Teacher");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    iSHome = false;

                }else if(mi.getItemId()==R.id.opt_google_drive){

                    GoogleDrive fragment = new GoogleDrive();
                    bndl.putString("ACCESS_PERSON","TEACHER");
                    fragment.setArguments(bndl);
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                    iSHome = false;

                    adService.showAd();
                }

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

    }


    /*
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem mi) {

        navigationView.setCheckedItem(mi.getItemId());

        if(mi.getItemId()==R.id.opt_home){

        }else if(mi.getItemId()==R.id.opt_post){

        }else if(mi.getItemId()==R.id.opt_student){
            Toast.makeText(Main2Activity.this, "ID : "+mi.getItemId(),Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, new StudentFragment()).commit();
            iSHome = false;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
    */

    private void homePage()
    {
        NewsFeed fragment = new NewsFeed();
        bndl.putInt("ACCESS_TYPE",FilesFragment.TEACHER);
        fragment.setArguments(bndl);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
        navigationView.setCheckedItem(R.id.opt_home);
        iSHome = true;
    }

    public void optionMenuBtn(View v) {
        drawerLayout.openDrawer(GravityCompat.START);
    }


    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else if(!iSHome){
            homePage();
        }else{
            super.onBackPressed();
        }
    }
}
