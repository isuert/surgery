package com.github.isuert.surgery;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;
import com.google.android.glass.widget.Slider.Indeterminate;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import com.github.isuert.surgery.adapters.ResultsCardScrollAdapter;
import com.github.isuert.surgery.models.Display;
import com.github.isuert.surgery.models.GlassConfig;
import com.github.isuert.surgery.webapi.WebApi;
import com.github.isuert.surgery.webapi.WebApiCreator;

public abstract class ResultsActivity<T> extends Activity {
    static final String TAG = "ResultsActivity";
    static final int DISPLAYS_MENU_GROUP_ID = 100;

    WebApi webApi;
    GlassConfig glassConfig;
    List<T> results;
    List<CardBuilder> cards;
    CardScrollView cardScrollView;
    ResultsCardScrollAdapter adapter;
    Indeterminate indeterminate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webApi = WebApiCreator.create();
        glassConfig = Parcels.unwrap(getIntent().getParcelableExtra("glassConfig"));
        results = Parcels.unwrap(getIntent().getParcelableExtra("results"));
        cards = new ArrayList<>();
        cardScrollView = new CardScrollView(this);
        adapter = new ResultsCardScrollAdapter(cards);
        cardScrollView.setAdapter(adapter);
        cardScrollView.activate();
        indeterminate = Slider.from(cardScrollView).startIndeterminate();

        setContentView(cardScrollView);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.results, menu);

            if (glassConfig.getDisplays() != null) {
                List<Display> displays = glassConfig.getDisplays();
                SubMenu subMenu = menu.findItem(R.id.show_on_display).getSubMenu();

                for (int i = 0; i < displays.size(); i++) {
                    subMenu.add(DISPLAYS_MENU_GROUP_ID, i, Menu.NONE, displays.get(i).getName());
                }
            }

            if (featureId == Window.FEATURE_OPTIONS_PANEL) {
                menu.findItem(R.id.next).setVisible(false);
                menu.findItem(R.id.previous).setVisible(false);
                menu.findItem(R.id.go_back).setVisible(false);
            }
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            boolean showPrevious = cardScrollView.getSelectedItemPosition() != 0;
            boolean showNext = cardScrollView.getSelectedItemPosition() != cards.size() - 1;

            menu.findItem(R.id.previous).setVisible(showPrevious);
            menu.findItem(R.id.next).setVisible(showNext);
            return true;
        }

        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {

            if (item.getGroupId() == DISPLAYS_MENU_GROUP_ID) {
                Log.d(TAG, "current time: " + System.currentTimeMillis());
                showOnDisplay(item.getItemId());
            } else {
                switch (item.getItemId()) {
                    case R.id.next:
                        next();
                        break;
                    case R.id.previous:
                        previous();
                        break;
                    case R.id.go_back:
                        goBack();
                        break;
                    default:
                        return true;
                }
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

    protected abstract void showOnDisplay(int displayIndex);

    private void next() {
        int nextPosition = cardScrollView.getSelectedItemPosition() + 1;
        cardScrollView.setSelection(nextPosition);
        invalidateOptionsMenu();
    }

    private void previous() {
        int previousPosition = cardScrollView.getSelectedItemPosition() - 1;
        cardScrollView.setSelection(previousPosition);
        invalidateOptionsMenu();
    }

    private void goBack() {
        finish();
    }
}
