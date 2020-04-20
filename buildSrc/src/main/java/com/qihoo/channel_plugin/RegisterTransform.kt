package com.qihoo.channel_plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.qihoo.channel_plugin.extension.eachFileRecurse
import com.qihoo.channel_plugin.util.CorJarUtil
import com.qihoo.channel_plugin.util.ScanUtil
import com.qihoo.channel_plugin.util.ScanUtil.scanJar
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

var CORE_CHANNEL_CLASS_FILE_NAME = "com/qihoo/channelapi/core/CoreChannel"

@JvmField
var CORE_CHANNEL_CLASS_NAME = "$CORE_CHANNEL_CLASS_FILE_NAME.class"

var REGISTER_METHOD_NAME = "registerChannelEvent"


var CHANNEL_CLASS_PACKAGE_NAME = "com/qihoo/flutter/channel/event"

var ROOT_CHANNEL_INTERFACE = "com/qihoo/annotation_api/base/RootChannel"


class RegisterTransform(project: Project) : Transform() {

    companion object {
        var fileContainsInitClass: File? = null
    }

    override fun getName(): String {
        return "CHANNEL_EVENT"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        // 获取所有的依赖 和 jar 统计到2个集合中
        var dirInputs = HashSet<DirectoryInput>()
        var jarInputs = HashSet<JarInput>()
        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { dirInput ->
                dirInputs.add(dirInput)
            }
            input.jarInputs.forEach { jarInput ->
                jarInputs.add(jarInput)
            }
        }

        var channelRootClass = mutableListOf<String>()
        // 开始收集所有改变的类文件

        if (dirInputs.isNotEmpty() || jarInputs.isNotEmpty()) {
            if (dirInputs.isNotEmpty()) {
                // 先把依赖项目和app项目的class路径加入
                addDirInput(transformInvocation, dirInputs, channelRootClass)
            }
            // 收集jar包中的类
            if (jarInputs.isNotEmpty()) {
                // 开始处理jar的类
                addJar(jarInputs, transformInvocation, channelRootClass)
            }
        }

        // 全部处理完毕,开始ASM注入注册代码
        if (fileContainsInitClass != null) {
            // 这个jar 中就有 CoreChannel
            if (channelRootClass.size > 0) {
                CorJarUtil.insertInitCodeIntoJarFile(channelRootClass, fileContainsInitClass)
            }

        }
    }

    private fun addJar(
        jarInputs: HashSet<JarInput>,
        transformInvocation: TransformInvocation,
        channelRootClass: MutableList<String>
    ) {
        jarInputs.forEach { jarInput ->
            var jarInputFile = jarInput.file

            var destName = jarInput.name

            var hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
            if (destName.endsWith(".jar")) {
                destName = destName.substring(0, destName.length - 4)
            }

            // 擦腚沟子 坑第一参数不能用jarInput.name,因为有重名的情况
            var jarOutputFile = transformInvocation.outputProvider.getContentLocation(
                destName + "_" + hexName, getOutputTypes(), getScopes(), Format.JAR
            )

            // 过滤不需要处理的jar 主要过滤 android 的jar
            if (shouldProcessPreDexJar(jarInputFile.absolutePath)) {
                scanJar(channelRootClass, jarInputFile, jarOutputFile)
            }
            FileUtils.copyFile(jarInputFile, jarOutputFile)
        }
    }

    private fun addDirInput(
        transformInvocation: TransformInvocation,
        dirInputs: HashSet<DirectoryInput>,
        channelRootClass: MutableList<String>
    ) {
        var leftSlash = File.separator == "/"

        // 只增加改变和增加的class
        dirInputs.forEach { dirInput ->
            var root = dirInput.file.absolutePath
            if (!root.endsWith(File.separator)) {
                root += File.separator
            }
            // 遍历文件中所有的类
            dirInput.file.eachFileRecurse { file ->
                checkFileIsChannelRoot(file, root, leftSlash, channelRootClass)
            }
            // 擦屁股
            var dirOutput = transformInvocation.outputProvider.getContentLocation(
                dirInput.name, outputTypes, scopes, Format.DIRECTORY
            )


            FileUtils.copyDirectory(dirInput.file, dirOutput)
        }

    }

    /**
     * 检测这个文件是不是需要实现了 RootChannel 接口的类
     */
    private fun checkFileIsChannelRoot(
        file: File,
        root: String,
        leftSlash: Boolean,
        channelRootClass: MutableList<String>
    ) {
        var path = file.absolutePath.replace(root, "")

        if (!leftSlash) {
            path = path.replace("\\\\", "/")
        }
        if (file.isFile() && path.startsWith(CHANNEL_CLASS_PACKAGE_NAME)) {
            if (file.name.endsWith(".class")) {
                println("需要判断的class:${file.absolutePath}")
                // 是class 判断是否是 实现了RootChannel
                ScanUtil.scanClass(channelRootClass, file.inputStream())
            }
        }
    }


    fun shouldProcessPreDexJar(path: String): Boolean {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

}


fun log(msg: String) {
    println(msg)
}