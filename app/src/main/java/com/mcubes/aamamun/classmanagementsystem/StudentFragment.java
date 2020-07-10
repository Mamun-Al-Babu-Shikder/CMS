package com.mcubes.aamamun.classmanagementsystem;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mcubes.aamamun.classmanagementsystem.holder.StudentHolder;
import com.mcubes.aamamun.classmanagementsystem.model.Student;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import id.zelory.compressor.Compressor;


public class StudentFragment extends Fragment {



    private boolean isDataExist = false;
    private static Storage storage;
    private Bundle bndl;
    private Context ctx;
    private View mView;
    private Button addStudentBtn, cancelBtn, saveBtn;
    private ShapesImage sImg;
    private EditText sId, sName, sPhone;
    private RadioGroup sSex;
    private Uri filePath;
    private Bitmap img;
    private Bitmap stuImgBitmap;
    //private Set<Integer> sidSet;
    private Map<String,Object> sidSet;

    private static LinearLayout notFoundLay;
    private static LinearLayout pb;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Student,StudentHolder> adapter;

    public static final int PICK_IMAGE = 102;
    private Dialog dialog;
    private static ProgressDialog pd;

    private DatabaseReference addStuRef,loadStuRef, loadStuRef2,delStuRef;
    private ValueEventListener loadStuListener;
    private StorageReference stoRef;

