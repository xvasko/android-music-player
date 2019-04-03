package com.matejvasko.player.firebase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matejvasko.player.App;
import com.matejvasko.player.authentication.Authentication;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import id.zelory.compressor.Compressor;

public class FirebaseFirestoreManager {

    private static final String TAG = "FirebaseFirestoreManage";

    static StorageReference imageStorage = FirebaseStorage.getInstance().getReference();


    public static void saveImageToFirestore(CropImage.ActivityResult cropImageResult, final FirebaseFirestoreManagerCallback callback) {
        final Uri profileFileUri = cropImageResult.getUri();
        final File profileImageFile = new File(profileFileUri.getPath());

        // TODO for the love of god, add compression to big image, and add restriction on size, maybe upload only thumb_image - thanks, future me ;)

        final StorageReference profileImageFirebasePath = imageStorage.child("profile_images").child(Authentication.getCurrentUserUid() + ".jpg");
        final StorageReference thumbImageFirebasePath = imageStorage.child("profile_images").child("thumbs").child(Authentication.getCurrentUserUid() + ".jpg");

        profileImageFirebasePath.putFile(profileFileUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "upload image to firestore: success");
                            // have to get the downloadUrl of a file this way - API has changed
                            profileImageFirebasePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();

                                    FirebaseDatabaseManager.getCurrentUserDatabase().child("image").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "set image url to user: success");
                                                    } else {
                                                        Log.w(TAG, "set image url to user: failure: ");
                                                    }
                                                }
                                            });
                                }
                            });
                        } else {
                            Log.w(TAG, "upload image to firestore: failure");
                        }
                    }
                });

        Bitmap thumbBitmap = null;
        try {
            thumbBitmap = new Compressor(App.getAppContext())
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(profileImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] thumbByteArray = baos.toByteArray();

        // upload compressed thumb
        UploadTask uploadTask = thumbImageFirebasePath.putBytes(thumbByteArray);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "upload thumb_image to firestore: success");
                    thumbImageFirebasePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            FirebaseDatabaseManager.setUserThumbImageUrl(downloadUrl, new FirebaseFirestoreManagerCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "set thumb_image url to user: success");
                                    callback.onSuccess();
                                }

                                @Override
                                public void onFailure() {
                                    callback.onFailure();
                                    Log.w(TAG, "set thumb_image url to user: failure");
                                }
                            });
                        }
                    });
                } else {
                    Log.w(TAG, "upload thumb_image to firestore: failure");
                }
            }
        });
    }

}
