package com.mcubes.aamamun.classmanagementsystem;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mcubes.aamamun.classmanagementsystem.model.User;

public class SignupActivity extends AppCompatActivity {

    private EditText etName,etEmail,etPhone,etPass,etConPass;
    private String name, email, phone, pass, conPass, sex[]={"Male", "Female", "Others"};
    private RadioGroup rg;
    private int selectedSexId, sSex = 0;
    private static ProgressDialog pd;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Storage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        storage = new Storage(this);
        mAuth = FirebaseAuth.getInstance();


        etName = (EditText) findViewById(R.id.name);
        etEmail = (EditText) findViewById(R.id.email);
        etPhone = (EditText) findViewById(R.id.phone);
        etPass = (EditText) findViewById(R.id.pass);
        etConPass = (EditText) findViewById(R.id.conPass);
        rg = (RadioGroup) findViewById(R.id.rg);


        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


    }



    public void signUpBtn(View v) {

        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        phone = etPhone.getText().toString().trim();
        pass = etPass.getText().toString();
        conPass = etConPass.getText().toString();

        selectedSexId = rg.getCheckedRadioButtonId();
        switch (selectedSexId) {
            case R.id.male:
                sSex = 0;
                break;
            case R.id.female:
                sSex = 1;
                break;
            case R.id.others:
                sSex = 2;
                break;
        }

        if(name.length()==0){
            etName.setError("Please enter your name");
        }else if(email.length()==0){
            etEmail.setError("Email address required");
        }else if(phone.length()<10 || phone.length()>15){
            etPhone.setError("Valid phone number required");
        }else if(pass.length()<6){
            etPass.setError("Password length too low");
        }else if(conPass.length()<6 || !conPass.equals(pass)){
            etConPass.setError("Confirm password must be same as user password");
        }else{
            signup();
        }

    }


    public void signup(){

        if(storage.isNetConnected()) {
            pd.show();

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    pd.dismiss();

                    if (task.isSuccessful()) {


                        User user = new User();
                        user.setName(name);
                        user.setEmail(email);
                        user.setPhone(phone);
                        user.setSex(sSex);
                        user.setEducation("Unknown");
                        user.setAddress("Unknown");
                        user.setImgUrl("Unknown");
                        user.setWork("Unknown");
                        user.setDob("Unknown");


                        userRef = FirebaseDatabase.getInstance().getReference().child("user").child(mAuth.getCurrentUser().getUid()).child("profile");
                        userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    mAuth.getCurrentUser().sendEmailVerification();
                                    AlertDialog.Builder alb = new AlertDialog.Builder(SignupActivity.this);
                                    alb.setTitle("Success");
                                    alb.setMessage("You successfully created an account to 'CMS', Please check your email '" + email + "' address and verify you account.");
                                    alb.setCancelable(false);
                                    alb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                            SignupActivity.this.finish();
                                        }
                                    });
                                    alb.setCancelable(false);
                                    alb.show();


                                } else {
                                    Toast.makeText(getBaseContext(), "Error : \n" + task.getException(), Toast.LENGTH_LONG).show();


                                }

                            }
                        });


                    } else {

                        //Toast.makeText(getBaseContext(), "Error : \n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        alert("Warning !",task.getException().getMessage());
                        /*
                        final String status = task.getException().toString();
                        if(status.contains("A network error")){
                            alert("Connection failed!","Please check your device net connection.");
                        }else if (status.equals("The email address is badly formatted.")) {
                            alert("Error !", "Please enter valid email address.");
                        }
                        */
                    }

                }
            });

        }else {
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



}
