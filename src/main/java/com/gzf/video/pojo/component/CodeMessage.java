package com.gzf.video.pojo.component;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.lang.reflect.Type;

/**
 * {"code" : code, "message" : "response message"}<br />
 * {"code" : code, "message" : "response message", "data" : data in json format}.
 */
public class CodeMessage {

    private Code code;

    private String message;

    public CodeMessage() {}

    public CodeMessage(final Code code, final String message) {
        this.code = code;
        this.message = message;
    }

    @JSONField
    public Code getCode() {
        return code;
    }

    public void setCode(final Code code) {
        this.code = code;
    }

    @JSONField
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }


    /**
     *  Success message.
     *
     * @return {"code" : 0, "message" : msg}
     */
    public static byte[] successMsg(final String msg) {
        return ("{\"code\":0,\"message\":\"" + msg + "\"}").getBytes();
    }

    /**
     *  Failed message.
     *
     * @return {"code" : 1, "message" : msg}
     */
    public static byte[] failedMsg(final String msg) {
        return ("{\"code\":1,\"message\":\"" + msg + "\"}").getBytes();
    }

    /**
     *  Success json.
     *
     * @return {"code" : 0, "message" : msg, "data" : json}
     */
    public static byte[] successJson(final String msg, final String json) {
        return ("{\"code\":0,\"message\":\"" + msg + "\",\"data\":" + json + "}").getBytes();
    }

    /**
     *  Failed json.
     *
     * @return {"code" : 1, "message" : msg, "data" : json}
     */
    public static byte[] failedJson(final String msg, final String json) {
        return ("{\"code\":1,\"message\":\"" + msg + "\",\"data\":" + json + "}").getBytes();
    }


    public enum Code implements JSONSerializable {
        SUCCESS(0), FAILED(1);

        private int code;

        Code(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "Code{" +
                    "code=" + code +
                    '}';
        }

        @Override
        public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) {
            serializer.write(code);
        }
    }
}
