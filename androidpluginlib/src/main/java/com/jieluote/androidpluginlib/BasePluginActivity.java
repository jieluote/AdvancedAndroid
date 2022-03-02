package com.jieluote.androidpluginlib;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.jieluote.androidpluginlib.proxy.IPlugin;
import com.jieluote.androidpluginlib.proxy.PluginApkInfo;
import static com.jieluote.androidpluginlib.proxy.PluginConstants.RUN_MODE;
import static com.jieluote.androidpluginlib.proxy.PluginConstants.RUN_MODE_APP;
import static com.jieluote.androidpluginlib.proxy.PluginConstants.RUN_MODE_PLUGIN_HOOK;
import static com.jieluote.androidpluginlib.proxy.PluginConstants.RUN_MODE_PLUGIN_PROXY;

public class BasePluginActivity extends Activity implements IPlugin {
    private static final String TAG = BasePluginActivity.class.getSimpleName();
    private Activity mActivity;
    private int runMode = RUN_MODE_APP;

    public Activity getPluginContext(){
        return mActivity;
    };

    public boolean isAppMode() {
        return runMode == RUN_MODE_APP;
    }

    public boolean isPluginProxyMode() {
        return runMode == RUN_MODE_PLUGIN_PROXY;
    }

    public boolean isPluginHookMode() {
        return runMode == RUN_MODE_PLUGIN_HOOK;
    }

    public boolean isPluginLifeMode() {
        return runMode == RUN_MODE_APP || runMode == RUN_MODE_PLUGIN_HOOK;
    }

    @Override
    public void attach(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle bundle) {
        if (bundle != null) {
            runMode = bundle.getInt(RUN_MODE);
        }
        if (getIntent() != null) {
            runMode = getIntent().getIntExtra(RUN_MODE, runMode);
        }
        Log.d(TAG, "onCreate,runMode:" + runMode);
        if (isAppMode()) {
            super.onCreate(bundle);
            mActivity = this;
        } else if (isPluginProxyMode()) {
            Log.d(TAG, "onCreate, activity:" + mActivity);
        } else if (isPluginHookMode()) {
            super.onCreate(bundle);
            mActivity = this;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        Log.d(TAG, "plugin activity xml id:" + layoutResID);
        if(isPluginLifeMode()) {
            super.setContentView(layoutResID);
        }else {
            Log.d(TAG, "setContentView, activity:" + mActivity);
            mActivity.setContentView(layoutResID);
        }
    }

    @Override
    public Resources getResources() {
        //重点！非独立运行的插件Activity,其资源默认直接从host读取(资源冲突),如果要读取插件自身的
        //要从插件APK中读取(类似于换肤原理)
        if(!isAppMode()){
            PluginApkInfo pluginApkInfo = PluginManager.getInstance().getPluginApk();
            return  pluginApkInfo.mResources;
        }
        return super.getResources();
    }

    @Override
    public <T extends View> T findViewById(int id) {
        //采用proxy方式的插件因为没有生命周期和上下文环境,所以不能直接调用findViewById,否则会报错
        //同理,其各生命周期方法中也不能调用super
        if(!isPluginLifeMode()) {
            return mActivity.findViewById(id);
        }
        return super.findViewById(id);
    }

    @Override
    public void onStart() {
        if (isPluginLifeMode()) {
            super.onStart();
        }
        Log.d(TAG, "onStart");
    }

    @Override
    public void onRestart() {
        if (isPluginLifeMode()) {
            super.onRestart();
        }
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onResume() {
        if (isPluginLifeMode()) {
            super.onResume();
        }
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        if (isPluginLifeMode()) {
            super.onPause();
        }
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        if (isPluginLifeMode()) {
            super.onStop();
        }
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        if (isPluginLifeMode()) {
            super.onDestroy();
        }
        Log.d(TAG, "onDestroy");
    }
}
