package com.mcubes.aamamun.classmanagementsystem;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail,etPass;
    private CheckBox cb;
    private String email, pass;
    private boolean isRemember;
    private static ProgressDialog pd;
    private FirebaseAuth mAuth;
    private Storage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        storage = new Storage(this);

        mAuth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.email);
        etPass = (EditText) findViewById(R.id.pass);
        cb = (CheckBox) findViewById(R.id.remember);

        if(storage.isRemember()){
            etEmail.setText(storage.getEmail());
            etPass.setText(storage.getPassword());
            cb.setChecked(true);
        }

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


    }


    public void loginBtn(View v)
    {
        email = etEmail.getText().toString().trim();
        pass = etPass.getText().toString();
        isRemember = cb.isChecked();

        if(email.length()==0){
            etEmail.setError("Enter valid email address");
        }else if(pass.length()==0){
            etPass.setError("Password required");
        }else{
            signIn();
        }

    }


    private void signIn(){

        if(storage.isNetConnected()) {

            pd.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    pd.dismiss();

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser().isEmailVerified()) {

                            storage.setLogin(true);
                            //Toast.makeText(getBaseContext(),"UID : "+mAuth.getCurrentUser().getUid(),Toast.LENGTH_LONG).show();
                            storage.setUID("" + mAuth.getCurrentUser().getUid());

                            storage.setEmail(email);
                            storage.setPassword(pass);
                            storage.setRemember(isRemember);
                            storage.setLogin(true);

                            LoginActivity.this.finish();

                            Intent next = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(next);

                        } else {
                            AlertDialog.Builder alb = new AlertDialog.Builder(LoginActivity.this);
                            alb.setTitle("Warning !");
                            alb.setMessage("Please check your email address '" + email + "' and verified it. We send a verification link to your email address.");
                            alb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            alb.show();
                        }

                    } else {

                       // Toast.makeText(getBaseContext(), "Error : \n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        alert("Warning !",task.getException().getMessage());
                        /*
                        final String status = task.getException().toString();
                        if(status.contains("A network error")){
                            alert("Connection failed!","Please check your device net connection.");
                        }else if(status.equals("There is no user record") || status.contains("The password is invalid")){
                            alert("Wrong !","Email address or password is wrong.");
                        }else if(status.equals("The email address is badly formatted.")){
                           alert("Error !","Please enter valid email address.");
                        }else{
                            alert("Error !","Please check your email address and password then try again.");
                        }
                        */

                    }
                }
            });

        }else{
            alert("Connection failed!","Please check your device net connection.");
        }


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


    public void forgotPassword(View v)
    {
        etEmail.setError(null);
        email =  etEmail.getText().toString().trim();
        if(email.length()==0){
            etEmail.setError("Please insert email address first.");
        }else{

            pd.show();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    pd.dismiss();
                    if(task.isSuccessful()){
                        alert("Success !","We send a reset password link to '"+email+"'. Please check your email and change password.");
                    }else{
                        alert("Failed !",task.getException().getMessage());
                    }
                }
            });

        }
    }




    public void gotoSignUpBtn(View v)
    {
        Intent next = new Intent(this,SignupActivity.class);
        startActivity(next);

    }



}
