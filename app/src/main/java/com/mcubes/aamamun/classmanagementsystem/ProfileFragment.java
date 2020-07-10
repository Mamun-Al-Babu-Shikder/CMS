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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.squareup.okhttp.internal.framed.Settings;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class ProfileFragment extends Fragment {

    private static Storage storage;
    private Context ctx;
    private View mView;
    private ShapesImage uImg;
    private TextView userName, userEmail, uName, uSex, uDob, uAddress, uEducation, uWork, uEmail, uPhone, mClass, tClass;
    private Button editName, editSex, editAddress, editDob, editEdu, editWork, editPhn, doneBtn, cancelBtn;
    private static ProgressDialog pd;
    private Bitmap userImgBitmap;

    private DatabaseReference userRef,userRef2, mClsRef, iClsRef;
    private StorageReference uImgRef;
    private ValueEventListener listener;

    private static String sex[] ={"Male", "Female","Others"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView =  inflater.inflate(R.layout.fragment_profile, container, false);


        storage = new Storage(ctx);

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        init();
        featchAndSetUserInfo();
        editProfile();

        return mView;
    }

    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!android.provider.Settings.System.canWrite(ctx)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            }
        }else{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent,"Select Photo"),102);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==101)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // Toast.makeText(ctx,"Permission Granted",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Select Photo"),102);

            } else {
                Toast.makeText(ctx,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==102 && data!=null && data.getData()!=null)
        {
            if(storage.isNetConnected()) {

                try {

                    Uri filePath = data.getData();
                    //Toast.makeText(ctx,"File Path : "+filePath.getPath(),Toast.LENGTH_SHORT).show();
                    Bitmap img = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), filePath);
                    uImg.setImageBitmap(img);

                    //Toast.makeText(ctx,"Real Path : "+getRealPathFromURI(filePath),Toast.LENGTH_SHORT).show();

                    compressedImage(new File(getPath(ctx,filePath)));

                    //compressedImage(new File(""+filePath.getPath()));
                    //uploadImage(filePath);
                    //ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //img.compress(Bitmap.CompressFormat.PNG, 30, out);

                    Toast.makeText(ctx,"File : "+getPath(ctx,filePath),Toast.LENGTH_SHORT).show();

                } catch (Exception ex) {
                    Toast.makeText(ctx, "Something wrong. : " + ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }else{
                alert("Connection error !","Please check your device net connection.");
            }

        }
    }





    private void init()
    {
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

        mClass = (TextView) mView.findViewById(R.id.mClass);
        tClass = (TextView) mView.findViewById(R.id.tClass);

        editName = (Button) mView.findViewById(R.id.editName);
        editSex = (Button) mView.findViewById(R.id.editSex);
        editDob = (Button) mView.findViewById(R.id.editDob);
        editAddress = (Button) mView.findViewById(R.id.editAddress);
        editEdu = (Button) mView.findViewById(R.id.editEdu);
        editWork = (Button) mView.findViewById(R.id.editWork);
        editPhn = (Button) mView.findViewById(R.id.editPhn);

    }

    private void editProfile()
    {

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue( userRef.child("name"));
            }
        });

        editAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue( userRef.child("address"));
            }
        });

        editEdu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue( userRef.child("education"));
            }
        });

        editWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue( userRef.child("work"));
            }
        });

        editPhn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue( userRef.child("phone"));
            }
        });

        editSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                    AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                    alb.setItems(sex, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, final int i) {
                            if(Storage.isNetConnected()) {
                                pd.show();
                                userRef.child("sex").setValue(i).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pd.hide();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ctx, "Can't updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                alert("Connection failed!","Please check your device net connection.");
                            }
                        }
                    });
                    alb.show();



            }
        });


        editDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDob();
            }
        });


        uImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateImage();
            }
        });


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


    private void updateImage()
    {
        checkPermission();
    }


    private void compressedImage(File imgFile)
    {

        try {
            userImgBitmap = new Compressor(ctx)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(50)
                    .compressToBitmap(imgFile);
        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        userImgBitmap.compress(Bitmap.CompressFormat.JPEG,50,bos);
        uploadImage(bos.toByteArray());

    }



    private void uploadImage(final Uri path )
    {
        pd.show();
        userRef2 = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("profile").child("imgUrl");
        uImgRef = FirebaseStorage.getInstance().getReference("user_image").child("image_"+storage.getUID()+"_"+System.currentTimeMillis()+"_"+(10000+(long)(Math.random()*10000))+".JPEG");
        uImgRef.putFile(path).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    pd.hide();
                    try {
                        Picasso.with(ctx).load(task.getResult().getDownloadUrl());
                        userRef2.setValue(""+task.getResult().getDownloadUrl()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                pd.hide();
                                if (task.isSuccessful()) {
                                    Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ctx, "Can't updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }catch (NullPointerException ex){
                        Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    pd.hide();
                    Toast.makeText(ctx,"Can't updated",Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                pd.setMessage("Uploaded "+(int)progress+"%");
            }
        });
    }



    private void uploadImage(final byte[] bytes )
    {
        if(storage.isNetConnected()) {
            pd.show();
            userRef2 = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("profile").child("imgUrl");
            uImgRef = FirebaseStorage.getInstance().getReference("user_image").child("image_" + storage.getUID() + "_" + System.currentTimeMillis() + "_" + (10000 + (long) (Math.random() * 10000)) + ".JPEG");
            uImgRef.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        pd.hide();
                        try {
                            Picasso.with(ctx).load(task.getResult().getDownloadUrl());
                            userRef2.setValue("" + task.getResult().getDownloadUrl()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    pd.hide();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ctx, "Can't updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (NullPointerException ex) {
                            Toast.makeText(ctx, "Ex : " + ex.toString(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        pd.hide();
                        Toast.makeText(ctx, "Can't updated", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    pd.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }else{
            alert("Connection failed!","Please check your device net connection.");
        }
    }



    private void updateValue(final DatabaseReference ref)
    {
          final Dialog dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_update);
            final EditText et = (EditText) dialog.findViewById(R.id.editValue);
            doneBtn = (Button) dialog.findViewById(R.id.doneBtn);
            cancelBtn = (Button) dialog.findViewById(R.id.cancleBtn);
            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String value = et.getText().toString().trim();
                    if (value.length() == 0) {
                        Toast.makeText(ctx, "Please fill up the gap", Toast.LENGTH_SHORT).show();
                    } else {
                        if(storage.isNetConnected()) {
                            dialog.dismiss();
                            pd.show();
                            ref.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    pd.hide();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ctx, "Can't updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            alert("Connection failed!","Please check your device net connection.");
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
            dialog.show();


    }


    private void updateDob()
    {
        final DatePicker dp;
        final View viw = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_ate_for_attendance, null);
        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
        alb.setView(viw);
        dp = (DatePicker) viw.findViewById(R.id.datePicker);
        alb.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(Storage.isNetConnected()) {
                    pd.show();
                    userRef.child("dob").setValue("" + dp.getDayOfMonth() + "/" + (dp.getMonth() + 1) + "/" + dp.getYear()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.hide();
                            if (task.isSuccessful()) {
                                Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ctx, "Can't updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    alert("Connection failed!","Please check your device net connection.");
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


    private void featchAndSetUserInfo()
    {

        userRef = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("profile");
        userRef.keepSynced(true);

        mClsRef = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("my_class_room");
        mClsRef.keepSynced(true);

        iClsRef = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("teacher_class_room");
        iClsRef.keepSynced(true);

        mClsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    //Toast.makeText(ctx,"Count My Class : "+dataSnapshot.getChildrenCount(),Toast.LENGTH_SHORT).show();
                   mClass.setText(""+ dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        iClsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //Toast.makeText(ctx,"Count Teacher's Class : "+dataSnapshot.getChildrenCount(),Toast.LENGTH_SHORT).show();
                    tClass.setText(""+dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
        if(userRef!=null && listener!=null) {
            userRef.removeEventListener(listener);
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
