package com.matejvasko.player.authentication;

import android.util.Log;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matejvasko.player.App;

import java.util.HashMap;

import androidx.annotation.NonNull;

public class Authentication {

    private static final String TAG = "Authentication";


    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUserUid() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        } else {
            return currentUser.getUid();
        }
    }

    public static void logIn(@NonNull String email, @NonNull String password, final AuthenticationCallback callback) {
        if (email.equals("") || password.equals("")) return; // TODO add some form of validation
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmailAndPassword: success");
                            Toast.makeText(App.getAppContext(), "Log in succeed.", Toast.LENGTH_SHORT).show();
                            callback.onUserRetrieved(auth.getCurrentUser());
                        } else {
                            Log.w(TAG, "signInWithEmailAndPassword: failure", task.getException());
                            Toast.makeText(App.getAppContext(), "Log in failed.", Toast.LENGTH_SHORT).show();
                            callback.onUserRetrieved(null);
                        }
                    }
                });
    }

    public static void signUp(@NonNull final String name, @NonNull final String email, @NonNull String password, final AuthenticationCallback callback) {
        if (name.equals("") || email.equals("") || password.equals("")) return; // TODO add some form of validation
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Get uId of just registered user
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("email", email);
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            database.child("users").child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "createUserWithEmail: success");
                                        Toast.makeText(App.getAppContext(), "DB save succeed.", Toast.LENGTH_SHORT).show();
                                        callback.onUserRetrieved( auth.getCurrentUser());
                                    } else {
                                        Log.w(TAG, "createUserWithEmail: failure - " + task.getException().toString());
                                        Toast.makeText(App.getAppContext(), "DB save did not succeed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(App.getAppContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                            callback.onUserRetrieved(null);
                        }
                    }
                });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

}
