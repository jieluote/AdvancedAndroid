package com.jieluote.androidpluginlib;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.jieluote.androidpluginlib.hook.InstrumentationProxy;
import com.jieluote.androidpluginlib.proxy.PluginApkInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class PluginManager {
    private static final String TAG = PluginManager.class.getSimpleName();
    private static final PluginManager instance = new PluginManager();
    private static final String FILE_NAME_APK = "plugin.apk";
    //private static final String FILE_NAME_DEX = "plugin.dex"; //也可加载dex,但是因为没有资源文件,运行UI类相关会报错。适合工具类加载
    private String fileName = FILE_NAME_APK;

    private PluginApkInfo mPluginApkInfo;

    public static PluginManager getInstance() {
        return instance;
    }

    private PluginManager() {
    }

    public PluginApkInfo getPluginApk(){
        return mPluginApkInfo;
    }

    /**
     * 加载apk
     * @param context
     */
    public void loadApkMultiDexClass(Context context) {
        String apkPath = getPluginAPKPath(context);
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
        mPluginApkInfo = new PluginApkInfo(packageInfo, resources, dexClassLoader);
        Log.d(TAG, "mPluginApk:" + mPluginApkInfo+",packageName:"+packageInfo.packageName);
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
            //AssetManager维护了资源包路径的数组
            //这里将插件的资源路径添加到AssetManager的资源路径数组中(通过反射隐藏方法addAssetPath) 从而实现插件资源的加载。
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
        String path = getPluginAPKPath(context);
        if (path.equals("") || !new File(path).exists()) {
            return copyAPKToCache(context);
        } else {
            return true;
        }
    }

    public String getPluginAPKPath(Context context) {
        String path = "";
        File cachedDir = context.getCacheDir();
        if (cachedDir.exists()) {
            File apkFile = new File(cachedDir, fileName);
            path = apkFile.getAbsolutePath();
            return path;
        }
        Log.d(TAG,"getPluginAPKPath:"+path);
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

    public void loadApkSingleDexClass(Context context) throws Exception {
        // 获取 pathList
        Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);

        // 获取 dexElements
        Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
        Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        // 获取宿主的Elements
        PathClassLoader hostDexClassLoader = (PathClassLoader) context.getClassLoader();
        Object hostPathListObject = pathListField.get(hostDexClassLoader);
        Object[] hostElements = (Object[]) dexElementsField.get(hostPathListObject);

        // 获取插件的Elements
        DexClassLoader pluginDexClassLoader = createDexClassLoader(getPluginAPKPath(context), context);
        Object pluginPathListObject = pathListField.get(pluginDexClassLoader);
        Object[] pluginElements = (Object[]) dexElementsField.get(pluginPathListObject);

        //生成新的数组(注意不能直接new,否则类型不匹配)
        Object[] newElements = (Object[]) Array.newInstance(pluginElements.getClass().getComponentType(), hostElements.length + pluginElements.length);

        // 给新数组赋值
        // 先用宿主的，再用插件的
        System.arraycopy(hostElements, 0, newElements, 0, hostElements.length);
        System.arraycopy(pluginElements, 0, newElements, hostElements.length, pluginElements.length);
        // 将生成的数组赋给 "dexElements" 属性
        dexElementsField.set(hostPathListObject, newElements);
    }

    /**
     * Hook两种方式启动Activity下的Instrumentation
     * @throws Exception
     */
    public void hookInstrumentation(Activity activity) throws Exception {
        //Hook ActivityThread的mInstrumentation变量,适用使用Application启动的场景
        //Application : startActivity --> ContextWrapper --> ContextImpl --> ActivityThread.getInstrumentation.execStartActivity
        Class<?> activityThread = null;
        activityThread = Class.forName("android.app.ActivityThread");
        Method sCurrentActivityThread = activityThread.getDeclaredMethod("currentActivityThread");
        sCurrentActivityThread.setAccessible(true);
        //获取ActivityThread 对象
        Object activityThreadObject = sCurrentActivityThread.invoke(activityThread);

        Field instrumentationFiled = activityThread.getDeclaredField("mInstrumentation");
        instrumentationFiled.setAccessible(true);
        Instrumentation instrumentation = (Instrumentation) instrumentationFiled.get(activityThreadObject);
        InstrumentationProxy instrumentationProxy = new InstrumentationProxy(instrumentation,activity.getPackageManager());
        instrumentationFiled.set(activityThreadObject, instrumentationProxy);

        // Hook Activity的mInstrumentation变量,适用使用Activity启动的场景
        // Activity : startActivity --> mInstrumentation.execStartActivity
        Field field = Activity.class.getDeclaredField("mInstrumentation");
        field.setAccessible(true);
        // 得到Activity中的Instrumentation对象
        Instrumentation instrumentationFromActivity = (Instrumentation) field.get(activity);
        // 创建InstrumentationProxy对象来代理Instrumentation对象
        InstrumentationProxy instrumentationProxyFromActivity = new InstrumentationProxy(instrumentationFromActivity,activity.getPackageManager());
        // 用代理去替换Activity中的Instrumentation对象
        field.set(activity, instrumentationProxyFromActivity);
    }
}
