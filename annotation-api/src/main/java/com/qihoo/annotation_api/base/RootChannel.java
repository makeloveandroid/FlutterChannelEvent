package com.qihoo.annotation_api.base;

import java.util.Map;

public interface RootChannel {
    void load(Map<String, Class<? extends BaseChannel>> events);
}
