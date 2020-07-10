package com.mcubes.aamamun.classmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class AboutUs extends Fragment {

    private Context ctx;
    private View mView;
    private LinearLayout gmailBtn, fbBtn, twiterBtn;
    private TextView gotoMbstuRadio;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_about_us,container, false);

        gmailBtn = (LinearLayout) mView.findViewById(R.id.gmailBtn);
        fbBtn = (LinearLayout) mView.findViewById(R.id.fbBtn);
        twiterBtn = (LinearLayout) mView.findViewById(R.id.twitterBtn);
        gotoMbstuRadio = (TextView) mView.findViewById(R.id.gotoMbstuRadio);

        gmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","mcubes@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fb = new Intent(Intent.ACTION_VIEW);
                fb.setData(Uri.parse("https://www.facebook.com/mcubesit"));
                startActivity(fb);
            }
        });

        twiterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fb = new Intent(Intent.ACTION_VIEW);
                fb.setData(Uri.parse("https://twitter.com/McubesL"));
                startActivity(fb);
            }
        });

        gotoMbstuRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fb = new Intent(Intent.ACTION_VIEW);
                fb.setData(Uri.parse("https://www.facebook.com/mbsturadio"));
                startActivity(fb);
            }
        });

        return mView;
    }
}
