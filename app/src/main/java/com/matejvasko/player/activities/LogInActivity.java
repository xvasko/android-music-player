package com.matejvasko.player.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.matejvasko.player.R;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.authentication.AuthenticationCallback;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout logInLayout, signUpLayout;
    private TextView signUpLink, logInLink;
    private EditText logInEmailEditText, logInPasswordEditText, signUpDisplayName, signUpEmailEditText, signUpPasswordEditText;
    private Button logInButton, signUpButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        prepareUI();
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Navigation.findNavController().navigateUp();
//            }
//        });
    }

    void prepareUI() {
        logInLayout = findViewById(R.id.log_in_layout);
        signUpLayout = findViewById(R.id.sign_up_layout);

        boolean signingUp = getIntent().getBooleanExtra("signing_up", false);
        if (signingUp) {
            logInLayout.setVisibility(View.INVISIBLE);
            signUpLayout.setVisibility(View.VISIBLE);
        }

        logInEmailEditText = findViewById(R.id.log_in_email);
        logInPasswordEditText = findViewById(R.id.log_in_password);
        logInButton = findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(this);
        signUpDisplayName = findViewById(R.id.sign_up_display_name);
        signUpEmailEditText = findViewById(R.id.sign_up_email);
        signUpPasswordEditText = findViewById(R.id.sign_up_password);
        signUpButton = findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(this);
        signUpLink = findViewById(R.id.sign_up_link);
        signUpLink.setOnClickListener(this);
        logInLink = findViewById(R.id.log_in_link);
        logInLink.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.log_in_link:
                logInLayout.setVisibility(View.VISIBLE);
                signUpLayout.setVisibility(View.INVISIBLE);
                break;
            case R.id.sign_up_link:
                logInLayout.setVisibility(View.INVISIBLE);
                signUpLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.log_in_button:
                String logInEmail = logInEmailEditText.getText().toString();
                String logInPassword = logInPasswordEditText.getText().toString();

                progressDialog.setTitle("Logging in!");
                progressDialog.setMessage("Please wait for server to respond...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Authentication.logIn(logInEmail, logInPassword, new AuthenticationCallback() {
                    @Override
                    public void onUserRetrieved(FirebaseUser user) {
                        progressDialog.dismiss();
                        if (user != null) {
                            FirebaseDatabaseManager.getUserData(Authentication.getCurrentUserUid(), new FirebaseDatabaseManagerCallback() {
                                @Override
                                public void onResult(Bundle userDataBundle) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("user_data_bundle", userDataBundle);
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.sign_up_button:
                String signUpName = signUpDisplayName.getText().toString();
                String signUpEmail = signUpEmailEditText.getText().toString();
                String signUpPassword = signUpPasswordEditText.getText().toString();

                progressDialog.setTitle("Creating new account!");
                progressDialog.setMessage("Please wait for server to respond...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Authentication.signUp(signUpName, signUpEmail, signUpPassword, new AuthenticationCallback() {
                    @Override
                    public void onUserRetrieved(FirebaseUser user) {
                        progressDialog.dismiss();
                        if (user != null) {
                            FirebaseDatabaseManager.getUserData(Authentication.getCurrentUserUid(), new FirebaseDatabaseManagerCallback() {
                                @Override
                                public void onResult(Bundle userDataBundle) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("user_data_bundle", userDataBundle);
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                }
                            });
                        }
                    }
                });
                break;
        }
    }
}
