package com.improving.comprojecttasktrimmer.NavView;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.improving.comprojecttasktrimmer.Notes.EditNote;
import com.improving.comprojecttasktrimmer.Notes.Notes;
import com.improving.comprojecttasktrimmer.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final int CAM_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    String currentPhotoPath;
    EditText Bio, Phone, Gender;
    TextView changePwd, changeProPic;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    String userId;
    FirebaseUser user;
    ImageView profileImage;
    StorageReference storageReference;




    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myFragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView Name=(TextView)myFragmentView.findViewById(R.id.editProfileName);
        Bio=(EditText)myFragmentView.findViewById(R.id.editProfileBio);
        Phone=(EditText)myFragmentView.findViewById(R.id.editProfilePhoneNo);
        Gender=(EditText)myFragmentView.findViewById(R.id.editProfileGender);
        changePwd=(TextView)myFragmentView.findViewById(R.id.EditProfileChangePwd);
        changeProPic=(TextView)myFragmentView.findViewById(R.id.changeProPic);
        profileImage = (ImageView) myFragmentView.findViewById(R.id.ProfilePic);
        firebaseAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        TextView userEmail = (TextView)myFragmentView.findViewById(R.id.editProfileEmail);
        userEmail.setText(user.getEmail());
        Name.setText(user.getDisplayName());

        StorageReference profileRef= storageReference.child("Users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openAlert();
            }
        }) ;

        changeProPic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openAlert();
            }
        }) ;




        return myFragmentView;



    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                //profileImage.setImageURI(imageUri);
                
                uploadImageToFirebase(imageUri);

                StorageReference profileRef= storageReference.child("Users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });

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
                getContext().sendBroadcast(mediaScanIntent);

                uploadImageToFirebase(contentUri);
                StorageReference profileRef= storageReference.child("Users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });

            }
        }
    }

    /*private String getFileExt(Uri contentUri){
        ContentResolver c = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));

    }*/

    private void uploadImageToFirebase(Uri imageUri) {
        final FirebaseUser user=firebaseAuth.getCurrentUser();
        final StorageReference fileRef=storageReference.child("Users/"+user.getUid()+"profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);

                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        user.updateProfile(request);

                        Toast.makeText(getContext(), "Image Uploaded",Toast.LENGTH_LONG).show();



                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "fail"+e,Toast.LENGTH_LONG).show();

            }
        });
    }



    private void openAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Profile Photo");

        //add a list
        String[] animals = {"Capture New Image", "Choose From Gallery", "Remove Profile Photo"};
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
                        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(openGalleryIntent, 1000);
                        break;
                    case 2:

                        StorageReference storageRef= storageReference.child("Users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");
                        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getContext(), "Failed To Delete Profile Picture"+ e, Toast.LENGTH_SHORT).show();

                            }
                        });

                        break;

                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){


            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, CAM_PERM_CODE );


        }else {

            dispatchTakePictureIntent();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAM_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getContext(), "Camera not supported", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Camera Permission is Required to Use Camera", Toast.LENGTH_LONG).show();

            }
        }
    }




    private File createImageFile() throws IOException {
        Toast.makeText(getContext(), "In onstartActivityResult",Toast.LENGTH_LONG).show();
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
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                Log.e("lpl","Inside Dispatch "+ ex.getMessage());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.improving.comprojecttasktrimmer.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}
