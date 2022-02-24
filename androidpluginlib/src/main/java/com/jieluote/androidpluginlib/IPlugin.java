package com.jieluote.androidpluginlib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public interface IPlugin {
    void attach(Activity activity);
    void onCreate(Bundle bundle);
    void onStart();
    void onRestart();
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onResume();
    void onPause();
    void onStop();
    void onDestroy();
}
