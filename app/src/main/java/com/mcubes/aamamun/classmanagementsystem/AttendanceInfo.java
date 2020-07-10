package com.mcubes.aamamun.classmanagementsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mcubes.aamamun.classmanagementsystem.model.DateAndAttend;
import com.mcubes.aamamun.classmanagementsystem.model.Student;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttendanceInfo extends AppCompatActivity {

    private static ActionBar ab;

    private Bundle bndl;
    private String clsId,stuId;
    private int i=0;

    private ShapesImage sImg;
    private TextView sName, sId,sSex, sPhone, tClass, aClass, pClass, percentViw;
    private ProgressBar percentPb;
    private ListView listView;
    private Handler hndl = new Handler();

    private DatabaseReference stuRef, attendRef;

    private List<DateAndAttend> dateList;
    private Map<String,Boolean> dateMap;

    private static String sex[] = {"Male","Female","Others"};
    private int tWorkDay=0,presentDay=0, percent=0;

    private Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_info);

        ab = getSupportActionBar();
        ab.setTitle("Attendance Info");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        bndl = getIntent().getExtras();
        clsId = bndl.getString("CLASS_ID");
        stuId = ""+bndl.getInt("STUDENT_ID");
        if(stuId.length()==1){
            stuId="0"+stuId;
        }

       // Toast.makeText(getBaseContext(),"CLASS_ID : "+clsId+", STU_ID : "+stuId,Toast.LENGTH_SHORT).show();

        dateList = new ArrayList<>();
        dateMap = new HashMap<>();

        init();
        fetchStudentData();
        fetchAttendanceData();
        //fetchAttendanceData2();
    }

    private void init()
    {
        sImg = (ShapesImage) findViewById(R.id.sImg);
        sId = (TextView) findViewById(R.id.sId);
        sName = (TextView) findViewById(R.id.sName);
        sSex = (TextView) findViewById(R.id.sSex);
        sPhone = (TextView) findViewById(R.id.sPhone);
        tClass = (TextView) findViewById(R.id.tClass);
        aClass = (TextView) findViewById(R.id.aClass);
        pClass = (TextView) findViewById(R.id.pClass);
        percentViw = (TextView) findViewById(R.id.percentViw);
        percentPb = (ProgressBar) findViewById(R.id.percentPb);


        listView = (ListView) findViewById(R.id.listView);



    }



    private void fetchStudentData()
    {
        stuRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("students").child(""+stuId);
        stuRef.keepSynced(true);

        stuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                   // Toast.makeText(getBaseContext(),"Stu : "+dataSnapshot.getValue().toString(),Toast.LENGTH_SHORT).show();

                    student = (Student) dataSnapshot.getValue(Student.class);

                    Picasso.with(getBaseContext()).load(student.getImg()).networkPolicy(NetworkPolicy.OFFLINE).into(sImg, new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError() {
                            Picasso.with(getBaseContext()).load(student.getImg()).into(sImg);
                        }
                    });

                    sId.setText("ID : "+student.getId());
                    sName.setText("Name : "+student.getName());
                    sSex.setText("Gender : "+sex[student.getSex()]);
                    sPhone.setText("Call : "+student.getPhone());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void fetchAttendanceData()
    {
        attendRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("attendance");
        attendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                tWorkDay = 0;
                presentDay = 0;
                percent = 0;
                if(dataSnapshot.exists())
                {
                    try{
                        Map<String, Object> map1 = (Map<String, Object>) dataSnapshot.getValue();
                        //Toast.makeText(getBaseContext(),"Map_01 : \n"+map1.toString().toString(),Toast.LENGTH_LONG).show();
                        Set<String> str = map1.keySet();

                        for(String s :  str){

                            Map<String, Object> map2 = (Map<String , Object>) map1.get(s);
                            //Toast.makeText(getBaseContext(),"Map_02 : \n"+map2.toString().toString(),Toast.LENGTH_LONG).show();

                            Map<String, Object> map3 = (Map<String, Object>) map2.get("sheet");
                            //Toast.makeText(getBaseContext(), "Map_03 : \n" + map3.toString().toString(), Toast.LENGTH_LONG).show();

                            if(map3!=null) {

                                if(map3.containsKey(""+stuId) && (boolean) map3.get(""+stuId)) {
                                    presentDay++;
                                    //dateMap.put((String) map2.get("date"),true );
                                    dateList.add(new DateAndAttend((String) map2.get("date"),true));
                                }else{
                                   // dateMap.put((String) map2.get("date"),false );
                                    dateList.add(new DateAndAttend((String) map2.get("date"),false));
                                }
                               // Toast.makeText(getBaseContext(), "Map_03 : \n" + map3.toString().toString(), Toast.LENGTH_LONG).show();
                            }else{
                               // dateMap.put((String) map2.get("date"),false );
                                dateList.add(new DateAndAttend((String) map2.get("date"),false));
                            }


                        }

                        //Toast.makeText(getBaseContext(),"Date Map : \n"+dateMap.toString(),Toast.LENGTH_LONG).show();

                        //tWorkDay = dateMap.size();
                        tWorkDay = (int) dataSnapshot.getChildrenCount();


                        if(tWorkDay!=0) {
                            percent = (int) ((presentDay*100)/tWorkDay);
                        }
                        displayAttendInfo();
                        //displayAttendList(dateMap);
                        displayAttendList(dateList);

                    }catch (Exception ex){
                        Toast.makeText(getBaseContext(),"Ex : \n"+ex.toString(),Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayAttendInfo()
    {
        i=0;
        tClass.setText("Total Working Day : "+tWorkDay);
        aClass.setText("Total Attend : "+presentDay);
        pClass.setText("Percentage : "+percent+"%");
        percentViw.setText(i+"%");
        percentPb.setMax(100);
        percentPb.setProgress(i);

        if(percent!=0) {
            hndl.postDelayed(delay, 7);
        }
    }


    private Runnable delay = new Runnable() {
        @Override
        public void run() {
           i++;
            if(i<percent){
                hndl.postDelayed(this,7);
            }
            percentViw.setText(i+"%");
            percentPb.setProgress(i);
        }
    };

    private void displayAttendList(final List<DateAndAttend> data)
    {


        BaseAdapter ba = new BaseAdapter() {
            @Override
            public int getCount() {
                return dateList.size();
            }

            @Override
            public Object getItem(int i) {
                return i;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View viw, ViewGroup viewGroup) {

                viw = getLayoutInflater().inflate(R.layout.attend_container ,null);
                ImageView pViewer = (ImageView) viw.findViewById(R.id.presentViewer);
                TextView dateViewer = (TextView) viw.findViewById(R.id.dateViewer);
                dateViewer.setText("Date : "+dateList.get(i).getDate());

                if(data.get(i).isBol()){
                    pViewer.setImageResource(R.drawable.ic_check_circle);
                }else{
                    pViewer.setImageResource(R.drawable.ic_circle_cross2);
                }

                return viw;
            }
        };


        listView.setAdapter(ba);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opt_menu4,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if(mi.getItemId()==android.R.id.home){
            this.finish();
        }else if(mi.getItemId()==R.id.opt_call){

            Intent next = new Intent(Intent.ACTION_DIAL);
            next.setData(Uri.parse("tel:" +student.getPhone().toString()));
            startActivity(next);

        }else if(mi.getItemId()==R.id.opt_send_sms){

            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address"  , student.getPhone().toString());
            smsIntent.putExtra("sms_body"  , "");
            startActivity(smsIntent);

        }
        return super.onOptionsItemSelected(mi);
    }
}
