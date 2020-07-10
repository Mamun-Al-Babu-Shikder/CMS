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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GoogleDrive extends Fragment {

    private boolean canAddLink = true;

    private Context ctx;
    private Storage storage;
    private View mView;
    private String clsId, access, driveLinkUrl="";


    private WebView webView;
    private LinearLayout notFoundLay,pb, goBack, refresh, goForward, actionBar,hideBar;
    private Button addGdrive;
    private Dialog dialog;
    private ProgressDialog pd;
    private ProgressBar loaderPb;

    private DatabaseReference gDriveRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        ctx = getActivity();
        storage = new Storage(ctx);
        clsId = getArguments().getString("CLASS_ID");
        access = getArguments().getString("ACCESS_PERSON");
        mView = inflater.inflate(R.layout.fragment_google_drive, container, false);

        //Toast.makeText(ctx,"CID : "+clsId+", AP : "+access,Toast.LENGTH_SHORT).show();

        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);




        actionBar = (LinearLayout) mView.findViewById(R.id.actionBar);
        goBack = (LinearLayout) mView.findViewById(R.id.goBack);
        refresh = (LinearLayout) mView.findViewById(R.id.refresh);
        hideBar = (LinearLayout) mView.findViewById(R.id.hideBar);
        goForward = (LinearLayout) mView.findViewById(R.id.goForward);
        webView = (WebView) mView.findViewById(R.id.webView);
        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        pb = (LinearLayout) mView.findViewById(R.id.pb);
        loaderPb = (ProgressBar) mView.findViewById(R.id.loaderPb);
        addGdrive = (Button) mView.findViewById(R.id.addGoogleDrive);

        if(access.equalsIgnoreCase("STUDENT")){
            addGdrive.setVisibility(View.GONE);
        }else{
            addGdrive.setVisibility(View.VISIBLE);
        }



        hideBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(actionBar.getVisibility()==View.VISIBLE){
                    actionBar.setVisibility(View.GONE);
                }else{
                    actionBar.setVisibility(View.VISIBLE);
                }
            }
        });


        gDriveRef = FirebaseDatabase.getInstance().getReference("google_drive").child(clsId).child("address");

        try {
           init();
        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex,Toast.LENGTH_SHORT).show();
        }




        return mView;
    }


    private void init()
    {

        notFoundLay.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);



        gDriveRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                canAddLink = true;
                if(dataSnapshot.exists()){


                    notFoundLay.setVisibility(View.GONE);
                    pb.setVisibility(View.GONE);

                    driveLinkUrl = (String) dataSnapshot.getValue();



                    loadLinkToWebView(driveLinkUrl);

                }else{
                    notFoundLay.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        addGdrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canAddLink) {
                    addGdriveLinkDialog();
                }else{
                    Toast.makeText(ctx,"Please wait...",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void addGdriveLinkDialog()
    {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_googledrive_link);
        final EditText et = (EditText) dialog.findViewById(R.id.editValue);
        Button done = (Button) dialog.findViewById(R.id.doneBtn);
        Button cancel = (Button) dialog.findViewById(R.id.cancleBtn);

        et.setText(driveLinkUrl);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et.setError(null);
                final String link = et.getText().toString().trim().replace(" ","");
                if(link.length()>13 || link.startsWith("http://") || link.startsWith("https://") ){
                    dialog.dismiss();
                    addGdriveLink(link);
                }else{
                    et.setError("Please enter valid address");
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

    private void addGdriveLink(String link)
    {
        if(Storage.isNetConnected()){

            pd.show();
            gDriveRef.setValue(link).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    pd.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(ctx,"Successfully added",Toast.LENGTH_SHORT).show();
                        init();
                    }else{
                        Toast.makeText(ctx,"Failed to add this address",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else{
            alert("Connection failed !","Please check your device net connection.");
        }
    }


    private void loadLinkToWebView(String linkUrl)
    {
        //Toast.makeText(ctx,"Link : "+linkUrl,Toast.LENGTH_SHORT).show();


        webView.loadUrl(linkUrl);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {}

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {}

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                super.onReceivedError(view, errorCode, description, failingUrl);
                webView.loadUrl("file:///android_asset/web/error.html");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaderPb.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String s1, String s2, String s3, long l) {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }


        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int p) {

                if(p>0 && p<100) {
                    loaderPb.setVisibility(View.VISIBLE);
                    loaderPb.setProgress(p);
                }else {
                    loaderPb.setVisibility(View.GONE);
                    loaderPb.setProgress(0);
                }

            }
        });


        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(webView.canGoBack()){
                    webView.goBack();
                }else{

                    webView.clearCache(true);
                    webView.clearFormData();
                    webView.clearHistory();
                    webView.clearMatches();
                    webView.clearSslPreferences();
                    onDetach();
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();
                refresh.startAnimation(animation());
            }
        });

        goForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
    }

    private Animation animation()
    {
        RotateAnimation ranim = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ranim.setDuration(500);
        return ranim;
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
