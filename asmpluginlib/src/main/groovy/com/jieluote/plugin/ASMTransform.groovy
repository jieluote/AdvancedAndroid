package com.jieluote.plugin;


import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

public class ASMTransform extends Transform {
    private Map<String, File> modifyMap = new HashMap<>();
    private Project project;

    public ASMTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return ASMTransform.class.getSimpleName();
    }

    /**
     * CLASSES 代表要处理的class文件(包含jar文件)
     * RESOURCES 代表要处理java的资源
     * @return
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 官方文档 Scope 有 7 种类型：
     * EXTERNAL_LIBRARIES ： 只有外部库
     * PROJECT ： 只有项目内容
     * PROJECT_LOCAL_DEPS ： 只有项目的本地依赖(本地jar)
     * PROVIDED_ONLY ： 只提供本地或远程依赖项
     * SUB_PROJECTS ： 只有子项目
     * SUB_PROJECTS_LOCAL_DEPS： 只有子项目的本地依赖项(本地jar)
     * TESTED_CODE ：由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 增量编译
     * @return
     */
    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println("*** transform1 ***");
        //如果非增量，则清空旧的输出内容
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        System.out.println("*** transform2 ***");
        // 获取输入（消费型输入，可以从中获取jar包和class文件夹路径，需要传递给下一个Transform）
        Collection<TransformInput> inputs = transformInvocation.getInputs();

        //引用型输入，无需输出
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs();
        System.out.println("*** transform3 ***");
        for (TransformInput input : inputs) {
            // 遍历输入，分别遍历其中的jar以及directory
            for (JarInput jarInput : input.getJarInputs()) {
                // 对jar文件进行处理
                transformJar(transformInvocation, jarInput);

            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                // 对directory进行处理
                transformDirectory(transformInvocation, directoryInput);
            }
        }
        System.out.println("*** transform4 ***");
    }

    private void transformJar(TransformInvocation invocation, JarInput input) throws IOException {
        System.out.println("***transformJar***");
        File tempDir = invocation.getContext().getTemporaryDir();
        String destName = input.getFile().getName();
        String hexName = DigestUtils.md5Hex(input.getFile().getAbsolutePath()).substring(0, 8);
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4);
        }
        File dest = invocation.getOutputProvider()
                .getContentLocation(destName + "_" + hexName, input.getContentTypes(), input.getScopes(), Format.JAR);

        JarFile originJar = new JarFile(input.getFile());
        File outputJar = new File(tempDir, "temp_"+input.getFile().getName());
        JarOutputStream output = new JarOutputStream(new FileOutputStream(outputJar));

        Enumeration<JarEntry> enumeration = originJar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry originEntry = enumeration.nextElement();
            InputStream inputStream = originJar.getInputStream(originEntry);

            String entryName = originEntry.getName();
            if (entryName.endsWith(".class")) {
                System.out.println("transformJar entryName:"+entryName);
                JarEntry destEntry = new JarEntry(entryName);
                output.putNextEntry(destEntry);

                byte[] sourceBytes = IOUtils.toByteArray(inputStream);
                // 修改class文件内容
                byte[] modifiedBytes = ASMUtils.getInstance().modifyClass(sourceBytes);
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                output.write(modifiedBytes);
                output.closeEntry();
            }
        }
        output.close();
        originJar.close();
        FileUtils.copyFile(outputJar, dest);
    }

    private void transformDirectory(TransformInvocation invocation, DirectoryInput input) throws IOException {
        System.out.println("*** transformDirectory ***");
        File tempDir = invocation.getContext().getTemporaryDir();
        // 获取输出路径，将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了(这个dest也是下一个transform的输入路径)
        File dest = invocation.getOutputProvider()
                .getContentLocation(input.getName(), input.getContentTypes(), input.getScopes(), Format.DIRECTORY);
        System.out.println("*** tempDir:"+tempDir.getAbsolutePath()+",dest:"+dest.getAbsolutePath());
        File dir = input.getFile();
        if (dir != null && dir.exists()) {
            traverseDirectory(tempDir, dir);
            FileUtils.copyDirectory(input.getFile(), dest);
            for (Map.Entry<String, File> entry : modifyMap.entrySet()) {
                File target = new File(dest.getAbsolutePath() + entry.getKey());
                if (target.exists()) {
                    target.delete();
                }
                FileUtils.copyFile(entry.getValue(), target);
                entry.getValue().delete();
            }
        }
    }

    private void traverseDirectory(File tempDir, File dir) throws IOException {
        System.out.println("*** traverseDirectory1 ***");
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                traverseDirectory(tempDir, file);
            } else if (file.getAbsolutePath().endsWith(".class")) {
                System.out.println("*** traverseDirectory2 ***");
                String className = path2ClassName(file.getAbsolutePath()
                        .replace(dir.getAbsolutePath() + File.separator, ""));
                System.out.println("*** traverseDirectory3 className:"+className);
                byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(file));
                System.out.println("*** traverseDirectory4 ***");
                byte[] modifiedBytes = ASMUtils.getInstance().modifyClass(sourceBytes);
                System.out.println("*** traverseDirectory5 ***");
                File modified = new File(tempDir, className.replace(".", "") + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                System.out.println("*** traverseDirectory6 modified:"+modified.getAbsolutePath());
                new FileOutputStream(modified).write(modifiedBytes);
                String key = file.getAbsolutePath().replace(dir.getAbsolutePath(), "");
                modifyMap.put(key, modified);
                System.out.println("*** traverseDirectory7 key:"+key);
            }
        }
    }

    static String path2ClassName(String pathName) {
        return pathName.replace(File.separator, ".").replace(".class", "");
    }
}
