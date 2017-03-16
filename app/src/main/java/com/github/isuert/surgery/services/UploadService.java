package com.github.isuert.surgery.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.github.isuert.surgery.webapi.WebApi;
import com.github.isuert.surgery.webapi.WebApiCreator;

public class UploadService extends IntentService {
    private static final String TAG = "UploadService";
    public static final String ACTION_SAVE_NOTE = "com.github.isuert.surgery.services.action.SAVE_NOTE";
    public static final String ACTION_SAVE_PICTURE = "com.github.isuert.surgery.services.action.SAVE_PICTURE";

    private WebApi webApi;

    public UploadService() {
        super("UploadService");
        webApi = WebApiCreator.create();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_SAVE_NOTE)) {
            String text = intent.getStringExtra("text");
            int operationId = intent.getIntExtra("operationId", 0);
            saveNote(text, operationId);
        } else if (intent.getAction().equals(ACTION_SAVE_PICTURE)) {
            byte[] pictureData = intent.getByteArrayExtra("pictureData");
            int operationId = intent.getIntExtra("operationId", 0);
            savePicture(pictureData, operationId);
        }
    }

    private void saveNote(String text, int operationId) {
        try {
            webApi.saveNote(text, operationId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePicture(byte[] pictureData, int operationId) {
        try {
            File pictureFile = getOutputMediaFile();
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(pictureData);
            fos.close();

            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), pictureFile);
            MultipartBody.Part imageBodyPart = MultipartBody.Part.
                    createFormData("image", pictureFile.getName(), imageBody);
            RequestBody operationIdBody = RequestBody.create(MediaType.parse("text/plain"),
                    String.valueOf(operationId));

            webApi.savePicture(imageBodyPart, operationIdBody).execute();
            pictureFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Surgery");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}
