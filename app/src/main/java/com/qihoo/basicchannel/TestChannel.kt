package com.qihoo.basicchannel

import android.util.Log
import com.qihoo.annotation_api.ChannelEvent
import com.qihoo.annotation_api.base.BaseChannel

@ChannelEvent(methodName = "testCall")
class TestChannel : BaseChannel() {
    override fun call() {
        Log.d("wyz","testCall  $channelData  $excessObj")
    }
}