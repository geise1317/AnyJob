

package com.example.anyjob.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.anyjob.pInformation;
import com.example.anyjob.R;
import com.example.anyjob.view.ContentsItemView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;

import static com.example.anyjob.Util.*;

@SuppressWarnings("ALL")
public class WritePostActivity<pInformation> extends GeneralActivity {
    private static final String TAG = "WritePostActivity: ";
    private FirebaseUser user;
    private RelativeLayout pButtonLayout;
    private int pathCount, successCount;
    private ImageView selectedImageView;
    private EditText selectedEditText;
    private RelativeLayout loaderLayout;
    private EditText title_EditText;
    private pInformation pInformation;
    private StorageReference storageRef;
    private final ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        parent = findViewById(R.id.post_contentsLayout);
        pButtonLayout = findViewById(R.id.pButtonLayout);
        loaderLayout = findViewById(R.id.loaderLayout);
        title_EditText = findViewById(R.id.title_EditText);
        EditText pDescriptionEdit = findViewById(R.id.contentsEditText);

        findViewById(R.id.post_postButton).setOnClickListener(onClickListener);
        findViewById(R.id.post_imageButton).setOnClickListener(onClickListener);
        findViewById(R.id.post_videoButton).setOnClickListener(onClickListener);
        findViewById(R.id.post_editImage).setOnClickListener(onClickListener);
        findViewById(R.id.post_editVideo).setOnClickListener(onClickListener);
        findViewById(R.id.post_delete).setOnClickListener(onClickListener);

        pButtonLayout.setOnClickListener(onClickListener);
        pDescriptionEdit.setOnFocusChangeListener(onFocusChangeListener);

        title_EditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    selectedEditText = null;
                }
            }
        });

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();
        pInformation = (pInformation)getIntent().getSerializableExtra("pInformation");
        postInit();
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.post_postButton:
                    postUpload();
                    break;
                case R.id.post_imageButton:
                    startActivity("image", 0);
                    break;
                case R.id.post_videoButton:
                    startActivity("video", 0);
                    break;
                case R.id.pButtonLayout:
                    if(pButtonLayout.getVisibility() == View.VISIBLE){
                        pButtonLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.post_editImage:
                    startActivity("Images", 1);
                    pButtonLayout.setVisibility(View.GONE);
                    break;
                case R.id.post_editVideo:
                    startActivity("Videos", 1);
                    pButtonLayout.setVisibility(View.GONE);
                    break;
                case R.id.post_delete:
                    View selectedView = (View)selectedImageView.getParent();
                    StorageReference storageApp = storageRef.child("posts/"+pInformation.getId()+"/"+storageUrlToName(pathList.get(parent.indexOfChild(selectedView) - 1)));
                    storageApp.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startToast(WritePostActivity.this, "Deleted file");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NonNull Exception e) {
                            startToast(WritePostActivity.this, "Failed to delete file");
                        }
                    });

                    pathList.remove(parent.indexOfChild(selectedView) - 1);
                    parent.removeView((View) selectedImageView.getParent());
                    pButtonLayout.setVisibility(View.GONE);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + v.getId());
            }
        }
    };

    private void postUpload(){
        final String title = ((EditText)findViewById(R.id.title_EditText)).getText().toString();
        if(title.length() > 0){
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = pInformation == null? firebaseFirestore.collection("posts").document() : firebaseFirestore.collection("posts").document(pInformation.getId());
            final Date date = pInformation == null ? new Date() : pInformation.getcreatedAt();
            for(int i=0; i<parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i); //ISSUE!!!
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            final boolean add = contentList.add(text);
                        }
                    } else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentList.add(path);
                        String[] pathArray;
                        pathArray = "\\.".split(path);
                        final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);
                        try {

                            UploadTask uploadTask;
                            try (InputStream stream = new FileInputStream(new File(pathList.get(pathCount)))) {
                                StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentList.size() - 1)).build();
                                uploadTask = mountainImagesRef.putStream(stream, metadata);
                            }
                            final int finalI = i;
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getCustomMetadata("index")));
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            successCount--;
                                            finalI.e("Log", "uri: " + uri);
                                            contentList.set(index, uri.toString());
                                            if (successCount == 0) {
                                                //Complete
                                                pInformation pInformation = new pInformation(title, contentList, user.getUid(), date);
                                                storeUpload(documentReference, pInformation);
                                            }
                                        }
                                    });
                                }
                            });
                        } catch (FileNotFoundException e) {
                            Log.e("Log", "Error: " + e.toString());
                        }
                        pathCount++;
                    }
                }
            }
            if(successCount == 0){
                storeUpload(documentReference, new pInformation(title, contentList, user.getUid(), date));
            }
        }
        else {
            startToast(WritePostActivity.this, "Please enter the title and description for your post");
            loaderLayout.setVisibility(View.GONE);
        }
    }

    private void storeUpload(DocumentReference documentReference, pInformation pInformation){
        final Task<Void> error_writing_document = documentReference.set(pInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentView successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, getString(R.string.document), e)
                        loaderLayout.setVisibility(View.GONE);
                    }
                });
    }

    private void startActivity(String media, int requestCode){
        Intent intent;
        intent = new Intent(this, GalleryActivity.class);
        final Intent intent1;
        intent1 = intent.putExtra(getString(R.string.SahibPhoto), media);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                if(resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    pathList.add(profilePath);

                    ContentsItemView contentsItemView = new ContentsItemView(this);

                    if(selectedEditText == null){
                        parent.addView(contentsItemView);
                    } else {
                        for(int i = 0; i < parent.getChildCount(); i++){
                            if (parent.getChildAt(i) == selectedEditText.getParent()){
                                parent.addView(contentsItemView, i + 1);
                                break;
                            }
                        }
                    }

                    contentsItemView.setImage(profilePath);
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pButtonLayout.setVisibility(View.VISIBLE);
                            selectedImageView = (ImageView) v;
                        }
                    });
                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                }
                break;
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    String profilePath = data.getStringExtra("profilePath");
                    String path = data.getStringExtra(profilePath);
                    pathList.set(parent.indexOfChild((View) selectedImageView.getParent())-1, path);
                    Glide.with(this).load(path).override(1000).into(selectedImageView);
                }
        }
    }

    private final View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                selectedEditText = (EditText) v;
            }
        }
    };

    private void postInit(){
        if(pInformation != null){
            title_EditText.setText(pInformation.getTitle());

            ArrayList<String> contentsList;
            contentsList = pInformation.getDescription();
            for(int i=0; i<contentsList.size(); i++){
                String contents = contentsList.get(i);
                if(isStorageUrl(contents)){
                    pathList.add(contents);
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);

                    contentsItemView.setImage(contents);
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pButtonLayout.setVisibility(View.VISIBLE);
                            selectedImageView = (ImageView) v;
                        }
                    });

                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                    if(i < contentsList.size()-1){
                        String nextContents = contentsList.get(i+1);
                        if(!isStorageUrl(nextContents)){
                            contentsItemView.setText(nextContents);
                        }
                    }

                }else if(i == 0){
                    ActionBar.Tab pDescriptionEdit = null;
                    Objects.requireNonNull(pDescriptionEdit).setText(contents);
                }
            }
        }
    }

}
