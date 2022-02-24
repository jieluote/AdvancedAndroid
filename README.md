# AdvancedAndroid 进阶/高级Android

#### 介绍
主要是集合了一些Android较有深度的技术点demo,有助于我们对Android的理解更深入一层
目前包括(未来还会不断更新)
Demo1:Annotation(注解) + APT(注解处理器) + Javapoet (ARoute,EventBus,ButterKnife等)
Demo2.ASM + Transform + Gradle Plugin(自定义插件) (无埋点监控,无感知织入,AOP切面编程等)
Demo3.插件化-动态代理实现

#### 详细
1.Demo1对应 annotationlib、annotationProcessorlib
  annotationlib中声明注解
  annotationProcessorlib中实现了注解处理器的逻辑
2.Demo2对应 asmlib、asmpluginlib
  asmlib声明注解及工具类
  asmpluginlib实现asm 插件
  扩展:
  1.也可以基于ByteX进行插件开发
  ByteX：ByteX是字节开源的基于Gradle Transform API和ASM的字节码插件平台。
  插件间自动无缝地整合成一个Transform，提高处理构建的效率。Transform过程中，对class文件的IO是比较耗时的，把所有的plugin整合成一个单独transform可以避免打包的额外时间开销呈线性增长
  简单说,就是多个插件构建过程融合成一个,减少线性耗时(但是具体实现起来也还是要依赖ASM、Transform,书写难度并没有降低)
  https://github.com/bytedance/ByteX/
  2.DexInjector(Dex插桩):字节跳动基于google dexter开发的修改APK(dex)工具,不同于ASM针对编译时修改class文件,这个修改的是dex文件,虽然不是开源的,但是也为我们来实现插桩带来的新的思路
3.Demo3对应 androidpluginapk、androidpluginlib
  androidpluginapk用于生成插件apk/dex,也可以独立运行
  androidpluginlib利用DexClassLoader读取apk信息,通过反射获取到插件Activity(PluginActivity)
  因为PluginActivity并没有生命周期,所以它的回调要依靠宿主的ProxyActivity,也就是说用ProxyActivity这个架子去代理PluginActivity的内容
    
#### 使用
在app - MainActivity中统一执行/调用,每个模块都有详细注释

