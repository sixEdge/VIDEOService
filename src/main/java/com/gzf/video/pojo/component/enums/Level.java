package com.gzf.video.pojo.component.enums;

import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.lang.reflect.Type;

public enum Level implements JSONSerializable {

    LEVEL_0, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6;

    Level() {}

    public int getLevel() {
        return this.ordinal();
    }

    @Override
    public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) {
        serializer.write(this.ordinal());
    }
}
