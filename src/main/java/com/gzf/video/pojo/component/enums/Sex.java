package com.gzf.video.pojo.component.enums;

import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.lang.reflect.Type;

public enum Sex implements JSONSerializable {
    BOY("Boy"), GIRL("Girl"), UNKNOWN("Unknown");

    private String sex;

    Sex(final String sex) {
        this.sex = sex;
    }

    public String getSex() {
        return sex;
    }

    @Override
    public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) {
        serializer.write(sex);
    }
}
