package com.mcubes.aamamun.classmanagementsystem;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.mcubes.aamamun.classmanagementsystem.model.PostData;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.karthikraj.shapesimage.ShapesImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class PostFragment extends Fragment {

    private Context ctx;
    private View mView;
    private static Storage storage;
    private Bundle bndl;
    private static AlertDialog.Builder alb;
    private static ProgressDialog pd;

    private Bitmap postImgBitmap;
    private Uri filePath=null;

    public ShapesImage poster_img;
    public TextView poster_name, post_time;
    private EditText post_stext, post_btext ,post_link;
    private CheckBox cbNormalText, cbBoldText,cbLink, cbImage;
    public Button menu_btn, postBtn;
    public ImageView post_img;

    private String stext,btext,link,poster_image="null",imgurl,clsId;

    private DatabaseReference userRef, postRef, notificationRef;
    private StorageReference imgRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_post,container, false);

        bndl = getArguments();
        storage = new Storage(ctx);

        clsId = bndl.getString("CLASS_ID");

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        init();


        return mView;
    }


    private void init()
    {
        cbNormalText = (CheckBox) mView.findViewById(R.id.cbNormalText);
        cbBoldText = (CheckBox) mView.findViewById(R.id.cbBoldText);
        cbLink = (CheckBox) mView.findViewById(R.id.cbLink);
        cbImage = (CheckBox) mView.findViewById(R.id.cbImage);
        postBtn = (Button) mView.findViewById(R.id.postBtn);


        poster_img = (ShapesImage) mView.findViewById(R.id.poster_img);
        poster_name = (TextView) mView.findViewById(R.id.poster_name);
        post_time = (TextView) mView.findViewById(R.id.post_time);
        menu_btn = (Button) mView.findViewById(R.id.menuBtn);
        post_stext = (EditText) mView.findViewById(R.id.post_stext);
        post_btext = (EditText) mView.findViewById(R.id.post_btext);
        post_img = (ImageView) mView.findViewById(R.id.post_img);
        post_link = (EditText) mView.findViewById(R.id.post_link);

        cbNormalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbNormalText.isChecked()){
                    post_stext.setVisibility(View.VISIBLE);
                }else {
                    post_stext.setVisibility(View.GONE);
                }
            }
        });

        cbBoldText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbBoldText.isChecked()){
                    post_btext.setVisibility(View.VISIBLE);
                }else{
                    post_btext.setVisibility(View.GONE);
                }
            }
        });

        cbLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbLink.isChecked()){
                    post_link.setVisibility(View.VISIBLE);
                }else{
                    post_link.setVisibility(View.GONE);
                }
            }
        });

        cbImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbImage.isChecked()){
                    post_img.setVisibility(View.VISIBLE);
                }else{
                    post_img.setVisibility(View.GONE);
                }
            }
        });


        loadUserDataAndTime();

        //postRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("news_feed").child("post");
        postRef = FirebaseDatabase.getInstance().getReference("news_feed").child(clsId).child("post");
        notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(clsId).child("notify");
        imgRef = FirebaseStorage.getInstance().getReference("post_image");

        post_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });


        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getValueAndPost();
            }
        });


    }



    private void getValueAndPost()
    {
        if(cbNormalText.isChecked() || cbBoldText.isChecked() || cbImage.isChecked() || cbLink.isChecked()){

            stext = post_stext.getText().toString().trim();
            btext = post_btext.getText().toString().trim();
            link = post_link.getText().toString().trim();

            if(cbNormalText.isChecked() && stext.length()==0){
                alert("Warning !","Please insert 'post normal text' or remove selection.");
            }else if(cbImage.isChecked() && filePath==null){
                alert("Warning !","Please insert 'Image' or remove selection.");
            }else if(cbBoldText.isChecked() && btext.length()==0){
                alert("Warning !","Please insert 'post bold text' or remove selection.");
            }else if(cbLink.isChecked() && link.length()<5){
                alert("Warning !","Please insert valid link or remove selection.");
            }else{


                final PostData post = new PostData();
                post.setPoster_id(storage.getUID());
                post.setPost_time(new Date().toLocaleString()+" ("+bndl.getString("POSTER_TYPE")+")");

                if(cbNormalText.isChecked()){
                    post.setPost_stext(stext);
                }else{
                    post.setPost_stext("null");
                }

                if(cbBoldText.isChecked()){
                    post.setPost_btext(btext);
                }else{
                    post.setPost_btext("null");
                }

                if(cbLink.isChecked()){

                    if( !link.startsWith("http://") || !link.startsWith("https://") ){
                        link="http://"+link;
                    }
                    post.setPost_link(link);

                }else{
                    post.setPost_link("null");
                }

                if(!cbImage.isChecked()){
                    post.setPost_img("null");
                    post.setImg_name("null");
                    uploadPost(post);
                }else{
                    uploadPostWithImage(post);
                }



            }


        }else{
            alert("Warning !","You should select at list one item.");
        }



    }


    private void uploadPost(PostData postData)
    {
        if(storage.isNetConnected()) {

            final String post_key = postRef.push().getKey();
            pd.show();
            postRef.child(post_key).setValue(postData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    pd.dismiss();
                    if(task.isSuccessful()){

                        clearAllComponent();
                        Toast.makeText(ctx,"Post Successfully uploaded.",Toast.LENGTH_SHORT).show();
                        sendNotification();

                    }else{
                        alert("Error!","Post can't upload. Please try again.");
                    }

                }
            });
        }else{
            alert("Connection failed!","Please check your device net connection.");
        }

    }


    private void sendNotification()
    {
        Map<String, String> map = new HashMap<>();
        map.put("id",""+((int) (Math.random()*10000000)+1 ));
        map.put("uid",storage.getUID());
        map.put("name",poster_name.getText().toString());
        map.put("image",poster_image);
        map.put("type",bndl.getString("POSTER_TYPE"));
        notificationRef.setValue(map);
    }


    private void clearAllComponent()
    {
        post_stext.setText(null);
        filePath = null;
        post_img.setImageResource(R.drawable.img_chooser);
        post_btext.setText(null);
        post_link.setText(null);
    }


    private void uploadPostWithImage(final PostData postData)
    {

        if(storage.isNetConnected()) {

            try {

                if(getPath(ctx,filePath)!=null) {

                    final byte[] imgBytes = compressedImage(new File(getPath(ctx, filePath)));
                    if (imgBytes == null) {
                        Toast.makeText(ctx, "Image location not found.", Toast.LENGTH_SHORT).show();
                        post_img.setImageResource(R.drawable.ic_choose_photo);
                        filePath = null;
                    } else {
                        pd.show();
                        final String img_name = "img_" + System.currentTimeMillis() + "_" + ((int) (10000 + (Math.random() * 100000))) + ".JPEG";
                        imgRef.child(img_name).putBytes(imgBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                //pd.dismiss();
                                if (task.isSuccessful()) {
                                    String imgUrl = task.getResult().getDownloadUrl().toString();
                                    postData.setPost_img(imgUrl);
                                    postData.setImg_name(img_name);
                                    uploadPost(postData);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(ctx, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                pd.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });

                    }

                }else{
                    alert("Warning !","Image location not found.");
                }

            } catch (Exception ex) {
                Toast.makeText(ctx, "Ex : " + ex.toString(), Toast.LENGTH_LONG).show();
            }

        }else{
            alert("Connection failed!","Please check your device net connection.");
        }

    }



    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!android.provider.Settings.System.canWrite(ctx)) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
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

        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 102 && data != null && data.getData() != null) {
                try {
                    filePath = data.getData();
                    //Toast.makeText(ctx,"File Path : "+filePath.getPath(),Toast.LENGTH_SHORT).show();
                    Bitmap img = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), filePath);
                    post_img.setImageBitmap(img);
                    //Toast.makeText(ctx,"Real Path : "+getRealPathFromURI(filePath),Toast.LENGTH_SHORT).show();
                    //compressedImage(new File(getPath(ctx,filePath)));
                    if(getPath(ctx, filePath)!=null) {
                        Toast.makeText(ctx, "File : " + getPath(ctx, filePath), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception ex) {
                    Toast.makeText(ctx, "Something wrong. : " + ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception ex){
            Toast.makeText(ctx, "Something wrong. : " + ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }



    private byte[] compressedImage(File imgFile)
    {
        try {
            postImgBitmap = new Compressor(ctx)
                    .setMaxWidth(350)
                    .setMaxHeight(300)
                    .setQuality(75)
                    .compressToBitmap(imgFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            postImgBitmap.compress(Bitmap.CompressFormat.JPEG,75,bos);
            return bos.toByteArray();
        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
        }

        return null;
    }







    private void loadUserDataAndTime()
    {
        userRef = FirebaseDatabase.getInstance().getReference().child("user").child(storage.getUID()).child("profile");
        userRef.keepSynced(true);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    final User user = dataSnapshot.getValue(User.class);
                    poster_name.setText(""+user.getName());
                    poster_image = user.getImgUrl();
                    Picasso.with(ctx).load(user.getImgUrl()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_user).into(poster_img, new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(user.getImgUrl()).placeholder(R.drawable.default_user).into(poster_img);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Date date = new Date();
        post_time.setText( date.toLocaleString()+" ("+bndl.getString("POSTER_TYPE")+")");
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