    //private static int img_bk[] = {R.drawable.cir_shp_alpha1,R.drawable.cir_shp_alpha2,R.drawable.cir_shp_alpha3,R.drawable.cir_shp_alpha4};
    //private static int textColor[] = {Color.parseColor("#328b23"), Color.parseColor("#f25347"),Color.parseColor("#845be1"),Color.parseColor("#8fa908")};
    private static String[] optMenu;// = {"Attendance info","View profile","Edit","Delete"};
    private static String[] optMenu1 = {"Attendance info","View profile","Edit","Delete"};
    private static String[] optMenu2 = {"Attendance info","View profile"};


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_student, container, false);

        storage = new Storage(ctx);
        bndl = getArguments();

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        if(bndl.getString("ACCESS_PERSON").equalsIgnoreCase("TEACHER")){
            optMenu = optMenu1;
        }else {
            optMenu = optMenu2;
        }

        init();

        return mView;
    }


    private void init()
    {


        //sidSet = new HashSet<>();

        String clsId = getArguments().getString("CLASS_ID");




        addStuRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("students");
        addStuRef.keepSynced(true);
        loadStuRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("students");
        loadStuRef.keepSynced(true);
        loadStuRef2 = FirebaseDatabase.getInstance().getReference("user");
        loadStuRef2.keepSynced(true);
        //stoRef = FirebaseStorage.getInstance().getReference("user_image");

        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        pb = (LinearLayout) mView.findViewById(R.id.pb);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        recyclerView.setHasFixedSize(true);
        addStudentBtn = (Button) mView.findViewById(R.id.addStudentBtn);
        if(bndl.getString("ACCESS_PERSON").equalsIgnoreCase("TEACHER")){
            addStudentBtn.setVisibility(View.VISIBLE);
            addStudentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createStudent(null);
                }
            });
        }else{
            addStudentBtn.setVisibility(View.GONE);
        }


        loadStuDataForCheckExist();
        loadStudentAndDisplay();


    }

    private void loadStuDataForCheckExist()
    {
        sidSet = new HashMap<>();
        loadStuListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    sidSet = (Map<String, Object>) dataSnapshot.getValue();
                    //Toast.makeText(ctx,"Exist ID : "+sidSet.keySet().toString(),Toast.LENGTH_SHORT).show();
                }else {
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        loadStuRef.addValueEventListener(loadStuListener);
    }



    private void loadStudentAndDisplay()
    {

        notFoundLay.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);

        adapter = new FirebaseRecyclerAdapter<Student, StudentHolder>(
                Student.class,
                R.layout.student_holder,
                StudentHolder.class,
                addStuRef
        ) {
            @Override
            protected void populateViewHolder(final StudentHolder vh, final Student model, int position) {

                pb.setVisibility(View.GONE);
                notFoundLay.setVisibility(View.GONE);
                isDataExist = true;

                try{
                    //sidSet.add(model.getId());

                   // vh.imgBk.setBackgroundResource(img_bk[position%img_bk.length]);
                   // vh.firstCharOfNam.setText(""+model.getName().toUpperCase().charAt(0));
                   // vh.firstCharOfNam.setTextColor(textColor[position%textColor.length]);

                    if(storage.isImageViewAtStudentList()){

                        Picasso.with(ctx).load(model.getImg()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_user).into(vh.sImg, new Callback() {
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onError() {
                                try {
                                    Picasso.with(ctx).load(model.getImg()).placeholder(R.drawable.default_user).into(vh.sImg);
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }

                    //vh.sId.setTypeface(tf);
                    //vh.sName.setTypeface(tf);

                    if((""+model.getId()).length()==1) {
                        vh.sId.setText("ID : 0" + model.getId());
                    }else{
                        vh.sId.setText("ID : " + model.getId());
                    }
                    vh.sName.setText("Name : "+model.getName());

                    vh.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent next = new Intent(ctx, AttendanceInfo.class);
                            next.putExtra("CLASS_ID",getArguments().getString("CLASS_ID"));
                            next.putExtra("STUDENT_ID",model.getId());
                            startActivity(next);

                        }
                    });


                    if(model.getUid().equalsIgnoreCase("null")){
                        vh.profileIndicatorAlpha.setBackgroundResource(R.drawable.cir_shp_alpha3);
                        vh.profileIndicator.setImageResource(R.drawable.cir_shp3);
                    }else{
                        vh.profileIndicatorAlpha.setBackgroundResource(R.drawable.cir_shp_alpha1);
                        vh.profileIndicator.setImageResource(R.drawable.cir_shp1);
                    }


                    vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            final AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                            alb.setItems(optMenu, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if(i==0){

                                        Intent next = new Intent(ctx, AttendanceInfo.class);
                                        next.putExtra("CLASS_ID",getArguments().getString("CLASS_ID"));
                                        next.putExtra("STUDENT_ID",model.getId());
                                        startActivity(next);

                                    }else if(i==1){

                                        if(model.getUid().equalsIgnoreCase("null")){
                                           Toast.makeText(ctx,"Profile not found",Toast.LENGTH_SHORT).show();
                                        }else{
                                            Intent next = new Intent(ctx, StudentProfileView.class);
                                            next.putExtra("UID",model.getUid());
                                            startActivity(next);
                                        }

                                    }else if(i==2){
                                        createStudent(model);
                                    }else if(i==3){

                                        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                                        alb.setTitle("Delete");
                                        alb.setMessage("Are you sure, want delete ?");
                                        alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                deleteStudent(model);
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
                                }
                            });
                            alb.show();

                            return true;
                        }
                    });

                }catch (Exception ex){
                    Toast.makeText(ctx,"Data not found",Toast.LENGTH_SHORT).show();
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



    private void deleteStudent(final Student stu)
    {

        if(storage.isNetConnected()) {

            pd.show();
            final String id;
            if (stu.getId() < 10) {
                id = "0" + stu.getId();
            } else {
                id = "" + stu.getId();
            }

            if (!stu.getUid().equalsIgnoreCase("null")) {
                delStuRef = FirebaseDatabase.getInstance().getReference("user").child(stu.getUid()).child("teacher_class_room").child(getArguments().getString("CLASS_ID")).child("status");
            }
            addStuRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ctx, "Successfully deleted", Toast.LENGTH_LONG).show();
                        if (!stu.getUid().equalsIgnoreCase("null")) {
                            delStuRef.setValue("You are Rejected");
                        }
                    } else {
                        Toast.makeText(ctx, "Can't delete", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            alert("Connection failed !","Please check your device net connection.");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==101)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Toast.makeText(ctx,"Permission Granted",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Photo"),PICK_IMAGE);

            } else {
                Toast.makeText(ctx,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE && data!=null && data.getData()!=null)
        {
            try {

                filePath = data.getData();
                Bitmap img = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), filePath);
                sImg.setImageBitmap(img);

            }catch(Exception ex){
                Toast.makeText(ctx,"Something wrong.",Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*
    public String getRealPathFromURI(Uri contentUri) {

        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = ctx.getContentResolver().query( contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
    */


    private byte[] compressedImage(Uri contentUri)
    {
        try {
            File imgFile = new File(getPath(ctx,contentUri));
            Toast.makeText(ctx,"File : "+imgFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();

            stuImgBitmap = new Compressor(ctx)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(50)
                    .compressToBitmap(imgFile);
        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        stuImgBitmap.compress(Bitmap.CompressFormat.JPEG,50,bos);

        return bos.toByteArray();
    }

    private void createStudent(final Student model)
    {
        filePath = null;

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

        if(model!=null){



                Picasso.with(ctx).load(model.getImg()).networkPolicy(NetworkPolicy.OFFLINE).into(sImg, new Callback() {
                   @Override
                   public void onSuccess() {}
                   @Override
                   public void onError() {
                      Picasso.with(ctx).load(model.getImg()).into(sImg);
                   }
                });


            sId.setText(""+model.getId());
            sId.setEnabled(false);
            sName.setText(""+model.getName());
            if(model.getSex()==0){
                RadioButton rb = (RadioButton) dialog.findViewById(R.id.male);
                rb.setChecked(true);
            }else if(model.getSex()==1){
                RadioButton rb = (RadioButton) dialog.findViewById(R.id.female);
                rb.setChecked(true);
            }else if(model.getSex()==2){
                RadioButton rb = (RadioButton) dialog.findViewById(R.id.others);
                rb.setChecked(true);
            }
            sPhone.setText(""+model.getPhone());
        }

        sImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(!Settings.System.canWrite(ctx)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Photo"),PICK_IMAGE);
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id,name,phn;
                int sex, sexId;

                id = sId.getText().toString().trim();
                name = sName.getText().toString().trim();
                phn = sPhone.getText().toString().trim();

                sexId = sSex.getCheckedRadioButtonId();
                //Toast.makeText(ctx,"CheckedRadioButtonId : "+sexId,Toast.LENGTH_SHORT).show();
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

                sId.setError(null);
                sName.setError(null);
                sPhone.setError(null);


                if(id.length()==0){
                    sId.setError("Enter student ID number");
                }else if(id.length()>8) {
                    sId.setError("Student ID is too high");
                }else if(name.length()==0){
                    sName.setError("Enter student name");
                }else if(phn.length()<10 || phn.length()>15){
                    sPhone.setError("Enter valid phone number");
                }else if(sex<0){
                    Toast.makeText(ctx,"Please choose student's Gender",Toast.LENGTH_SHORT).show();
                }else{

                    final Student student = new Student();

                    /*
                    final String sid;
                    if (Integer.parseInt(id) < 10) {
                        sid = "0" + id;
                    } else {
                        sid = "" + id;
                    }
                    */

                    student.setId(Integer.parseInt(id));
                    student.setName("" + name);
                    student.setSex(sex);
                    student.setPhone("" + phn);

                    if(model==null) {
                        student.setUid("null");
                        student.setImg("null");
                    }else{
                        student.setUid(model.getUid());
                        student.setImg(model.getImg());
                    }


                    if(model!=null){
                        dialog.dismiss();
                        addStudentInfoToDatabase(student, false);
                    }else {

                        if (!sidSet.isEmpty()) {

                            // Toast.makeText(ctx,"set : "+sidSet.toString(),Toast.LENGTH_LONG).show();

                            int iid= Integer.parseInt(id);
                            if(iid<10){
                                id="0"+iid;
                            }

                            //Toast.makeText(ctx,"ID : "+id,Toast.LENGTH_SHORT).show();

                            if (sidSet.containsKey(id)) {

                                AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                                alb.setTitle("Warning !");
                                alb.setMessage("Student ID '" + id + "'already exist. Do you want to replace it ?");
                                alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        dialog.dismiss();
                                        addStudentInfoToDatabase(student, true);
                                    }
                                });
                                alb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                                alb.show();

                            } else {

                                dialog.dismiss();
                                addStudentInfoToDatabase(student, false);

                            }
                        } else {

                            // Toast.makeText(ctx, "set is empty ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            addStudentInfoToDatabase(student, false);

                        }

                    }

                }

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();

    }


    private void addStudentInfoToDatabase(final Student student, boolean isExist)
    {

        if(storage.isNetConnected()) {

            pd.show();

            /*
            String id = "" + student.getId();
            if (id.length() == 1) {
                id = "0" + id;
            }
            */

            final String sid;
            if (student.getId() < 10) {
                sid = "0" + student.getId();
            } else {
                sid = "" + student.getId();
            }

            if (isExist) {
                sendReplacedMessage(student);
            } else {
                addStuRef.child(sid).setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(ctx, "Student info successfully saved", Toast.LENGTH_SHORT).show();
                            uploadImage(""+sid);
                        } else {
                            Toast.makeText(ctx, "Some error occurred to save student Info", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }else{
            alert("Connection error !","Please check your device net connection and try again.");
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
                    loadStuRef2.child(uid).child("teacher_class_room").child(getArguments().getString("CLASS_ID")).child("status").setValue("You are replaced").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){


                                String id = ""+stu.getId();
                                if(id.length()==1){
                                    id="0"+id;
                                }

                                addStuRef.child(id).setValue(stu).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        pd.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ctx, "Student info successfully saved", Toast.LENGTH_SHORT).show();
                                            uploadImage("" + stu.getId());
                                        } else {
                                            Toast.makeText(ctx, "Some error occurred to save student Info", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });


                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }








    private void uploadImage(final String sid) {


        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(ctx);
            //progressDialog.setTitle("Uploading...");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            stoRef = FirebaseStorage.getInstance().getReference("user_image/"+ "image_"+System.currentTimeMillis()+"_"+(10000+(int)(Math.random()*10000))+".JPEG");
            stoRef.putBytes(compressedImage(filePath)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();
                            Toast.makeText(ctx, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                            Uri url = taskSnapshot.getDownloadUrl();

                            addStuRef.child(sid).child("img").setValue(url.toString());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ctx, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }



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

        if(loadStuRef!=null){
            loadStuRef.removeEventListener(loadStuListener);
        }
        super.onDetach();
    }







    //------------- Path from Uri -----------//

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        Log.i("URI",uri+"");
        String result = uri+"";
        // DocumentProvider
        //  if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        if (isKitKat && (result.contains("media.documents"))) {
            String[] ary = result.split("/");
            int length = ary.length;
            String imgary = ary[length-1];
            final String[] dat = imgary.split("%3A");
            final String docId = dat[1];
            final String type = dat[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
            } else if ("audio".equals(type)) {
            }
            final String selection = "_id=?";
            final String[] selectionArgs = new String[] {
                    dat[1]
            };
            return getDataColumn(context, contentUri, selection, selectionArgs);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }



}
