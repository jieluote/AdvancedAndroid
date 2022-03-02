package com.jieluote.androidpluginlib.hook;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class InstrumentationProxy extends Instrumentation {
    private static final String TAG = InstrumentationProxy.class.getSimpleName();
    //持有代理对象的引用
    private Instrumentation mInstrumentation;
    private PackageManager mPackageManager;


    public InstrumentationProxy(Instrumentation instrumentation, PackageManager packageManager) {
        this.mInstrumentation = instrumentation;
        this.mPackageManager = packageManager;
    }

    /**
     * step1.替换为本地已经注册过的占坑StubActivity,以绕过AMS验证
     */
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                            Intent intent, int requestCode, Bundle options) {
        Log.d(TAG, "execStartActivity run, this:" + this.hashCode());
        List<ResolveInfo> resolveInfo = mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        //判断启动的插件Activity是否在AndroidManifest.xml中注册过
        if (null == resolveInfo || resolveInfo.size() == 0) {
            Log.d(TAG, "resolveInfo == null");
            //保存目标插件
            intent.putExtra("hookname", intent.getComponent().getClassName());
            //设置为占坑Activity
            intent.setClassName(who, "com.jieluote.advancedandroid.StubActivity");
        }
        try {
            //利用反射获取到execStartActivity方法
            Method execStartActivity = Instrumentation.class.getDeclaredMethod("execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            return (ActivityResult) execStartActivity.invoke(mInstrumentation, who, contextThread, token, target, intent, requestCode, options);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * step2.绕过AMS验证后再替换为插件Activity启动
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Log.d(TAG, "newActivity run, this:" + this.hashCode());
        //取出插件Activity的包名
        String intentName = intent.getStringExtra("hookname");
        //如果之前设置了hookname,那就是需要启动的插件Activity,就走替换,否则走原逻辑
        if (!TextUtils.isEmpty(intentName)) {
            Log.d(TAG, "intentName:" + intentName);
            return super.newActivity(cl, intentName, intent);
        }
        return super.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        super.callActivityOnCreate(activity, icicle);
        Log.d(TAG, "callActivityOnCreate run, this:" + this.hashCode());
    }


}
