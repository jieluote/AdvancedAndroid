package com.jieluote.androidpluginapk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.jieluote.androidpluginlib.IPlugin;
import static com.jieluote.androidpluginlib.PluginConstants.RUN_MODE;
import static com.jieluote.androidpluginlib.PluginConstants.RUN_MODE_APP;

public class PluginActivity extends Activity implements IPlugin {
    private static final String TAG = PluginActivity.class.getSimpleName();
    private Activity mActivity;
    private int runMode = RUN_MODE_APP;

    public boolean isAppMode() {
        return runMode == RUN_MODE_APP;
    }

    @Override
    public void attach(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate");
        if (bundle != null) {
            runMode = bundle.getInt(RUN_MODE);
        }
        if (isAppMode()) {
            super.onCreate(bundle);
            setContentView(R.layout.plugin_activity_layout);
            mActivity = this;
        } else {
            try {
                mActivity.setContentView(R.layout.plugin_activity_layout);
            } catch (Resources.NotFoundException e) {
                Log.d(TAG, "NotFoundException:" + e);
            }
        }
    }

    @Override
    public void onStart() {
        if (isAppMode()) {
            super.onStart();
        }
        Log.d(TAG, "onStart");
    }

    @Override
    public void onRestart() {
        if (isAppMode()) {
            super.onRestart();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onResume() {
        if (isAppMode()) {
            super.onResume();
        }
        Log.d(TAG, "onResume");
        Toast.makeText(mActivity, "plugin activity onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        if (isAppMode()) {
            super.onPause();
        }
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        if (isAppMode()) {
            super.onStop();
        }
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        if (isAppMode()) {
            super.onDestroy();
        }
        Log.d(TAG, "onDestroy");
    }
}
