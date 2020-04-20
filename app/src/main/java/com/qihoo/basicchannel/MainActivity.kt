package com.qihoo.basicchannel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.qihoo.channelapi.core.CoreChannel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun test(view: View) {
    }

    /**
     * 主要是调用 CoreChannel 的 loadModel 方法
     */
    fun init(view: View) {
        Log.d("wyz","初始化!")
        CoreChannel.init()
    }

    fun test1(view: View) {
        CoreChannel.call("testCall", "testCall数据", "附加数据")

    }
    fun test2(view: View) {
        CoreChannel.call("testCall2", "testCall2数据", "附加数据")

    }
    fun test3(view: View) {
        CoreChannel.call("JavaTest", "收到数据", "附加数据")
    }


}
