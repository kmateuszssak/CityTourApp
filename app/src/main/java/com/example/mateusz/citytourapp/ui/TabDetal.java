package com.example.mateusz.citytourapp.ui;

/**
 * Created by Mateusz on 11.02.2018.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.mateusz.citytourapp.ComposeTweetActivity;
import com.example.mateusz.citytourapp.GalleryActivity;
import com.example.mateusz.citytourapp.MapsActivity;
import com.example.mateusz.citytourapp.Model.poznanModels.Feature;
import com.example.mateusz.citytourapp.R;
import com.example.mateusz.citytourapp.tweeter.DataStoreClass;
import com.example.mateusz.citytourapp.tweeter.TwitterHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mateusz on 11.02.2018.
 */

public class TabDetal extends Fragment {

    static final int REQUEST_TAKE_PHOTO = 1888;

    MapsActivity activity;

    private TextView title;
    private TextView description;
    private NetworkImageView mNetworkImageView;
    private ImageLoader mImageLoader;

    private FloatingActionButton takePhotoButton;
    private FloatingActionButton composeTweetButton;

    String mCurrentPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getActivity() instanceof MapsActivity) {
            activity = (MapsActivity) getActivity();
        }

        View view = inflater.inflate(R.layout.tab_detal, container, false);

        title = view.findViewById(R.id.title_selected_item);
        description = view.findViewById(R.id.description_long_selected_item);
        mNetworkImageView = (NetworkImageView) view.findViewById(R.id.networkImage_selected_item);

        takePhotoButton = (FloatingActionButton) view.findViewById(R.id.takePhotoButton);
        composeTweetButton = (FloatingActionButton) view.findViewById(R.id.composeTweetButton);

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTakePhotoButtonClick(v);
            }
        });

        composeTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onComposeTweetButtonClick(v);
            }
        });

        setSelectedFeatureOnPage();

        return view;
    }

    private void onTakePhotoButtonClick(View v) {
        if (activity.getSelectedFeature() != null) {
            boolean result = checkPermission(activity);

            if (result) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Brak uprawnień", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Wybierz najpierw konkretny obiekt", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //TODO (dla Mateusza) dodaj nowy ekran na którym możesz dodawać zdjęcia i wrzucać na tweetera
    private void onComposeTweetButtonClick(View v) {
        if (activity.getSelectedFeature() != null) {

            Intent intent = new Intent(activity, ComposeTweetActivity.class);
            intent.putExtra("TweetMessage", "Byłem dzisiaj w " + activity.getSelectedFeature().getProperties().getNazwa());
            startActivity(intent);
            /*TwitterHelper twitterHelper = DataStoreClass.getGlobalTwitterHelper();

            twitterHelper.tweet(activity, "Byłem dzisiaj w " + activity.getSelectedFeature().getProperties().getNazwa(), "podroze");*/
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Wybierz najpierw konkretny obiekt", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            setSelectedFeatureOnPage();
        }
    }

    public void setSelectedFeatureOnPage() {
        Feature feature = activity.getSelectedFeature();

        if (feature == null) {
            mNetworkImageView.setImageResource(android.R.color.transparent);
            //Api z Poznania pobiera nie dokończone opisy.
            title.setText("Wybierz najpierw miejsce na mapie");
            description.setText("");
            return;
        }

        final String url = "http://www.poznan.pl/mim/upload/obiekty/" + feature.getProperties().getGrafika();
        setupNetworkImageViewSource(url);
        //Api z Poznania pobiera nie dokończone opisy.
        title.setText(feature.getProperties().getNazwa());

        String opis = feature.getProperties().getOpis();
        opis = opis.substring(3);
        description.setText(opis);
    }

    private void setupNetworkImageViewSource(String url) {
        mImageLoader = CustomVolleyRequestQueue.getInstance(activity.getApplicationContext())
                .getImageLoader();

        mImageLoader.get(url, ImageLoader.getImageListener(mNetworkImageView,
                R.mipmap.ic_launcher, android.R.drawable //Deafault image
                        .ic_dialog_alert));//Error image
        mNetworkImageView.setImageUrl(url, mImageLoader);
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
