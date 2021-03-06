# AdvancedAndroid 进阶/高级Android

#### 介绍
主要是集合了一些Android较有深度的技术点demo,有助于我们对Android的理解更深入一层</br>
目前包括(未来还会不断更新)</br>
- Demo1_1:Annotation(注解) + APT(注解处理器) + Javapoet(ARoute,EventBus,ButterKnife等)</br>
- Demo1_2:SPI(服务提供接口) + AutoService + ServiceLoader</br>
- Demo2.ASM + Transform + Gradle Plugin(自定义插件) (用于无埋点监控,无感知织入,AOP切面编程等)</br>
- Demo3.插件化-静态代理 + 反射 + 接口回调 实现 (借鉴dynamic-load-apk原理)</br>
- Demo4.插件化-hook Instrument</br>
- Demo5.Hook技术-Xposed(用于热修复、AOP)

#### 详细
1.Demo1_1对应 annotationlib、annotationProcessorlib</br>
  annotationlib中声明注解</br>
  annotationProcessorlib中实现了注解处理器的逻辑 
  AutoService 注解</br>
2.Demo1_2对应 annotationProcessorlib下的spilib</br>
    分别用手动和自动(AutoService注解)两种方式实现SPI:
    1.手动: Java和Kotlin</br>
    2.自动: JavaScript</br>
  Demo1_1和Demo1_2 里的这些技术点经常用于组件化实现(一部分)
3.Demo2对应 asmlib、asmpluginlib</br>
  asmlib声明注解及工具类</br>
  asmpluginlib实现asm 插件</br>
  *扩展:*</br>
  1. 也可以基于ByteX进行插件开发</br>
  ByteX：ByteX是字节开源的基于Gradle Transform API和ASM的字节码插件平台。</br>
  插件间自动无缝地整合成一个Transform，提高处理构建的效率。Transform过程中，对class文件的IO是比较耗时的，把所有的plugin整合成一个单独transform可以避免打包的额外时间开销呈线性增长</br>
  简单说,就是多个插件构建过程融合成一个,减少线性耗时(但是具体实现起来也还是要依赖ASM、Transform,书写难度并没有降低)</br>
  https://github.com/bytedance/ByteX/</br>
  2. DexInjector(Dex插桩):字节跳动基于google dexter开发的修改APK(dex)工具,不同于ASM针对编译时修改class文件,这个修改的是dex文件,虽然不是开源的,但是也为我们来实现插桩带来的新的思路</br></br>
  
4.Demo3对应 androidpluginapk、androidpluginlib(proxy部分)</br>
  androidpluginapk用于生成插件apk/dex,也可以独立运行
  androidpluginlib利用DexClassLoader读取apk信息,通过反射获取到插件Activity(PluginActivity)</br>
  因为PluginActivity并没有生命周期,所以它的回调要依靠宿主的ProxyActivity,也就是说用ProxyActivity这个架子去代理PluginActivity的内容</br>

5.Demo4对应 androidpluginapk、androidpluginlib(hook部分)</br>
  不同于demo3的插件Activity完全依附在代理Activity中,并没有自己的上下文环境</br>
  Hook的方式是启动完整的具有生命周期的Activity,只不过在启动验证的阶段通过提前放置"占坑"Activity欺骗过AMS的验证,在启动后再</br>
  替换为插件Activity。同时需要注意解决资源冲突的问题。</br>
  
6.Demo5对应 xposedlib</br>
  Xposed的Hook技术已经跳出了虚拟机层面,直接在创建JVM前就下了手脚, 通过替换zygote进程实现了控制手机上所有app进程。</br>
  拿到了进程后再将需要hook的函数注册为Native层函数。当执行到这一函数是虚拟机会优先执行Native层函数,然后再去执行Java层函数,达到Hook目的。</br>
  *扩展:*</br>
  Xposed因为要替换系统文件,所以需要设备root,这无疑提高了使用门槛,通常用于游戏外挂。</br>
  阿里开源的Dexposed,基于Xposed,无需root(只修改本应用,不能修改其它应用),更适合应用开发(AOP、监控、热修复)，但不支持Android5.0以上的ART内核。</br>
  被同阿里系的Andfix、Hotfix、Sophix(商业未开源)代替
    
  
#### 使用
在app - MainActivity中统一执行/调用,每个模块都有详细注释

