package com.improving.comprojecttasktrimmer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.improving.comprojecttasktrimmer.NavView.EditProfile;
import com.improving.comprojecttasktrimmer.NavView.ProfileFragment;
import com.improving.comprojecttasktrimmer.Notes.Notes;
import com.improving.comprojecttasktrimmer.Notes.noteDetails;
import com.squareup.picasso.Picasso;

public class home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener {
    LinearLayout notes;
    DrawerLayout drawerLayout;
    FirebaseUser user;
    ImageView ProfileImage;
    StorageReference storageReference;
    FirebaseAuth fAuth;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        storageReference= FirebaseStorage.getInstance().getReference();
        drawerLayout = findViewById(R.id.drawerLayout);

        notes=(LinearLayout) findViewById(R.id.notesview) ;
        ImageView imageMenu = findViewById(R.id.imageMenu);
        user = FirebaseAuth.getInstance().getCurrentUser();

        fAuth=FirebaseAuth.getInstance();
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

            navigationView.setItemIconTintList(null);

        //NavController navController= Navigation.findNavController(EditProfile, R.id.navHostFragment);
        //NavigationUI.setupWithNavController(navigationView, navController);




        View headerView = navigationView.getHeaderView(0);
        final ImageView ProfileImage=headerView.findViewById(R.id.imageProfile);
        TextView username = headerView.findViewById(R.id.name);
        TextView userEmail = headerView.findViewById(R.id.email);
        userEmail.setText(user.getEmail());
        username.setText(user.getDisplayName());

            final StorageReference fileRef=storageReference.child("Users/"+fAuth.getCurrentUser().getUid()+"profile.jpg");


            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(ProfileImage);




                }
            });








        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ints = new Intent(home.this, Notes.class);
                startActivity(ints);
            }
        });


    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        Integer item_id=item.getItemId();

        switch(item_id) {

            case R.id.menuLogout:
                openAlert();
                break;

            case R.id.menuLearning:
            case R.id.menuFitness:
            case R.id.menuWork:
            case R.id.menuPersonal:
            case R.id.menuOccassions:
                Toast.makeText(this,"Coming Soon",Toast.LENGTH_SHORT).show();
                break;


            default:
                Intent intent = new Intent(home.this, EditProfile.class);
                intent.putExtra("Item_Id",item_id);

                startActivity(intent);
                break;
        }
        return false;
    }

    private void openAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), Splash.class));
                        finish();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {



                    }
                });
        warning.show();

    }


    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
        popup.inflate(R.menu.option_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.item1:
                Toast.makeText(this,"Item 1 is clicked",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item2:
                Toast.makeText(this,"Item 2 is clicked",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item3:
                Toast.makeText(this,"Item 3 is clicked",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.subitem1:
                Toast.makeText(this,"Subitem1 is clicked",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.subitem2:
                Toast.makeText(this,"Subitem2 is clicked",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}
