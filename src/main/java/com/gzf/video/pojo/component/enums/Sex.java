package com.gzf.video.pojo.component.enums;

import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.lang.reflect.Type;

public enum Sex implements JSONSerializable {
    UNKNOWN("unknown"), BOY("boy"), GIRL("girl");

    private String info;

    Sex(final String info) {
        this.info = info;
    }

    public int getSex() {
        return this.ordinal();
    }

    public String getInfo() {
        return info;
    }

    @Override
    public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) {
        serializer.write(this.ordinal());
    }
}
