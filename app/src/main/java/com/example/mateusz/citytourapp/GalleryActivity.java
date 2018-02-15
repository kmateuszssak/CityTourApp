package com.example.mateusz.citytourapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import android.widget.TextView;

import com.example.mateusz.citytourapp.ImagesSet.GlideApp;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO zdjęcię na pełnym ekranie po kliknięciu
public class GalleryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference database;

    GalleryActivity galleryActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_gallery);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Zdjęcia");

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference();

        database.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> mapaURLs = (Map<String, String>) dataSnapshot.getValue();

                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
                ContentAdapter adapter = new ContentAdapter(storageReference, mapaURLs);
                recyclerView.setAdapter(adapter);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(galleryActivity));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture;
        public TextView name;
        public TextView description;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.card_image);
            //name = (TextView) itemView.findViewById(R.id.card_title);
            description = (TextView) itemView.findViewById(R.id.card_text);
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private int LENGTH = 2;
        private Map<String, String> photoListUrls;
        private ArrayList<StorageReference> photoListReferences;
        private ArrayList<String> featuresNames;

        public ContentAdapter(StorageReference storageReference, Map<String, String> mapaURLs) {
            photoListUrls = mapaURLs;
            photoListReferences = new ArrayList<StorageReference>();
            featuresNames = new ArrayList<String>();
            LENGTH = mapaURLs.size();

            for (Map.Entry<String, String> entry : photoListUrls.entrySet()) {
                StorageReference reference = storageReference.child(entry.getValue());
                photoListReferences.add(reference);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final int pos = position;
            final ViewHolder holder1 = holder;
            StorageReference reference = photoListReferences.get(position);
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    String featureName = storageMetadata.getCustomMetadata("featureName");

                    Glide.with(galleryActivity.getApplicationContext())
                            .load(photoListReferences.get(pos))
                            .into(holder1.picture);
                    holder1.description.setText(featureName);

                    featuresNames.add(featureName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });
        }

        @Override
        public int getItemCount() {
            return LENGTH;
        }
    }

    private String getUserFeaturePathImages(Integer featureID) {
        return user.getUid() + "/images/" + featureID;
    }

    private String getUserAllImagesPath() {
        return user.getUid() + "/images/";
    }
}
