package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

import net.karthikraj.shapesimage.ShapesImage;

/**
 * Created by A.A.MAMUN on 2/14/2019.
 */

public class StudentHolder2 extends RecyclerView.ViewHolder {

    public RelativeLayout imgBk;
    public ShapesImage sImg;
    public TextView sId, sName, firstCharOfNam;
    public CheckBox cb;


    public StudentHolder2(View itemView) {
        super(itemView);

        //imgBk = (RelativeLayout) itemView.findViewById(R.id.imgBk);
        //firstCharOfNam = (TextView) itemView.findViewById(R.id.firstCharOfNam);
        sImg = (ShapesImage) itemView.findViewById(R.id.sImg);
        sId = (TextView) itemView.findViewById(R.id.sId);
        sName = (TextView) itemView.findViewById(R.id.sName);
        cb = (CheckBox) itemView.findViewById(R.id.cb);
    }
}
