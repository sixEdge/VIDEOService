package com.gzf.video.pojo.component;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.lang.reflect.Type;

/**
 * {"code" : code, "message" : "response message"}.
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
     *  Success state.
     *
     * @return {"code" : 0, "message" : ""}
     */
    public static byte[] successState(final String msg) {
        return ("{\"code\" : 0, \"message\" : \"" + msg + "\"}").getBytes();
    }

    /**
     *  Failed state.
     *
     * @return {"code" : 1, "message" : ""}
     */
    public static byte[] failedState(final String msg) {
        return ("{\"code\" : 1, \"message\" : \"" + msg + "\"}").getBytes();
    }





    @Override
    public String toString() {
        return "CodeMessage{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
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
