package com.github.isuert.surgery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.github.isuert.surgery.models.Xray;

public class XraysActivity extends ResultsActivity<Xray> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareCards();
    }

    protected void showOnDisplay(int displayIndex) {
        String displayId = glassConfig.getDisplays().get(displayIndex).getId();
        int xrayIndex = cardScrollView.getSelectedItemPosition();
        int xrayId = results.get(xrayIndex).getId();

        indeterminate.show();
        webApi.showXrayOnDisplay(displayId, xrayId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                indeterminate.hide();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.alert_error,
                        Toast.LENGTH_LONG).show();
                indeterminate.hide();
            }
        });
    }

    private void prepareCards() {
        for (Xray xray : results) {
            File dir = new File(Environment.getExternalStorageDirectory(), "Surgery");
            File file = new File(dir.getPath() + File.separator
                    + "IMG_" + xray.getId() + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

            CardBuilder card = new CardBuilder(this, CardBuilder.Layout.CAPTION)
                    .addImage(bitmap)
                    .setFootnote(xray.getName());

            cards.add(card);
        }

        adapter.notifyDataSetChanged();
    }
}
