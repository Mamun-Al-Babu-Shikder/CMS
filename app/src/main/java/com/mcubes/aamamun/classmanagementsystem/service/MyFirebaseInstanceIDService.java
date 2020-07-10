package com.mcubes.aamamun.classmanagementsystem.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by A.A.MAMUN on 3/18/2019.
 */


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private DatabaseReference reference;

    @Override
    public void onTokenRefresh() {

        try {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            sendRegistrationToServer(refreshedToken);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void sendRegistrationToServer(String device_token)
    {
        try {
            String uid =FirebaseAuth.getInstance().getCurrentUser().getUid();
            reference = FirebaseDatabase.getInstance().getReference("user").child(uid).child("device_token");
            reference.setValue(device_token);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
