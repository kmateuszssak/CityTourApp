package com.example.mateusz.citytourapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

public class SelectImageActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference database;

    SelectImageActivity selectImageActivity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_gallery);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Wybierz zdjęcie");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference();

        database.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> mapaURLs = (Map<String, String>) dataSnapshot.getValue();

                if (mapaURLs == null || mapaURLs.isEmpty()) {
                    selectImageActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(selectImageActivity.getApplicationContext(), "Brak zdjęć", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
                SelectImageActivity.ContentAdapter adapter = new SelectImageActivity.ContentAdapter(storageReference, mapaURLs, new ClickListener() {
                    @Override
                    public void onPositionClicked(int position) {
                        // callback performed on click
                    }

                    @Override
                    public void onLongClicked(int position) {
                        // callback performed on click
                    }
                });
                recyclerView.setAdapter(adapter);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(selectImageActivity));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView picture;
        public TextView name;
        public TextView description;
        public Button button;
        private WeakReference<ClickListener> listenerRef;
        private ArrayList<String> photoListUrls;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent, ClickListener listener, ArrayList<String> urls) {
            super(inflater.inflate(R.layout.item_card_select_image, parent, false));

            //listenerRef = new WeakReference<>(listener);

            picture = (ImageView) itemView.findViewById(R.id.card_image);
            //name = (TextView) itemView.findViewById(R.id.card_title);
            description = (TextView) itemView.findViewById(R.id.card_text);
            button = (Button) itemView.findViewById(R.id.action_button);

            button.setOnClickListener(this);
            photoListUrls = urls;
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            //listenerRef.get().onPositionClicked(adapterPosition);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", photoListUrls.get(adapterPosition));
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public interface ClickListener {

        void onPositionClicked(int position);

        void onLongClicked(int position);
    }

    /**
     * Adapter to display recycler view.
     */
    public class ContentAdapter extends RecyclerView.Adapter<SelectImageActivity.ViewHolder> {
        // Set numbers of List in RecyclerView.
        private int LENGTH = 2;
        private Map<String, String> photoListUrls;
        private ArrayList<StorageReference> photoListReferences;
        private ArrayList<String> featuresNames;
        private ArrayList<String> urls;
        private ClickListener listener;

        public ContentAdapter(StorageReference storageReference, Map<String, String> mapaURLs, ClickListener listener) {
            photoListUrls = mapaURLs;
            photoListReferences = new ArrayList<StorageReference>();
            featuresNames = new ArrayList<String>();
            urls = new ArrayList<String>();
            listener = listener;
            LENGTH = mapaURLs.size();

            for (Map.Entry<String, String> entry : photoListUrls.entrySet()) {
                StorageReference reference = storageReference.child(entry.getValue());
                urls.add(entry.getValue());
                photoListReferences.add(reference);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent, listener, urls);
        }

        @Override
        public void onBindViewHolder(SelectImageActivity.ViewHolder holder, int position) {

            final int pos = position;
            final SelectImageActivity.ViewHolder holder1 = holder;
            StorageReference reference = photoListReferences.get(position);
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    String featureName = storageMetadata.getCustomMetadata("featureName");

                    Glide.with(selectImageActivity.getApplicationContext())
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
