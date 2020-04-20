package com.qihoo.channelapi.core

import com.qihoo.annotation_api.base.BaseChannel
import com.qihoo.annotation_api.base.RootChannel
import java.lang.IllegalArgumentException

object CoreChannel {
    /**
     * 判断是否初始化标记
     */
    private var hasInit: Boolean = false

    var methodMap = mutableMapOf<String, Class<BaseChannel>>()

    /**
     * 初始化一次
     */
    fun init() {
        if (!hasInit) {
            hasInit = true
            loadModel()
        }
    }

    /**
     * 加载方法数据
     * 这里要通过Transfrom 注入代码
     */
    @JvmStatic
    private fun loadModel() {
        // 这里最终会注入调用 registerChannelEvent 方法
    }

    /**
     * 开始注册 registerChannelEvent 通过类的路径
     */
    @JvmStatic
    fun registerChannelEvent(rootChannel: RootChannel) {
        rootChannel.load(methodMap as Map<String, Class<out BaseChannel>>?)
    }

    /**
     * 所有的通道方法最终会调用这里
     */
    fun call(methodName: String, data: String, excessObj: Any) {
        val clazz = methodMap[methodName]
        if (clazz == null) {
            throw IllegalArgumentException("没有查询到注册的 $methodName 方法!!")
        } else {
            clazz.newInstance().init(methodName, data, excessObj).call()
        }
    }
}