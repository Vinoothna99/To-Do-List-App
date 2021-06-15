package com.improving.comprojecttasktrimmer.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.improving.comprojecttasktrimmer.R;
import com.improving.comprojecttasktrimmer.home;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class signUp extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText name,emailID,pwd,repwd ;
    String userID;
    Button BTsignin;
    TextView TVlogin;
    ImageView logo;
    FirebaseAuth pfirebaseAuth;
    FirebaseFirestore fstore;
    FirebaseUser fuser;
    ProgressBar preogressBarSignup;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        pfirebaseAuth = FirebaseAuth.getInstance();
        fuser=pfirebaseAuth.getCurrentUser();
        fstore=FirebaseFirestore.getInstance();
        name=(EditText)findViewById(R.id.et3);
        emailID=(EditText)findViewById(R.id.et1);
        pwd=(EditText)findViewById(R.id.et2);
        repwd=(EditText)findViewById(R.id.et4);
        BTsignin=(Button)findViewById(R.id.bt1);
        TVlogin=(TextView)findViewById(R.id.TVlogin);
        preogressBarSignup=(ProgressBar)findViewById(R.id.progressBarSignup);



        TVlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ints = new Intent(signUp.this, login.class);
                startActivity(ints);
            }
        });



        BTsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preogressBarSignup.setVisibility(View.VISIBLE);

                final String email = emailID.getText().toString();
                final String userName = name.getText().toString();
                String password = pwd.getText().toString();
                String repassword = repwd.getText().toString();


                if(email.isEmpty()){
                    emailID.setError("Please enter email id");
                    emailID.requestFocus();
                }
                else if(password.isEmpty()){
                    pwd.setError("Please enter your password");
                    pwd.requestFocus();
                }
                else if(email.isEmpty() && password.isEmpty()){
                    Toast.makeText(signUp.this, "Fields are Empty!",Toast.LENGTH_SHORT).show();
                }
                else if(password.length()<6){
                    Toast.makeText(signUp.this, "Password should not be less than 6 characters",Toast.LENGTH_SHORT).show();
                }
                else if(!(password.equals(repassword))){
                    pwd.setError("Password does not match");
                    pwd.requestFocus();
                }

                else if(!(email.isEmpty() && password.isEmpty())){

                    pfirebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(signUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {

                                if (pfirebaseAuth.getCurrentUser() != null){
                                    Toast.makeText(signUp.this, "This email account is already registered. Please login to continue", Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(signUp.this, "Registration unsuccessful. Please try again", Toast.LENGTH_SHORT).show();

                                }

                            }
                            else{

                                userID= pfirebaseAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fstore.collection("Users").document(userID);
                                Map<String,Object> user = new HashMap<>();
                                user.put("Name",userName);
                                user.put("Email",email);

                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Tag","Successfully registered");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Tag","onFailure: "+e.toString());

                                    }
                                });


                                FirebaseUser usr = pfirebaseAuth.getCurrentUser();
                                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName)

                                        .build();
                                usr.updateProfile(request);

                                sendEmailVerification();




                            }
                        }
                    });
                }
                else{
                    Toast.makeText(signUp.this, "Some Error Occurred!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendEmailVerification(){
        final FirebaseUser pUser =pfirebaseAuth.getCurrentUser();
        if(pUser!=null){
            pUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        pfirebaseAuth.signOut();
                        Toast.makeText(signUp.this, "Verification email sent!",Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(),login.class));


                    }else {

                        Toast.makeText(signUp.this, "Verification email hasn't been sent. Try again!",Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }



}

