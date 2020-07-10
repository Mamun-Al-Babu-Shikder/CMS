package com.mcubes.aamamun.classmanagementsystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mcubes.aamamun.classmanagementsystem.holder.ClassHolder;
import com.mcubes.aamamun.classmanagementsystem.holder.ClassHolder2;
import com.mcubes.aamamun.classmanagementsystem.model.BasicClassInfo;
import com.mcubes.aamamun.classmanagementsystem.model.JoinReqStatus;
import com.mcubes.aamamun.classmanagementsystem.model.Student;

import java.util.HashMap;
import java.util.Map;


public class TeachersClassRoom extends Fragment {


    private boolean isDataExist = false;
    private Storage storage;
    private Context ctx;
    private View mView;

    private LinearLayout notFoundLay;
    private ProgressBar pb;
    private RecyclerView recyclerView;
    private static Button joinClsBtn,cancel, done;
    private static Dialog dialog;
    private static ProgressDialog pd;


    private String jClsId, jDescription;
    //private static int[] color = {Color.parseColor("#8e09ba"),Color.parseColor("#ef0cba"),Color.parseColor("#1dbbe7"),Color.parseColor("#f7c12b"), Color.parseColor("#18cc57"), Color.parseColor("#1965e7") };
    //private static int[] color = {Color.parseColor("#9b8e09ba"),Color.parseColor("#9bef0cba")/*,Color.parseColor("#9b1dbbe7"), Color.parseColor("#9b18cc57")/*, Color.parseColor("#1965e7") */};
    //private static int[] bkcolor = {R.drawable.left_round_shp1,R.drawable.left_round_shp2/*,R.drawable.left_round_shp3, R.drawable.left_round_shp5/*, Color.parseColor("#1965e7") */};

