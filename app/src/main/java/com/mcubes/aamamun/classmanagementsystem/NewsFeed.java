package com.mcubes.aamamun.classmanagementsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mcubes.aamamun.classmanagementsystem.holder.PostHolder;
import com.mcubes.aamamun.classmanagementsystem.model.PostData;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.URL;


public class NewsFeed extends Fragment {

    private Context ctx;
    private View mView;
    private Storage storage;
    private Bundle bndl;
    private boolean isDataExist = false;
    private RecyclerView recyclerView;
    private LinearLayout notFoundLay, pb;
    private FirebaseRecyclerAdapter<PostData, PostHolder> adapter;

    private String clsId;

    private DatabaseReference userRef,postRef, likeRef, commentRef, dislikeRef;
    private StorageReference imgRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        try {

            ctx = getActivity();
            mView = inflater.inflate(R.layout.fragment_news_feed, container, false);
            bndl = getArguments();
            storage = new Storage(ctx);
            clsId = bndl.getString("CLASS_ID");

            //Toast.makeText(ctx,"ClASS ID : \n"+clsId,Toast.LENGTH_SHORT).show();

             init();

        }catch (Exception ex){
            Toast.makeText(ctx,"Ext-init : \n"+ex,Toast.LENGTH_SHORT).show();
        }

