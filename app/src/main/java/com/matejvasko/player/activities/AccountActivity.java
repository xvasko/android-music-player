package com.matejvasko.player.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.AccountPagerAdapter;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.databinding.ActivityAccountBinding;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;
import com.matejvasko.player.firebase.FirebaseFirestoreManager;
import com.matejvasko.player.firebase.FirebaseFirestoreManagerCallback;
import com.matejvasko.player.models.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    private ProgressDialog progressDialog;
    private ActivityAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);

        progressDialog = new ProgressDialog(this);

        binding.accountChangeImageButton.setOnClickListener(new View.OnClickListener() {
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
                binding.setUser(user);
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Friend Requests"));
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
                        Log.d(TAG, "onSuccess: image saved successfully");
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG, "onFailure: failure while saving image");
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
