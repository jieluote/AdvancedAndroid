package com.jieluote.androidpluginlib.proxy;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import com.jieluote.androidpluginlib.PluginManager;
import androidx.annotation.Nullable;

/**
 * 代理Activity,会去调用插件Activity的所有方法(壳子是代理的,内容是插件的)
 */
public class ProxyActivity extends Activity {
    private static final String TAG = ProxyActivity.class.getSimpleName();
    private String mClassName;
    private PluginApkInfo mPluginApkInfo;
    private IPlugin mIPluginActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ProxyActivity onCreate");
        mClassName = getIntent().getStringExtra("className");
        mPluginApkInfo = PluginManager.getInstance().getPluginApk();
        launchPluginActivity();
    }

    private void launchPluginActivity() {
        if (mPluginApkInfo == null) {
            Log.d(TAG, "not find plugin apk");
            finish();
            return;
        }
        try {
            //clazz 就是插件Activity的实例对象,但是该对象没有生命周期,没有上下文环境,因此在生命周期方法内不能直接调用super或者this
            Class<?> clazz = mPluginApkInfo.mDexClassLoader.loadClass(mClassName); //重点
            Object object = clazz.newInstance();
            if (object instanceof IPlugin) {
                mIPluginActivity = (IPlugin) object;
                mIPluginActivity.attach(this);
                Bundle bundle = new Bundle();
                bundle.putInt(PluginConstants.RUN_MODE, PluginConstants.RUN_MODE_PLUGIN_PROXY);
                mIPluginActivity.onCreate(bundle);
            }
        } catch (Exception e) {
            Log.d(TAG, "launchPluginActivity Exception:"+e);
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        if(mPluginApkInfo != null) {
            Log.d(TAG,"getResources run");
            return mPluginApkInfo.mResources;
        } else {
            Log.d(TAG,"getResources mPluginApkInfo == null");
            return super.getResources();
        }
    }

    @Override
    public AssetManager getAssets() {
        if(mPluginApkInfo != null) {
            return mPluginApkInfo.mAssetManager;
        }else {
            return super.getAssets();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if(mPluginApkInfo != null) {
            return mPluginApkInfo.mDexClassLoader;
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
