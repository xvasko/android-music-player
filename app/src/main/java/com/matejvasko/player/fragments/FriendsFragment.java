package com.matejvasko.player.fragments;


import android.app.Activity;
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
import android.widget.LinearLayout;
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
import com.google.gson.Gson;
import com.matejvasko.player.App;
import com.matejvasko.player.R;
import com.matejvasko.player.activities.AccountActivity;
import com.matejvasko.player.activities.LogInActivity;
import com.matejvasko.player.activities.ProfileActivity;
import com.matejvasko.player.adapters.FriendListAdapter;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.databinding.FragmentFriendsBinding;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;
import com.matejvasko.player.models.User;
import com.matejvasko.player.viewmodels.FriendViewModel;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

    private static final String TAG = "FriendsFragment";

    private LinearLayout loggedInLayout, notLoggedInLayout;
    private TextView userName, userEmail;
    private EditText searchFriendEditText;
    private ImageView userImage;

    private FriendViewModel friendViewModel;
    private FriendListAdapter friendListAdapter;

    private Observer<PagedList<User>> observer;

    private FragmentFriendsBinding binding;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friends, container, false);
        View view = binding.getRoot();

        friendViewModel = ViewModelProviders.of(this).get(FriendViewModel.class);
        friendListAdapter = new FriendListAdapter(getActivity());

        RecyclerView recyclerView = view.findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(friendListAdapter);
        observer = new Observer<PagedList<User>>() {
            @Override
            public void onChanged(PagedList<User> friends) {
                friendListAdapter.submitList(friends);
            }
        };
        if (Authentication.getCurrentUser() != null) {
            friendViewModel.getFriends().observe(this, observer);
        }

        prepareUI(view);

        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popup_menu:
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.setOnMenuItemClickListener(FriendsFragment.this);
                popupMenu.inflate(R.menu.account_options_items);
                popupMenu.show();
                break;
            case R.id.log_in_request_sign_up_link:
                Intent signUpIntent = new Intent(getActivity(), LogInActivity.class);
                signUpIntent.putExtra("signing_up", true);
                startActivityForResult(signUpIntent, 1);
                break;
            case R.id.log_in_request_log_in_button:
                Intent logInIntent = new Intent(getActivity(), LogInActivity.class);
                logInIntent.putExtra("signing_up", false);
                startActivityForResult(logInIntent, 1);
                break;
            case R.id.search_friend_button:
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                final String email = searchFriendEditText.getText().toString();
                ref.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String userId = "";

                            for (final DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
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
            case R.id.friends_user_image:
                Intent profileIntent = new Intent(getActivity(), AccountActivity.class);
                startActivity(profileIntent);
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Gson gson = new Gson();
                User user = gson.fromJson(data.getExtras().getString("user"), User.class);
                userName.setText(user.getName());
                userEmail.setText(user.getEmail());
                if (!user.getThumbImage().equals("default")) {
                    Glide.with(App.getAppContext()).load(user.getThumbImage()).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userImage);
                } else {
                    Glide.with(App.getAppContext()).load(user.getThumbImage()).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userImage);
                }
                friendViewModel.getFriends().observe(this, observer);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void prepareUI(View view) {
        searchFriendEditText = view.findViewById(R.id.search_friend_edit_text);
        loggedInLayout = view.findViewById(R.id.friends_tab_logged_in_layout);
        notLoggedInLayout = view.findViewById(R.id.not_logged_in_layout);
        userImage = view.findViewById(R.id.friends_user_image);
        userImage.setOnClickListener(this);
        userName = view.findViewById(R.id.friends_user_name);
        userEmail = view.findViewById(R.id.friends_user_email);

        TextView signUpLink = view.findViewById(R.id.log_in_request_sign_up_link);
        signUpLink.setOnClickListener(this);
        Button logInButton = view.findViewById(R.id.log_in_request_log_in_button);
        logInButton.setOnClickListener(this);
        ImageButton searchFriendButton = view.findViewById(R.id.search_friend_button);
        searchFriendButton.setOnClickListener(this);
        ImageButton popupMenuButton = view.findViewById(R.id.popup_menu);
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
                friendViewModel.getFriends().removeObserver(observer);
                Authentication.signOut();
                notLoggedInLayout.setVisibility(View.VISIBLE);
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
            FirebaseDatabaseManager.getUserData(Authentication.getCurrentUserUid(), new FirebaseDatabaseManagerCallback() {
                @Override
                public void onResult(User user) {
                    binding.setUser(user);
                }
            });
            loggedInLayout.setVisibility(View.VISIBLE);
            notLoggedInLayout.setVisibility(View.INVISIBLE);
        } else {
            loggedInLayout.setVisibility(View.INVISIBLE);
            notLoggedInLayout.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

}