    private DatabaseReference reqClsRef, loadClsRef, checkRef, checkRef2;
    private FirebaseRecyclerAdapter<JoinReqStatus, ClassHolder2> adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_teachers_class_room, container, false);

        storage = new Storage(ctx);
        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        loadClsRef = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("teacher_class_room");
        loadClsRef.keepSynced(true);
        reqClsRef = FirebaseDatabase.getInstance().getReference("class");
        reqClsRef.keepSynced(true);

        init();

        return mView;
    }

    private void init()
    {
        joinClsBtn = (Button) mView.findViewById(R.id.joinClassBtn);
        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        pb = (ProgressBar) mView.findViewById(R.id.pb);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        joinClsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(adapter.getItemCount()<50) {
                    joinToNewClass();
                }else{
                    alert("Class overflow","Sorry !!!\nYou can't involved more than '50' class.");
                }

            }
        });

        fetchJoinedClassAndDisplay();

    }


    private void fetchJoinedClassAndDisplay()
    {
        pb.setVisibility(View.VISIBLE);

        adapter = new FirebaseRecyclerAdapter<JoinReqStatus, ClassHolder2>(
                JoinReqStatus.class,
                R.layout.joined_class_holder,
                ClassHolder2.class,
                loadClsRef
        ) {
            @Override
            protected void populateViewHolder(final ClassHolder2 vh, final JoinReqStatus model, final int position) {

                isDataExist = true;

                pb.setVisibility(View.GONE);
                notFoundLay.setVisibility(View.GONE);

                final String cid = adapter.getRef(position).getKey();

                /*
                vh.bk.setBackgroundResource(bkcolor[position%bkcolor.length]);
                vh.cidText.setTextColor(color[position%color.length]);
                */

                vh.cid.setText(""+cid);
                final String status = model.getStatus();
                vh.status.setText(""+status);
                /*
                if(status.equalsIgnoreCase("You are connected")){
                    vh.status.setTextColor(Color.parseColor("#5982e1"));
                }else if(status.equalsIgnoreCase("Request Sent")){
                    vh.status.setTextColor(Color.parseColor("#0daf2e"));
                }else{
                    vh.status.setTextColor(Color.parseColor("#ff0000"));
                }
                */


                reqClsRef.child(cid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            final BasicClassInfo bci = dataSnapshot.getValue(BasicClassInfo.class);
                            vh.cCode.setText(""+bci.getCode());
                            vh.cTitle.setText(""+bci.getTitle());

                            vh.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if(status.equalsIgnoreCase("You are connected")) {

                                        try {

                                            Intent next = new Intent(ctx, Main3Activity.class);
                                            next.putExtra("STUDENT_ID", model.getId());
                                            next.putExtra("CLASS_ID", adapter.getRef(position).getKey());
                                            next.putExtra("CLASS_CODE", bci.getCode());
                                            next.putExtra("CLASS_TITLE", bci.getTitle());
                                            next.putExtra("UID", bci.getFounder());
                                            startActivity(next);

                                        }catch (Exception ex){
                                            Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                        }else{
                            vh.cCode.setVisibility(View.GONE);
                            vh.cTitle.setText("Class not longer exist.");
                            vh.cTitle.setTextColor(Color.RED);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




                vh.crossBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                        alb.setTitle("Warning !");
                        if(status.equalsIgnoreCase("You are connected")){
                            alb.setMessage("Do you want to leave from this class ?");
                        }else{
                            alb.setMessage("Do you want to cancel request ?");
                        }
                        alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(storage.isNetConnected()) {

                                    if (status.equalsIgnoreCase("You are connected")) {
                                        leaveFromClass(cid);
                                    } else {
                                        cancelRequest(cid);
                                    }
                                }else{
                                    alert("Connection error !","Please check your device net connection and try again.");
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

    private void leaveFromClass(String cid)
    {
        pd.show();
        loadClsRef.child(cid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.hide();
                if(task.isSuccessful()){
                    Toast.makeText(ctx,"Success",Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(ctx,"Failed to leave",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void cancelRequest(final String cid)
    {
        pd.show();

        loadClsRef.child(cid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                 pd.hide();
                if(task.isSuccessful()){
                    reqClsRef.child(cid).child("request").child(storage.getUID()).removeValue();
                    Toast.makeText(ctx,"Request canceled",Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(ctx,"Can't cancel request",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void joinToNewClass()
    {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_join_to_class);
        final EditText etJoinClsId = (EditText) dialog.findViewById(R.id.joinClsId);
        final EditText etJoinSeacription  = (EditText) dialog.findViewById(R.id.description);
        done = (Button) dialog.findViewById(R.id.doneBtn);
        cancel = (Button) dialog.findViewById(R.id.cancleBtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                etJoinClsId.setError(null);
                etJoinSeacription.setError(null);
                jClsId = etJoinClsId.getText().toString().trim();
                jDescription = etJoinSeacription.getText().toString().trim();
                if(jClsId.length()==0){
                    etJoinClsId.setError("Please enter class id");
                }else if(jDescription.length()==0 || jDescription.length()>150){
                    etJoinSeacription.setError("Description length must be 1 to 150");
                }else{

                    if(storage.isNetConnected()) {
                        pd.show();
                        testIsClassExist();
                    }else {
                        alert("Connection error !","Please check your device net connection and try again.");
                    }

                }
            }
        });
        dialog.show();

    }


    private void testIsClassExist()
    {
        checkRef = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("teacher_class_room");

        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild(jClsId)){
                        pd.hide();
                        alert("Warning !","You are already involved in this class. Please try with another class id.");
                    }else{
                        testIsClassExist2();
                    }
                }else{
                    testIsClassExist2();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void testIsClassExist2()
    {
        checkRef2 = FirebaseDatabase.getInstance().getReference("class").child(jClsId).child("founder");

        checkRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){


                    if(dataSnapshot.getValue().toString().equals(storage.getUID())){
                        pd.hide();
                        alert("Warning !","Sorry, You are the Teacher of this class. So you can't join to this class as a student.");
                    }else{
                        sendJoinRequest();
                    }
                    /*
                   if(dataSnapshot.hasChild(jClsId)){

                       sendJoinRequest();
                   }else{
                       pd.hide();
                       alert("Not found !","Class not found. Please try with valid class id");
                   }
                   */
                }else{
                    pd.hide();
                    alert("Not found !","Class not found. Please try with valid class id");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void checkTheFounderOfClass()
    {
        checkRef2.child(jClsId).child("founder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendJoinRequest()
    {

        dialog.dismiss();
        loadClsRef.child(jClsId).child("status").setValue("Request Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.hide();
                if (task.isSuccessful()) {
                    reqClsRef.child(jClsId).child("request").child(storage.getUID())
                            .child("status")
                            .setValue(jDescription)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ctx, "Request successfully sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
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
