package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

/**
 * Created by A.A.MAMUN on 2/10/2019.
 */

public class ClassHolder extends RecyclerView.ViewHolder
{
    public TextView pos, cCode, cTitle, clsId;
    public LinearLayout posBk, sideBk;
    public Button menuBtn;

    public ClassHolder(View itemView) {

        super(itemView);

       posBk = (LinearLayout) itemView.findViewById(R.id.posBk);
       sideBk = (LinearLayout) itemView.findViewById(R.id.sideBk);
        pos = (TextView) itemView.findViewById(R.id.pos);
        cCode = (TextView) itemView.findViewById(R.id.cCode);
        cTitle = (TextView) itemView.findViewById(R.id.cTitle);
        clsId = (TextView) itemView.findViewById(R.id.clsID);
        menuBtn = (Button) itemView.findViewById(R.id.menuBtn);
    }
}