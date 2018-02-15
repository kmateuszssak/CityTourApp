package com.example.mateusz.citytourapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.mateusz.citytourapp.ImagesSet.UploadInfo;
import com.example.mateusz.citytourapp.Model.ModelDanych;
import com.example.mateusz.citytourapp.Model.poznanModels.Feature;
import com.example.mateusz.citytourapp.scheduler.JobSchedulerService;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    static final int REQUEST_TAKE_PHOTO = 1888;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private JobScheduler m_aScheduler = null;
    private boolean m_aFlagGetSettings = false;
    private String TAG = "MAPS_ACTIVITY";

    private ModelDanych dane = null;
    private FirebaseAuth aAuth;
    private FirebaseAuth.AuthStateListener aAuthListener;

    /**
     * Wybrane miejsce przez użytkownika - główny kontekst aplikacji.
     */
    private Feature selectedFeature = null;

    FirebaseUser user = null;
    private StorageReference mStorageRef;
    //private DatabaseReference mDataReference;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        aAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //sprawdzanie czy twitterowy login
        for(String provider : user.getProviders())
        {
            if(!provider.equals("twitter.com"))
            {
                new AlertDialog.Builder(this)
                        .setTitle("Wymagane jest połączenie z kontem Twitter")
                        .setMessage("Brak konta Twiiter uniemożliwi pracę z aplikacją.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("MainActivity", "Uzytkownik wcisnął okej.");
                                //TODO automatyczne wylokowywanie
                            }
                        })
                        .show();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        aAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                if(fbUser != null)
                {
                    Log.i("SettingsActivity", "User zalogowany do firebase");
                }
            }
        };

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //tylko pierwsze wywolanie activity to zmieniamy stale
                if(!m_aFlagGetSettings)
                {
                    dane = pobierzDane(dataSnapshot);
                    Constans.PROMIEN = dane.getPromien();
                    Constans.CZAS_ODSWIEZANIA = dane.getNotyfikacja();
                    Constans.czyZabytkoweKoscioly = dane.isWyswietlaj_zabytkowe_koscioly();
                    Constans.czyZabytki = dane.isWyswietlaj_zabytki();

                    //dopiero po pobraniu ustawien ustawiamy scheduler
                    scheduleBTSchecker();

                    m_aFlagGetSettings = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //error with database
                Toast.makeText(getApplicationContext(), "Error with database! Please try again.", Toast.LENGTH_LONG).show();
            }
        });

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

        //mDataReference = FirebaseDatabase.getInstance().getReference("images");

        setNavigationHeaderUserData(user);
        setTwitterHelperSession();
    }

    private void setNavigationHeaderUserData(FirebaseUser user) {
        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        TextView textViewEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email);
        NetworkImageView imageView = (NetworkImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);

        if (user != null) {
            Uri uri = user.getPhotoUrl();
            String url = "";
            if (uri != null) {
                url = uri.toString();
            }

            String email = null;
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();

                if (providerId.compareTo("twitter.com") == 0) {
                    // UID specific to the provider
                    String uid = profile.getUid();

                    // Name, email address, and profile photo Url
                    String name = profile.getDisplayName();
                    email = profile.getEmail();
                    Uri photoUrl = profile.getPhotoUrl();

                    url = photoUrl.toString();
                }
            }

            textView.setText(user.getDisplayName());
            if (email == null) {
                textViewEmail.setText("Brak adresu");
            } else {
                textViewEmail.setText(email);
            }

            setupProfileImageInNavigationHeader(url, imageView);
        }
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
            case "EkranGlowny":
                //TODO żeby nas zabierało do spowrtem do mapy -> w drawer.xml dodać item
                break;
            case "Zdjęcia":
                startActivity(new Intent(MapsActivity.this, GalleryActivity.class));
                break;
            case "Ustawienia":
                try
                {
                    Intent intent = new Intent(this, SettingsActivity.class);// New activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
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

    /*
    Schedulowanie JOBa w tle
     */
    public void scheduleBTSchecker(){
        if(this.m_aScheduler == null)
        {
            //Tworzymy Joba, który będzie wykonywany w tle przez serwis
            ComponentName aServiceName = new ComponentName(this, JobSchedulerService.class);
            JobInfo aJobInfo = new JobInfo.Builder(1, aServiceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(Constans.CZAS_ODSWIEZANIA * 1000)
                    .setPersisted(true)
                    .build();

            this.m_aScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int nResult = m_aScheduler.schedule(aJobInfo);
            if (nResult == JobScheduler.RESULT_SUCCESS) {
                Log.i(TAG, "Job został schedulowany...");
                //Toast.makeText(this, "Job został schedulowany... " + (Constans.CZAS_ODSWIEZANIA * 1000), Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            this.m_aScheduler.cancelAll();
            this.m_aScheduler = null;
            scheduleBTSchecker();
            Log.i(TAG, "Job został zatrzymany...");
            //Toast.makeText(this, "Job został zatrzymany...", Toast.LENGTH_SHORT).show();
        }
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

                saveInCloud(destination, selectedFeature.getProperties().getNazwa());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Błędy podczas zapisu zdjęcia", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Błędy podczas zapisu zdjęcia", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveInCloud(File file, String featureName) {
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Uri fileName = Uri.fromFile(file);
        final StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("featureId", selectedFeature.getId().toString())
                .setCustomMetadata("featureName", featureName)
                .build();
        final String uriInCloud = setPhotoUriInCloud(file.getName());
        final StorageReference photoRef = mStorageRef.child(uriInCloud);

        photoRef.putFile(fileName)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String name = taskSnapshot.getMetadata().getName();
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                        // Aktualnie nie wykorzystywane
                        UploadInfo info = new UploadInfo(name, downloadUrl);

                        database.child(user.getUid()).push().setValue(uriInCloud);

                        photoRef.updateMetadata(metadata);

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

    @NonNull
    private String getPhotoDatabaseInformation() {
        String url = user.getUid() + "/images/" + selectedFeature.getId();
        return url;
    }

    private void writeNewImageInfoToDB(String name, String url, DatabaseReference photoDataReference) {
        UploadInfo info = new UploadInfo(name, url);

        String key = photoDataReference.push().getKey();
        photoDataReference.child(key).setValue(info);
    }

    //Pobranie dnaych z firebase
    private ModelDanych pobierzDane(DataSnapshot dataSnapshot) {
        ModelDanych currentData = new ModelDanych();
        currentData.setPromien(dataSnapshot.getValue(ModelDanych.class).getPromien());
        currentData.setNotyfikacja(dataSnapshot.getValue(ModelDanych.class).getNotyfikacja());
        currentData.setWyswietlaj_zabytki(dataSnapshot.getValue(ModelDanych.class).isWyswietlaj_zabytki());
        currentData.setWyswietlaj_zabytkowe_koscioly(dataSnapshot.getValue(ModelDanych.class).isWyswietlaj_zabytkowe_koscioly());

        return currentData;
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.aAuth.addAuthStateListener(aAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(aAuthListener != null)
        {
            aAuth.removeAuthStateListener(aAuthListener);
        }
    }
}
