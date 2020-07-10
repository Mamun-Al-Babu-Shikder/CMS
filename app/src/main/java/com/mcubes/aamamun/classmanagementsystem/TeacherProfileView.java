package com.mcubes.aamamun.classmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


public class TeacherProfileView extends Fragment {

    private static Context ctx;
    private View mView;

    private Bundle bndl;
    private ShapesImage uImg;
    private static ImageView call_btn, sms_btn;
    private TextView userName, userEmail, uName, uSex, uDob, uAddress, uEducation, uWork, uEmail, uPhone;

    private DatabaseReference userRef;
    private ValueEventListener listener;

    private static String sex[] ={"Male", "Female","Others"};
    private String phnNumber;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_teacher_profile_view, container, false);


        bndl = getArguments();
        init();
        featchAndSetUserInfo();

        return mView;
    }




    private void init()
    {
        call_btn = (ImageView) mView.findViewById(R.id.call_btn);
        sms_btn = (ImageView) mView.findViewById(R.id.sms_btn);

        uImg = (ShapesImage) mView.findViewById(R.id.uImg);
        userName = (TextView) mView.findViewById(R.id.userName);
        userEmail = (TextView) mView.findViewById(R.id.userEmail);
        uName = (TextView) mView.findViewById(R.id.uName);
        uSex = (TextView) mView.findViewById(R.id.uSex);
        uDob = (TextView) mView.findViewById(R.id.uDob);
        uAddress = (TextView) mView.findViewById(R.id.uAddress);
        uEducation = (TextView) mView.findViewById(R.id.uEducation);
        uWork = (TextView) mView.findViewById(R.id.uWork);
        uEmail = (TextView) mView.findViewById(R.id.uEmail);
        uPhone = (TextView) mView.findViewById(R.id.uPhone);



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

                        Picasso.with(ctx).load(u.getImgUrl()).placeholder(R.drawable.user).into(uImg);
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


                        call_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                makeCall();
                            }
                        });

                        sms_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendSms();
                            }
                        });

                    }catch(Exception ex){
                        Toast.makeText(ctx,"Error : \n"+ex.toString(),Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(ctx,"User info not found",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(listener);
    }

    public void makeCall()
    {
        if(uPhone.getText().toString().length()!=0) {
            Intent next = new Intent(Intent.ACTION_DIAL);
            next.setData(Uri.parse("tel:" +uPhone.getText().toString()));
            startActivity(next);
        }else{
            Toast.makeText(ctx,"Phone number not found",Toast.LENGTH_LONG).show();
        }

    }

    public void sendSms()
    {

        if(uPhone.getText().toString().length()!=0) {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address"  , uPhone.getText().toString());
            smsIntent.putExtra("sms_body"  , "");
            startActivity(smsIntent);
        }else{
            Toast.makeText(ctx,"Phone number not found",Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public void onDetach() {
        userRef.removeEventListener(listener);
        super.onDetach();
    }


}
