package com.mcubes.aamamun.classmanagementsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.mcubes.aamamun.classmanagementsystem.service.AdmobAdService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;

import org.w3c.dom.Text;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private static Storage storage;
    private View navView;
    private ActionBarDrawerToggle toggle;
    private Button closeBtn;
    private TextView userName, userEmail;
    private ShapesImage userImg;
    private LinearLayout optHome, optProfile, optMyClassRoom, optTeacherClassRoom;
    boolean isHome = false;

    private static DatabaseReference userRef;
    private static Task<Void> token_ref;
    private static ValueEventListener listener;

    public AdmobAdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        storage = new Storage(this);


        //-------- Ad Service ---------//
        adService = new AdmobAdService(this);


        sendDeviceToken();
        initialization();


        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_navigation_drawer, R.string.close_navigation_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();




    }

    private void sendDeviceToken(){

        String device_token =  FirebaseInstanceId.getInstance().getToken();
        token_ref = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("device_token").setValue(device_token);
    }



    private void initialization()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navView = navigationView.inflateHeaderView(R.layout.nav_header);

        userImg = (ShapesImage) navView.findViewById(R.id.userImage);
        userName = (TextView) navView.findViewById(R.id.userName);
        userEmail = (TextView) navView.findViewById(R.id.userEmail);

        setUserInfo();

        closeBtn = (Button) navView.findViewById(R.id.closeDrawerBtn);
        optHome = (LinearLayout) navView.findViewById(R.id.optHome);
        optProfile = (LinearLayout) navView.findViewById(R.id.optProfile);
        optMyClassRoom = (LinearLayout) navView.findViewById(R.id.optMyClassRoom);
        optTeacherClassRoom = (LinearLayout) navView.findViewById(R.id.optTeacherClassRoom);




        setHomeFragment();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        optHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHomeFragment();
            }
        });

        optProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, new ProfileFragment()).commit();
                isHome = false;
                drawerLayout.closeDrawer(GravityCompat.START);

               // adService.showAd();

            }
        });

        optMyClassRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, new MyClassRoom()).commit();
                isHome = false;
                drawerLayout.closeDrawer(GravityCompat.START);


                adService.showAd();
            }
        });

        optTeacherClassRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, new TeachersClassRoom()).commit();
                isHome = false;
                drawerLayout.closeDrawer(GravityCompat.START);

                adService.showAd();
            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem mi) {

                if(mi.getItemId()==R.id.optFeedBack){

                    SendFeedback feedback = new SendFeedback();
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,feedback).commit();
                    isHome = false;
                    drawerLayout.closeDrawer(GravityCompat.START);

                }else if(mi.getItemId()==R.id.optAboutUs){

                    AboutUs fragment = new AboutUs();
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,fragment).commit();
                    isHome = false;
                    drawerLayout.closeDrawer(GravityCompat.START);

                   // adService.showAd();

                }else if(mi.getItemId()==R.id.optLogout){

                    //Toast.makeText(MainActivity.this,"Logout",Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alb = new AlertDialog.Builder(MainActivity.this);
                    alb.setTitle("Logout");
                    alb.setMessage("Do you want to logout ?");
                    alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            storage.setLogin(false);
                            MainActivity.this.finish();
                            Intent next = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(next);
                        }
                    });
                    alb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    alb.show();
                }
                return true;
            }
        });

    }

    private void setUserInfo()
    {
       // Toast.makeText(getBaseContext(),"UID : "+storage.getUID(),Toast.LENGTH_LONG).show();

        userRef = FirebaseDatabase.getInstance().getReference().child("user").child(storage.getUID()).child("profile");
        userRef.keepSynced(true);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                if(dataSnapshot.exists()){

                    //Toast.makeText(getBaseContext(),"User data : "+dataSnapshot.getValue().toString(),Toast.LENGTH_LONG).show();


                    final User user = dataSnapshot.getValue(User.class);
                    Picasso.with(MainActivity.this).load(user.getImgUrl()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_user).into(userImg, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(MainActivity.this).load(user.getImgUrl()).placeholder(R.drawable.default_user).into(userImg);
                        }
                    });

                    userName.setText(""+user.getName());
                    userEmail.setText(""+user.getEmail());
                }else{
                    Toast.makeText(getBaseContext(),"User data not found",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        userRef.addValueEventListener(listener);


    }

    private void setHomeFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, new HomeFragment()).commit();
        isHome = true;
        drawerLayout.closeDrawer(GravityCompat.START);

        //adService.showAd();
    }


    public void optionMenuBtn (View v){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else if(isHome){
            this.finish();
        }else{
           setHomeFragment();
        }
    }

    @Override
    protected void onDestroy() {
        userRef.removeEventListener(listener);
        userRef.onDisconnect();
        super.onDestroy();
    }
}
