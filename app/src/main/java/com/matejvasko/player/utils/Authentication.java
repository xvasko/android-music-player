package com.matejvasko.player.utils;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matejvasko.player.App;

import androidx.annotation.NonNull;

public class Authentication {

    private static final String TAG = "Authentication";

    static FirebaseUser currentUser;

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void logIn(@NonNull String email, @NonNull String password, final AuthenticationCallback callback) {
        if (email.equals("") || password.equals("")) return;
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "logInWithEmail: success");
                            Toast.makeText(App.getAppContext(), "Log in succeed.", Toast.LENGTH_SHORT).show();
                            currentUser = auth.getCurrentUser();
                            callback.onUserRetrieved(currentUser);
                        } else {
                            Log.w(TAG, "logInWithEmail: failure", task.getException());
                            Toast.makeText(App.getAppContext(), "Log in failed.", Toast.LENGTH_SHORT).show();
                            callback.onUserRetrieved(null);
                        }
                    }
                });
    }

    public static void signUp(@NonNull String email, @NonNull String password, final AuthenticationCallback callback) {
        if (email.equals("") || password.equals("")) return;
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(App.getAppContext(), "Sign up succeed.", Toast.LENGTH_SHORT).show();
                            currentUser = auth.getCurrentUser();
                            callback.onUserRetrieved(currentUser);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(App.getAppContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                            callback.onUserRetrieved(currentUser);
                        }
                    }
                });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

}
