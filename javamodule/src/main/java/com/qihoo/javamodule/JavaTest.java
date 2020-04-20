package com.qihoo.javamodule;

import android.util.Log;

import com.qihoo.annotation_api.ChannelEvent;
import com.qihoo.annotation_api.base.BaseChannel;

@ChannelEvent(methodName = "JavaTest")
public class JavaTest extends BaseChannel {
    @Override
    public void call() {
        Log.d("wyz", "JavaTest:" + channelData);
    }
}
