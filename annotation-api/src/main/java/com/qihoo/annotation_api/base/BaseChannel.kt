package com.qihoo.annotation_api.base

/**
 * 通道事件基础类
 */
abstract class BaseChannel {
    /**
     * 通道需要调用的方法昵称
     */
    lateinit var methodName: String
    /**
     * 通道发来的数据
     */
    lateinit var channelData: String
    /**
     * 附加的数据
     */
    lateinit var excessObj: Any

    /**
     * 初始化数据
     */
    fun init(methodName: String, channelData: String, excessObj: Any): BaseChannel {
        this.methodName = methodName
        this.channelData = channelData
        this.excessObj = excessObj
        return this
    }

    /**
     * 核心调用方法,最终实现
     */
     abstract fun call()

}