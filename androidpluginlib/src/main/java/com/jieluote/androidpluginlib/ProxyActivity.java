package com.jieluote.androidpluginlib;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * 代理Activity,会去调用插件Activity的所有方法(壳子是代理的,内容是插件的)
 */
public class ProxyActivity extends Activity {
    private static final String TAG = ProxyActivity.class.getSimpleName();
    private String mClassName;
    private PluginApk mPluginApk;
    private IPlugin mIPluginActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClassName = getIntent().getStringExtra("className");
        mPluginApk = PluginManager.getInstance().getPluginApk();
        launchPluginActivity();
    }

    private void launchPluginActivity() {
        if (mPluginApk == null) {
            Log.d(TAG, "not find plugin apk");
            finish();
            return;
        }
        try {
            //clazz 就是插件Activity的实例对象,但是该对象没有生命周期,没有上下文环境,因此在生命周期方法内不能直接调用super或者this
            Class<?> clazz = mPluginApk.mDexClassLoader.loadClass(mClassName); //重点
            Object object = clazz.newInstance();
            if (object instanceof IPlugin) {
                mIPluginActivity = (IPlugin) object;
                mIPluginActivity.attach(this);
                Bundle bundle = new Bundle();
                bundle.putInt(PluginConstants.RUN_MODE, PluginConstants.RUN_MODE_PLUGIN);
                mIPluginActivity.onCreate(bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        if(mPluginApk != null) {
            return mPluginApk.mResources;
        } else {
            return super.getResources();
        }
    }

    @Override
    public AssetManager getAssets() {
        if(mPluginApk != null) {
            return mPluginApk.mAssetManager;
        }else {
            return super.getAssets();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if(mPluginApk != null) {
            return mPluginApk.mDexClassLoader;
        }else {
            return super.getClassLoader();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mIPluginActivity != null){
            mIPluginActivity.onResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mIPluginActivity != null){
            mIPluginActivity.onStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIPluginActivity != null){
            mIPluginActivity.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIPluginActivity != null){
            mIPluginActivity.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mIPluginActivity != null){
            mIPluginActivity.onDestroy();
        }
    }
}
