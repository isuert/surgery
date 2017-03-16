package com.github.isuert.surgery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.Slider;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.github.isuert.surgery.models.GlassConfig;
import com.github.isuert.surgery.models.Operation;
import com.github.isuert.surgery.models.Patient;
import com.github.isuert.surgery.models.Surgeon;
import com.github.isuert.surgery.models.Test;
import com.github.isuert.surgery.models.Xray;
import com.github.isuert.surgery.services.UploadService;
import com.github.isuert.surgery.webapi.WebApi;
import com.github.isuert.surgery.webapi.WebApiCreator;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int TAKE_NOTES_REQUEST = 1;
    List<Test> tests;
    List<Xray> xrays;
    private WebApi webApi;
    private String deviceId;
    private GlassConfig glassConfig;
    private View view;
    private Slider.Indeterminate indeterminate;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        webApi = WebApiCreator.create();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        view = new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED)
                .setText("Getting configured..." +
                        "\nDevice: " + deviceId)
                .getView();
        indeterminate = Slider.from(view).startIndeterminate();

        setContentView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glassConfig == null) {
            getConfigured();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case R.id.show_xrays:
                    showXrays();
                    break;
                case R.id.show_tests:
                    showTests();
                    break;
                case R.id.take_notes:
                    takeNotes();
                    break;
                case R.id.take_a_picture:
                    takeAPicture();
                    break;
                case R.id.get_configured:
                    getConfigured();
                    break;
                default:
                    return true;
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_NOTES_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = results.get(0);

            Intent intent = new Intent(this, UploadService.class);
            intent.setAction(UploadService.ACTION_SAVE_NOTE);
            intent.putExtra("text", text);
            intent.putExtra("operationId", glassConfig.getOperation().getId());
            startService(intent);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showXrays() {
        Intent intent = new Intent(this, XraysActivity.class);
        intent.putExtra("glassConfig", Parcels.wrap(glassConfig));
        intent.putExtra("results", Parcels.wrap(xrays));
        startActivity(intent);
    }

    private void showTests() {
        Intent intent = new Intent(this, TestsActivity.class);
        intent.putExtra("glassConfig", Parcels.wrap(glassConfig));
        intent.putExtra("results", Parcels.wrap(tests));
        startActivity(intent);
    }

    private void takeNotes() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, TAKE_NOTES_REQUEST);
    }

    private void takeAPicture() {
        Intent intent = new Intent(this, TakePicturesActivity.class);
        intent.putExtra("glassConfig", Parcels.wrap(glassConfig));
        startActivity(intent);
    }

    private void getConfigured() {
        indeterminate.show();
        webApi.getGlassConfig(deviceId).enqueue(new Callback<GlassConfig>() {
            @Override
            public void onResponse(Call<GlassConfig> call, Response<GlassConfig> response) {
                glassConfig = response.body();

                Operation operation = glassConfig.getOperation();
                Surgeon surgeon = glassConfig.getSurgeon();
                Patient patient = glassConfig.getPatient();

                view = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT_FIXED)
                        .setText("Operation: " + operation.getName() +
                                "\nPatient: " + patient.getName() + " " + patient.getSurname() +
                                "\nSurgeon: " + surgeon.getName() + " " + surgeon.getSurname() +
                                "\nDevice: " + deviceId)
                        .getView();

                setContentView(view);
                indeterminate.hide();
                getXrays();
                getTests();
            }

            @Override
            public void onFailure(Call<GlassConfig> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.alert_error,
                        Toast.LENGTH_LONG).show();
                indeterminate.hide();
            }
        });
    }

    private void getXrays() {
        indeterminate.show();
        webApi.getXrays(glassConfig.getOperation().getId()).enqueue(new Callback<List<Xray>>() {
            @Override
            public void onResponse(Call<List<Xray>> call, Response<List<Xray>> response) {
                xrays = response.body();
                new GetXrayBitmapsAsyncTask(MainActivity.this, xrays, indeterminate).execute();
            }

            @Override
            public void onFailure(Call<List<Xray>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.alert_error,
                        Toast.LENGTH_LONG).show();
                indeterminate.hide();
            }
        });
    }

    private void getTests() {
        indeterminate.show();
        webApi.getTests(glassConfig.getOperation().getId()).enqueue(new Callback<List<Test>>() {
            @Override
            public void onResponse(Call<List<Test>> call, Response<List<Test>> response) {
                tests = response.body();
                indeterminate.hide();
            }

            @Override
            public void onFailure(Call<List<Test>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.alert_error,
                        Toast.LENGTH_LONG).show();
                indeterminate.hide();
            }
        });
    }

    private static class GetXrayBitmapsAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Context context;
        protected List<Xray> xrays;
        protected Slider.Indeterminate indeterminate;
        protected File dir = new File(Environment.getExternalStorageDirectory(), "Surgery");

        public GetXrayBitmapsAsyncTask(Context context, List<Xray> xrays, Slider.Indeterminate indeterminate) {
            this.context = context;
            this.xrays = xrays;
            this.indeterminate = indeterminate;
        }

        @Override
        protected void onPreExecute() {
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (File file : dir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (Xray xray : xrays) {
                try {
                    File file = new File(dir.getPath() + File.separator
                            + "IMG_" + xray.getId() + ".jpg");
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    Bitmap bitmap = Picasso.with(context)
                            .load(xray.getImage())
                            .get();

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            indeterminate.hide();
        }
    }
}
