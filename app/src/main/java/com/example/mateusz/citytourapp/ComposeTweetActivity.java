package com.example.mateusz.citytourapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mateusz.citytourapp.tweeter.DataStoreClass;
import com.example.mateusz.citytourapp.tweeter.TwitterHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class ComposeTweetActivity extends AppCompatActivity {

    public static final Integer REQ_IMAGE = 1434;

    private EditText editText;
    private ImageView imageView;
    private Button button;
    private Button buttonSend;

    private ComposeTweetActivity composeTweetActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_tweet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stwórz tweet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editText = (EditText) findViewById(R.id.shipper_field);
        imageView = (ImageView) findViewById(R.id.image_view_tweet);
        button = (Button) findViewById(R.id.button_dodaj_zdjecie);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(composeTweetActivity, SelectImageActivity.class);
                startActivityForResult(intent, REQ_IMAGE);
            }
        });

        buttonSend = (Button) findViewById(R.id.button_wyslij_tweet);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTweet();
            }
        });

        Intent intent = getIntent();

        String tweetMessage = intent.getStringExtra("TweetMessage");

        editText.setText(tweetMessage);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private String selectedImageUrl = null;
    private Uri selectedImageURI = null;
    Uri imageUri = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference reference = storageReference.child(result);

            File localFile = null;

            try {
                localFile =  new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
                reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            } catch (Exception ex) {

            }

            if (localFile != null) {
                selectedImageUrl = localFile.getAbsolutePath();

                /*imageUri = FileProvider.getUriForFile(ComposeTweetActivity.this,
                        BuildConfig.APPLICATION_ID + ".file_provider",
                        localFile);*/
                imageUri = Uri.fromFile(localFile);
            }


            /*reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    String url = storageMetadata.getCustomMetadata("url");

                    selectedImageUrl = url;//TODO jak to podam to mi się wyświetla zdjęcie, ale wywala jak chcę wysłać
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });

            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    selectedImageURI = uri;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });*/

            Glide.with(this.getApplicationContext())
                    .load(reference)
                    .into(imageView);

            imageView.setVisibility(ImageView.VISIBLE);
        }
        //selectedImageUrl = null;
        //selectedImageURI = null;
    }

    private void sendTweet() {
        TwitterHelper twitterHelper = DataStoreClass.getGlobalTwitterHelper();

        twitterHelper.tweet(this, editText.getText().toString(), "podroze", imageUri);

        Toast.makeText(getApplicationContext(), "Tweet wysłany", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}
