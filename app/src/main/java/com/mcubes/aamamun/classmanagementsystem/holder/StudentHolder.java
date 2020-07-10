package com.mcubes.aamamun.classmanagementsystem.holder;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

import net.karthikraj.shapesimage.ShapesImage;

/**
 * Created by A.A.MAMUN on 2/13/2019.
 */

public class StudentHolder extends RecyclerView.ViewHolder
{

    public LinearLayout profileIndicatorAlpha;
    public RelativeLayout imgBk;
    public ShapesImage sImg;
    public TextView sId, sName, firstCharOfNam;
    public ImageView profileIndicator;


    public StudentHolder(View itemView) {
        super(itemView);

        //imgBk = (RelativeLayout) itemView.findViewById(R.id.imgBk);
        //firstCharOfNam = (TextView) itemView.findViewById(R.id.firstCharOfNam);
        sImg = (ShapesImage) itemView.findViewById(R.id.sImg);

        sId = (TextView) itemView.findViewById(R.id.sId);
        sName = (TextView) itemView.findViewById(R.id.sName);

        profileIndicator = (ImageView) itemView.findViewById(R.id.profileIndicator);
        profileIndicatorAlpha = (LinearLayout) itemView.findViewById(R.id.profileIndicatorAlpha);
    }
}
