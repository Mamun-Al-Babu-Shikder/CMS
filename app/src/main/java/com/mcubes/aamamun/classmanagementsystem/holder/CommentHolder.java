package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

import net.karthikraj.shapesimage.ShapesImage;

/**
 * Created by A.A.MAMUN on 3/1/2019.
 */

public class CommentHolder extends RecyclerView.ViewHolder {

    public ShapesImage img;
    public TextView name,date,comment,copy,edit,delete;

    public CommentHolder(View itemView) {
        super(itemView);

        img = (ShapesImage) itemView.findViewById(R.id.img);
        name = (TextView) itemView.findViewById(R.id.name);
        date = (TextView) itemView.findViewById(R.id.date);
        comment = (TextView) itemView.findViewById(R.id.comment);
        copy = (TextView) itemView.findViewById(R.id.copy);
        edit = (TextView) itemView.findViewById(R.id.edit);
        delete = (TextView) itemView.findViewById(R.id.delete);
    }
}
