package com.example.fido_j.api;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthApi {
    private String username,password,sessionID,challenge;
    AuthenticatorAttestationResponse response;
    private static final HashMap<String,List<Cookie>> cookieStore = new HashMap<>();
    OkHttpClient client = new OkHttpClient().newBuilder()
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
                    cookieStore.put(httpUrl.host(),list);
                    Log.d("HttpUrl:",""+httpUrl);
                }

                @NonNull
                @Override
                public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
                    List<Cookie> cookies = cookieStore.get(httpUrl.host());
                    if(cookies!=null){
                        Log.d("Cookie",""+cookies.get(0));
                    }
                    return cookies!=null ? cookies:new ArrayList<Cookie>();
                }
            })
            .build();

    private final String BASE_URL = "https://entertaining-maddening-beluga.glitch.me/auth";
    public void username(String username){
        this.username=username;
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("username",username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(String.valueOf(json), JSON); // new
        Request request = new Request.Builder()
                .url(BASE_URL+"/username")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Fail:", e.toString());
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                Log.d("result:",""+result);
            }
        });

    }
    public void password(String password){
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(String.valueOf(json), JSON); // new
        Request request = new Request.Builder()
                .url(BASE_URL+"/password")
                .post(body)
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
                Log.d("result:",""+result+"\n");
            }
        });
    }
    public String registerRequest(){
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        JSONObject AuthenticatorSelection = new JSONObject();
        Log.d("Cookie:",""+cookieStore);
        try {
            json.put("attestation","none");
            AuthenticatorSelection.put("authenticatorAttachment","platform");
            AuthenticatorSelection.put("userVerification","required");
            json.putOpt("authenticatorSelection",AuthenticatorSelection);
            Log.d("JsonObject",""+json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(String.valueOf(json), JSON); // new
        Request request = new Request.Builder()
                .url(BASE_URL+"/registerRequest")
                .header("X-Requested-With","XMLHttpRequest")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("RegisterFail:", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                try {
                    JSONObject json = new JSONObject(result);
                    challenge= String.valueOf(json.get("challenge"));
                    Log.d("Challenge",""+json.get("challenge"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("RegisterResult:",""+result);
            }
        });
        return challenge;
    }
}

