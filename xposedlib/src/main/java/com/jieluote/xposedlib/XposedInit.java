package com.jieluote.xposedlib;

import android.util.Log;
import android.widget.TextView;
import java.lang.reflect.Field;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d("xposedtest", "run handleLoadPackage!");
        if (lpparam.packageName.equals("com.jieluote.advancedandroid")) {
            XposedHelpers.findAndHookMethod("com.jieluote.advancedandroid.MainActivity", lpparam.classLoader, "demo5_testXposed", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    //不能通过Class.forName()来获取Class ，在跨应用时会失效
                    Class c = lpparam.classLoader.loadClass("com.jieluote.advancedandroid.MainActivity");
                    //获取到textView,并更改其文案
                    Field field = c.getDeclaredField("mTv");
                    field.setAccessible(true);
                    //param.thisObject 为执行该方法的对象，在这里指MainActivity,
                    TextView textView = (TextView) field.get(param.thisObject);
                    textView.setText("Xposed run!");
                }
            });
        }
        Log.d("xposedtest", "finished handleLoadPackage!");
    }
}
