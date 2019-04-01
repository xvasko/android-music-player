package com.matejvasko.player.workmanager;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploadWorker extends Worker {

    private static final String TAG = "UploadWorker";

    DatabaseReference userDatabase;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("current_song");
    }

    @NonNull
    @Override
    public Result doWork() {
        String songData = getInputData().getString("song_name");
        Log.d(TAG, "doWork: uploading " + songData);
        userDatabase.setValue(songData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: song successfully uploaded to database");
                } else {
                    Log.d(TAG, "onComplete: uploading song to database failed");
                }

            }
        });

        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped: ");
    }
}
