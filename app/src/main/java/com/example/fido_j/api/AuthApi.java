package com.example.fido_j.api;

import android.app.Activity;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.EC2Algorithm;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
    private PublicKeyCredentialRpEntity rpEntity;
    private PublicKeyCredentialUserEntity userEntity;
    private List<PublicKeyCredentialParameters> parametersList;
    private AuthenticatorSelectionCriteria.Builder authenticatorEntity;
    private PublicKeyCredentialCreationOptions options;
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
    public PublicKeyCredentialCreationOptions registerRequest(){
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
            Log.d("RegisterRequest",""+e.getMessage());
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
                    JSONObject rp = json.getJSONObject("rp");
                    JSONObject user = json.getJSONObject("user");
                    JSONArray pubKeyParams = json.getJSONArray("pubKeyCredParams");
                    JSONObject authenticatorSelection = json.getJSONObject("authenticatorSelection");
                    if(rpEntity==null) {
                        rpEntity = new PublicKeyCredentialRpEntity(String.valueOf(rp.get("id")), String.valueOf(rp.get("name")), null);
                    }
                    if (userEntity==null) {
                        userEntity= new PublicKeyCredentialUserEntity(
                                String.valueOf(user.get("id")).getBytes(),
                                String.valueOf(user.get("name")),
                                null,
                                String.valueOf(user.get("displayName"))
                        );
                    }
                    parametersList = Collections.singletonList(new PublicKeyCredentialParameters(
                            PublicKeyCredentialType.PUBLIC_KEY.toString(),
                            EC2Algorithm.ES256.getAlgoValue()
                    ));
                    authenticatorEntity = new AuthenticatorSelectionCriteria.Builder();
                    try {
                        authenticatorEntity.setAttachment(Attachment.fromString(authenticatorSelection.getString("authenticatorAttachment")));
                    } catch (Attachment.UnsupportedAttachmentException e) {
                        e.printStackTrace();
                    }
                    options = new PublicKeyCredentialCreationOptions.Builder()
                            .setUser(userEntity)
                            .setChallenge(Base64.encode(challenge.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT))
                            .setParameters(parametersList)
                            .setTimeoutSeconds(Double.valueOf(1800000))
                            .setAuthenticatorSelection(authenticatorEntity.build())
                            .setRp(rpEntity)
                            .build();
                } catch (JSONException e) {
                    Log.d("ChallengeErr",""+e.getMessage());
                }
                Log.d("RegisterResult:",""+result);
            }
        });
        return options;
    }
    public void registerResponse(String keyHandle,String datajson,String attestationObject,PublicKeyCredential credential){
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        JSONObject response = new JSONObject();
        try {
            json.put("id",credential.getId());
            json.put("type",PublicKeyCredentialType.PUBLIC_KEY.toString());
            json.put("rawId",credential.getId());
            response.put("clientDataJSON",datajson);
            response.put("attestationObject",attestationObject);
            json.putOpt("response",response);
            Log.d("RegisterResponse",""+json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        RequestBody body = RequestBody.create(String.valueOf(json), JSON); // new
//        Request request = new Request.Builder()
//                .url(BASE_URL+"/registerResponse")
//                .post(body)
//                .build();
//        Call call = client.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.d("RegisterResponseErr",""+e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                Log.d("RegisterResponse",""+response.body().string());
//            }
//        });
    }
}

