package com.example.mateusz.citytourapp.ui;

/**
 * Created by Mateusz on 11.02.2018.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.mateusz.citytourapp.MapsActivity;
import com.example.mateusz.citytourapp.Model.Feature;
import com.example.mateusz.citytourapp.R;

/**
 * Created by Mateusz on 11.02.2018.
 */

public class TabDetal extends Fragment {

    MapsActivity activity;

    private TextView title;
    private TextView description;
    private NetworkImageView mNetworkImageView;
    private ImageLoader mImageLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if ( getActivity() instanceof MapsActivity){
            activity = (MapsActivity) getActivity();
        }

        View view = inflater.inflate(R.layout.tab_detal, container, false);

        //title = view.findViewById(R.id.title_selected_item);
        //description = view.findViewById(R.id.description_long_selected_item);
        mNetworkImageView = (NetworkImageView) view.findViewById(R.id.networkImageView);

        //setSelectedFeatureOnPage();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            setSelectedFeatureOnPage();
        }
    }

    private void setSelectedFeatureOnPage() {
        Feature feature = activity.getSelectedFeature();

        if(feature == null)
            return;

        final String url = "http://www.poznan.pl/mim/upload/obiekty/" + feature.properties.grafika;
        setupNetworkImageViewSource(url);

        title.setText(feature.properties.nazwa);
        description.setText(feature.properties.opis);
    }

    private void setupNetworkImageViewSource(String url) {
        mImageLoader = CustomVolleyRequestQueue.getInstance(activity.getApplicationContext())
                .getImageLoader();

        mImageLoader.get(url, ImageLoader.getImageListener(mNetworkImageView,
                R.mipmap.ic_launcher, android.R.drawable //Deafault image
                        .ic_dialog_alert));//Error image
        mNetworkImageView.setImageUrl(url, mImageLoader);
    }
}
