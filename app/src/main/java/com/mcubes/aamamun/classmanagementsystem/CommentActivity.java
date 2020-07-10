package com.mcubes.aamamun.classmanagementsystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mcubes.aamamun.classmanagementsystem.holder.CommentHolder;
import com.mcubes.aamamun.classmanagementsystem.holder.PostHolder;
import com.mcubes.aamamun.classmanagementsystem.model.Comment;
import com.mcubes.aamamun.classmanagementsystem.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class CommentActivity extends AppCompatActivity {

    private ActionBar ab;
    private Bundle bndl;
    private Storage storage;
    private String clsId,postId;
    private EditText etComment;
    private RecyclerView recyclerView;
    private ProgressDialog pd;


    private DatabaseReference userRef,commentRef;
    private FirebaseRecyclerAdapter<Comment,CommentHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Comment");

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");

        storage = new Storage(this);
        bndl = getIntent().getExtras();
        clsId = bndl.getString("CLASS_ID");


        init();




    }

    private void init()
    {
        etComment = (EditText) findViewById(R.id.etComment);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(false);


        fetchCommentAndDisplay();
    }



    private void fetchCommentAndDisplay()
    {

        userRef = FirebaseDatabase.getInstance().getReference().child("user");
        userRef.keepSynced(true);
        /*
        commentRef = FirebaseDatabase.getInstance().getReference("class").child(clsId).child("news_feed").child("comment").child(bndl.getString("POST_ID"));
        commentRef.keepSynced(true);
        */
        commentRef = FirebaseDatabase.getInstance().getReference("news_feed").child(clsId).child("comment").child(bndl.getString("POST_ID"));
        commentRef.keepSynced(true);



        adapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                Comment.class,
                R.layout.comment_holder,
                CommentHolder.class,
                commentRef
        ) {
            @Override
            protected void populateViewHolder(final CommentHolder vh, final Comment model, final int position) {



                userRef.child(model.getCommenter_id()).child("profile").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            final User user = dataSnapshot.getValue(User.class);
                            vh.name.setText(user.getName());

                            Picasso.with(getBaseContext()).load(user.getImgUrl())
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.default_user)
                                    .into(vh.img, new Callback() {
                                        @Override
                                        public void onSuccess() {}
                                        @Override
                                        public void onError() {
                                            Picasso.with(getBaseContext()).load(user.getImgUrl()).placeholder(R.drawable.default_user).into(vh.img);
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                vh.date.setText(model.getDate());
                vh.comment.setText(model.getComment());

                if(storage.getUID().equals(model.getCommenter_id())){

                    vh.edit.setEnabled(true);
                    vh.delete.setEnabled(true);
                    vh.edit.setVisibility(View.VISIBLE);
                    vh.delete.setVisibility(View.VISIBLE);

                    vh.edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editComment(adapter.getRef(position).getKey(),model.getComment());
                        }
                    });

                    vh.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                             deleteComment(adapter.getRef(position).getKey());
                        }
                    });

                    vh.copy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            etComment.setText(model.getComment());
                            Toast.makeText(getBaseContext(),"Copy to comment box",Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{

                    vh.edit.setVisibility(View.GONE);
                    vh.delete.setVisibility(View.GONE);

                    vh.copy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            etComment.setText(model.getComment());
                            Toast.makeText(getBaseContext(),"Copy to comment box",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        };

        recyclerView.setAdapter(adapter);

    }

    private void editComment(final String id, final String comnt)
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_edit_comment);

        final EditText etComment = (EditText) dialog.findViewById(R.id.editComment);
        final Button done = (Button) dialog.findViewById(R.id.doneBtn);
        final Button cancel = (Button) dialog.findViewById(R.id.cancleBtn);

        etComment.setText(""+comnt);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String editComnt = etComment.getText().toString().trim();
                if(editComnt.length()!=0){
                    commentRef.child(id).child("comment").setValue(editComnt);
                    etComment.setText("");
                    dialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void deleteComment(final String id)
    {
        AlertDialog.Builder alb = new AlertDialog.Builder(this);
        alb.setTitle("Warning!");
        alb.setMessage("Do you want to delete this comment ?");
        alb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(storage.isNetConnected()) {
                    commentRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }else{
                    alert("Connection failed!","Please check your device net connection.");
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

    public void commentBtn(View v)
    {
        if(storage.isNetConnected()){

            final String comment = etComment.getText().toString().trim();
            if(comment.length()!=0){

                final String key = commentRef.push().getKey();
                Comment com = new Comment();
                com.setCommenter_id(storage.getUID());
                com.setDate(new Date().toLocaleString());
                //com.setDate(new Date().toLocaleString()+" ("+bndl.getString("POSTER_TYPE")+")");
                com.setComment(comment);
                commentRef.child(key).setValue(com).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    }
                });
                etComment.setText("");
            }
        }else{
            alert("Connection failed!","Please check your device net connection.");
        }

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if(mi.getItemId()==android.R.id.home){
            CommentActivity.this.finish();
        }
        return super.onOptionsItemSelected(mi);
    }



    private void alert(String title, String message){

        AlertDialog.Builder alb = new AlertDialog.Builder(this);
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






}
