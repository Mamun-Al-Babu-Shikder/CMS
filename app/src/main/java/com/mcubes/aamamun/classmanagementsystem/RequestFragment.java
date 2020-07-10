package com.mcubes.aamamun.classmanagementsystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mcubes.aamamun.classmanagementsystem.holder.RequestHolder;
import com.mcubes.aamamun.classmanagementsystem.model.JoinReqStatus;
import com.mcubes.aamamun.classmanagementsystem.model.Student;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;


public class RequestFragment extends Fragment {


    private boolean isDataExist = false;
    private static Storage storage;
    private Context ctx;
    private Bundle bndl;
    private View mView;

    private LinearLayout notFoundLay;
    private ProgressBar pb;
    private RecyclerView recyclerView;
    private static Button cancel, done;
    private static Dialog dialog;
    private static ProgressDialog pd;
    private Button cancelBtn, saveBtn;
    private ShapesImage sImg;
    private EditText sId, sName, sPhone;
    private RadioGroup sSex;

    private DatabaseReference loadReqRef, loadStuRef, addStuRef;
    private FirebaseRecyclerAdapter<JoinReqStatus, RequestHolder> adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        bndl = getArguments();
        storage = new Storage(ctx);
        mView = inflater.inflate(R.layout.fragment_request,container,false);

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        addStuRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("students");
        addStuRef.keepSynced(true);
        loadStuRef = FirebaseDatabase.getInstance().getReference("user");
        loadStuRef.keepSynced(true);
        loadReqRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("request");
        loadReqRef.keepSynced(true);


        init();

