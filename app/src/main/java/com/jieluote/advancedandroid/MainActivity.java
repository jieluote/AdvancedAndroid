package com.jieluote.advancedandroid;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jieluote.androidpluginlib.PluginManager;
import com.jieluote.androidpluginlib.ProxyActivity;
import com.jieluote.annotationlib.saveFileAnnotation;
import com.jieluote.asmlib.TrackMethod;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * APT: 在编译期间指定注解处理器,可以在编译期间执行一些逻辑,比如生成java文件(可借助javapoet),详见 AnnotationProcessor
 * Transform: Android官方提供的构建插件,在class被打包为dex前的间隙,提供修改class的机会(DVM不能直接以流的方式读取class文件,JVM则可以),详见 ASMTransform
 * ASM: ASM是一个Java字节码修改工具(类库),可直接修改class文件(读取->修改->写回), 详见 ASMClassVisitor、ASMMethodVisitor
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    public static final String PATH = Environment.getExternalStorageDirectory().getPath();
    private static final int REQUEST_CODE = 100;
    private static String[] REQUEST_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        demo1_testAPT();

        //2.asm test
        demo2_testASM();

        //3.plugin proxy test
        demo3_testProxyPlugin();
    }

    /**
     * 测试APT方法
     * 添加了saveFileAnnotation注解的方法,无需调用,在编译期就会自动生成一个.java文件
     * 路径为 \app\build\generated\ap_generated_sources\debug\out\com\jieluote\advancedandroid
     * (具体逻辑见 AnnotationProcessor})
     */
    @saveFileAnnotation()
    public void demo1_testAPT(){
        Log.d(TAG,"run aptMethod");
    }

    /***
     * 测试asm方法
     * 添加了TrackMethod注解的方法,当运行时会自动在方法前后各打印一条日志
     */
    @TrackMethod(parameter = "aopMethodParam")
    public void demo2_testASM() {
        Log.d(TAG,"run asmMethod");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插件化-动态代理方式加载
     */
    private void demo3_testProxyPlugin() {
        if(PluginManager.getInstance().checkHasPlugin(this)){
            PluginManager.getInstance().loadApk(PluginManager.getInstance().getAPKPath(this),this);
            //跳转到代理Activity
            Intent intent = new Intent(MainActivity.this, ProxyActivity.class);
            intent.putExtra("className","com.jieluote.androidpluginapk.PluginActivity");
            startActivity(intent);
        }
    }
}