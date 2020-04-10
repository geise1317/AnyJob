package com.example.anyjob.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.example.anyjob.PostInfo;
import com.example.anyjob.R;
import com.example.anyjob.view.ContentsItemView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
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

import static com.example.anyjob.Util.isStorageUrl;
import static com.example.anyjob.Util.startToast;
import static com.example.anyjob.Util.storageUrlToName;

public class WritePostActivity extends BasicActivity {
    private static final String TAG = "WritePostActivity: ";
    private FirebaseUser user;
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private RelativeLayout post_ButtonLayout;
    private int pathCount, successCount;
    private ImageView selectedImageView;
    private EditText selectedEditText;
    private RelativeLayout loaderLayout;
    private EditText title_EditText;
    private EditText post_DescriptionEditText;
    private PostInfo postInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        parent = findViewById(R.id.post_contentsLayout);
        post_ButtonLayout = findViewById(R.id.post_ButtonLayout);
        loaderLayout = findViewById(R.id.loaderLayout);
        title_EditText = findViewById(R.id.title_EditText);
        post_DescriptionEditText = findViewById(R.id.contentsEditText);

        findViewById(R.id.post_postButton).setOnClickListener(onClickListener);
        findViewById(R.id.post_imageButton).setOnClickListener(onClickListener);
        findViewById(R.id.post_videoButton).setOnClickListener(onClickListener);
        findViewById(R.id.post_editImage).setOnClickListener(onClickListener);
        findViewById(R.id.post_editVideo).setOnClickListener(onClickListener);
        findViewById(R.id.post_delete).setOnClickListener(onClickListener);

        post_ButtonLayout.setOnClickListener(onClickListener);
        post_DescriptionEditText.setOnFocusChangeListener(onFocusChangeListener);

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
        postInfo = (PostInfo)getIntent().getSerializableExtra("postInfo");
        postInit();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.post_postButton:
                    postUpload();
                    break;
                case R.id.post_imageButton:
                    startActivity(GalleryActivity.class, "image", 0);
                    break;
                case R.id.post_videoButton:
                    startActivity(GalleryActivity.class, "video", 0);
                    break;
                case R.id.post_ButtonLayout:
                    if(post_ButtonLayout.getVisibility() == View.VISIBLE){
                        post_ButtonLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.post_editImage:
                    startActivity(GalleryActivity.class, "image", 1);
                    post_ButtonLayout.setVisibility(View.GONE);
                    break;
                case R.id.post_editVideo:
                    startActivity(GalleryActivity.class, "video", 1);
                    post_ButtonLayout.setVisibility(View.GONE);
                    break;
                case R.id.post_delete:
                    View selectedView = (View)selectedImageView.getParent();
                    StorageReference deserRef = storageRef.child("posts/"+postInfo.getId()+"/"+storageUrlToName(pathList.get(parent.indexOfChild(selectedView) - 1)));
                    deserRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startToast(WritePostActivity.this, "Deleted file");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            startToast(WritePostActivity.this, "Failed to delete file");
                        }
                    });

                    pathList.remove(parent.indexOfChild(selectedView) - 1);
                    parent.removeView((View) selectedImageView.getParent());
                    post_ButtonLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void postUpload(){
        final String title = ((EditText)findViewById(R.id.title_EditText)).getText().toString();
        //final String description = ((EditText)findViewById(R.id.post_DescriptionEditText)).getText().toString();
        if(title.length() > 0){
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = postInfo == null? firebaseFirestore.collection("posts").document() : firebaseFirestore.collection("posts").document(postInfo.getId());
            final Date date = postInfo == null ? new Date() : postInfo.getcreatedAt();
            for(int i=0; i<parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i); //ISSUE!!!
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            contentList.add(text);
                        }
                    } else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentList.add(path);
                        String[] pathArray = path.split("\\.");
                        final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);
                        try {

                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentList.size() - 1)).build();
                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            successCount--;
                                            Log.e("Log", "uri: " + uri);
                                            contentList.set(index, uri.toString());
                                            if (successCount == 0) {
                                                //Complete
                                                PostInfo postInfo = new PostInfo(title, contentList, user.getUid(), date);
                                                storeUpload(documentReference, postInfo);
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
                storeUpload(documentReference, new PostInfo(title, contentList, user.getUid(), date));
            }
        }
        else {
            startToast(WritePostActivity.this, "Please enter title and description");
            loaderLayout.setVisibility(View.GONE);
        }
    }

    private void storeUpload(DocumentReference documentReference, PostInfo postInfo){
        documentReference.set(postInfo)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    loaderLayout.setVisibility(View.GONE);
                    finish();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                    loaderLayout.setVisibility(View.GONE);
                }
            });
    }

    private void startActivity(Class c, String media, int requestCode){
        Intent intent = new Intent(this, c);
        intent.putExtra("media", media);
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
                            post_ButtonLayout.setVisibility(View.VISIBLE);
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

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                selectedEditText = (EditText) v;
            }
        }
    };

    private void postInit(){
        if(postInfo != null){
            title_EditText.setText(postInfo.getTitle());

            ArrayList<String> contentsList = postInfo.getDescription();
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
                            post_ButtonLayout.setVisibility(View.VISIBLE);
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
                    post_DescriptionEditText.setText(contents);
                }
            }
        }
    }

}
