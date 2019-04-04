package com.matejvasko.player.authentication;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.matejvasko.player.App;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.models.User;

import androidx.annotation.NonNull;

public class Authentication {

    private static final String TAG = "Authentication";


    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUserUid() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "getCurrentUserUid: is empty");
            return "";
        } else {
            Log.d(TAG, "getCurrentUserUid: is " + currentUser.getUid());
            return currentUser.getUid();
        }
    }

    public static void logIn(@NonNull String email, @NonNull String password, final AuthenticationCallback callback) {
        if (email.equals("") || password.equals("")) return; // TODO add some form of validation
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "logInWithEmailAndPassword: success");
                            Toast.makeText(App.getAppContext(), "Log in succeed.", Toast.LENGTH_SHORT).show();
                            saveDeviceTokenToDatabase();
                            callback.onSuccess();
                        } else {
                            Log.w(TAG, "logInWithEmailAndPassword: failure", task.getException());
                            Toast.makeText(App.getAppContext(), "Log in failed.", Toast.LENGTH_SHORT).show();
                            callback.onFailure();
                        }
                    }
                });
    }

    public static void signUp(@NonNull final String name, @NonNull final String email, @NonNull String password, final AuthenticationCallback callback) {
        if (name.equals("") || email.equals("") || password.equals(""))
            return; // TODO add some form of validation
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();

                            final User user = new User();
                            user.setName(name);
                            user.setEmail(email);
                            user.setImage("default");
                            user.setThumbImage("default");
                            user.setDeviceToken(FirebaseInstanceId.getInstance().getToken());

                            FirebaseDatabaseManager.rootDatabase.child("users").child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        callback.onSuccess();
                                    } else {
                                        callback.onFailure();
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(App.getAppContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                            callback.onFailure();
                        }
                    }
                });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private static void saveDeviceTokenToDatabase() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabaseManager.getCurrentUserDatabase().child("deviceToken").setValue(deviceToken)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: device token saved");
                    }
                });
    }

}
