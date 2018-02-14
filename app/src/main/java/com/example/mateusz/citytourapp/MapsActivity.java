package com.example.mateusz.citytourapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.example.mateusz.citytourapp.scheduler.JobSchedulerService;
import com.example.mateusz.citytourapp.tweeter.DataStoreClass;
import com.example.mateusz.citytourapp.tweeter.TwitterHelper;
import com.example.mateusz.citytourapp.ui.CustomVolleyRequestQueue;
import com.example.mateusz.citytourapp.ui.Pager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

public class MapsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private JobScheduler m_aScheduler = null;

    /**
     *  Wybrane miejsce przez użytkownika - główny kontekst aplikacji.
     */
    private Feature selectedFeature = null;

    FirebaseUser user = null;

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
        scheduleBTSchecker();
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
        ((Pager)viewPager.getAdapter()).refreshDataOnTabDetal();
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

    /*
    Schedulowanie JOBa w tle
     */
    public void scheduleBTSchecker(){
        if(this.m_aScheduler == null)
        {
            //Tworzymy Joba, który będzie wykonywany w tle przez serwis
            ComponentName aServiceName = new ComponentName(this, JobSchedulerService.class);
            //TODO Dac modyfikowalny z opcji periodic time
            JobInfo aJobInfo = new JobInfo.Builder(1, aServiceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    //.setPeriodic(10000)
                    .setPersisted(true)
                    .build();

            this.m_aScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int nResult = m_aScheduler.schedule(aJobInfo);
            if (nResult == JobScheduler.RESULT_SUCCESS) {
                Toast.makeText(this, "Job został schedulowany...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            this.m_aScheduler.cancelAll();
            this.m_aScheduler = null;
            scheduleBTSchecker();
            Toast.makeText(this, "Job został zatrzymany...", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchTab(int position) {

        tabLayout.getTabAt(position).select();
    }

    public void setSelectedFeature(Feature feature) {
        selectedFeature = feature;
    }

    public  Feature getSelectedFeature() {
        return selectedFeature;
    }
}
