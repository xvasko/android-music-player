package com.matejvasko.player;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import id.zelory.compressor.Compressor;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    
    DatabaseReference userDatabase;
    StorageReference imageStorage;
    FirebaseUser user;

    private static final int GALLERY_PICK = 1;

    // Layout
    ImageView accountImage;
    TextView accountName;
    TextView accountEmail;
    Button accountChangeImage;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        progressDialog = new ProgressDialog(this);

        accountImage = findViewById(R.id.account_image);
        accountName = findViewById(R.id.account_name);
        accountEmail = findViewById(R.id.account_email);
        accountChangeImage = findViewById(R.id.account_change_image_button);

        user = FirebaseAuth.getInstance().getCurrentUser();
        imageStorage = FirebaseStorage.getInstance().getReference();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        userDatabase.keepSynced(true);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.child("email").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();


                if (!image.equals("default")) {
                    Glide.with(AccountActivity.this).load(image).placeholder(R.drawable.ic_perm_identity_black_24dp).into(accountImage);
                }

                accountName.setText(name);
                accountEmail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        accountChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountActivity.this);

//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//
//                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri  profileFileUri   = result.getUri();
                final File profileImageFile = new File(profileFileUri.getPath());

                progressDialog.setTitle("Uploading image...");
                progressDialog.setMessage("Please wait for server to respond.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final StorageReference profileImageFirebasePath = imageStorage.child("profile_images").child(user.getUid() + ".jpg");
                final StorageReference thumbImageFirebasePath = imageStorage.child("profile_images").child("thumbs").child(user.getUid() + ".jpg");

                profileImageFirebasePath.putFile(profileFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // have to get the downloadUrl of a file this way - API has changed
                            profileImageFirebasePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                 public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();

                                    userDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "putFile: success");
                                                progressDialog.dismiss();
                                                Toast.makeText(AccountActivity.this, "Image url saved to DB.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            Log.w(TAG, "putFile: failure - " + task.getException().toString());
                            progressDialog.dismiss();
                        }
                    }
                });

                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(AccountActivity.this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(profileImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbByteArray = baos.toByteArray();

                // upload compressed thumb
                UploadTask uploadTask = thumbImageFirebasePath.putBytes(thumbByteArray);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            thumbImageFirebasePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();

                                    userDatabase.child("thumb_image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "upload task: success");
                                                progressDialog.dismiss();
                                                Toast.makeText(AccountActivity.this, "Image url saved to DB.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.w(TAG, "onActivityResult: ERROR ", error);
            }
        }
    }
}
