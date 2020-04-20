package com.qihoo.channel_plugin.util

import com.qihoo.channel_plugin.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

object ScanUtil {

    fun scanClass(classList: MutableList<String>, inputStream: InputStream) {
        var cr = ClassReader(inputStream)
        var cw = ClassWriter(cr, 0)
        var cv = ScanClassVisitor(classList, Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }


    fun scanJar(classList: MutableList<String>,jarFile: File, destFile: File) {
        var file = JarFile(jarFile)
        var enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            var jarEntry = enumeration.nextElement() as JarEntry
            var entryName = jarEntry.getName()
            if (entryName.startsWith(CHANNEL_CLASS_PACKAGE_NAME)) {
                var inputStream = file.getInputStream(jarEntry)
                scanClass(classList,inputStream)
                inputStream.close()
            } else if (CORE_CHANNEL_CLASS_NAME == entryName) {
                // 找到需要需要修改的类了
                log("找到需要核心注册类2${destFile.absolutePath}")
                RegisterTransform.fileContainsInitClass = destFile
            }
        }
        file.close()
    }
}

class ScanClassVisitor(var classList: MutableList<String>, api: Int, classVisitor: ClassVisitor) :
    ClassVisitor(api, classVisitor) {
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)

        // 遍历每一个类的接口,是否有RootChannel
        interfaces?.forEach { itName ->
            if (itName == ROOT_CHANNEL_INTERFACE) {
                log("找到需要调用的类啦:$itName   $name")
                classList.add(name!!)
            }
        }

    }
}
