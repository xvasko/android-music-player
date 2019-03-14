package com.matejvasko.player.utils;

import com.google.firebase.auth.FirebaseUser;

public interface AuthenticationCallback {

    void onUserRetrieved(FirebaseUser user);

}
