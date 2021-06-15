package com.improving.comprojecttasktrimmer.Notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.allyants.notifyme.NotifyMe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.improving.comprojecttasktrimmer.R;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditNote extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    Intent data;
    EditText editNoteContent, editNoteTitle;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    ProgressBar progressBarEdit;
    StorageReference storageReference;
    Calendar now = Calendar.getInstance();
    TimePickerDialog tpd;
    DatePickerDialog dpd;
    StorageReference mStorage;
    private StorageTask mUploadTask;
    String currentPhotoPath;
    ImageView attachImage;

    SpeechRecognizer speechRecognizer;
    Intent mSpeechRecognizerIntent;
    private  Uri fileUri;
    public static final int CAM_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int RESULT_LOAD_IMAGE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        progressBarEdit=(ProgressBar)findViewById(R.id.progressBarEdit);
        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        user=fAuth.getCurrentUser();
        data = getIntent();
        mStorage = FirebaseStorage.getInstance().getReference();
        editNoteContent=(EditText)findViewById(R.id.editNoteContent);
        storageReference= FirebaseStorage.getInstance().getReference();
        editNoteTitle=(EditText)findViewById(R.id.editNoteTitle);
        ImageView attachment=(ImageView)findViewById(R.id.attachmentIcon);
        ImageView alarm=(ImageView)findViewById(R.id.alarmIcon);
        ImageView camera = (ImageView)findViewById(R.id.cameraIcon);
        ImageView mic=(ImageView)findViewById(R.id.micIcon);

        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");
        String picUri = data.getStringExtra("picUri");
        attachImage = (ImageView)findViewById(R.id.attachImage);
        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);


        StorageReference profileRef = storageReference.child("Users/"+fAuth.getCurrentUser().getUid()+"."+picUri);



        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(attachImage);

            }
        });

        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                attachmentAlert();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                attachmentAlert();
            }
        });


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener(){

            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if(matches != null)
                    editNoteContent.setText(editNoteContent.getText().toString()+matches.get(0));

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });



        mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()){
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(EditNote.this, "Listening....", Toast.LENGTH_LONG).show();
                        editNoteContent.setText(editNoteContent.getText().toString());
                        speechRecognizer.startListening(mSpeechRecognizerIntent);
                        break;


                }
                return false;
            }
        });

        dpd = DatePickerDialog.newInstance(
                EditNote.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        tpd= TimePickerDialog.newInstance(
                EditNote.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                false
        );

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getFragmentManager(), "Datepickerdialog");

            }
        });

        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle= editNoteTitle.getText().toString();
                String nContent=editNoteContent.getText().toString();

                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(EditNote.this, "Cannot save with Empty Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBarEdit.setVisibility(View.VISIBLE);
                DocumentReference docref= fStore.collection("Users").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map<String,Object> note = new HashMap<>();
                note.put("title",nTitle);
                note.put("content", nContent);

                docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(EditNote.this, "Note Saved", Toast.LENGTH_SHORT).show();
                        onBackPressed();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                        progressBarEdit.setVisibility(View.VISIBLE);
                    }
                });



            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void attachmentAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditNote.this);
        builder.setTitle("Attach");

        //add a list
        String[] animals = {"Image", "Cancel Notification"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        openAlert();
                        break;
                    case 1:
                        NotifyMe.cancel(getApplicationContext(), "test");
                        break;


                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    private void openAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditNote.this);
        builder.setTitle("Attach Image");

        //add a list
        String[] animals = {"Capture New Image", "Choose From Gallery"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                            dispatchTakePictureIntent();
                            //Toast.makeText(getContext(), "Version < M", Toast.LENGTH_LONG).show();
                        }
                        else {
                            askCameraPermissions();
                            //Toast.makeText(getContext(), "Version > M, so ask permissions", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 1:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,  RESULT_LOAD_IMAGE);

                        //Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        //startActivityForResult(openGalleryIntent, 1000);
                        break;

                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE ){
            if(resultCode == Activity.RESULT_OK){
                if(data.getData()!= null) {
                   fileUri = data.getData();
                    Picasso.get().load(fileUri).into(attachImage);

                    uploadImgToFirebase(fileUri);
                   /* StorageReference profileRef = storageReference.child("Users/" + fAuth.getCurrentUser().getUid() + "profile.jpg");
                    profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Picasso.get().load(uri).into(profileImage);
                        }
                    });*/




                }

            }
        }

        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){

                File f = new File(currentPhotoPath);
                Log.d("tag", "Absolute URL of image is: " + Uri.fromFile(f));

                //profileImage.setImageURI(Uri.fromFile(f));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                EditNote.this.sendBroadcast(mediaScanIntent);

                Picasso.get().load(contentUri).into(attachImage);
                uploadImgToFirebase(contentUri);




            }
        }
    }

    private void uploadImgToFirebase(final Uri fileUri) {

        progressBarEdit.setVisibility(View.VISIBLE);
        final String picUri=System.currentTimeMillis()+"."+getFileExtensions(fileUri);
        final StorageReference imageRef = mStorage.child("Users/"+fAuth.getCurrentUser().getUid()+"."+picUri);
        mUploadTask=imageRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String nTitle= editNoteTitle.getText().toString();
                        String nContent=editNoteContent.getText().toString();

                        if(nTitle.isEmpty() || nContent.isEmpty()){
                            Toast.makeText(EditNote.this, "Cannot save with Empty Field", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentReference docref= fStore.collection("Users").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                        Map<String,Object> note = new HashMap<>();
                        note.put("title",nTitle);
                        note.put("content", nContent);
                        note.put("picUri", picUri);

                        docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(EditNote.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditNote.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                                progressBarEdit.setVisibility(View.VISIBLE);
                            }
                        });

                                                //Toast.makeText(AddNote.this, "Upload Success"+ imageRef.toString(), Toast.LENGTH_LONG).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Upload Failed", Toast.LENGTH_SHORT).show();

                    }
                });
    }





    private String getFileExtensions(Uri fileUri){
        ContentResolver contentResolver= getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }


    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(EditNote.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){


            ActivityCompat.requestPermissions(EditNote.this, new String[] {Manifest.permission.CAMERA}, CAM_PERM_CODE );


        }else {

            dispatchTakePictureIntent();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAM_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (EditNote.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(EditNote.this, "Camera not supported", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(EditNote.this, "Camera Permission is Required to Use Camera", Toast.LENGTH_LONG).show();

            }
        }
    }




    private File createImageFile() throws IOException {
        Toast.makeText(EditNote.this, "In onstartActivityResult",Toast.LENGTH_LONG).show();
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */

        );



        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(EditNote.this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                Log.e("lpl","Inside Dispatch "+ ex.getMessage());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(EditNote.this,
                        "com.improving.comprojecttasktrimmer.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }



    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        now.set(Calendar.YEAR,year);
        now.set(Calendar.MONTH, monthOfYear);
        now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        tpd.show(getFragmentManager(), "Timepickerdialog");

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND, second);

        NotifyMe notifyMe = new NotifyMe.Builder(getApplicationContext())
                .title(editNoteTitle.getText().toString())
                .content("Reminder for your notes")
                .color(255,0,0,255)
                .led_color(255,255,255,255)
                .time(now)
                .addAction(new Intent(), "Snooze", false)
                .key("test")
                .addAction(new Intent(), "Dismiss", true, false)
                .addAction(new Intent(), "Done")
                .large_icon(R.mipmap.ic_launcher)
                .build();
    }
}
