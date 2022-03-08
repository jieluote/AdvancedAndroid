package com.jieluote.advancedandroid;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jieluote.androidpluginlib.PluginManager;
import com.jieluote.androidpluginlib.proxy.PluginConstants;
import com.jieluote.androidpluginlib.proxy.ProxyActivity;
import com.jieluote.annotationlib.saveFileAnnotation;
import com.jieluote.asmlib.TrackMethod;
import com.jieluote.spilib.IAndroidLanguage;
import com.jieluote.spilib.IWebLanguage;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import java.util.ServiceLoader;

/**
 * APT: 在编译期间指定注解处理器,可以在编译期间执行一些逻辑,比如生成java文件(可借助javapoet),详见 AnnotationProcessor 类
 * SPI: Service Provider Interface,是一种服务发现机制。它通过在ClassPath路径下的META-INF/services文件夹查找文件,自动加载文件里所定义的类,配合使用的一般有ServiceLoader类,AutoService注解
 * Transform: Android官方提供的构建插件,在class被打包为dex前的间隙,提供修改class的机会(DVM不能直接以流的方式读取class文件,JVM则可以),详见 ASMTransform 类
 * ASM: ASM是一个Java字节码修改工具(类库),可直接修改class文件(读取->修改->写回), 详见 ASMClassVisitor、ASMMethodVisitor 类
 * 插件化: 宿主APK加载另外一个插件APK(项目)中的资源(dex、resources等),目的避免APK过大、频繁升级、团队协作门槛高等问题,详见 androidpluginlib 库
 * Xposed:被誉为Hook之王,由框架和模块(自己实现的APP)两部分组成,可以用于Java层的任意Hook(也可替换资源、布局等),不过前提需要root,因为需要替换/system/bin/app_process等系统文件，详见 XposedInit 类
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    public static final String PATH = Environment.getExternalStorageDirectory().getPath();
    private TextView mTv;
    private static final int REQUEST_CODE = 100;
    private static String[] REQUEST_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = findViewById(R.id.activity_tv);
        requestPermion();
    }

    public void requestPermion() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, REQUEST_CODE);
                } else {
                    startBusiness();
                }
            } else {
                startBusiness();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "requestPermion:" + e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // SDK权限处理结果
        if (requestCode == REQUEST_CODE) {
            // 申请权限被拒绝，则退出程序。
            if (grantResults == null || grantResults.length == 0) {
                Log.d(TAG, "permissions denied");
                return;
            }
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissions denied");
            } else {
                Log.d(TAG, "permissions granted");
                startBusiness();
            }
        }
    }

    private void startBusiness() {
        //1.apt test
        //demo1_testAPT();

        //2.spi test
        //demo1_testSPI();

        //3.asm test
        //demo2_testASM();

        //4.plugin proxy test
        //demo3_testProxyPlugin();

        //5.plugin hook test
        //demo4_testHookPlugin();

        //6.xposed hook test
        //demo5_testXposed();
    }

    /**
     * SPI步骤:
     * 1.定义一个接口文件 ILanguage
     * 2.写出多个该接口文件的实现 Java、Kotlin
     * 3.在src/main/resources/下建立 /META-INF/services 目录,新增一个以接口命名的文件,内容是接口实现类全路径
     * 4.使用java.util.ServiceLoader 类获此接口的所有实现类(最终由Class.forName创建)
     *
     * AutoService:
     * Google提供的开源库,自动生成目录,不必手动添加(注意 Java、Kotlin和 JavaScript 的区别)
     *
     * 编译后,
     * 手动方式 接口文件文件位于     lib\build\resources\main\META-INF\services
     * AutoService方式 接口文件位于 lib\build\classes\java\main\META-INF\services
     */
    private void demo1_testSPI() {
        Log.d(TAG,"run demo1_testSPI");
        //获取接口实现类(手动方式)
        ServiceLoader<IAndroidLanguage> androidLanguages = ServiceLoader.load(IAndroidLanguage.class);
        for (IAndroidLanguage language : androidLanguages) {
            Log.d(TAG,"android language:"+ language.name());
        }
        //获取接口实现类(AutoService方式)
        ServiceLoader<IWebLanguage> webLanguages = ServiceLoader.load(IWebLanguage.class);
        for (IWebLanguage language : webLanguages) {
            Log.d(TAG,"web language:"+ language.name());
        }
    }

    /**
     * 测试APT方法(此方法空实现,只是一个功能说明)
     * 添加了saveFileAnnotation注解的方法,无需调用,在编译期就会自动生成一个.java文件
     * 路径为 \app\build\generated\ap_generated_sources\debug\out\com\jieluote\advancedandroid
     * (具体逻辑见 AnnotationProcessor})
     */
    @saveFileAnnotation()
    public void demo1_testAPT(){
        Log.d(TAG,"run demo1_testAPT");
    }

    /***
     * 测试asm方法
     * 添加了TrackMethod注解的方法,当运行时会自动在方法前后各打印一条日志
     */
    @TrackMethod(parameter = "aopMethodParam")
    public void demo2_testASM() {
        Log.d(TAG,"run demo2_testASM");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插件化-静态代理方式加载
     */
    private void demo3_testProxyPlugin() {
        if(PluginManager.getInstance().checkHasPlugin(this)){
            Log.d(TAG,"run demo3_testProxyPlugin");
            PluginManager.getInstance().loadApkMultiDexClass(this);
            //跳转到代理Activity
            Intent intent = new Intent(this, ProxyActivity.class);
            intent.putExtra("className","com.jieluote.androidpluginapk.PluginActivity");
            startActivity(intent);
        }
    }

    /**
     * 插件化-Hook Instrumentation方式加载
     */
    private void demo4_testHookPlugin() {
        if (PluginManager.getInstance().checkHasPlugin(this)) {
            Log.d(TAG, "run demo4_testHookPlugin");
            try {
                //加载APK中的资源,目的是为了解决资源冲突(不是必须)
                PluginManager.getInstance().loadApkMultiDexClass(this);
                //融合插件和宿主的dex为同一个,使操作插件中的类就和操作本地的类一样(必须)
                PluginManager.getInstance().loadApkSingleDexClass(this);
                //进行Hook
                PluginManager.getInstance().hookInstrumentation(this);
            } catch (Exception e) {
                Log.d(TAG, "demo4 hook Exception " + e);
                e.printStackTrace();
            }
            Intent intent = new Intent();
            intent.setClassName("com.jieluote.androidpluginapk", "com.jieluote.androidpluginapk.PluginActivity");
            intent.putExtra(PluginConstants.RUN_MODE, PluginConstants.RUN_MODE_PLUGIN_HOOK);
            startActivity(intent);
        }
    }

    /**
     * Hook技术-Xposed,用来魔改任何应用java层代码(此方法空实现,只是一个功能说明)
     * Xposed 官网：http://repo.xposed.info/
     * Xposed 项目 github 地址：https://github.com/rovo89
     * Xposed 官方教程 :https://github.com/rovo89/XposedBridge/wiki/Development-tutorial
     * Xposed Api 之XposedBridge.jar 下载:https://jcenter.bintray.com/de/robv/android/xposed/api/
     * ————————————————————————————————————————————————
     *
     * Xposed使用前提 :
     *   1.设备Root
     *   2.系统版本7.0及以下(对ART支持不好)
     *   3.需要安装 Xposed Installer APK(Xposed框架),用于下载刷机包,并替换zygote进程等文件(也可以自己实现，参考https://www.jianshu.com/p/6b4a80654d4e)
     * 这些要求比较麻烦,我是直接用的夜神模拟器 https://support.yeshen.com/zh-CN/qt/xp
     * Xposed使用步骤:
     *   1.在app下的AndroidManifest.xml中添加meta-data信息,这样可以让框架识别出你的应用是一个Xposed模块,从而具有Hook能力
     *   2.compileOnly 'de.robv.android.xposed:api:82' (注意不能是implementation,否则报错),相当于引入XposedBridgeAPI
     *   3.新建类实现 IXposedHookLoadPackage,重写handleLoadPackage方法,在里面实现Hook逻辑
     *   4.assets下新建名为"xposed_init"的文本文件,里面为类的全类名,目的让Xposed模块找到入口,用来启动Hook程序
     *   2-4见xposedlib
     *   备注: 如果运行有什么问题,可以在logcat中查看"Xposed"日志
     */
    private void demo5_testXposed() {
        Log.d(TAG, "run demo5_testXposed");
    }
}