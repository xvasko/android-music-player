package com.matejvasko.player.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.matejvasko.player.AccountActivity;
import com.matejvasko.player.App;
import com.matejvasko.player.R;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.authentication.AuthenticationCallback;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "FriendsFragment";

    ConstraintLayout logInLayout;
    ConstraintLayout signUpLayout;
    ConstraintLayout loggedInLayout;
    TextView signUpLink;
    TextView logInLink;
    TextView signOutLink;
    TextView userEmail;
    EditText logInEmailEditText;
    EditText logInPasswordEditText;
    EditText registerDisplayName;
    EditText registerEmailEditText;
    EditText registerPasswordEditText;
    Button logInButton;
    Button registerButton;

    ProgressDialog progressDialog;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_friends, container, false);

        progressDialog = new ProgressDialog(getActivity());

        logInLayout = view.findViewById(R.id.log_in_layout);
        signUpLayout = view.findViewById(R.id.sign_up_layout);
        loggedInLayout = view.findViewById(R.id.friends_tab_logged_in_layout);

        userEmail = view.findViewById(R.id.user_email_text_view);

        signUpLink = view.findViewById(R.id.sign_up_link);
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInLayout.setVisibility(View.INVISIBLE);
                signUpLayout.setVisibility(View.VISIBLE);
            }
        });
        logInLink = view.findViewById(R.id.log_in_link);
        logInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInLayout.setVisibility(View.VISIBLE);
                signUpLayout.setVisibility(View.INVISIBLE);
            }
        });
        signOutLink = view.findViewById(R.id.sign_out_link);
        signOutLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Authentication.signOut();
                logInLayout.setVisibility(View.VISIBLE);
                loggedInLayout.setVisibility(View.INVISIBLE);
            }
        });

        logInEmailEditText = view.findViewById(R.id.log_in_email);
        logInPasswordEditText = view.findViewById(R.id.log_in_password);
        logInButton = view.findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = logInEmailEditText.getText().toString();
                String password = logInPasswordEditText.getText().toString();

                progressDialog.setTitle("Logging in!");
                progressDialog.setMessage("Please wait for server to respond.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Authentication.logIn(email, password, new AuthenticationCallback() {
                    @Override
                    public void onUserRetrieved(FirebaseUser user) {
                        progressDialog.dismiss();
                        if (user != null) {
                            loggedInLayout.setVisibility(View.VISIBLE);
                            logInLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        registerDisplayName = view.findViewById(R.id.register_display_name);
        registerEmailEditText = view.findViewById(R.id.register_email);
        registerPasswordEditText = view.findViewById(R.id.register_password);
        registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = registerDisplayName.getText().toString();
                String email = registerEmailEditText.getText().toString();
                String password = registerPasswordEditText.getText().toString();

                progressDialog.setTitle("Signing in!");
                progressDialog.setMessage("Please wait for server to respond.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Authentication.signUp(name, email, password, new AuthenticationCallback() {
                    @Override
                    public void onUserRetrieved(FirebaseUser user) {
                        progressDialog.dismiss();
                        if (user != null) {
                            loggedInLayout.setVisibility(View.VISIBLE);
                            signUpLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        Button popupMenuButton = view.findViewById(R.id.popup_menu);
        popupMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.setOnMenuItemClickListener(FriendsFragment.this);
                popupMenu.inflate(R.menu.account_options_items);
                popupMenu.show();
            }
        });

        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.account_settings:
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                startActivity(intent);
                return true;
            case R.id.account_log_out:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = Authentication.getCurrentUser();

        if (currentUser != null) {
            loggedInLayout.setVisibility(View.VISIBLE);
            signUpLayout.setVisibility(View.INVISIBLE);
            logInLayout.setVisibility(View.INVISIBLE);
        } else {
            loggedInLayout.setVisibility(View.INVISIBLE);
            signUpLayout.setVisibility(View.INVISIBLE);
            logInLayout.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "onStart: ");
    }

}
