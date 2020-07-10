package com.mcubes.aamamun.classmanagementsystem;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;

public class StudentProfileView extends AppCompatActivity {

    private Bundle bndl;
    private ShapesImage uImg;
    private TextView userName, userEmail, uName, uSex, uDob, uAddress, uEducation, uWork, uEmail, uPhone;

    private DatabaseReference userRef;
    private ValueEventListener listener;

    private static String sex[] ={"Male", "Female","Others"};
    private String phnNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile_view);
        getSupportActionBar().hide();

        bndl = getIntent().getExtras();
        init();
        featchAndSetUserInfo();

    }

    private void init()
    {
        uImg = (ShapesImage) findViewById(R.id.uImg);
        userName = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.userEmail);
        uName = (TextView) findViewById(R.id.uName);
        uSex = (TextView) findViewById(R.id.uSex);
        uDob = (TextView) findViewById(R.id.uDob);
        uAddress = (TextView) findViewById(R.id.uAddress);
        uEducation = (TextView) findViewById(R.id.uEducation);
        uWork = (TextView) findViewById(R.id.uWork);
        uEmail = (TextView) findViewById(R.id.uEmail);
        uPhone = (TextView) findViewById(R.id.uPhone);

    }

    private void featchAndSetUserInfo()
    {

        userRef = FirebaseDatabase.getInstance().getReference("user").child(bndl.getString("UID")).child("profile");
        userRef.keepSynced(true);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    try {
                        User u = dataSnapshot.getValue(User.class);

                        Picasso.with(StudentProfileView.this).load(u.getImgUrl()).placeholder(R.drawable.user).into(uImg);
                        userName.setText(u.getName());
                        userEmail.setText(u.getEmail());
                        uName.setText(u.getName());
                        uSex.setText(sex[u.getSex()]);
                        uDob.setText(u.getDob());
                        uAddress.setText(u.getAddress());
                        uEducation.setText(u.getEducation());
                        uWork.setText(u.getWork());
                        uEmail.setText(u.getEmail());
                        uPhone.setText(u.getPhone());

                    }catch(Exception ex){
                        Toast.makeText(getBaseContext(),"Error : \n"+ex.toString(),Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getBaseContext(),"User info not found",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(listener);
    }

    public void makeCall(View v)
    {
        if(uPhone.getText().toString().length()!=0) {
            Intent next = new Intent(Intent.ACTION_DIAL);
            next.setData(Uri.parse("tel:" +uPhone.getText().toString()));
            startActivity(next);
        }else{
            Toast.makeText(getBaseContext(),"Phone number not found",Toast.LENGTH_LONG).show();
        }

    }

    public void sendSms(View v)
    {

        if(uPhone.getText().toString().length()!=0) {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address"  , uPhone.getText().toString());
            smsIntent.putExtra("sms_body"  , "");
            startActivity(smsIntent);
        }else{
            Toast.makeText(getBaseContext(),"Phone number not found",Toast.LENGTH_LONG).show();
        }

    }

    public void gotoBackBtn(View v) {
        StudentProfileView.this.finish();
    }

    @Override
    protected void onDestroy() {
        if(userRef!=null){
            userRef.removeEventListener(listener);
        }
        super.onDestroy();
        //Toast.makeText(getBaseContext(),"OnDestry()",Toast.LENGTH_LONG).show();

    }
}