        return mView;
    }


    private void init()
    {
        notFoundLay = (LinearLayout) mView.findViewById(R.id.notFoundLay);
        pb = (LinearLayout) mView.findViewById(R.id.pb);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(ctx);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);




        try {
            roadPostAndDisplay();
        }catch (Exception ex){
            Toast.makeText(ctx,"Ex : "+ex,Toast.LENGTH_LONG).show();
        }


    }


    private void roadPostAndDisplay()
    {

        userRef = FirebaseDatabase.getInstance().getReference("user");
        imgRef = FirebaseStorage.getInstance().getReference("post_image");

        /*
        likeRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("news_feed").child("like");
        likeRef.keepSynced(true);
        dislikeRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("news_feed").child("dislike");
        dislikeRef.keepSynced(true);
        commentRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("news_feed").child("comment");
        commentRef.keepSynced(true);
        postRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("news_feed").child("post");
        postRef.keepSynced(true);
        */

        likeRef = FirebaseDatabase.getInstance().getReference("news_feed").child(clsId).child("like");
        likeRef.keepSynced(true);
        dislikeRef = FirebaseDatabase.getInstance().getReference("news_feed").child(clsId).child("dislike");
        dislikeRef.keepSynced(true);
        commentRef = FirebaseDatabase.getInstance().getReference("news_feed").child(clsId).child("comment");
        commentRef.keepSynced(true);
        postRef = FirebaseDatabase.getInstance().getReference("news_feed").child(clsId).child("post");
        postRef.keepSynced(true);


        pb.setVisibility(View.VISIBLE);
        notFoundLay.setVisibility(View.GONE);

        try {

            adapter = new FirebaseRecyclerAdapter<PostData, PostHolder>(
                    PostData.class,
                    R.layout.post_holder,
                    PostHolder.class,
                    postRef
            ) {

                @Override
                protected void populateViewHolder(final PostHolder vh, final PostData model, final int position) {


                    try {

                        isDataExist = true;
                        pb.setVisibility(View.GONE);
                        notFoundLay.setVisibility(View.GONE);


                        final boolean[] isLike = new boolean[adapter.getItemCount()];
                        final Boolean[] isDislike = new Boolean[adapter.getItemCount()];
                        //final String postId = adapter.getRef(position).getKey();

                        try {
                            //userRef = FirebaseDatabase.getInstance().getReference("user").child(model.getPoster_id()).child("profile");
                            userRef.child(model.getPoster_id()).child("profile").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {

                                        try {
                                            final User user = dataSnapshot.getValue(User.class);
                                            vh.poster_name.setText("" + user.getName());
                                            try {
                                                Picasso.with(ctx).load(user.getImgUrl()).placeholder(R.drawable.default_user).networkPolicy(NetworkPolicy.OFFLINE).into(vh.poster_img, new Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(ctx).load(user.getImgUrl()).placeholder(R.drawable.default_user).into(vh.poster_img);
                                                    }
                                                });
                                            } catch (Exception ex) {
                                                Toast.makeText(ctx, "Can't load image", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {
                                            Toast.makeText(ctx, "failed to load user data", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            vh.post_time.setText(model.getPost_time());


                            if (model.getPost_stext().equalsIgnoreCase("null")) {
                                vh.post_stext.setVisibility(View.GONE);
                            } else {
                                vh.post_stext.setVisibility(View.VISIBLE);
                                vh.post_stext.setText(model.getPost_stext());
                            }


                            if (model.getPost_img().equalsIgnoreCase("null")) {
                                vh.post_img.setVisibility(View.GONE);
                                //Toast.makeText(ctx,"Post Image not Found \n",Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(ctx,"Post Image Found \n"+model.getPost_img(),Toast.LENGTH_SHORT).show();
                                vh.post_img.setVisibility(View.VISIBLE);

                                try {
                                    Picasso.with(ctx).load(model.getPost_img()).placeholder(R.drawable.img_chooser).networkPolicy(NetworkPolicy.OFFLINE).into(vh.post_img, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            try {
                                                Picasso.with(ctx).load(model.getPost_img()).placeholder(R.drawable.img_chooser).into(vh.post_img);
                                            }catch (Exception ex){
                                                Toast.makeText(ctx, "Can't load image", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    Toast.makeText(ctx, "Can't load image", Toast.LENGTH_SHORT).show();
                                }


                                try {
                                    vh.post_img.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.parse(model.getPost_img()), "image/*");
                                                startActivity(intent);
                                            } catch (Exception ex) {
                                                Toast.makeText(ctx, "Image can't view", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }catch (Exception ex){
                                    Toast.makeText(ctx, "Image can't view", Toast.LENGTH_SHORT).show();
                                }

                            }




                            if (model.getPost_btext().equalsIgnoreCase("null")) {
                                vh.bTextBk.setBackgroundResource(0);
                                vh.post_btext.setVisibility(View.GONE);
                            } else {
                                vh.post_btext.setVisibility(View.VISIBLE);
                                vh.bTextBk.setBackgroundResource(R.drawable.bk3);
                                String btext = model.getPost_btext();
                                if (btext.length() > 250) {
                                    btext = btext.substring(0, 249) + "...";
                                }
                                vh.post_btext.setText(btext);
                            }

                            if (model.getPost_link().equalsIgnoreCase("null")) {
                                vh.post_link.setVisibility(View.GONE);
                            } else {
                                vh.post_link.setVisibility(View.VISIBLE);
                                vh.post_link.setText(model.getPost_link());
                                vh.post_link.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        try {
                                            Intent next = new Intent(Intent.ACTION_VIEW);
                                            next.setData(Uri.parse(model.getPost_link()));
                                            startActivity(next);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }


                            if (storage.getUID().equals(model.getPoster_id())) {

                                vh.menu_btn.setVisibility(View.VISIBLE);
                                final PopupMenu pop = new PopupMenu(ctx, vh.menu_btn);
                                pop.getMenu().add("Delete");
                                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem mi) {
                                        if (mi.getTitle().equals("Delete")) {
                                            deletePost(adapter.getRef(position).getKey(), model);
                                        }
                                        return true;
                                    }
                                });
                                vh.menu_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        pop.show();
                                    }
                                });

                            } else {
                                vh.menu_btn.setVisibility(View.GONE);
                            }


                            likeRef.child(adapter.getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String like = "0 Like";
                                    if (dataSnapshot.exists()) {

                                        long count = dataSnapshot.getChildrenCount();
                                        if (count > 999) {
                                            count /= 1000;
                                            like = count + "K Like";
                                        } else {
                                            like = count + " Like";
                                        }
                                        vh.post_like.setText(like);

                                    } else {
                                        vh.like_icon.setImageResource(R.drawable.ic_like);
                                        vh.post_like.setText("0 Like");
                                    }


                                    if (dataSnapshot.hasChild(storage.getUID())) {
                                        vh.like_icon.setImageResource(R.drawable.ic_like2);
                                        isLike[position] = true;
                                    } else {
                                        vh.like_icon.setImageResource(R.drawable.ic_like);
                                        isLike[position] = false;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            commentRef.child(adapter.getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {
                                        String comment = "0 Comment";
                                        long count = dataSnapshot.getChildrenCount();
                                        if (count > 999) {
                                            count /= 1000;
                                            comment = count + "K Comment";
                                        } else {
                                            comment = count + " Comment";
                                        }
                                        vh.post_comment.setText(comment);
                                    } else {
                                        vh.post_comment.setText("0 Comment");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            dislikeRef.child(adapter.getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {

                                        String dislike = "0 Dislike";
                                        long count = dataSnapshot.getChildrenCount();
                                        if (count > 999) {
                                            count /= 1000;
                                            dislike = count + "K Dislike";
                                        } else {
                                            dislike = count + " Dislike";
                                        }
                                        vh.post_dislike.setText(dislike);
                                    } else {
                                        vh.dislike_icon.setImageResource(R.drawable.ic_dislike);
                                        vh.post_dislike.setText("0 Dislike");
                                    }

                                    if (dataSnapshot.hasChild(storage.getUID())) {
                                        vh.dislike_icon.setImageResource(R.drawable.ic_dislike2);
                                        isDislike[position] = true;
                                    } else {
                                        vh.dislike_icon.setImageResource(R.drawable.ic_dislike);
                                        isDislike[position] = false;
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            vh.like_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if (storage.isNetConnected()) {
                                        if (isLike[position]) {
                                            likeRef.child(adapter.getRef(position).getKey()).child(storage.getUID()).removeValue();
                                        } else {
                                            if (isDislike[position]) {
                                                dislikeRef.child(adapter.getRef(position).getKey()).child(storage.getUID()).removeValue();
                                            }
                                            likeRef.child(adapter.getRef(position).getKey()).child(storage.getUID()).setValue(true);
                                        }
                                    } else {
                                        alert("Connection failed!", "Please check your device net connection.");
                                    }
                                }
                            });

                            vh.dislike_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    if (storage.isNetConnected()) {
                                        if (isDislike[position]) {
                                            dislikeRef.child(adapter.getRef(position).getKey()).child(storage.getUID()).removeValue();
                                        } else {
                                            if (isLike[position]) {
                                                likeRef.child(adapter.getRef(position).getKey()).child(storage.getUID()).removeValue();
                                            }
                                            dislikeRef.child(adapter.getRef(position).getKey()).child(storage.getUID()).setValue(true);
                                        }
                                    } else {
                                        alert("Connection failed!", "Please check your device net connection.");
                                    }

                                }
                            });


                            vh.comment_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(ctx, CommentActivity.class);
                                    bndl.putString("POST_ID", adapter.getRef(position).getKey());
                                    intent.putExtras(bndl);
                                    startActivity(intent);
                                }
                            });


                        } catch (Exception ex) {
                            Toast.makeText(ctx, "Ex : " + ex.toString(), Toast.LENGTH_SHORT).show();
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                }
            };

        }catch (Exception ex){
            ex.printStackTrace();
        }



        /*
        if(adapter.getItemCount()==0){
            pb.setVisibility(View.GONE);
            notFoundLay.setVisibility(View.VISIBLE);
        }
        */


        recyclerView.setAdapter(adapter);
        new Handler().postDelayed(delay,1000*30*1);


    }

    private Runnable delay = new Runnable() {

        @Override
        public void run() {
            pb.setVisibility(View.GONE);
            if(isDataExist) {
                notFoundLay.setVisibility(View.GONE);
            }else{
                notFoundLay.setVisibility(View.VISIBLE);
            }
        }
    };




    private void deletePost(final String postId, final PostData postData)
    {

        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
        alb.setTitle("Delete");
        alb.setMessage("Do you want to delete this post ?");
        alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(storage.isNetConnected()) {

                    postRef.child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){


                                likeRef.child(postId).removeValue();
                                dislikeRef.child(postId).removeValue();
                                commentRef.child(postId).removeValue();

                                if(!postData.getImg_name().equalsIgnoreCase("null")){
                                    imgRef.child(postData.getImg_name()).delete();
                                }

                                Toast.makeText(ctx,"Successfully deleted",Toast.LENGTH_SHORT).show();

                                adapter.notifyDataSetChanged();
                            }
                        }
                    });

                }else{
                    alert("Connection failed !","Please check your device net connection.");
                }

            }
        });
        alb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alb.show();


    }


    private void alert(String title, String message){

        AlertDialog.Builder alb = new AlertDialog.Builder(ctx);
        alb.setTitle(title);
        alb.setMessage(message);
        alb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alb.show();
    }

    @Override
    public void onDetach() {

        /*
        if(postRef!=null){
            postRef.onDisconnect();
        }

        if(userRef!=null){
            userRef.onDisconnect();
        }


        if(likeRef!=null){
            likeRef.onDisconnect();
        }

        if(dislikeRef!=null){
            dislikeRef.onDisconnect();
        }

        if(commentRef!=null){
            commentRef.onDisconnect();
        }
        */


        super.onDetach();
    }

}
