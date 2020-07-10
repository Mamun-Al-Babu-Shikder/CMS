package com.mcubes.aamamun.classmanagementsystem.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mcubes.aamamun.classmanagementsystem.R;

import net.karthikraj.shapesimage.ShapesImage;

import org.w3c.dom.Text;

/**
 * Created by A.A.MAMUN on 2/27/2019.
 */

public class PostHolder extends RecyclerView.ViewHolder
{
    public ShapesImage poster_img;
    public ImageView post_img;
    public TextView poster_name, post_time, post_stext, post_btext ,post_link, post_like, post_dislike, post_comment;
    public LinearLayout like_btn, comment_btn, dislike_btn;
    public FrameLayout bTextBk;
    public Button menu_btn;
    public ImageView like_icon, comment_icon, dislike_icon;

    public PostHolder(View itemView) {

        super(itemView);

        bTextBk = (FrameLayout) itemView.findViewById(R.id.bTextBk);
        poster_img = (ShapesImage) itemView.findViewById(R.id.poster_img);
        poster_name = (TextView) itemView.findViewById(R.id.poster_name);
        post_time = (TextView) itemView.findViewById(R.id.post_time);
        menu_btn = (Button) itemView.findViewById(R.id.menuBtn);
        post_stext = (TextView) itemView.findViewById(R.id.post_stext);
        post_btext = (TextView) itemView.findViewById(R.id.post_btext);
        post_img = (ImageView) itemView.findViewById(R.id.post_img);
        post_link = (TextView) itemView.findViewById(R.id.post_link);
        post_like = (TextView) itemView.findViewById(R.id.post_like);
        post_comment = (TextView) itemView.findViewById(R.id.post_comment);
        post_dislike = (TextView) itemView.findViewById(R.id.post_dislike);
        like_icon = (ImageView) itemView.findViewById(R.id.like_icon);
        dislike_icon = (ImageView) itemView.findViewById(R.id.dislike_icon);
        comment_icon = (ImageView) itemView.findViewById(R.id.comment_icon);
        like_btn = (LinearLayout) itemView.findViewById(R.id.like_btn);
        dislike_btn = (LinearLayout) itemView.findViewById(R.id.dislike_btn);
        comment_btn = (LinearLayout) itemView.findViewById(R.id.comment_btn);


    }
}