        return mView;
    }


    private void init()
    {
        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        pb = (ProgressBar) mView.findViewById(R.id.pb);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));


        fetchJoinedRequestAndDisplay();

    }



    private void fetchJoinedRequestAndDisplay()
    {
        pb.setVisibility(View.VISIBLE);
        notFoundLay.setVisibility(View.GONE);

        adapter = new FirebaseRecyclerAdapter<JoinReqStatus, RequestHolder>(
                JoinReqStatus.class,
                R.layout.joined_request_holder,
                RequestHolder.class,
                loadReqRef
        ) {
            @Override
            protected void populateViewHolder(final RequestHolder vh, JoinReqStatus model, int position) {

                pb.setVisibility(View.GONE);
                notFoundLay.setVisibility(View.GONE);
                isDataExist = true;

                try {
                    final String uid = adapter.getRef(position).getKey();
                    loadStuRef.child(uid).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final Student student = dataSnapshot.getValue(Student.class);
                                vh.firstCharOfNam.setText("" + student.getName().toUpperCase().charAt(0));
                                vh.stuName.setText("" + student.getName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    vh.status.setText("" + model.getStatus());


                    vh.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                            alb.setTitle("Warning !");
                            alb.setMessage("Do you want to cancel request ?");
                            alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (storage.isNetConnected()) {
                                        cancelRequest(uid);
                                    } else {
                                        alert("Connection error !", "Please check device net connection and try again.");
                                    }
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
                    });

                    vh.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            acceptRequest(uid);
                        }
                    });

                }catch (Exception ex){
                    ex.printStackTrace();
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
        new Handler().postDelayed(delay,1000*30*1);

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


    private void cancelRequest(final String uid)
    {
        pd.show();
        loadStuRef.child(uid).child("teacher_class_room").child(bndl.getString("CLASS_ID")).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loadReqRef.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        pd.hide();
                        if(task.isSuccessful()){
                          Toast.makeText(ctx,"Request Canceled",Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }
                        }
                    });
                }else{
                    pd.hide();
                    Toast.makeText(ctx,"Can't  Cancel, Please try again",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void acceptRequest(final String uid)
    {
        //Toast.makeText(ctx,"Request Accepted",Toast.LENGTH_SHORT).show();


        try {

            dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_add_student);


            sImg = (ShapesImage) dialog.findViewById(R.id.sImg);
            sId = (EditText) dialog.findViewById(R.id.sId);
            sName = (EditText) dialog.findViewById(R.id.sName);
            sSex = (RadioGroup) dialog.findViewById(R.id.sSex);
            sPhone = (EditText) dialog.findViewById(R.id.sPhone);


            cancelBtn = (Button) dialog.findViewById(R.id.cancleBtn);
            saveBtn = (Button) dialog.findViewById(R.id.doneBtn);

            pd.show();

            loadStuRef.child(uid).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){

                        pd.hide();
                        //Toast.makeText(ctx,"Data : "+dataSnapshot.getValue(),Toast.LENGTH_SHORT).show();

                        int[] sex = {R.id.male,R.id.female,R.id.female};

                        final User user = dataSnapshot.getValue(User.class);
                        Picasso.with(ctx).load(user.getImgUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(sImg, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ctx).load(user.getImgUrl()).into(sImg);
                            }
                        });
                        sName.setText(""+user.getName());
                        sPhone.setText(""+user.getPhone());
                        sSex.check(sex[user.getSex()]);


                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        saveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final String id, name, phn;
                                final int sexId,sex;

                                sId.setError(null);
                                sName.setError(null);
                                sPhone.setError(null);

                                id = sId.getText().toString();
                                name = sName.getText().toString();
                                phn = sPhone.getText().toString();
                                sexId = sSex.getCheckedRadioButtonId();

                                switch (sexId){
                                    case R.id.male:
                                        sex = 0;
                                        break;
                                    case R.id.female:
                                        sex = 1;
                                        break;
                                    case R.id.others:
                                        sex = 2;
                                        break;
                                    default:
                                        sex = -1;
                                        break;
                                }


                                if(id.length()==0){
                                    sId.setError("Enter student ID number");
                                }else if(id.length()>8) {
                                    sId.setError("Student ID is too high");
                                }else if(name.length()==0){
                                    sName.setError("Enter student name");
                                }else if(phn.length()==0){
                                    sPhone.setError("Enter valid phone number");
                                }else if(sex<0){
                                    Toast.makeText(ctx,"Please choose student's Gender",Toast.LENGTH_SHORT).show();
                                }else {

                                    final Student student = new Student();

                                    student.setUid(uid);
                                    student.setImg(user.getImgUrl());
                                    student.setId(Integer.parseInt(id));
                                    student.setName("" + name);
                                    student.setSex(sex);
                                    student.setPhone("" + phn);

                                    if(storage.isNetConnected()) {
                                        checkIsStuIdExistOrNot(student);
                                    }else{
                                        alert("Connection error !","Please check your device net connection and try again.");
                                    }
                                }

                            }
                        });

                        dialog.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();

        }

    }


    private void checkIsStuIdExistOrNot(final Student stu)
    {
       // Toast.makeText(ctx,"Stu : "+stu.toString(),Toast.LENGTH_SHORT).show();

        pd.show();
        addStuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild(""+stu.getId())){
                        pd.hide();
                        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                        alb.setTitle("Warning !");
                        alb.setMessage("Id already exist. Do you want to replace it ?");
                        alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveStudent(stu,true);
                            }
                        });
                        alb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        alb.show();
                    }else{
                        saveStudent(stu,false);
                    }
                }else{
                    saveStudent(stu,false);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void saveStudent(final Student stu,final boolean isExist)
    {

        if(storage.isNetConnected()) {

            dialog.dismiss();
            pd.show();

            final String sid;
            if (stu.getId() < 10) {
                sid = "0" + stu.getId();
            } else {
                sid = "" + stu.getId();
            }

            addStuRef.child(sid).setValue(stu).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    loadReqRef.child(stu.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    Toast.makeText(ctx, "Successfully saved", Toast.LENGTH_SHORT).show();
                    JoinReqStatus jrs = new JoinReqStatus(sid,"You are connected");
                    loadStuRef.child(stu.getUid()).child("teacher_class_room").child(bndl.getString("CLASS_ID")).setValue(jrs);

                    if(isExist==true) {
                        sendReplacedMessage(stu);
                    }


                    pd.hide();
                }
            });

        }else {
            alert("Connection error !","Please check device net connection and try again.");
        }

    }


    private void sendReplacedMessage(final Student stu)
    {

        final String sid;
        if (stu.getId() < 10) {
            sid = "0" + stu.getId();
        } else {
            sid = "" + stu.getId();
        }

        addStuRef.child(sid).child("uid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = (String) dataSnapshot.getValue();
                if(!uid.equalsIgnoreCase("null") && !uid.equals(stu.getUid())){
                    loadStuRef.child(uid).child("teacher_class_room").child(bndl.getString("CLASS_ID")).child("status").setValue("You are replaced");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void alert(String title, String message){

        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
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
    public void onDetach() {
        super.onDetach();
    }

}
