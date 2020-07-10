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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mcubes.aamamun.classmanagementsystem.holder.ClassHolder;
import com.mcubes.aamamun.classmanagementsystem.model.BasicClassInfo;

import java.util.HashMap;
import java.util.Map;


public class MyClassRoom extends Fragment {


    private boolean isDataExist = false;
    private static Storage storage;
    private static ProgressDialog pd;
    private static AlertDialog.Builder alb;
    private Context ctx;
    private View mView;
    private Button addBtn;
    private RecyclerView recyclerView;
    private static ProgressBar pb;

    private LinearLayout notFoundLay;
    private static Button cancel, done;
    private static EditText cTitle, cCode;

    //private static int[] color = {Color.parseColor("#9b8e09ba"),Color.parseColor("#9bef0cba")/*,Color.parseColor("#9b1dbbe7"), Color.parseColor("#9b18cc57")/*, Color.parseColor("#1965e7") */};
    //private static int[] bkcolor = {R.drawable.left_round_shp1,R.drawable.left_round_shp2/*,R.drawable.left_round_shp3, R.drawable.left_round_shp5/*, Color.parseColor("#1965e7") */};

    private DatabaseReference addClsRef, addClsRef2, loadClsRef;
    private FirebaseRecyclerAdapter<BasicClassInfo, ClassHolder> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_my_class_room, container, false);
        storage = new Storage(ctx);

        init();
        fetchClassAndCreateList();


        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        addClsRef = FirebaseDatabase.getInstance().getReference("class");
        addClsRef2 = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("my_class_room");

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapter.getItemCount()<50) {
                    addNewClass();
                }else{
                   alert("Class overflow","Sorry !!!\nYou can't create more than '50' class.");
                }
            }
        });


        return mView;
    }


    private void init()
    {

        addBtn = (Button) mView.findViewById(R.id.addClassBtn);
        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        pb = (ProgressBar) mView.findViewById(R.id.pb);

    }


    private void fetchClassAndCreateList()
    {
        pb.setVisibility(View.VISIBLE);
        notFoundLay.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        recyclerView.setHasFixedSize(false);

        loadClsRef = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("my_class_room");
        adapter = new FirebaseRecyclerAdapter<BasicClassInfo, ClassHolder>(
                BasicClassInfo.class,
                R.layout.my_class_holder,
                ClassHolder.class,
                loadClsRef
        ) {
            @Override
            protected void populateViewHolder(ClassHolder viewHolder,final BasicClassInfo model, int position) {


                isDataExist = true;
                notFoundLay.setVisibility(View.GONE);
                pb.setVisibility(View.GONE);


                if(position<9) {
                    viewHolder.pos.setText("0" + (position+1));
                }else{
                    viewHolder.pos.setText("" + (position+1));
                }
                /*
                viewHolder.pos.setTextColor(color[position%color.length]);
                viewHolder.posBk.setBackgroundResource(bkcolor[position%color.length]);
                viewHolder.sideBk.setBackgroundColor(color[position%color.length]);
                */

                viewHolder.cCode.setText(""+model.getCode());
                viewHolder.cTitle.setText(""+model.getTitle());
                final String clsID = adapter.getRef(position).getKey();
                viewHolder.clsId.setText(""+clsID);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                     Intent next = new Intent(ctx,Main2Activity.class);
                     next.putExtra("CLASS_ID", clsID);
                     next.putExtra("CLASS_CODE", model.getCode());
                     next.putExtra("CLASS_TITLE", model.getTitle());
                     startActivity(next);

                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        String[] opt = {"Edit Class","Delete Class"};
                        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                        alb.setItems(opt, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0){
                                    editClass(clsID,model.getCode(),model.getTitle());
                                }else if(i==1){
                                    deleteClass(clsID);
                                }
                            }
                        });
                        alb.show();

                        return true;
                    }
                });

                final PopupMenu popupMenu = new PopupMenu(ctx,viewHolder.menuBtn);
                popupMenu.getMenu().add("Edit Class");
                popupMenu.getMenu().add("Delete Class");
                viewHolder.menuBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupMenu.show();
                    }
                });

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem mi) {
                        if(mi.getTitle().equals("Edit Class")){
                            editClass(clsID,model.getCode(),model.getTitle());
                        }else if(mi.getTitle().equals("Delete Class")){
                            deleteClass(clsID);
                        }
                        return true;
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

    private void editClass(final  String clsUid, String code, String title)
    {

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_class);
        ((TextView)dialog.findViewById(R.id.dialogTitle)).setText("Edit Class");

        cCode = (EditText) dialog.findViewById(R.id.cCode);
        cTitle = (EditText) dialog.findViewById(R.id.cTitle);
        done = (Button) dialog.findViewById(R.id.doneBtn);
        cancel = (Button) dialog.findViewById(R.id.cancleBtn);

        cCode.setText(code);
        cTitle.setText(title);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String code,title;
                title = cTitle.getText().toString().trim();
                code = cCode.getText().toString().trim();

                cCode.setError(null);
                cTitle.setError(null);

                if(code.length()==0 ){
                    cCode.setError("Please enter Course Code.");
                }else if(title.length()==0 ){
                    cTitle.setError("Please enter Course Title.");
                }else{
                    dialog.dismiss();

                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put("code",code);
                    map.put("title",title);

                    addClsRef.child(clsUid).updateChildren(map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null) {
                               // Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_LONG).show();
                               // adapter.notifyDataSetChanged();
                            }
                        }
                    });

                    addClsRef2.child(clsUid).updateChildren(map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null) {
                                Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_LONG).show();
                               // adapter.notifyDataSetChanged();
                            }
                        }
                    });


                    /*
                    final BasicClassInfo bcs = new BasicClassInfo();
                    bcs.setCode(code);
                    bcs.setTitle(title);
                    bcs.setFounder(storage.getUID());

                    addClsRef.child(clsUid).setValue(bcs).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(ctx,"success to student side",Toast.LENGTH_SHORT).show();

                                addClsRef2.child(clsUid).setValue(bcs).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            pd.dismiss();
                                            Toast.makeText(ctx,"success to teacher side",Toast.LENGTH_SHORT).show();
                                        }else{
                                            alert("Warning !","Error : "+task.getException().toString());
                                        }
                                    }
                                });

                            }else{
                                alert("Warning !","Error : "+task.getException().toString());
                            }
                        }
                    });
                    */


                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();


    }

    private void deleteClass(final String class_id)
    {

        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
        alb.setTitle("Delete");
        alb.setMessage("You must lost you class data. Are you sure want to delete this class ?");
        alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(Storage.isNetConnected()) {

                    pd.show();
                    addClsRef2.child(class_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        pd.dismiss();
                        addClsRef.child(class_id).removeValue();
                        Toast.makeText(ctx, "Successfully deleted.", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();

                        }
                    });
                }else{
                    alert("Connection failed !", "Please check your device net connection.");
                }

            }
        });
        alb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alb.show();
    }



    private void addNewClass()
    {

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_class);

        cTitle = (EditText) dialog.findViewById(R.id.cTitle);
        cCode = (EditText) dialog.findViewById(R.id.cCode);
        done = (Button) dialog.findViewById(R.id.doneBtn);
        cancel = (Button) dialog.findViewById(R.id.cancleBtn);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String code,title;
                title = cTitle.getText().toString();
                code = cCode.getText().toString();

                cCode.setError(null);
                cTitle.setError(null);

                if(code.length()==0 ){
                    cCode.setError("Please enter Course Code.");
                }else if(title.length()==0 ){
                    cTitle.setError("Please enter Course Title.");
                }else{
                    dialog.dismiss();
                    createClassToDatabase(code, title);
                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void createClassToDatabase(String code, String title)
    {
        if(storage.isNetConnected()) {

            pd.show();
            final BasicClassInfo bcs = new BasicClassInfo();
            bcs.setCode(code);
            bcs.setTitle(title);
            bcs.setFounder(storage.getUID());
            final String clsUid = addClsRef.push().getKey();

            addClsRef.child(clsUid).setValue(bcs).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(ctx, "success to student side", Toast.LENGTH_SHORT).show();

                        addClsRef2.child(clsUid).setValue(bcs).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    pd.dismiss();
                                    Toast.makeText(ctx, "success to teacher side", Toast.LENGTH_SHORT).show();
                                } else {
                                    alert("Warning !", "Error : " + task.getException().toString());
                                }
                            }
                        });

                    } else {
                        alert("Warning !", "Error : " + task.getException().toString());
                    }
                }
            });

        }else{
            alert("Connection failed!","Please check your device net connection.");
        }
    }

    private void alert(String title, String message){

        alb = new AlertDialog.Builder(ctx);
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

