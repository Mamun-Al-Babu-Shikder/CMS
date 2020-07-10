package com.mcubes.aamamun.classmanagementsystem;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mcubes.aamamun.classmanagementsystem.holder.DateHolder;
import com.mcubes.aamamun.classmanagementsystem.model.ClassDate;

import org.w3c.dom.Text;


public class AttendanceFragment extends Fragment {


    private boolean isDataExist= false;
    private Context ctx;
    private View mView;
    private Bundle bndl;

    private LinearLayout notFoundLay,pb;
    private Button addDateBtn;
    private ProgressDialog pd;
    private AlertDialog.Builder alb;
    private RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<ClassDate, DateHolder> adapter;
    private DatabaseReference addDateRef, loadDateRef, delDateRef;

    private static String optMenu[] = {"Open","Delete"};


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView =  inflater.inflate(R.layout.fragment_attendance, container, false);

        bndl = getArguments();

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);



        init();
        fetchDataAndCreateList();

        return mView;
    }


    private void init()
    {

        addDateBtn = (Button) mView.findViewById(R.id.addDateBtn);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        pb = (LinearLayout) mView.findViewById(R.id.pb);

        addDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDate();
            }
        });

    }


    private void fetchDataAndCreateList()
    {
        pb.setVisibility(View.VISIBLE);
        notFoundLay.setVisibility(View.GONE);

        loadDateRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance");
        loadDateRef.keepSynced(true);
        adapter = new FirebaseRecyclerAdapter<ClassDate, DateHolder>(
                ClassDate.class,
                R.layout.class_date_holder,
                DateHolder.class,
                loadDateRef
        ) {
            @Override
            protected void populateViewHolder(DateHolder vh, final ClassDate model, final int pos) {

                isDataExist = true;

                pb.setVisibility(View.GONE);
                notFoundLay.setVisibility(View.GONE);


                try {


                    final String id = adapter.getRef(pos).getKey();
                    if (pos < 9) {
                        vh.pos.setText("(Lecture 0" + (pos + 1) + ")");
                    } else {
                        vh.pos.setText("(Lecture " + (pos + 1) + ")");
                    }

                    //vh.pos.setTypeface(tf);
                    //vh.date.setTypeface(tf);

                    vh.date.setText("Date : " + model.getDate());

                    vh.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            try {
                                Intent next = new Intent(ctx, AttendanceSheet.class);
                                next.putExtra("CLASS_ID", bndl.getString("CLASS_ID"));
                                next.putExtra("DATE_ID", id);
                                next.putExtra("ACCESS_DATE", model.getDate());
                                startActivity(next);
                            }catch (Exception ex){
                                Toast.makeText(ctx,"Error : "+ex.toString(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                            alb.setItems(optMenu, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (i == 0) {

                                        Intent next = new Intent(ctx, AttendanceSheet.class);
                                        next.putExtra("CLASS_ID", bndl.getString("CLASS_ID"));
                                        next.putExtra("DATE_ID", id);
                                        next.putExtra("ACCESS_DATE", model.getDate());
                                        startActivity(next);

                                    } else if (i == 1) {

                                        AlertDialog.Builder alb2 = new AlertDialog.Builder(ctx);
                                        alb2.setTitle("Delete");
                                        alb2.setMessage("Do you want to delete ?");
                                        alb2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                deleteClassDate(id);
                                            }
                                        });
                                        alb2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        });
                                        alb2.show();
                                    }
                                }
                            });
                            alb.show();

                            return true;
                        }
                    });


                }catch (Exception ex){
                    Toast.makeText(ctx,"Error : "+ex.toString(),Toast.LENGTH_SHORT).show();
                }


            }
        };

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


    private void deleteClassDate(String id)
    {
        if(Storage.isNetConnected()) {

            delDateRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance").child(id);
            delDateRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        adapter.notifyDataSetChanged();
                        //adapter.notifyItemRangeRemoved(0,adapter.getItemCount());
                        Toast.makeText(ctx, "Successfully deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else{
            Toast.makeText(ctx,"Please check net connection.",Toast.LENGTH_SHORT).show();
        }
    }




    private void addDate()
    {

        addDateRef = FirebaseDatabase.getInstance().getReference("class").child(bndl.getString("CLASS_ID")).child("attendance");
        final String dateId = addDateRef.push().getKey();

        View viw;
        alb = new AlertDialog.Builder(ctx);
        viw = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_ate_for_attendance, null);
        alb.setView(viw);
        final DatePicker datePicker = (DatePicker) viw.findViewById(R.id.datePicker);
        alb.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(Storage.isNetConnected()) {
                    pd.show();
                    addDateRef.child(dateId).child("date").setValue(datePicker.getDayOfMonth() + "/" + (1 + datePicker.getMonth()) + "/" + datePicker.getYear())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    pd.dismiss();
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(ctx, "Some error occurred", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(ctx, "Success", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ctx, "Ex : \n" + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    alert("Connection failed !","Please check your device net connection.");
                }
               // Toast.makeText(ctx," Date : "+datePicker.getYear()+"/"+datePicker.getMonth()+"/"+datePicker.getDayOfMonth(),Toast.LENGTH_LONG).show();
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

}
