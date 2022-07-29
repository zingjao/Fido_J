package com.example.fido_j.api;

import android.util.JsonWriter;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.StringWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthApi {
    OkHttpClient client = new OkHttpClient().newBuilder().build();
    private final String BASE_URL = "https://entertaining-maddening-beluga.glitch.me/auth";
    public void username(){
        FormBody.Builder formBody = new FormBody.Builder();//創建表單請求體
        Request request = new Request.Builder()
                .url(BASE_URL+"/username")
                .method("POST",formBody.build())
                .post(formBody.build())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Fail:", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                Log.d("result:",""+result);
            }
        });
    }
    public void password(){
        FormBody.Builder formBody = new FormBody.Builder();//創建表單請求體
        formBody.add("username","zhangsan");//傳遞鍵值對參數
        Request request = new Request.Builder()
                .url(BASE_URL+"/password")
                .post(formBody.build())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Fail:", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                Log.d("result:",""+result);
            }
        });
    }
    public void credentials(){
        FormBody.Builder formBody = new FormBody.Builder();//創建表單請求體
        formBody.add("username","zhangsan");//傳遞鍵值對參數
        Request request = new Request.Builder()
                .url(BASE_URL+"/credentials")
                .post(formBody.build())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Fail:", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                Log.d("result:",""+result);
            }
        });
    }
}
