package com.mcubes.aamamun.classmanagementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class SendFeedback extends Fragment {


    private Context ctx;
    private View mView;
    private EditText etTitle, etComment;
    private Button send;
    private String title, comment;
    private AlertDialog.Builder alb;
    private ProgressDialog pd;
    DatabaseReference reference;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ctx = getActivity();
        mView = inflater.inflate(R.layout.fragment_send_feedback, container, false);

        alb = new AlertDialog.Builder(ctx);
        alb.setTitle("Connection failed !");
        alb.setMessage("Please check your device net connection.");
        alb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });


        pd = new ProgressDialog(ctx);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        etTitle = (EditText) mView.findViewById(R.id.title);
        etComment = (EditText) mView.findViewById(R.id.comment);
        send = (Button) mView.findViewById(R.id.sendBtn);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                title = etTitle.getText().toString().trim();
                comment = etComment.getText().toString().trim();

                etTitle.setError(null);
                etComment.setError(null);

                if(title.length()==0){
                    etTitle.setError("Please enter title");
                }else if(comment.length()==0){
                    etComment.setError("Please enter your comment");
                }else{
                    sendFeedBack(title,comment);
                }

            }
        });




        return mView;
    }


    private void sendFeedBack(String title, String comment)
    {

        if(Storage.isNetConnected()) {

            pd.show();

            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            reference = FirebaseDatabase.getInstance().getReference("feedback");

            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("title", title);
            map.put("comment", comment);

            reference.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    pd.dismiss();
                    if(task.isSuccessful()){
                        etTitle.setText("");
                        etComment.setText("");
                        Toast.makeText(ctx,"Successfully sent",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ctx,"failed to send feedback",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            alb.show();
        }

    }

}
