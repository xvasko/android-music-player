package com.matejvasko.player.authentication;

import com.google.firebase.auth.FirebaseUser;

public interface AuthenticationCallback {

    void onUserRetrieved(FirebaseUser user);

}
