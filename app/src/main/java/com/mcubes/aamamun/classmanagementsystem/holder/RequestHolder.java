package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

/**
 * Created by A.A.MAMUN on 2/18/2019.
 */

public class RequestHolder extends RecyclerView.ViewHolder
{
    public TextView firstCharOfNam,stuName,status;
    public Button cancel,accept;

    public RequestHolder(View itemView) {

        super(itemView);

        firstCharOfNam = (TextView) itemView.findViewById(R.id.firstCharOfNam);
        stuName = (TextView) itemView.findViewById(R.id.sName);
        status = (TextView) itemView.findViewById(R.id.status);
        cancel = (Button) itemView.findViewById(R.id.cancel);
        accept = (Button) itemView.findViewById(R.id.accept);
    }
}
