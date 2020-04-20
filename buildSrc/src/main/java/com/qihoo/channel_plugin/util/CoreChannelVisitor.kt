package com.qihoo.channel_plugin.util

import com.qihoo.channel_plugin.CORE_CHANNEL_CLASS_FILE_NAME
import com.qihoo.channel_plugin.REGISTER_METHOD_NAME
import com.qihoo.channel_plugin.log
import org.objectweb.asm.*

var CORE_CHANNEL_TO_METHOD_NAME = "loadModel"

class CoreChannelVisitor(var channelRootClass: List<String>, cw: ClassWriter) :
    ClassVisitor(Opcodes.ASM5, cw) {

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, desc, signature, exceptions)
        // 1.判断对应方法是否是 loadModel() 方法
        if (name == CORE_CHANNEL_TO_METHOD_NAME) {
            mv = ChannelMethodVisitor(channelRootClass, mv)
        }
        return mv
    }

}

class ChannelMethodVisitor(var channelRootClass: List<String>, mv: MethodVisitor) :
    MethodVisitor(Opcodes.ASM5, mv) {
    override fun visitInsn(opcode: Int) {
        if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
            // 2. channelRootClass 存放的就是收集到的所有 JavaPoet 生成的类.
            channelRootClass.forEach { it ->
                // 3. new 一个生成对象
                mv.visitTypeInsn(Opcodes.NEW, it)
                mv.visitInsn(Opcodes.DUP)
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, it, "<init>", "()V", false);

                // 4.静态调用 registerChannelEvent(RootChannel rootChannel) 方法
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC
                    , CORE_CHANNEL_CLASS_FILE_NAME
                    , REGISTER_METHOD_NAME
                    , "(Lcom/qihoo/annotation_api/base/RootChannel;)V"
                    , false
                )
            }
        }


        super.visitInsn(opcode)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitMaxs(maxStack + 4, maxLocals)
    }
}