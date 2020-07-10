package com.mcubes.aamamun.classmanagementsystem;

import android.*;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mcubes.aamamun.classmanagementsystem.holder.FileHolder;
import com.mcubes.aamamun.classmanagementsystem.holder.PostHolder;
import com.mcubes.aamamun.classmanagementsystem.model.FileData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FilesFragment extends Fragment {

    public static final int TEACHER = 1,STUDENT = 2;
    private boolean isDataExist = false;
    private Context ctx;
    private View mView;
    private Storage storage;
    private Bundle bndl;
    private int acces_type;
    private String clsId;
    private Button uploadFileBtn;
    private RecyclerView recyclerView;
    private FrameLayout notFoundLay;
    private LinearLayout pb;
    private AlertDialog.Builder alb;
    private ProgressDialog pd;
    private Dialog dlg;
    private ListView lv;
    private BaseAdapter ba;
    private List<FileData> fileList;
    private static double fileSize, fileSize2;
    private static String fileSizStr;
    private FileData picFiledata;


    private File upDir, dwnDir;
    private FirebaseRecyclerAdapter<FileData,FileHolder> adapter;

    private DatabaseReference fileDataRef;
    private StorageReference storageReference;

    private static final String[] opt = {"Choose file","From upload folder"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_files, container, false);

        bndl = getArguments();
        clsId = bndl.getString("CLASS_ID");
        acces_type = bndl.getInt("ACCESS_TYPE");

        upDir = new File(Environment.getExternalStorageDirectory().getPath()+"/CMS/upload");
        dwnDir = new File(Environment.getExternalStorageDirectory().getPath()+"/CMS/download");

        storageReference = FirebaseStorage.getInstance().getReference("files");
        fileDataRef = FirebaseDatabase.getInstance().getReference("files").child(clsId);

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        init();

        return mView;
    }


    private void init()
    {
        uploadFileBtn = (Button) mView.findViewById(R.id.uploadFileBtn);
        if(acces_type==STUDENT){
            uploadFileBtn.setVisibility(View.GONE);
        }


        uploadFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if(!Settings.System.canWrite(ctx)){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},101);
                    }
                }else{
                   selectOptionToChooseFile();
                }

            }
        });

        notFoundLay = (FrameLayout) mView.findViewById(R.id.notFoundLay);
        pb = (LinearLayout) mView.findViewById(R.id.pb);

        recyclerView = (RecyclerView)mView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));



        fetchAndDisplayFiles();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==101) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectOptionToChooseFile();
            }else{
                Toast.makeText(ctx,"Permission Denied",Toast.LENGTH_SHORT).show();
            }

        }else if(requestCode==103){

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 downloadFile(picFiledata);
            }else{
                Toast.makeText(ctx,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void selectOptionToChooseFile()
    {

        if(!upDir.exists()){
            upDir.mkdirs();
        }

        if(!dwnDir.exists()){
            dwnDir.mkdirs();
        }

        alb = new  AlertDialog.Builder(ctx);
        alb.setItems(opt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent,"Choose file"),102);
                }else if(i==1){
                    readAllFilesFromUploadFolder();
                }
            }
        });
        alb.show();
    }




    private String getFileSizAsString(Long size)
    {
        double fileSize = (double) size;
        String fileSizStr = "";

        if(fileSize>1000){
            fileSize/=1024;
            if(fileSize>1000){
                fileSize/=1024;
                fileSizStr = (new DecimalFormat("###.##").format(fileSize))+" MB";
            }else{
                fileSizStr = (new DecimalFormat("###.##").format(fileSize))+" KB";
            }
        }else{
            fileSizStr = (new DecimalFormat("###.##").format(fileSize))+" B";
        }

        return fileSizStr;
    }



    private void readAllFilesFromUploadFolder()
    {

        try {

            fileList = new ArrayList<>();

            final File[] files = upDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }
            });

            for (File f : files) {
                if(f.isFile()) {
                    fileList.add(new FileData(f.getName(), getFileSizAsString(f.length())));
                }
            }

            dlg = new Dialog(ctx);
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dlg.setContentView(R.layout.dialog_choose_file_from_upload_dir);
            dlg.setCancelable(false);
            dlg.show();

            ImageView emptyFile = (ImageView) dlg.findViewById(R.id.emptyFile);
            Button exitDialog = (Button) dlg.findViewById(R.id.exitDialog);

            exitDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dlg.dismiss();
                }
            });

            if (fileList.size() == 0) {
                emptyFile.setVisibility(View.VISIBLE);
            } else {

                emptyFile.setVisibility(View.GONE);

                lv = (ListView) dlg.findViewById(R.id.fileList);

                ba = new BaseAdapter() {

                    @Override
                    public int getCount() {
                        return fileList.size();
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
                    public View getView(int i, View view, ViewGroup viewGroup) {

                        final FileData fd = fileList.get(i);
                        view = LayoutInflater.from(ctx).inflate(R.layout.upload_file_holder, viewGroup, false);

                        final ImageView fileIcon = (ImageView) view.findViewById(R.id.fileIcon);
                        final TextView fileName = (TextView) view.findViewById(R.id.fileName);
                        final TextView fileSize = (TextView) view.findViewById(R.id.fileSize);

                        fileName.setText("" + fd.getFile_name());
                        fileSize.setText("" + fd.getFile_size());

                        final String fName = fd.getFile_name();
                        if(fName.toLowerCase().endsWith(".doc")){
                            fileIcon.setImageResource(R.drawable.icon_doc);
                        }else  if(fName.toLowerCase().endsWith(".docx")){
                            fileIcon.setImageResource(R.drawable.icon_docx);
                        }else if(fName.toLowerCase().endsWith(".jpeg")){
                            fileIcon.setImageResource(R.drawable.icon_jpeg);
                        }else if(fName.toLowerCase().endsWith(".jpg")){
                            fileIcon.setImageResource(R.drawable.icon_jpg);
                        }else if(fName.toLowerCase().endsWith(".mp4")){
                            fileIcon.setImageResource(R.drawable.icon_mp4);
                        }else if(fName.toLowerCase().endsWith(".pdf")){
                            fileIcon.setImageResource(R.drawable.icon_pdf);
                        }else if(fName.toLowerCase().endsWith(".png")){
                            fileIcon.setImageResource(R.drawable.icon_png);
                        }else if(fName.toLowerCase().endsWith(".ppt")){
                            fileIcon.setImageResource(R.drawable.icon_ppt);
                        }else if(fName.toLowerCase().endsWith(".txt")){
                            fileIcon.setImageResource(R.drawable.icon_txt);
                        }else if(fName.toLowerCase().endsWith("..xls")){
                            fileIcon.setImageResource(R.drawable.icon_xls);
                        }else{
                            fileIcon.setImageResource(R.drawable.icon_file);
                        }



                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if( fd.getFile_size().split(" ")[0].equals("MB") && Double.parseDouble(fd.getFile_size().split(" ")[0])>150){
                                    alert("Warning !","File size is too high.");
                                }else{
                                    dlg.dismiss();
                                    uploadFile(fd);
                                }
                            }
                        });

                        return view;
                    }
                };

                lv.setAdapter(ba);


            }


        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : \n"+ex.toString(),Toast.LENGTH_LONG).show();
        }

    }



    private void uploadFile(final FileData fileData)
    {

        if(storage.isNetConnected()) {
            try {

                pd.show();

                final String filNam = "file_" + System.currentTimeMillis() + "_" + (100000 + (int) (Math.random() * 100000)) + "_" + fileData.getFile_name();
                fileData.setName_at_store(filNam);

                storageReference.child(filNam).putFile(Uri.fromFile(new File(upDir + "/" + fileData.getFile_name()))).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            fileData.setUpload_date(new Date().toLocaleString());
                            fileData.setDownload_link(task.getResult().getDownloadUrl().toString());

                            fileDataRef.push().setValue(fileData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    pd.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ctx, "File successfully uploaded", Toast.LENGTH_SHORT).show();
                                    } else {
                                        alert("Error !", "Can't upload file. Please try again.");
                                    }
                                }
                            });

                        } else {
                            pd.dismiss();
                            alert("Error !", "Can't upload file. Please try again.");
                        }
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        pd.setMessage("Uploaded... " + (int) progress + "%");
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            alert("Connection failed !","Please check your device net connection.");
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==102 && data!=null && data.getData()!=null){

            try {

                final Uri filePath = data.getData();
                if(getPath(ctx,data.getData())!=null) {

                    File f = new File(getPath(ctx, data.getData()));
                    FileData fileData = new FileData();
                    fileData.setFile_name(f.getName());
                    fileData.setFile_size(getFileSizAsString(f.length()));

                    uploadFile(filePath,fileData);
                }

                /*
                Toast.makeText(ctx, "File : " + getPath(ctx,data.getData()), Toast.LENGTH_SHORT).show();
                String file[] = getFileNameAndExtension(getPath(ctx,data.getData()));
                Toast.makeText(ctx, "Name : " + file[0] + ", Ext : " + file[1], Toast.LENGTH_LONG).show();
                */



            }catch (Exception ex){
               // Toast.makeText(ctx,"Sorry, File location not found.",Toast.LENGTH_LONG).show();
                alert("Warning !","Sorry, File location not found.\nYou can upload this file from \"/sdcard/CMS/upload\" folder.");
            }

        }
    }

    private void uploadFile(Uri filePathUri, final FileData fileData)
    {
        if(storage.isNetConnected()) {

            final String filNam = "file_" + System.currentTimeMillis() + "_" + (100000 + (int) (Math.random() * 10000)) + "_" + fileData.getFile_name();
            pd.show();
            storageReference.child(filNam).putFile(filePathUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        fileData.setName_at_store(filNam);
                        fileData.setDownload_link(task.getResult().getDownloadUrl().toString());
                        fileData.setUpload_date(new Date().toLocaleString());
                        fileDataRef.push().setValue(fileData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                pd.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(ctx, "File successfully uploaded", Toast.LENGTH_SHORT).show();
                                } else {
                                    alert("Error!", "Can't upload file. Please try again.");
                                }
                            }
                        });

                    } else {
                        pd.dismiss();
                        alert("Error !", "Can't upload file. Please try again.");
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    pd.setMessage("Uploaded... " + (int) progress + "%");
                }
            });
        }else{
            alert("Connection failed !","Please check your device net connection.");
        }



    }


    private String[] getFileNameAndExtension(String uri)
    {
        String val[] = uri.split("/");
        String name="",ext="",name_ext = val[val.length-1];
        if(name_ext.contains(".") && !name_ext.startsWith(".") && !name_ext.endsWith(".")) {
            final int len = name_ext.length();
            for (int i=len-1;i>0;i--){
                if(name_ext.charAt(i)=='.'){
                    break;
                }else{
                    ext+=name_ext.charAt(i);
                }
            }
            StringBuilder sb = new StringBuilder(ext);
            ext = sb.reverse().toString();
            name = name_ext.replace("."+ext,"");

            return new String[]{name,ext};
        }else{
            return new String[]{null,null};
        }
    }




    private void fetchAndDisplayFiles()
    {
        pb.setVisibility(View.VISIBLE);
        notFoundLay.setVisibility(View.GONE);

        try {

            adapter = new FirebaseRecyclerAdapter<FileData, FileHolder>(FileData.class, R.layout.file_holder, FileHolder.class, fileDataRef) {

                @Override
                protected void populateViewHolder(FileHolder vh, final FileData model, final int position) {

                    isDataExist = true;
                    notFoundLay.setVisibility(View.GONE);
                    pb.setVisibility(View.GONE);

                    final String key = adapter.getRef(position).getKey().toString();

                    try{


                        final String fName = model.getFile_name();
                        if(fName.toLowerCase().endsWith(".doc")){
                            vh.fileIcon.setImageResource(R.drawable.icon_doc);
                        }else  if(fName.toLowerCase().endsWith(".docx")){
                            vh.fileIcon.setImageResource(R.drawable.icon_docx);
                        }else if(fName.toLowerCase().endsWith(".jpeg")){
                            vh.fileIcon.setImageResource(R.drawable.icon_jpeg);
                        }else if(fName.toLowerCase().endsWith(".jpg")){
                            vh.fileIcon.setImageResource(R.drawable.icon_jpg);
                        }else if(fName.toLowerCase().endsWith(".mp4")){
                            vh.fileIcon.setImageResource(R.drawable.icon_mp4);
                        }else if(fName.toLowerCase().endsWith(".pdf")){
                            vh.fileIcon.setImageResource(R.drawable.icon_pdf);
                        }else if(fName.toLowerCase().endsWith(".png")){
                            vh.fileIcon.setImageResource(R.drawable.icon_png);
                        }else if(fName.toLowerCase().endsWith(".ppt")){
                            vh.fileIcon.setImageResource(R.drawable.icon_ppt);
                        }else if(fName.toLowerCase().endsWith(".txt")){
                            vh.fileIcon.setImageResource(R.drawable.icon_txt);
                        }else if(fName.toLowerCase().endsWith("..xls")){
                            vh.fileIcon.setImageResource(R.drawable.icon_xls);
                        }else{
                            vh.fileIcon.setImageResource(R.drawable.icon_file);
                        }



                        vh.fileName.setText(fName);
                        vh.fileSize.setText(model.getFile_size());

                        vh.downloadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                                alb.setTitle("Download");
                                alb.setMessage("Name : "+model.getFile_name()+"\nSize : "+model.getFile_size()+"\nUpload Date : "+model.getUpload_date()+"\nSave As : \n"+model.getName_at_store());
                                alb.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        picFiledata = model;
                                        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                                            if(!Settings.System.canWrite(ctx)){
                                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},103);
                                            }
                                        }else{
                                            downloadFile(model);
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
                        });

                        if(acces_type==TEACHER) {

                            vh.deleteBtn.setVisibility(View.VISIBLE);
                            vh.deleteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
                                    alb.setTitle("Delete");
                                    alb.setMessage("Do you want to delete this file ?");
                                    alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            deleteFile(model.getName_at_store(),key);
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
                        }else{
                            vh.deleteBtn.setVisibility(View.GONE);
                        }

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }


                }

            };


            recyclerView.setAdapter(adapter);
            new Handler().postDelayed(delay,1000*30*1);

        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex.toString(),Toast.LENGTH_SHORT).show();
        }


    }


    private void downloadFile(FileData fileData)
    {
        if(storage.isNetConnected()) {
            try {
                Toast.makeText(ctx, "Start downloading...", Toast.LENGTH_SHORT).show();
                DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileData.getDownload_link()));
                request.setTitle(fileData.getFile_name());
                request.setDescription("Downloading file...");
                request.setDestinationInExternalPublicDir("/CMS/download", fileData.getName_at_store());
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                dm.enqueue(request);
            } catch (Exception ex) {
                Toast.makeText(ctx, "Download failed", Toast.LENGTH_SHORT).show();
            }
        }else{
            alert("Connection failed !","Please check your device net connection.");
        }
    }


    private void deleteFile(final String fileName, String key)
    {
        if(storage.isNetConnected()) {
            pd.show();
            fileDataRef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        adapter.notifyDataSetChanged();
                        storageReference.child(fileName).delete();
                        Toast.makeText(ctx, "Successfully deleted.", Toast.LENGTH_SHORT).show();
                    } else {
                        alert("Failed!", "Can't delete this file. Please try again.");
                    }
                }
            });
        }else{
            alert("Connection failed!","Please check your device net connection.");
        }

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
    public void onDetach() {
        super.onDetach();
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

