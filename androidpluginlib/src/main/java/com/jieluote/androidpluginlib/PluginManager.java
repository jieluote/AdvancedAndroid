package com.jieluote.androidpluginlib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class PluginManager {
    private static final String TAG = PluginManager.class.getSimpleName();
    private static final PluginManager instance = new PluginManager();
    private static final String FILE_NAME_APK = "plugin.apk";
    //private static final String FILE_NAME_DEX = "plugin.dex"; //也可加载dex,但是因为没有资源文件,运行UI类相关会报错。适合工具类加载
    private String fileName = FILE_NAME_APK;

    private PluginApk mPluginApk;

    public static PluginManager getInstance() {
        return instance;
    }

    private PluginManager() {
    }

    public PluginApk getPluginApk(){
        return mPluginApk;
    }

    /**
     * 加载apk
     * @param apkPath
     * @param context
     */
    public void loadApk(String apkPath,Context context) {
        //创建 packageInfo,这里要注意,如果读取的是dex而非apk文件,因为不含清单文件,所以packInfo为null
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES
                        | PackageManager.GET_PROVIDERS);
        //创建 DexClassLoader
        DexClassLoader dexClassLoader = createDexClassLoader(apkPath, context);
        //创建 AssetManager
        AssetManager am = createAssetManager(apkPath, context);
        //创建 Resources
        Resources resources = createResources(am, context);
        mPluginApk = new PluginApk(packageInfo, resources, dexClassLoader);
        Log.d(TAG, "mPluginApk:" + mPluginApk);
    }

    /**
     * 获取到插件中的Resource
     */
    private Resources createResources(AssetManager am,Context context) {
        Resources resources = context.getResources();
        return new Resources(am,resources.getDisplayMetrics(),resources.getConfiguration());
    }

    /**
     * 获取插件的AssetManager
     */
    private AssetManager createAssetManager(String apkPath,Context context) {
        try {
            AssetManager am = AssetManager.class.newInstance();
            Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            method.invoke(am, apkPath);
            return am;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**'
     * 创建DexClassLoader 重点
     * DexClassLoader可加载外部的dex, pathClassLoader一般只能加载系统内部的dex
     */
    private DexClassLoader createDexClassLoader(String apkPath,Context context) {
        File file = context.getDir("dex", Context.MODE_PRIVATE);
        return new DexClassLoader(apkPath, file.getAbsolutePath(), null, context.getClassLoader());
    }

    /**
     * 是否存在插件
     * @param context
     * @return
     */
    public boolean checkHasPlugin(Context context) {
        String path = getAPKPath(context);
        if (path.equals("") || !new File(path).exists()) {
            return copyAPKToCache(context);
        } else {
            return true;
        }
    }

    public String getAPKPath(Context context) {
        String path = "";
        File cachedDir = context.getCacheDir();
        if (cachedDir.exists()) {
            File apkFile = new File(cachedDir, fileName);
            path = apkFile.getAbsolutePath();
            return path;
        }
        return path;
    }

    public boolean copyAPKToCache(Context context) {
        boolean result = false;
        File cachedDir = context.getCacheDir();
        if (!cachedDir.exists()) {
            cachedDir.mkdirs();
        }
        File outFile = new File(cachedDir, fileName);
        String path = outFile.getAbsolutePath();
        try {
            if (!outFile.exists()) {
                boolean success = outFile.createNewFile();
                if (success) {
                    //从外部(可以是网络、磁盘,这里为了简便放在了assets中)将apk文件读取到应用内部的cache目录里
                    InputStream is = context.getAssets().open(fileName);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    byte[] buffer = new byte[is.available()];
                    int byteCount = 0;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                    Log.d(TAG, "APK copy success path:" + path);
                    result = true;
                    return result;
                }
            } else {
                Log.d(TAG, "APK already exist, APK path:" + path);
                return result;
            }
        } catch (Exception e) {
            result = false;
            Log.e(TAG, "copyAPKToCache:" + e);
        }
        return result;
    }

}
