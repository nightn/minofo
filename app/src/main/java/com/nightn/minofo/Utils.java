package com.nightn.minofo;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by WWQ on 2017/4/2.
 */

public class Utils {
    //发送 http 请求
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}