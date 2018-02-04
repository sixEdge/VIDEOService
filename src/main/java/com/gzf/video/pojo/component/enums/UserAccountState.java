package com.gzf.video.pojo.component.enums;

import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.lang.reflect.Type;

public enum UserAccountState implements JSONSerializable {

    /** Not yet activated, needs to be authenticate. */
    NOT_ACTIVE(0),

    /** Active. */
    ACTIVE(1),

    /** Has been sealed. */
    SEALED(2);

    private int code;

    UserAccountState(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) {
        serializer.write(code);
    }
}
