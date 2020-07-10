package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

/**
 * Created by A.A.MAMUN on 3/3/2019.
 */

public class FileHolder extends RecyclerView.ViewHolder {

    public ImageView fileIcon;
    public TextView fileName, fileSize;
    public Button downloadBtn, deleteBtn;


    public FileHolder(View itemView) {
        super(itemView);

        fileIcon = (ImageView) itemView.findViewById(R.id.fileIcon);
        fileName = (TextView) itemView.findViewById(R.id.fileName);
        fileSize = (TextView) itemView.findViewById(R.id.fileSize);
        downloadBtn = (Button) itemView.findViewById(R.id.downloadBtn);
        deleteBtn = (Button) itemView.findViewById(R.id.deleteBtn);
    }
}
