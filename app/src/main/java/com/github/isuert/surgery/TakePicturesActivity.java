package com.github.isuert.surgery;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.Slider;

import org.parceler.Parcels;

import com.github.isuert.surgery.models.GlassConfig;
import com.github.isuert.surgery.ui.CameraPreview;

public class TakePicturesActivity extends Activity {
    private static final String TAG = "TakePicturesActivity";

    private GlassConfig glassConfig;
    private Camera camera;
    private CameraPreview cameraPreview;
    private Slider.Indeterminate indeterminate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        glassConfig = Parcels.unwrap(getIntent().getParcelableExtra("glassConfig"));
        indeterminate = Slider.from(getWindow().getDecorView()).startIndeterminate();
        indeterminate.hide();

        openCamera();
        cameraPreview = new CameraPreview(this, camera);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameLayout.addView(cameraPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
        cameraPreview.setCamera(camera);
    }

    @Override
    protected void onPause() {
        cameraPreview.setCamera(null);
        releaseCamera();
        super.onPause();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.take_picture, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {

            switch (item.getItemId()) {
                case R.id.capture:
                    capture();
                    break;
                case R.id.go_back:
                    goBack();
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

    private void openCamera() {
        releaseCamera();

        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void capture() {
        indeterminate.show();
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                indeterminate.hide();

                Intent intent = new Intent(TakePicturesActivity.this, PictureActivity.class);
                intent.putExtra("pictureData", data);
                intent.putExtra("glassConfig", Parcels.wrap(glassConfig));
                startActivity(intent);
            }
        });
    }

    private void goBack() {
        finish();
    }
}
