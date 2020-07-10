package com.mcubes.aamamun.classmanagementsystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeFragment extends Fragment {

    private Context ctx;
    private Storage storage;
    private View mView;
    private ProgressBar pb,pb2;
    private TextView tv,tv2;
    private DatabaseReference ref,ref2;
    private int mcls=0,tcls=0,total=0;

    private AdView mAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        pb = (ProgressBar) mView.findViewById(R.id.percentPb);
        pb2 = (ProgressBar) mView.findViewById(R.id.percentPb2);

        tv = (TextView) mView.findViewById(R.id.percentViw);
        tv2 = (TextView) mView.findViewById(R.id.percentViw2);

        storage = new Storage(ctx);




        //-----------Ad VIew--------//
        MobileAds.initialize(ctx, "ca-app-pub-3578920841741120~6856771711");
        mAdView = (AdView) mView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        loadClass();


        return mView;
    }


    private void loadClass()
    {
        ref = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("my_class_room");
        ref2 = FirebaseDatabase.getInstance().getReference("user").child(storage.getUID()).child("teacher_class_room");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    mcls = (int)dataSnapshot.getChildrenCount();
                }

                ref2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            tcls = (int) dataSnapshot.getChildrenCount();
                           // Toast.makeText(ctx,"tcls : "+tcls,Toast.LENGTH_SHORT).show();
                        }

                        total = mcls + tcls;

                        if(total==0){
                            pb.setProgress(0);
                            pb2.setProgress(0);
                            tv.setText("0%");
                            tv2.setText("0%");
                        }else{

                            mcls = (int) ((mcls*100)/total);
                            tcls = (int) ((tcls*100)/total);
                            pb.setProgress(mcls);
                            pb2.setProgress(tcls);
                            tv.setText(mcls+"%");
                            tv2.setText(tcls+"%");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



}
