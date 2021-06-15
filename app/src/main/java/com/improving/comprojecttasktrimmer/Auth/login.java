package com.improving.comprojecttasktrimmer.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.improving.comprojecttasktrimmer.R;
import com.improving.comprojecttasktrimmer.home;

public class login extends AppCompatActivity {
    EditText emailID,pwd;
    Button BTlogin;
    ImageView logo;
    TextView register, resetPassLocal;
    FirebaseAuth mfirebaseAuth;
    static final int REQUEST_CODE = 123;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mfirebaseAuth = FirebaseAuth.getInstance();
        emailID=(EditText)findViewById(R.id.et1);
        pwd=(EditText)findViewById(R.id.et2);
        BTlogin=(Button)findViewById(R.id.bt1);
        register=(TextView)findViewById(R.id.TVlogin);
        resetPassLocal=(TextView)findViewById(R.id.resetPassLocal);


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mfirebaseAuth.getCurrentUser();
                if( mFirebaseUser !=null ){
                    FirebaseUser firebaseUser = mfirebaseAuth.getCurrentUser();
                    Boolean emailflag = firebaseUser.isEmailVerified();

                    if(emailflag) {



                        Toast.makeText(login.this, "Logging in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(login.this, home.class));
                        finish();


                    }
                    else{
                        Toast.makeText(login.this,"Verify your email to continue",Toast.LENGTH_SHORT).show();
                        //mfirebaseAuth.signOut();
                    }

                }
                else {
                    //Toast.makeText(login.this, "Please register before signin", Toast.LENGTH_SHORT).show();
                }
            }
        };




        BTlogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = emailID.getText().toString();
                String password = pwd.getText().toString();
                if(email.isEmpty()){
                    emailID.setError("Please enter email id");
                    emailID.requestFocus();
                }
                else if(password.isEmpty()){
                    pwd.setError("Please enter your password");
                    pwd.requestFocus();
                }
                else if(email.isEmpty() && password.isEmpty()){
                    Toast.makeText(login.this, "Fields are Empty!", Toast.LENGTH_SHORT).show();
                }
                else if(!(email.isEmpty() && password.isEmpty())){

                    mfirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!task.isSuccessful()) {

                                if (mfirebaseAuth.getCurrentUser() == null){
                                    Toast.makeText(login.this, "The Login ID or the Password you have entered is incorrect. ", Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(login.this, "Login unsuccessful. Please try again", Toast.LENGTH_SHORT).show();

                                }

                            }

                            else{
                                    checkEmailVerification();

                            }
                        }
                    });
                }
                else{
                    Toast.makeText(login.this, "Some Error Occurred!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        resetPassLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetPassword = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter your Email to receive Reset Link.");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      String password= resetPassword.getText().toString().trim();
                      mfirebaseAuth.sendPasswordResetEmail(password).addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {
                              Toast.makeText(login.this, "Reset Link is sent to your Email !",Toast.LENGTH_SHORT).show();

                          }
                      }).addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              Toast.makeText(login.this, "Error! Reset Link not sent."+ e.getMessage(),Toast.LENGTH_SHORT).show();

                          }
                      });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordResetDialog.create().show();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intents = new Intent(login.this, signUp.class);
                startActivity(intents);
            }
        });

    }



    @Override
    protected void onStart(){
        super.onStart();
        mfirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void checkEmailVerification(){
        FirebaseUser firebaseUser = mfirebaseAuth.getCurrentUser();
        Boolean emailflag = firebaseUser.isEmailVerified();

        if(emailflag){

            Toast.makeText(login.this,"Logging in",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(login.this,home.class));
            finish();

        }
        else{

            Toast.makeText(login.this,"Verify your email to continue",Toast.LENGTH_SHORT).show();
            mfirebaseAuth.signOut();
        }
    }
}

