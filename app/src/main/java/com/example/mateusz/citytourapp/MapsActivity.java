package com.example.mateusz.citytourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.mateusz.citytourapp.Model.poznanModels.Feature;
import com.example.mateusz.citytourapp.tweeter.DataStoreClass;
import com.example.mateusz.citytourapp.tweeter.TwitterHelper;
import com.example.mateusz.citytourapp.ui.CustomVolleyRequestQueue;
import com.example.mateusz.citytourapp.ui.Pager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MapsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    static final int REQUEST_TAKE_PHOTO = 1888;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    /**
     * Wybrane miejsce przez użytkownika - główny kontekst aplikacji.
     */
    private Feature selectedFeature = null;

    FirebaseUser user = null;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("Mapa"));
        tabLayout.addTab(tabLayout.newTab().setText("Detale"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        //Creating our pager adapter
        Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);


        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        user = FirebaseAuth.getInstance().getCurrentUser();
        setNavigationHeaderUserData(user);
        setTwitterHelperSession();

        // TODO tutaj powinniśmy uruchomić joba który będzie sprawdzał pozycję z BTS'a.
    }

    private void setNavigationHeaderUserData(FirebaseUser user) {
        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        NetworkImageView imageView = (NetworkImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);

        String url = user.getPhotoUrl().toString();
        for (UserInfo profile : user.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if (providerId.compareTo("twitter.com") == 0) {
                // UID specific to the provider
                String uid = profile.getUid();

                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                String email = profile.getEmail();
                Uri photoUrl = profile.getPhotoUrl();

                url = photoUrl.toString();
            }
        }
        ;

        textView.setText(user.getDisplayName());
        setupProfileImageInNavigationHeader(url, imageView);
    }

    @NonNull
    private void setTwitterHelperSession() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        TwitterHelper m_aTwitterObject = DataStoreClass.getGlobalTwitterHelper();
        m_aTwitterObject.setM_aSession(twitterSession);
    }

    private void setupProfileImageInNavigationHeader(String url, NetworkImageView mNetworkImageView) {
        ImageLoader mImageLoader = CustomVolleyRequestQueue.getInstance(getApplicationContext())
                .getImageLoader();

        mImageLoader.get(url, ImageLoader.getImageListener(mNetworkImageView,
                R.drawable.person, android.R.drawable //Deafault image
                        .ic_dialog_alert));//Error image
        mNetworkImageView.setImageUrl(url, mImageLoader);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        viewPager.setCurrentItem(tab.getPosition());
        ((Pager) viewPager.getAdapter()).refreshDataOnTabDetal();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        String menuItemName = item.toString();

        switch (menuItemName) {
            case "Zdjęcia":

                break;
            case "Ustawienia":
                // TODO ekran nowe activity, gdzie można ustawić częstość sprawdzania pozycji z BTS'a oraz zapis/odczyt tego ustawienia z chmury
                break;
            case "Wyloguj":
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                                finish();
                            }
                        });
                break;
            default:
                break;
        }

        return true;
    }

    public void switchTab(int position) {

        tabLayout.getTabAt(position).select();
    }

    public void setSelectedFeature(Feature feature) {
        selectedFeature = feature;
    }

    public Feature getSelectedFeature() {
        return selectedFeature;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            //mImageView.setImageBitmap(imageBitmap);

            File destination = new File(Environment.getExternalStorageDirectory() + "/Pictures/",
                    System.currentTimeMillis() + ".jpg");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            FileOutputStream fo;
            try {
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();

                Toast.makeText(getApplicationContext(), "Plik zapisano", Toast.LENGTH_LONG).show();

                saveInCloud(destination);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Błędy podczas zapisu zdjęcia", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Błędy podczas zapisu zdjęcia", Toast.LENGTH_LONG).show();
            }

            //((Pager)viewPager.getAdapter()).galleryAddPic();
        }
    }

    private void saveInCloud(File file) {
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Uri fileName = Uri.fromFile(file);
        StorageReference riversRef = mStorageRef.child(setPhotoUriInCloud(file.getName()));

        riversRef.putFile(fileName)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Toast.makeText(getApplicationContext(), "Udało zapisać się zdjęcie w chmurze", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Nie udało się zapisać zdjęcia w chmurze", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @NonNull
    private String setPhotoUriInCloud(String fileName) {
        return user.getUid() + "/images/" + selectedFeature.getId() + "/" + fileName;
    }
}
