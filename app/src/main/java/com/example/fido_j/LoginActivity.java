package com.example.fido_j;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.fido_j.api.AuthApi;
import com.example.fido_j.credentials.CredentialsActivity;
import com.example.fido_j.databinding.ActivityLoginBinding;
import com.example.fido_j.username.MainActivity;
import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.EC2Algorithm;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import kotlin.text.Charsets;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private BiometricManager manager;
    private BiometricPrompt.PromptInfo prompt;
    private BiometricPrompt biometricPrompt;
    private AuthApi api=new AuthApi();
    private String challenge;
    private int REQUEST_CODE_REGISTER =1;
    private PublicKeyCredentialRpEntity rpEntity;
    private PublicKeyCredentialUserEntity userEntity;
    private List<PublicKeyCredentialParameters> parametersList;
    private int AUTH_ACTIVITY_RES_5=5;
    private String publicKey,credId;
    private PublicKeyCredentialRequestOptions requestOptions;
    private Task<PendingIntent> fido2PendingIntent;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
        binding= DataBindingUtil.setContentView(this, R.layout.activity_login);
        activity=this;
        init();
//        prompt=new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("指紋認證")
//                .setSubtitle("使用掃描器認證以進行下一步")
//                .setNegativeButtonText("取消")
//                .build();
//        biometricPrompt = new BiometricPrompt(LoginActivity.this, ContextCompat.getMainExecutor(this),
//                new BiometricPrompt.AuthenticationCallback() {
//                    @Override
//                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                        super.onAuthenticationError(errorCode, errString);
//                        Toast.makeText(getApplicationContext(),"Authen Error:"+errString,Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                        super.onAuthenticationSucceeded(result);
//                        Toast.makeText(getApplicationContext(), "登入成功!", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(LoginActivity.this, CredentialsActivity.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putString("Challenge",challenge);
//                        intent.putExtras(bundle);
//                        startActivity(intent);
//                        finish();
//                    }
//
//                    @Override
//                    public void onAuthenticationFailed() {
//                        super.onAuthenticationFailed();
//                        Toast.makeText(getApplicationContext(),"Authen Failed",Toast.LENGTH_SHORT).show();
//                    }
//                });
        binding.btnRegist.setOnClickListener(view->{
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
        binding.btnNext.setOnClickListener(view->{
            String username = binding.etUsername.getText().toString();
            if(!"".equals(username)){
                //建publicKeyCredentialRequestOptions
                api.username(username, new AuthApi.AccountInterface() {
                    @Override
                    public void AccountSuccess(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            JSONArray credentials = json.getJSONArray("credentials");
                            if(!credentials.isNull(0)){
                                Log.d("Credentials",""+credentials.getJSONObject(0).toString());
                                publicKey = credentials.getJSONObject(0).getString("publicKey");
                                credId = credentials.getJSONObject(0).getString("credId");
                            }//後續不用靠password登入，密碼打api為空格
                            api.password(" ", new AuthApi.PasswordInterface() {
                                @Override
                                public void PasswordSuccess() {
                                    api.signinRequest(credId, new AuthApi.SignRequestInterface() {
                                        @Override
                                        public void SignRequestSuccess(PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions) {
                                            requestOptions=publicKeyCredentialRequestOptions;
                                            Fido2ApiClient fido2ApiClient = Fido.getFido2ApiClient(getApplicationContext());
                                            fido2PendingIntent = fido2ApiClient.getSignPendingIntent(requestOptions);
                                            fido2PendingIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                                                @Override
                                                public void onSuccess(PendingIntent pendingIntent) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                Log.d("IntentSenderrrrr",""+pendingIntent.getIntentSender().toString());
                                                                activity.startIntentSenderForResult(
                                                                        pendingIntent.getIntentSender(),
                                                                        2,
                                                                        null, // fillInIntent,
                                                                        0, // flagsMask,
                                                                        0, // flagsValue,
                                                                        0); //extraFlags);
                                                            } catch (Exception e) {
                                                                Log.d("LOGTAG", "" + e.getMessage());
                                                            }
                                                        }
                                                    }).start();
                                                }
                                            });
                                        }

                                        @Override
                                        public void SignRequestFail(String msg) {
                                            Log.d("SignInRequestFail",""+msg);
                                        }
                                    });
                                }

                                @Override
                                public void PasswordFail(String msg) {
                                    Log.d("PasswordFail",""+msg);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void AccountFail(String msg) {
                        Log.d("AccountFail",""+msg);
                    }
                });
                //打api(判斷user_credential_sign_count) 0:登入
//                PublicKeyCredentialCreationOptions options = new PublicKeyCredentialCreationOptions.Builder()
//                        .setRp(rpEntity)
//                        .setUser(userEntity)
//                        .setChallenge(challenge())
//                        .setParameters(parametersList).build();
//                Fido2ApiClient fido2ApiClient = Fido.getFido2ApiClient(getApplicationContext());
//                Task<Fido2PendingIntent> fido2PendingIntent= fido2ApiClient.getRegisterIntent(options);
//                fido2PendingIntent.addOnSuccessListener(new OnSuccessListener<Fido2PendingIntent>() {
//                    @Override
//                    public void onSuccess(Fido2PendingIntent fido2PendingIntent) {
//                        if (fido2PendingIntent.hasPendingIntent()) {
//                            Log.d("LOGTAG", "launching Fido2 Pending Intent");
//                            try {
//                                fido2PendingIntent.launchPendingIntent(LoginActivity.this, REQUEST_CODE_REGISTER);
//                            } catch (IntentSender.SendIntentException e) {
//                                Log.d("LOGTAG", ""+e.getMessage());
//                            }
//                        }
//                    }
//                });
//                biometricPrompt.authenticate(prompt);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RequestOptions",""+requestOptions);
        if (resultCode == RESULT_OK) {
            if (data.hasExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)) {
                byte[] fido2Response = data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA);
                Log.d("Response Extra",""+fido2Response);
                if(requestCode==2){
                    handleSignResponse(fido2Response);
                }
            }
            else if (data.hasExtra(Fido.FIDO2_KEY_ERROR_EXTRA)){
                handleErrorResponse(data.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA));
            }
        }
    }
    private void handleErrorResponse(byte[] errorBytes) {
        AuthenticatorErrorResponse authenticatorErrorResponse = AuthenticatorErrorResponse.deserializeFromBytes(errorBytes);
        String errorName = authenticatorErrorResponse.getErrorCode().name();
        String errorMessage = authenticatorErrorResponse.getErrorMessage();
        Log.e("LOG_TAG", "errorCode.name:"+errorName);
        Log.e("LOG_TAG", "errorMessage:"+errorMessage);
    }

    private void handleSignResponse(byte[] fido2Response) {
        AuthenticatorAssertionResponse response = AuthenticatorAssertionResponse.deserializeFromBytes(fido2Response);
        String keyHandleBase64 = Base64.encodeToString(response.getKeyHandle(), Base64.DEFAULT);
        String clientDataJson = new String(response.getClientDataJSON(), Charsets.UTF_8);
        String authenticatorDataBase64 = Base64.encodeToString(response.getAuthenticatorData(), Base64.DEFAULT);
        String signatureBase64 = Base64.encodeToString(response.getSignature(), Base64.DEFAULT);

        Log.d("LOG_TAG", "keyHandleBase64:"+keyHandleBase64);
        Log.d("LOG_TAG", "clientDataJSON:"+clientDataJson);
        Log.d("LOG_TAG", "authenticatorDataBase64:"+authenticatorDataBase64);
        Log.d("LOG_TAG", "signatureBase64:"+signatureBase64);

        String signFido2Result = "Authenticator Assertion Response\n\n" +
                "keyHandleBase64:\n" +
                "$keyHandleBase64\n\n" +
                "clientDataJSON:\n" +
                "$clientDataJson\n\n" +
                "authenticatorDataBase64:\n" +
                "$authenticatorDataBase64\n\n" +
                "signatureBase64:\n" +
                "$signatureBase64\n";
    }
    public void init(){
        rpEntity = new PublicKeyCredentialRpEntity("strategics-fido2.firebaseapp.com", "Fido2Demo", null);
        userEntity = new PublicKeyCredentialUserEntity(
                "demo2@example.com".getBytes(),
                "demo2@example.com",
                null,
                "Demo User2"
        );
        parametersList =Collections.singletonList(new PublicKeyCredentialParameters(
                PublicKeyCredentialType.PUBLIC_KEY.toString(),
                EC2Algorithm.ES256.getAlgoValue()
        ));

    }
}