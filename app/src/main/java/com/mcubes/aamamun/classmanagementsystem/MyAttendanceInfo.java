package com.mcubes.aamamun.classmanagementsystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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


public class MyAttendanceInfo extends Fragment {


    private static Context ctx;
    private static View mView;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_my_attendance_info, container, false);

        bndl = getArguments();
        clsId = bndl.getString("CLASS_ID");
        stuId = ""+bndl.getString("STUDENT_ID");
        if(stuId.length()==1){
            stuId="0"+stuId;
        }

        //Toast.makeText(ctx,"CLASS_ID : "+clsId+", STU_ID : "+stuId,Toast.LENGTH_SHORT).show();

        dateList = new ArrayList<>();
        dateMap = new HashMap<>();

        init();
        fetchStudentData();
        fetchAttendanceData();


        return mView;
    }







    private void init()
    {
        sImg = (ShapesImage) mView.findViewById(R.id.sImg);
        sId = (TextView) mView.findViewById(R.id.sId);
        sName = (TextView) mView.findViewById(R.id.sName);
        sSex = (TextView) mView.findViewById(R.id.sSex);
        sPhone = (TextView) mView.findViewById(R.id.sPhone);
        tClass = (TextView) mView.findViewById(R.id.tClass);
        aClass = (TextView) mView.findViewById(R.id.aClass);
        pClass = (TextView) mView.findViewById(R.id.pClass);
        percentViw = (TextView) mView.findViewById(R.id.percentViw);
        percentPb = (ProgressBar) mView.findViewById(R.id.percentPb);


        listView = (ListView) mView.findViewById(R.id.listView);



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

                    Picasso.with(ctx).load(student.getImg()).networkPolicy(NetworkPolicy.OFFLINE).into(sImg, new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(student.getImg()).into(sImg);
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
                        Toast.makeText(ctx,"Ex : \n"+ex.toString(),Toast.LENGTH_LONG).show();
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

                viw = LayoutInflater.from(ctx).inflate(R.layout.attend_container ,null);
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
    public void onDetach() {
        super.onDetach();
    }

}
