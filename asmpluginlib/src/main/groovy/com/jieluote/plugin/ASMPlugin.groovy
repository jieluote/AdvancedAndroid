package com.jieluote.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * 插件类,用groovy、java、kotlin实现均可
 * 步骤:
 * 1.新建groovy文件,实现plugin接口
 * 2.在src\main\resources\META-INF\gradle-plugins下新增pluginName.properties文件,内容为全类名
 * 3.在lib build中配置插件上传地址
 * 4.Gradle - Tasks - upload - uploadArchives,生成repo文件
 * 5.在root的build.gradle文件中class中引入上传地址
 * 6.app module build.gradle引入插件 apply pluginName
 * 参考 https://www.jianshu.com/p/dc558e14215e
 */
class ASMPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("***run ASMPlugin***");
        //与build.gradle中android{}一样，代码中由AppExtension类表示。
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        assert appExtension != null;
        appExtension.registerTransform(new ASMTransform(project));
    }
}
