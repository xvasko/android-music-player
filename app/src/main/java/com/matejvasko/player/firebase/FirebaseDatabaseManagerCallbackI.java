package com.matejvasko.player.firebase;

import android.os.Bundle;

import com.google.firebase.database.DatabaseError;

public interface FirebaseDatabaseManagerCallbackI {

    void onResult(Bundle userDataBundle);

    void onResult(String friendshipState);

    void onSuccess();

    void onFailure();

}
