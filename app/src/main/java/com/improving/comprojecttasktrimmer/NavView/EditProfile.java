package com.improving.comprojecttasktrimmer.NavView;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.improving.comprojecttasktrimmer.MapsActivity;
import com.improving.comprojecttasktrimmer.Notes.Notes;
import com.improving.comprojecttasktrimmer.R;
import com.improving.comprojecttasktrimmer.home;

public class EditProfile extends AppCompatActivity {

    TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        Integer item_id=intent.getIntExtra("Item_Id",0);

        NavigationView navigationView = findViewById(R.id.navigationView);


        title=(TextView)findViewById(R.id.EditProfileTitle);
        /*if(savedInstanceState ==null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new HomeFragment()).commit();
            //navigationView.setCheckedItem(R.id.);
        }*/

        switch(item_id) {

            case R.id.menuProfile:
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new ProfileFragment()).commit();
                title.setText("Profile");
                break;

            case R.id.menuSettings:

                title.setText("Settings");
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new SettingsFragment()).commit();
                break;

            case R.id.menuNotifications:
                title.setText("Notifications");
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new NotificationsFragment()).commit();
                break;
            case R.id.menuInbox:
                title.setText("Inbox");
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new InboxFragment()).commit();
                break;
            case R.id.menuInfo:
                title.setText("Info");
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new InfoFragment()).commit();
                break;
            case R.id.menuCollection:
                Intent ints = new Intent(EditProfile.this, MapsActivity.class);
                startActivity(ints);
                finish();
                break;
            case R.id.menuSupport:
                title.setText("Support");
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new SupportFragment()).commit();
                break;
            case R.id.menuHealth:
                title.setText("Pedometer");
                getSupportFragmentManager().beginTransaction().replace(R.id.navHostFragment, new HealthFragment()).commit();
                break;



            default:
                Toast.makeText(this,"Switch statement problem",Toast.LENGTH_SHORT).show();

                break;
        }



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


}
