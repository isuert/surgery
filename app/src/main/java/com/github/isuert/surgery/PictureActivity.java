package com.github.isuert.surgery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.glass.view.WindowUtils;

import org.parceler.Parcels;

import com.github.isuert.surgery.models.GlassConfig;
import com.github.isuert.surgery.services.UploadService;

public class PictureActivity extends Activity {
    private static final String TAG = "PictureActivity";

    private GlassConfig glassConfig;
    private byte[] pictureData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_taken_picture);

        glassConfig = Parcels.unwrap(getIntent().getParcelableExtra("glassConfig"));
        pictureData = getIntent().getByteArrayExtra("pictureData");

        Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.picture, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {

            switch (item.getItemId()) {
                case R.id.accept:
                    accept();
                    break;
                case R.id.refuse:
                    discard();
                    break;
                default:
                    return true;
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void accept() {
        Intent intent = new Intent(this, UploadService.class);
        intent.setAction(UploadService.ACTION_SAVE_PICTURE);
        intent.putExtra("pictureData", pictureData);
        intent.putExtra("operationId", glassConfig.getOperation().getId());
        startService(intent);

        finish();
    }

    private void discard() {
        finish();
    }
}
