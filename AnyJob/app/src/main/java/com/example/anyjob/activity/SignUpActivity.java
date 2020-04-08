package com.example.anyjob.activity;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.example.anyjob.MemberInfo;
import com.example.anyjob.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.anyjob.Util.startToast;

public class SignUpActivity extends BasicActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignUpActivity";
    private ImageView profileImageView;
    private String profilePath;
    private RelativeLayout loaderLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        loaderLayout = findViewById(R.id.loaderLayout);
        profileImageView = findViewById(R.id.user_imageView);
        profileImageView.setOnClickListener(onClickListener);
        findViewById(R.id.signup_button).setOnClickListener(onClickListener);
        findViewById(R.id.user_imageView).setOnClickListener(onClickListener);
        findViewById(R.id.gallery_Button).setOnClickListener(onClickListener);
        findViewById(R.id.camera_button).setOnClickListener(onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.signup_button:
                    signUp();
                    break;
                case R.id.user_imageView:
                    CardView cardView = findViewById(R.id.buttonCardView);
                    if(cardView.getVisibility() == View.VISIBLE){
                        cardView.setVisibility(View.GONE);
                    }else {
                        cardView.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.gallery_Button:
                    if (ContextCompat.checkSelfPermission(SignUpActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        // Should we show an explanation?
                        ActivityCompat.requestPermissions(SignUpActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                1);
                        Log.d("onclickListener", "Request Permission");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        } else {
                            startToast(SignUpActivity.this, "Please Grant Permission.");
                            Log.d("onclickListener", "permission not granted");
                        }
                    } else {
                        // Permission has already been granted
                        //startActivity(GalleryActivity.class);
                        startActivity(GalleryActivity.class, "image", 0);
                        Log.d("onclickListener", "permission already granted");
                    }
                    break;
                case R.id.camera_button:
                    startActivity(CameraActivity.class);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startActivity(GalleryActivity.class);
                    startActivity(GalleryActivity.class, "image", 0);
                } else {
                    startToast(SignUpActivity.this, "Please Grant Permission.");
                }
            }
        }
    }


    private void signUp(){
        String email = ((EditText)findViewById(R.id.email_editText)).getText().toString();
        String password = ((EditText)findViewById(R.id.password_editText)).getText().toString();
        String passwordCheck = ((EditText)findViewById(R.id.passwordCheck_editText)).getText().toString();
        String name = ((EditText)findViewById(R.id.userNameEditText)).getText().toString();
        String address = ((EditText)findViewById(R.id.userAddressEditText)).getText().toString();
        String phoneNum = ((EditText)findViewById(R.id.userPhoneNumEditText)).getText().toString();
        String age = ((EditText)findViewById(R.id.userAgeEditText)).getText().toString();

        if(email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0 && name.length() > 0 && address.length() > 0 && phoneNum.length() > 7 && age.length() > 0) {
            if (password.equals(passwordCheck)) {
                loaderLayout.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    storageUploader();
                                    startToast(SignUpActivity.this, "Sign Up Successfully Completed.");
                                    startActivity(LoginActivity.class);
                                    //updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    if (task.getException() != null) {
                                        startToast(SignUpActivity.this, task.getException().toString());
                                        //updateUI(null);
                                    }
                                }

                                // ...
                            }
                        });
            } else {
                startToast(SignUpActivity.this, "Passwords are different.");
            }
        }else {
            startToast(SignUpActivity.this, "Please Enter Email or Password");
        }
    }

    private void startActivity(Class c){
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0: {
                if(resultCode == Activity.RESULT_OK) {
                    profilePath = data.getStringExtra("profilePath");
                    Bitmap bmp = BitmapFactory.decodeFile(profilePath);
                    profileImageView.setImageBitmap(bmp);
                }
                break;
            }
        }
    }

    private void storageUploader(){
        final String name = ((EditText)findViewById(R.id.userNameEditText)).getText().toString();
        final String address = ((EditText)findViewById(R.id.userAddressEditText)).getText().toString();
        final String phoneNum = ((EditText)findViewById(R.id.userPhoneNumEditText)).getText().toString();
        final String age = ((EditText)findViewById(R.id.userAgeEditText)).getText().toString();

        if(name.length() > 0 && address.length() > 0 && phoneNum.length() > 9 && age.length() > 0){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");
            if(profilePath == null){
                MemberInfo memberInfo = new MemberInfo(name, address, phoneNum, age);
                storeUploader(memberInfo);
            }
            try{
                InputStream stream = new FileInputStream(new File(profilePath));
                UploadTask uploadTask = mountainImagesRef.putStream(stream);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.e("Log", "storageUploader not succesful");
                            throw task.getException();
                        }
                        return mountainImagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.d("Log", "Complete: " + downloadUri);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            MemberInfo memberInfo = new MemberInfo(name, address, phoneNum, age, downloadUri.toString());
                            if(user != null) {
                                db.collection("users").document(user.getUid()).set(memberInfo);
                                Log.d("Log", "Completed uploading to database");
                            }

                        } else {
                            // Handle failures
                            // ...
                            Log.e("Log", "Fail");
                        }
                    }
                });
            }catch (FileNotFoundException e){
                Log.e("Log", "Error: "+ e.toString());
            }
        }
        else {
            startToast(SignUpActivity.this, "Please enter user information");
        }
        loaderLayout.setVisibility(View.GONE);
    }

    private void storeUploader(MemberInfo memberInfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //db.collection("users").document()
    }

    private void startActivity(Class c, String media, int requestCode){
        Intent intent = new Intent(this, c);
        intent.putExtra("media", media);
        startActivityForResult(intent, requestCode);
    }
}
