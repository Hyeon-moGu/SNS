package com.simplesns.sns.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Response<T> {

    private String resultCode;
    private T result;

    public static Response<Void> error(String errorCode){
        return new Response<>(errorCode, null);
    }

    public static Response<Void> success(){
        return new Response<Void>("SUCCESS", null);
    }

    public static <T> Response<T> success(T result){
        return new Response<>("SUCCESS", result);
    }

    public String toStream() {
        if(result == null){
            return "{" +
                    "\" resultcode\":"+ "\"" + resultCode + "\", " +
                    "\"result\":" + null + "}";
        }

        return "{" +
                "\" resultcode\":"+ "\"" + resultCode + "\", " +
                "\"result\":" + "\"" + result + "\"" + "}";
    }
}
