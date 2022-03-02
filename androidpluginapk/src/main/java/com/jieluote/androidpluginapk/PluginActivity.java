package com.jieluote.androidpluginapk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.jieluote.androidpluginlib.BasePluginActivity;
import com.jieluote.androidpluginlib.proxy.IPlugin;

/**
 * 插件Activity,只需要关注自身的逻辑即可,至于其它逻辑在宿主的BasePluginActivity控制
 */
public class PluginActivity extends BasePluginActivity implements IPlugin {
    private static final String TAG = PluginActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        View rootLy = findViewById(R.id.plugin_root_ly);
        rootLy.setBackgroundColor(getResources().getColor(R.color.back_color));
        TextView tv = findViewById(R.id.plugin_activity_tv);
        tv.setText(getResources().getString(R.string.activity_text));
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getPluginContext(), "plugin activity onResume", Toast.LENGTH_SHORT).show();
    }
}
