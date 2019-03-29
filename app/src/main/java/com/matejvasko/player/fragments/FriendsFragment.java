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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.activities.AccountActivity;
import com.matejvasko.player.activities.ProfileActivity;
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.FriendListAdapter;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.authentication.AuthenticationCallback;
import com.matejvasko.player.models.Friend;
import com.matejvasko.player.utils.FirebaseRepository;
import com.matejvasko.player.viewmodels.FriendViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

    private static final String TAG = "FriendsFragment";

    private ConstraintLayout logInLayout, signUpLayout, loggedInLayout;
    private TextView signUpLink, logInLink, userName, userEmail;
    private EditText logInEmailEditText, logInPasswordEditText, signUpDisplayName, signUpEmailEditText, signUpPasswordEditText, searchFriendEditText;
    private Button logInButton, signUpButton, searchFriendButton;
    private ProgressDialog progressDialog;
    private ImageView userImage;
    private ImageButton popupMenuButton;

    private RecyclerView recyclerView;

    private DatabaseReference userDatabase;

    private FriendViewModel friendViewModel;
    private FriendListAdapter friendListAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_friends, container, false);

        friendViewModel = ViewModelProviders.of(this).get(FriendViewModel.class);
        friendListAdapter = new FriendListAdapter();

        recyclerView = view.findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(friendListAdapter);
        friendViewModel.getFriends().observe(this, new Observer<PagedList<Friend>>() {
            @Override
            public void onChanged(PagedList<Friend> friends) {
                System.out.println("observed list size: " + friends.size());
                friendListAdapter.submitList(friends);
            }
        });


        prepareUI(view);

        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up_link:
                logInLayout.setVisibility(View.INVISIBLE);
                signUpLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.log_in_link:
                logInLayout.setVisibility(View.VISIBLE);
                signUpLayout.setVisibility(View.INVISIBLE);
                break;
            case R.id.popup_menu:
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.setOnMenuItemClickListener(FriendsFragment.this);
                popupMenu.inflate(R.menu.account_options_items);
                popupMenu.show();
                break;
            case R.id.log_in_button: {
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
                            retrieveUserData(user);
                        }
                    }
                });
                break;
            }
            case R.id.sign_up_button: {
                String name = signUpDisplayName.getText().toString();
                String email = signUpEmailEditText.getText().toString();
                String password = signUpPasswordEditText.getText().toString();

                progressDialog.setTitle("Signing up!");
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
                            retrieveUserData(user);
                        }
                    }
                });
                break;
            }
            case R.id.search_friend_button:
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                final String email = searchFriendEditText.getText().toString();
                ref.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String userId = "";

                            for (final DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                userId = userSnapshot.getKey();
                            }

                            if (!userId.equals("")) {
                                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                                intent.putExtra("user_id", userId);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(getActivity(), "USER NOT FOUND", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            default:
                break;
        }
    }

    private void prepareUI(View view) {
        progressDialog = new ProgressDialog(getActivity());
        searchFriendEditText = view.findViewById(R.id.search_friend_edit_text);
        searchFriendButton = view.findViewById(R.id.search_friend_button);
        searchFriendButton.setOnClickListener(this);
        logInLayout = view.findViewById(R.id.log_in_layout);
        signUpLayout = view.findViewById(R.id.sign_up_layout);
        loggedInLayout = view.findViewById(R.id.friends_tab_logged_in_layout);
        userImage = view.findViewById(R.id.friends_user_image);
        userName = view.findViewById(R.id.friends_user_name);
        userEmail = view.findViewById(R.id.friends_user_email);

        signUpLink = view.findViewById(R.id.sign_up_link);
        signUpLink.setOnClickListener(this);
        logInLink = view.findViewById(R.id.log_in_link);
        logInLink.setOnClickListener(this);

        logInEmailEditText = view.findViewById(R.id.log_in_email);
        logInPasswordEditText = view.findViewById(R.id.log_in_password);
        logInButton = view.findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(this);
        signUpDisplayName = view.findViewById(R.id.sign_up_display_name);
        signUpEmailEditText = view.findViewById(R.id.sign_up_email);
        signUpPasswordEditText = view.findViewById(R.id.sign_up_password);
        signUpButton = view.findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(this);
        popupMenuButton = view.findViewById(R.id.popup_menu);
        popupMenuButton.setOnClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.account_settings:
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                startActivity(intent);
                return true;
            case R.id.account_log_out:
                Authentication.signOut();
                logInLayout.setVisibility(View.VISIBLE);
                loggedInLayout.setVisibility(View.INVISIBLE);
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
            retrieveUserData(currentUser);
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

    private void retrieveUserData(FirebaseUser currentUser) {

        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                userName.setText(name);
                userEmail.setText(email);

                if (!image.equals("default")) {
                    Glide.with(getActivity()).load(image).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userImage);
                } else {
                    Glide.with(getActivity()).load(R.drawable.ic_perm_identity_black_24dp).into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
