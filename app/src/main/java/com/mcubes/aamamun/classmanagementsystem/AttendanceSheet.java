package com.mcubes.aamamun.classmanagementsystem;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mcubes.aamamun.classmanagementsystem.holder.StudentHolder;
import com.mcubes.aamamun.classmanagementsystem.holder.StudentHolder2;
import com.mcubes.aamamun.classmanagementsystem.model.Student;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AttendanceSheet extends AppCompatActivity {

    private boolean isDataExist=false;
    private Storage storage;
    private Bundle bndl;
    private static ActionBar ab;

    private RecyclerView recyclerView;
    private LinearLayout notFoundLay;
    private LinearLayout pb;
    private ProgressDialog pd;

    private DatabaseReference loadStuRef, loadStuRef2, loadExistIdRef, loadExistIdRef2, aSheetRef;
    private ValueEventListener listener;
    private FirebaseRecyclerAdapter<Student,StudentHolder2> adapter;

    private Map<String,Boolean> sIdSet, attendInfo=null;
    private  Map<String, Object> stuInfo;
    //private static int img_bk[] = {R.drawable.cir_shp_alpha1,R.drawable.cir_shp_alpha2,R.drawable.cir_shp_alpha3,R.drawable.cir_shp_alpha4};
    //private static int textColor[] = {Color.parseColor("#328b23"), Color.parseColor("#f25347"),Color.parseColor("#845be1"),Color.parseColor("#8fa908")};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_sheet);

        bndl = getIntent().getExtras();
        storage = new Storage(this);

        sIdSet = new HashMap<>();

        ab = getSupportActionBar();
        ab.setTitle("Attendance Sheet");
        ab.setSubtitle("Date : "+bndl.get("ACCESS_DATE"));

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        init();


    }


    private void init()
    {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notFoundLay = (LinearLayout) findViewById(R.id.notFoundLay);
        pb = (LinearLayout) findViewById(R.id.pb);

        //Toast.makeText(getBaseContext(),"Class Id : "+bndl.getString("CLASS_ID")+"\nDATE_ID : "+bndl.getString("DATE_ID"),Toast.LENGTH_LONG).show();

        //loadExistId();
        fetchAndSetStudentData();


    }


    /*
    private void loadExistId(){

        loadExistIdRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance").child(bndl.getString("DATE_ID")).child("sheet");
        loadExistIdRef.keepSynced(true);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    try {
                        sIdSet = (Map<String, Boolean>) dataSnapshot.getValue();
                        // Toast.makeText(getBaseContext(),"Map : "+sIdSet.toString(),Toast.LENGTH_LONG).show();
                    }catch(Exception ex){
                        Toast.makeText(getBaseContext(),"Ex : \n"+ex.toString(),Toast.LENGTH_LONG).show();
                    }
                }
                fetchAndSetStudentData();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        loadExistIdRef.addListenerForSingleValueEvent(listener);

    }
    */


    private void fetchAndSetStudentData()
    {

        notFoundLay.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);


        loadStuRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("students");
        loadStuRef.keepSynced(true);
        aSheetRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance").child(bndl.getString("DATE_ID")).child("sheet");
        aSheetRef.keepSynced(true);
        loadExistIdRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance").child(bndl.getString("DATE_ID")).child("sheet");
        loadExistIdRef.keepSynced(true);

        adapter = new FirebaseRecyclerAdapter<Student, StudentHolder2>(
                Student.class,
                R.layout.student_holder2,
                StudentHolder2.class,
                loadStuRef
        ) {
            @Override
            protected void populateViewHolder(final StudentHolder2 vh, final Student model, int position) {

                isDataExist = true;
                pb.setVisibility(View.GONE);
                notFoundLay.setVisibility(View.GONE);

                try{

                   // vh.imgBk.setBackgroundResource(img_bk[position%img_bk.length]);
                   // vh.firstCharOfNam.setText(""+model.getName().toUpperCase().charAt(0));
                   // vh.firstCharOfNam.setTextColor(textColor[position%textColor.length]);

                    if(storage.isImageViewAtAttendanceSheet()) {


                        Picasso.with(getBaseContext()).load(model.getImg()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_user).into(vh.sImg, new Callback() {
                            @Override
                            public void onSuccess() {}

                            @Override
                            public void onError() {
                                try {
                                    Picasso.with(getBaseContext()).load(model.getImg()).placeholder(R.drawable.default_user).into(vh.sImg);
                                }catch (Exception ex){
                                    Toast.makeText(getBaseContext(),"Can't load image",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    //vh.sId.setTypeface(tf);
                    //vh.sName.setTypeface(tf);

                    if((""+model.getId()).length()==1) {
                        vh.sId.setText("0" + model.getId());
                    }else{
                        vh.sId.setText("" + model.getId());
                    }
                    vh.sName.setText(""+model.getName());


                   // Toast.makeText(getBaseContext(),"Map -> : "+sIdSet.toString(),Toast.LENGTH_LONG).show();


                    try{


                        String id = ""+model.getId();
                        if(id.length()==1){
                            id="0"+id;
                        }
                        loadExistIdRef.child(id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    if((boolean)dataSnapshot.getValue()){
                                        vh.cb.setChecked(true);
                                    }else{
                                        vh.cb.setChecked(false);
                                    }
                                }else{
                                    vh.cb.setChecked(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }catch (Exception ex){
                         Toast.makeText(getBaseContext(),"Ex : \n"+ex.toString(),Toast.LENGTH_SHORT).show();
                    }

                    /*
                    try {
                        if (sIdSet != null) {

                            if (sIdSet.containsKey("" + model.getId()) && sIdSet.get("" + model.getId())) {
                                vh.cb.setChecked(true);
                            } else {
                                vh.cb.setChecked(false);
                            }
                        }
                    }catch (Exception ex){
                        Toast.makeText(getBaseContext(),"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
                    }
                    */



                    vh.cb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Toast.makeText(getBaseContext(),"Value : "+vh.cb.isChecked(),Toast.LENGTH_SHORT).show();
                            String id = ""+model.getId();
                            if(id.length()==1){
                                id="0"+id;
                            }
                            aSheetRef.child(id).setValue(vh.cb.isChecked());
                            sIdSet.put(""+model.getId(),vh.cb.isChecked());
                            aSheetRef.keepSynced(true);
                        }
                    });




                }catch (Exception ex){
                    Toast.makeText(getBaseContext(),"Data not found",Toast.LENGTH_SHORT).show();
                }


            }
        };

        /*
        if(adapter.getItemCount()==0){
            pb.setVisibility(View.GONE);
            notFoundLay.setVisibility(View.VISIBLE);
        }
        */

        recyclerView.setAdapter(adapter);
        new Handler().postDelayed(delay,1000*20*1);

    }


    private Runnable delay = new Runnable() {
        @Override
        public void run() {
            pb.setVisibility(View.GONE);
            if(isDataExist) {
                notFoundLay.setVisibility(View.GONE);
            }else{
                notFoundLay.setVisibility(View.VISIBLE);
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        m.add("Send Report");
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if(mi.getItemId()==android.R.id.home){
            this.finish();
        }else if(mi.getTitle().equals("Send Report")){
            sendReport();
        }
        return super.onOptionsItemSelected(mi);
    }



    private void sendReport()
    {
        loadStuRef2 = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("students");
        loadExistIdRef2 = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance").child(bndl.getString("DATE_ID")).child("sheet");

        loadStuRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    stuInfo = (Map<String, Object>) dataSnapshot.getValue();

                    loadExistIdRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){
                                attendInfo = (Map<String, Boolean>) dataSnapshot.getValue();
                                //Toast.makeText(getBaseContext(),"Stu_Info : \n"+stuInfo.toString(),Toast.LENGTH_LONG).show();
                                //Toast.makeText(getBaseContext(),"Attend_Info : \n"+attendInfo.toString(),Toast.LENGTH_LONG).show();
                                sendReportToAbsentStudent();
                            }else{
                                Toast.makeText(getBaseContext(),"All Student Absent",Toast.LENGTH_SHORT).show();
                                sendReportToAbsentStudent();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else {
                    Toast.makeText(getBaseContext(),"Student not found to send report",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void sendReportToAbsentStudent()
    {

        AlertDialog.Builder alb =  new AlertDialog.Builder(this);
        alb.setTitle("Warning !");
        alb.setMessage("Do you want to send report using Message? This may cost your money.");
        alb.setPositiveButton("Done", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {}

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 101);
                }else {
                    getTextMessage();
                }
            }
        });
        alb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alb.show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==101){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getTextMessage();
            }else{
                Toast.makeText(getBaseContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getTextMessage()
    {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_sms);
        final EditText et_sms = (EditText) dialog.findViewById(R.id.sms);
        Button doneBtn = (Button) dialog.findViewById(R.id.doneBtn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancleBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et_sms.setError(null);
                String sms = et_sms.getText().toString().trim();
                if(sms.length()==0){
                    et_sms.setError("Message length should be 1 to 250");
                }else{
                    dialog.dismiss();
                    sendMessage(sms);
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void sendMessage(String sms)
    {

        String report_to = "";
        int count_send_report = 0;

        try {

            if(attendInfo!=null) {

                Set<String> stu_ids = stuInfo.keySet();
                for (String id : stu_ids) {

                    if (!(attendInfo.containsKey(id) && attendInfo.get(id) == true) ) {

                        Map<String, Intent> stu_int_val = (Map<String, Intent>) stuInfo.get(id);
                        Map<String, String> stu_str_val = (Map<String, String>) stuInfo.get(id);

                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(stu_str_val.get("phone"), null, sms, null, null);
                            report_to += "ID : " + stu_int_val.get("id") + "\nName : " + stu_str_val.get("name") + "\n\n";
                        } catch (Exception ex) {
                            Toast.makeText(getBaseContext(), "Can't send sms", Toast.LENGTH_SHORT).show();
                        }
                        count_send_report++;

                        //Toast.makeText(getBaseContext(), "Name : " + stu.get("name"),Toast.LENGTH_SHORT).show();
                    }
                }

            }else{

                Set<String> stu_ids = stuInfo.keySet();
                for (String id : stu_ids) {

                    Map<String, Intent> stu_int_val = (Map<String, Intent>) stuInfo.get(id);
                    Map<String, String> stu_str_val = (Map<String, String>) stuInfo.get(id);

                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(stu_str_val.get("phone"), null, sms, null, null);
                        report_to += "ID : " + stu_int_val.get("id") + "\nName : " + stu_str_val.get("name") + "\n\n";
                    } catch (Exception ex) {
                        Toast.makeText(getBaseContext(), "Can't send sms", Toast.LENGTH_SHORT).show();
                    }
                    count_send_report++;
                }
                
            }

        }catch (Exception ex){
            Toast.makeText(getBaseContext(),"Ex : "+ex,Toast.LENGTH_SHORT).show();
        }

        if(report_to.length()!=0) {
            alert("Report send to", report_to);
        }

        Toast.makeText(getBaseContext(),"Total Absent Student : "+count_send_report,Toast.LENGTH_SHORT).show();

    }


    private void alert(String title, String message){

        AlertDialog.Builder alb = new AlertDialog.Builder(this);
        alb.setTitle(title);
        alb.setMessage(message);
        alb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alb.show();
    }





    @Override
    protected void onDestroy() {

        /*
        if(loadStuRef!=null){
            loadStuRef.onDisconnect();
        }

        if(aSheetRef!=null){
            aSheetRef.onDisconnect();
        }

        if(loadExistIdRef!=null){
            loadExistIdRef.onDisconnect();
        }
        */

        super.onDestroy();
        //loadExistIdRef.removeEventListener(listener);
    }
}
