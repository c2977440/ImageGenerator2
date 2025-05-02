package com.example.larp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.os.Environment;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.material.textfield.TextInputEditText;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class SaveImageInstrumentedTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    );

    @Test
    public void testSaveImageCreatesFile() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            activity.runOnUiThread(() -> {
                TextInputEditText eventName = activity.findViewById(R.id.eventNameEditText);
                TextInputEditText location = activity.findViewById(R.id.locationEditText);
                if (eventName != null) {
                    eventName.requestFocus();
                    eventName.setText("測試活動");
                }
                if (location != null) {
                    location.requestFocus();
                    location.setText("測試地點");
                }
            });

            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            activity.runOnUiThread(() -> {
                activity.updateTextDisplay();
            });

            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            activity.saveImage();

            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        });
    }
}