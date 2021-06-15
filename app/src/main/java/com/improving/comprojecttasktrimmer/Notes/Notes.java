package com.improving.comprojecttasktrimmer.Notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.improving.comprojecttasktrimmer.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.improving.comprojecttasktrimmer.model.Adapter;
import com.improving.comprojecttasktrimmer.model.Note;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Notes extends AppCompatActivity  {
    RecyclerView noteLists;
    Adapter adapter;
    ImageView Plus;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    String myText;
    String Title;
    String Content;
    String AES = "AES";

    Intent data;
    StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        user=fAuth.getCurrentUser();
        data = getIntent();
        mStorage = FirebaseStorage.getInstance().getReference();



        Query query = fStore.collection("Users").document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, final int i, @NonNull final Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                Title=note.getTitle();
                Content=note.getContent();
                final int code= getRandomColor();
                noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code, null));
                final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
                final String picUri = noteAdapter.getSnapshots().getSnapshot(i).getString("picUri");




                noteViewHolder.view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), noteDetails.class);
                        i.putExtra("title",note.getTitle());
                        i.putExtra("content",note.getContent());
                        i.putExtra("code", code);
                        i.putExtra("picUri",picUri);
                        i.putExtra("noteId",docId);
                        v.getContext().startActivity(i);
                    }
                });

                ImageView menuIcon = noteViewHolder.view.findViewById(R.id.menuIcon);

                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
                        PopupMenu menu = new PopupMenu(v.getContext(),v);
                        menu.setGravity(Gravity.END);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent i = new Intent(v.getContext(), EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("picUri",picUri);
                                i.putExtra("noteId", docId);
                                startActivity(i);
                                finish();
                                return false;
                            }
                        });
                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {


                                DocumentReference docRef = fStore.collection("Users").document(user.getUid()).collection("myNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(Notes.this, "Failed To Delete Note"+ e, Toast.LENGTH_SHORT).show();

                                    }
                                });
                                return false;
                            }
                        });

                        menu.getMenu().add("Encrypt").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                AlertDialog.Builder mydialog = new AlertDialog.Builder(Notes.this);
                                mydialog.setTitle("Enter the encryption password");

                                final EditText PasswordInput = new EditText(Notes.this);
                                PasswordInput.setInputType(InputType.TYPE_CLASS_TEXT);
                                mydialog.setView(PasswordInput);

                                mydialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        myText=PasswordInput.getText().toString();
                                        try {

                                            DocumentReference docRef1 = fStore.collection("Users").document(user.getUid()).collection("myNotes").document(docId);
                                            docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Notes.this, "Failed To Delete Note"+ e, Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                            String nTitle = encrypt(Title, myText);
                                            String nContent = encrypt(Content, myText);
                                            String npicUri = "Poo";
                                            if (nTitle.isEmpty() || nContent.isEmpty()) {
                                                Toast.makeText(Notes.this, "Cannot save with Empty Field", Toast.LENGTH_SHORT).show();
                                                return;
                                            }


                                            final DocumentReference docref= fStore.collection("Users").document(user.getUid()).collection("myNotes").document();
                                            Map<String,Object> note = new HashMap<>();
                                            note.put("title",nTitle);
                                            note.put("content", nContent);
                                            note.put("picUri", npicUri);



                                            docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(Notes.this, "Message Encrypted", Toast.LENGTH_SHORT).show();
                                                    String docs = docref.getId();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Notes.this, "Error, Try again", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                mydialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                mydialog.show();


                                return false;
                            }
                        });

                        menu.getMenu().add("Decrypt").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                AlertDialog.Builder mydialog = new AlertDialog.Builder(Notes.this);
                                mydialog.setTitle("Enter the decryption password");

                                final EditText PasswordInput = new EditText(Notes.this);
                                PasswordInput.setInputType(InputType.TYPE_CLASS_TEXT);
                                mydialog.setView(PasswordInput);

                                mydialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        myText=PasswordInput.getText().toString();
                                        try {

                                            DocumentReference docRef1 = fStore.collection("Users").document(user.getUid()).collection("myNotes").document(docId);
                                            docRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Notes.this, "Failed To Delete Note"+ e, Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                            String dnTitle = decrypt(Title, myText);
                                            String dnContent = decrypt(Content, myText);
                                            String npicUri = "Poo";
                                            if (dnTitle.isEmpty() || dnContent.isEmpty()) {
                                                Toast.makeText(Notes.this, "Cannot save with Empty Field", Toast.LENGTH_SHORT).show();
                                                return;
                                            }


                                            final DocumentReference docref= fStore.collection("Users").document(user.getUid()).collection("myNotes").document();
                                            Map<String,Object> note = new HashMap<>();
                                            note.put("title",dnTitle);
                                            note.put("content", dnContent);
                                            note.put("picUri", npicUri);



                                            docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(Notes.this, "Message Decrypted", Toast.LENGTH_SHORT).show();
                                                    String docs = docref.getId();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Notes.this, "Error, Try again", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                mydialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                mydialog.show();


                                return false;
                            }
                        });

                        menu.show();

                    }
                });



            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout, parent,false);

                return new NoteViewHolder(view);
            }
        };

        Plus=(ImageView)findViewById(R.id.plus);
        noteLists=(RecyclerView)findViewById(R.id.notelist);


        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ints = new Intent(Notes.this, AddNote.class);
                startActivity(ints);
            }
        });
    }

    public String encrypt(String Data, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;
    }

    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public String decrypt(String Data, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] decodedValue = Base64.decode(Data, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent;
        View view;
        CardView mCardView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            mCardView = itemView.findViewById(R.id.noteCard);
            view=itemView;

        }
    }
    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.lightblue);
        colorCode.add(R.color.lightpink);
        colorCode.add(R.color.pastelyellow);
        colorCode.add(R.color.burgundy);
        colorCode.add(R.color.green);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.violet);
        colorCode.add(R.color.orange);
        colorCode.add(R.color.grey);
        colorCode.add(R.color.cream);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    @Override
    protected void onStart(){
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop(){
        super.onStop();
        noteAdapter.stopListening();
    }
}
