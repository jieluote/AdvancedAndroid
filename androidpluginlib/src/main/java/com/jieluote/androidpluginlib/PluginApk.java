package com.jieluote.androidpluginlib;

import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import dalvik.system.DexClassLoader;

/**
 * APk 解析加载信息类
 */
public class PluginApk {
    public PackageInfo mPackageInfo;
    public Resources mResources;
    public AssetManager mAssetManager;
    public DexClassLoader mDexClassLoader;

    public PluginApk(PackageInfo packageInfo, Resources resources,DexClassLoader dexClassLoader) {
        mPackageInfo = packageInfo;
        mResources = resources;
        mAssetManager = resources.getAssets();
        mDexClassLoader = dexClassLoader;
    }
}
