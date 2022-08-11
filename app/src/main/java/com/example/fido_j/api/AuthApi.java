package com.example.fido_j.api;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.fido_j.Account;
import com.example.fido_j.BuildConfig;
import com.google.android.gms.fido.common.Transport;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.EC2Algorithm;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
    private String username;
    private String password;
    private String sessionID;
    private String challenge,requestChallenge;
    AuthenticatorAttestationResponse response;
    private static final HashMap<String,List<Cookie>> cookieStore = new HashMap<>();
    private PublicKeyCredentialRpEntity rpEntity;
    private PublicKeyCredentialUserEntity userEntity;
    public PublicKeyCredentialDescriptor descriptorEntity;
    private List<PublicKeyCredentialParameters> parametersList;
    private AuthenticatorSelectionCriteria.Builder authenticatorEntity;
    private PublicKeyCredentialCreationOptions options;
    private PublicKeyCredentialRequestOptions requestOptions;
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
    public void username(String username, AccountInterface accountInterface){
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
                 accountInterface.AccountFail(e.toString());
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                Log.d("result:",""+result);
                accountInterface.AccountSuccess(result);
            }
        });

    }
    public void password(String password,PasswordInterface passwordInterface){
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
                passwordInterface.PasswordFail(e.toString());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                Log.d("result:",""+result+"\n");
                passwordInterface.PasswordSuccess();
            }
        });
    }
    public void registerRequest(RequestInterface requestInterface){
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
                requestInterface.RequestFail(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 連線成功(會失敗1~2次屬正常)
                String result = response.body().string();
                try {
                    JSONObject json = new JSONObject(result);
                    if(json.getString("challenge")!=null){
                        try{
                            challenge= String.valueOf(json.get("challenge"));
                        }
                        catch(Exception e){
                            Log.d("ChallengeError",""+e.getMessage());
                        }
                    }
                    Log.d("Challenge",""+json.get("challenge"));
                    Log.d("JsonRequest",""+json.toString());
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        options = new PublicKeyCredentialCreationOptions.Builder()
                                .setUser(userEntity)
                                .setChallenge(java.util.Base64.getUrlDecoder().decode(challenge))
                                .setParameters(parametersList)
                                .setTimeoutSeconds(Double.valueOf(1800000))
                                .setAuthenticatorSelection(authenticatorEntity.build())
                                .setRp(rpEntity)
                                .build();
                    }
                    requestInterface.RequestSuccess(options);
                } catch (JSONException e) {
                    Log.d("ChallengeErr",""+e.getMessage());
                }
                Log.d("RegisterResult:",""+result);
            }
        });
    }
    public void registerResponse(String keyHandle,String datajson,String attestationObject,PublicKeyCredential credential,ResponseInterface responseInterface){
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
        RequestBody body = RequestBody.create(String.valueOf(json), JSON); // new
        Request request = new Request.Builder()
                .url(BASE_URL+"/registerResponse")
                .header("X-Requested-With","XMLHttpRequest")
                .header("User-Agent", BuildConfig.APPLICATION_ID+"/"+BuildConfig.VERSION_NAME +
                        "(Android "+Build.VERSION.RELEASE+"; "+Build.MODEL+"; "+Build.BRAND+")")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("RegisterResponseErr",""+e.getMessage());
                responseInterface.ResponseFail(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                Log.d("RegisterResponseSuccess",""+result);
                try {
                    JSONObject json = new JSONObject(result);
                    if(json.getString("username")!=null){
                        responseInterface.ResponseSuccess(json);
                    }
                } catch (Exception e) {
                    responseInterface.ResponseFail(e.getMessage());
                }

            }
        });
    }
    public void signinRequest(String credId,SignRequestInterface signRequestInterface){
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        //此api不用json值
        RequestBody body = RequestBody.create("{}", JSON); // new
        Request request = new Request.Builder()
                .url(BASE_URL+"/signinRequest?credId="+credId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("User-Agent", BuildConfig.APPLICATION_ID+"/"+BuildConfig.VERSION_NAME +
                        "(Android "+Build.VERSION.RELEASE+"; "+Build.MODEL+"; "+Build.BRAND+")")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                signRequestInterface.SignRequestFail(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result=response.body().string();
                Log.d("SignInRequestResult",""+result);
                try {
                    ArrayList<Transport> transports= new ArrayList<>();
                    transports.add(Transport.INTERNAL);
                    JSONObject json = new JSONObject(result);
                    if(descriptorEntity==null){
                        JSONObject allowCredentials = json.getJSONArray("allowCredentials").getJSONObject(0);
                        Log.d("AllowCredentials", "" + allowCredentials);

                        descriptorEntity=new PublicKeyCredentialDescriptor("public-key",
                                allowCredentials.getString("id").getBytes(StandardCharsets.UTF_8),
                                transports
                        );
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requestOptions = new PublicKeyCredentialRequestOptions.Builder()
                                .setRpId(String.valueOf(json.get("rpId")))
                                .setChallenge(Base64.getUrlDecoder().decode(json.getString("challenge")))
                                .setAllowList(Collections.singletonList(descriptorEntity))
                                .setTimeoutSeconds(Double.valueOf(1800000))
                                .build();
                    }
                    signRequestInterface.SignRequestSuccess(requestOptions);
                } catch (Exception e) {
                    Log.d("Build-RequestOptions","Failed By:"+e.getMessage());
                }

            }
        });
    }
    public void signinResponse(){

    }
    public interface AccountInterface{
        void AccountSuccess(String result);
        void AccountFail(String msg);
    }
    public interface PasswordInterface{
        void PasswordSuccess();
        void PasswordFail(String msg);
    }
    public interface RequestInterface{
        void RequestSuccess(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions);
        void RequestFail(String msg);
    }
    public interface ResponseInterface{
        void ResponseSuccess(JSONObject json);
        void ResponseFail(String msg);
    }
    public interface SignRequestInterface{
        void SignRequestSuccess(PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions);
        void SignRequestFail(String msg);
    }
}

