package com.github.isuert.surgery;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.github.isuert.surgery.models.Test;
import com.github.isuert.surgery.models.TestResult;

public class TestsActivity extends ResultsActivity<Test> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareCards();
    }

    protected void showOnDisplay(int displayIndex) {
        String displayId = glassConfig.getDisplays().get(displayIndex).getId();
        int testIndex = cardScrollView.getSelectedItemPosition();
        int testId = results.get(testIndex).getId();

        indeterminate.show();
        webApi.showTestOnDisplay(displayId, testId).enqueue(new Callback<Void>() {
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
        for (int i = 0; i < results.size(); i++) {
            Test test = results.get(i);
            List<TestResult> results = test.getResults();
            String text = "";
            for (int j = 0; j < results.size(); j++) {
                TestResult result = results.get(j);
                text += result.getName() + ": "
                        + result.getValue() + " "
                        + result.getUnit() + "\n";
            }

            CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED)
                    .setText(text)
                    .setFootnote(test.getType());

            cards.add(card);
        }
        adapter.notifyDataSetChanged();
    }
}
