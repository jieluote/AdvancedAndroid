package com.jieluote.androidpluginlib.proxy;

public class PluginConstants {
    public static final String RUN_MODE = "runMode";//运行模式
    public static final int RUN_MODE_APP = 1;      //做为独立APP运行
    public static final int RUN_MODE_PLUGIN_PROXY = 2;   //做为插件依附宿主APP运行-代理方式无生命周期
    public static final int RUN_MODE_PLUGIN_HOOK = 3;   //做为插件依附宿主APP运行-Hook方式有生命周期
}
