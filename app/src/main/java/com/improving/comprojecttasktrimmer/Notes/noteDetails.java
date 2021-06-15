package com.improving.comprojecttasktrimmer.Notes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.allyants.notifyme.NotifyMe;
import com.firebase.ui.firestore.BuildConfig;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.improving.comprojecttasktrimmer.R;

import com.improving.comprojecttasktrimmer.model.Note;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class noteDetails extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    Intent data;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    private RecyclerView mUploadList;
    TextView NoteDetailsContent;
    ConstraintLayout layoutCapture;
    String picUri;
    Uri imageUri;
    StorageReference mStorage;
    ImageView attachImage;
    Calendar now = Calendar.getInstance();
    TimePickerDialog tpd;
    DatePickerDialog dpd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        user=fAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();
        layoutCapture = (ConstraintLayout)findViewById(R.id.noteDetailsContentLayout);
        NoteDetailsContent = (TextView)findViewById(R.id.noteDetailsContent);
        attachImage = (ImageView)findViewById(R.id.attachImage);

       /* mUploadList=(RecyclerView)findViewById(R.id.noteDetailsAttachment);
        uploadListAdapter = new UploadListAdapter(fileNameList, fileDoneList);
        mUploadList.setLayoutManager(new LinearLayoutManager(this));
        mUploadList.setHasFixedSize(true);
        mUploadList.setAdapter(uploadListAdapter);*/
        data = getIntent();
        final String docId = data.getStringExtra("noteId");



        final ImageView attachImage = (ImageView)findViewById(R.id.attachImage);
        TextView content = findViewById(R.id.noteDetailsContent);
        TextView title = findViewById(R.id.noteDetailsTitle);
        String picUri = data.getStringExtra("picUri");
        content.setMovementMethod(new ScrollingMovementMethod());

        content.setText(data.getStringExtra("content"));
        title.setText(data.getStringExtra("title"));

        StorageReference profileRef = mStorage.child("Users/"+fAuth.getCurrentUser().getUid()+"."+picUri);



        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(attachImage);

            }
        });

        content.setBackgroundColor(getResources().getColor(data.getIntExtra("code",0),null));


        ImageView share=(ImageView)findViewById(R.id.shareIcon);
        ImageView alarm=(ImageView)findViewById(R.id.alarmIcon);

        ImageView opMenu=(ImageView)findViewById(R.id.menuIcon);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                noteShare();
            }
        });

        dpd = DatePickerDialog.newInstance(
                noteDetails.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        tpd= TimePickerDialog.newInstance(
                noteDetails.this,
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




        opMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //final String docId = noteAdapter.getSnapshots().getSnapshot(m).getId();

                PopupMenu menu = new PopupMenu(v.getContext(),v);
                menu.setGravity(Gravity.END);
                menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent i = new Intent(v.getContext(), EditNote.class);
                        i.putExtra("title",data.getStringExtra("title"));
                        i.putExtra("content",data.getStringExtra("content"));
                        i.putExtra("picUri", data.getStringExtra("picUri"));
                        i.putExtra("noteId", data.getStringExtra("noteId"));

                        startActivity(i);
                        finish();
                        return false;
                    }
                });
                menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {



                        DocumentReference docRef= fStore.collection("Users").document(user.getUid()).collection("myNotes").document(docId);
                        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                onBackPressed();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(noteDetails.this, "Failed To Delete Note"+ e, Toast.LENGTH_SHORT).show();

                            }
                        });
                        return false;
                    }
                });

                menu.getMenu().add("Cancel Notification").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {



                        NotifyMe.cancel(getApplicationContext(), "test");
                        return  false;
                    }
                });

                menu.show();


            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), EditNote.class);
                i.putExtra("title",data.getStringExtra("title"));
                i.putExtra("content",data.getStringExtra("content"));
                i.putExtra("picUri",data.getStringExtra("picUri"));
                i.putExtra("noteId", data.getStringExtra("noteId"));
                startActivity(i);
                finish();

            }
        });
    }

    private void noteShare(){

        Bitmap bitmap= getBitmapFromView(layoutCapture);
        try {
            File file = new File(this.getExternalCacheDir(),"aqua.jpg");
            FileOutputStream fout = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
            file.setReadable(true,false);
            Uri photoUri = Uri.fromFile(file);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){

                photoUri = FileProvider.getUriForFile(noteDetails.this,
                        "com.improving.comprojecttasktrimmer.provider",
                        file);
                //photoUri = FileProvider.getUriForFile(noteDetails.this, BuildConfig.APPLICATION_ID + "com.improving.comprojecttasktrimmer.provider", file);
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, photoUri);
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent,"share by"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @SuppressLint("ResourceAsColor")
    private Bitmap getBitmapFromView(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if(bgDrawable!=null){
            bgDrawable.draw(canvas);
        }else{
            canvas.drawColor(android.R.color.white);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
                .title(data.getStringExtra("title"))
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
