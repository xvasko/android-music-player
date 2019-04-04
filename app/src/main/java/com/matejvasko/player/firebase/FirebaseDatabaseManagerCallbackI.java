package com.matejvasko.player.firebase;

import com.matejvasko.player.models.User;

public interface FirebaseDatabaseManagerCallbackI {

    void onResult(User user);

    void onResult(String friendshipState);

    void onSuccess();

    void onFailure();

}
