package com.matejvasko.player.activities;

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
import com.google.android.material.tabs.TabLayout;
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
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.AccountPagerAdapter;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;
import com.matejvasko.player.firebase.FirebaseFirestoreManager;
import com.matejvasko.player.firebase.FirebaseFirestoreManagerCallback;
import com.matejvasko.player.models.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import id.zelory.compressor.Compressor;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    
    DatabaseReference userDatabase;
    StorageReference imageStorage;
    FirebaseUser user;

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
        accountChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountActivity.this);
            }
        });

        FirebaseDatabaseManager.getUserData(Authentication.getCurrentUserUid(), new FirebaseDatabaseManagerCallback() {
            @Override
            public void onResult(User user) {
                accountName.setText(user.getName());
                accountEmail.setText(user.getEmail());
                String image = user.getImage();
                if (!image.equals("default")) {
                    Glide.with(getApplicationContext()).load(image).placeholder(R.drawable.ic_perm_identity_black_24dp).into(accountImage);
                }
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        imageStorage = FirebaseStorage.getInstance().getReference();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("User Requests"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.account_pager);
        final AccountPagerAdapter pagerAdapter = new AccountPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Uploading image...");
                progressDialog.setMessage("Please wait for server to respond.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                FirebaseFirestoreManager.saveImageToFirestore(result, new FirebaseFirestoreManagerCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("CCCCC saved success");
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        System.out.println("CCCCC saved failure");
                        progressDialog.dismiss();
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.w(TAG, "onActivityResult: ERROR ", error);
            }
        }
    }
}
