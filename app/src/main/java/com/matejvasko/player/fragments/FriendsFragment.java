package com.matejvasko.player.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matejvasko.player.R;
import com.matejvasko.player.utils.Authentication;
import com.matejvasko.player.utils.AuthenticationCallback;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

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
    EditText registerEmailEditText;
    EditText registerPasswordEditText;
    Button logInButton;
    Button registerButton;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_friends, container, false);

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

                Authentication.logIn(email, password, new AuthenticationCallback() {
                    @Override
                    public void onUserRetrieved(FirebaseUser user) {
                        if (user != null) {
                            loggedInLayout.setVisibility(View.VISIBLE);
                            logInLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        registerEmailEditText = view.findViewById(R.id.register_email);
        registerPasswordEditText = view.findViewById(R.id.register_password);
        registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmailEditText.getText().toString();
                String password = registerPasswordEditText.getText().toString();

                Authentication.signUp(email, password, new AuthenticationCallback() {
                    @Override
                    public void onUserRetrieved(FirebaseUser user) {
                        if (user != null) {
                            loggedInLayout.setVisibility(View.VISIBLE);
                            signUpLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        Log.d(TAG, "onCreateView: ");
        return view;
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
