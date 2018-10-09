package com.matejvasko.player;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.matejvasko.player.fragments.FriendsFragment;
import com.matejvasko.player.fragments.library.LibraryFragment;
import com.matejvasko.player.fragments.MapFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bottom_nav) BottomNavigationView bottomNav;
    @BindView(R.id.fragment_container)
    RelativeLayout fragmentContainer;

    private LibraryFragment libraryFragment;
    private FriendsFragment friendsFragment;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        libraryFragment = new LibraryFragment();
        friendsFragment = new FriendsFragment();
        mapFragment = new MapFragment();

        setFragment(libraryFragment);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_nav_library:
                        setFragment(libraryFragment);
                        return true;
                    case R.id.bottom_nav_friends:
                        setFragment(friendsFragment);
                        return true;
                    case R.id.bottom_nav_map:
                        setFragment(mapFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }
}
