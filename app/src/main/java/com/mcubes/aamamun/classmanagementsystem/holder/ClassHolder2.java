package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

/**
 * Created by A.A.MAMUN on 2/17/2019.
 */

public class ClassHolder2 extends RecyclerView.ViewHolder {

    public LinearLayout bk;
    public TextView cidText,cid, cCode, cTitle, status;
    public Button crossBtn;

    public ClassHolder2(View itemView) {
        super(itemView);

        bk = (LinearLayout) itemView.findViewById(R.id.bk);
        cidText = (TextView) itemView.findViewById(R.id.cidText);
        cid = (TextView) itemView.findViewById(R.id.cid);
        cCode = (TextView) itemView.findViewById(R.id.cCode);
        cTitle = (TextView) itemView.findViewById(R.id.cTitle);
        status = (TextView) itemView.findViewById(R.id.status);

        crossBtn = (Button) itemView.findViewById(R.id.crossBtn);


    }
}
