package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

/**
 * Created by A.A.MAMUN on 2/24/2019.
 */

public class DateHolder extends RecyclerView.ViewHolder
{
    public TextView pos,date;

    public DateHolder(View itemView) {
        super(itemView);

        pos = (TextView) itemView.findViewById(R.id.datePos);
        date = (TextView) itemView.findViewById(R.id.dateView);

    }
}
