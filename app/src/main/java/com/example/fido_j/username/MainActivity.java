package com.example.fido_j.username;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.fido_j.BuildConfig;
import com.example.fido_j.LoginActivity;
import com.example.fido_j.R;
import com.example.fido_j.api.AuthApi;
import com.example.fido_j.credentials.CredentialsActivity;
import com.example.fido_j.databinding.ActivityMainBinding;
import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.EC2Algorithm;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import kotlin.text.Charsets;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AuthApi api=new AuthApi();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String Preferences_Username_Key="USER_NAME_KEY";
    private String Preferences_Password_Key="USER_PASSWORD_KEY";
    private String Credentials_Key="CREDENTIALS_KEY";
    private BiometricManager manager;
    private BiometricPrompt.PromptInfo prompt;
    private BiometricPrompt biometricPrompt;
    private String username,password,savedUsername;
    private String userAgent=BuildConfig.APPLICATION_ID+"/"+BuildConfig.VERSION_NAME+
            "(Android "+Build.VERSION.RELEASE+"; "+Build.MODEL+"; "+Build.BRAND+")";
    private int REQUEST_CODE_REGISTER=1;
    private Bundle bundle;
    private int credentials;
    private PublicKeyCredentialRpEntity rpEntity;
    private PublicKeyCredentialUserEntity userEntity;
    private List<PublicKeyCredentialParameters> parametersList;
    private String challenge;
    private ActivityResultLauncher<IntentSenderRequest> registerRequest;
    private IntentSenderRequest senderRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        preferences= getSharedPreferences("Save",MODE_PRIVATE);
        editor=preferences.edit();
//        prompt=new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("指紋認證")
//                .setSubtitle("使用掃描器認證以進行下一步")
//                .setNegativeButtonText("取消")
//                .build();
//        biometricPrompt = new BiometricPrompt(MainActivity.this, ContextCompat.getMainExecutor(this),
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
//                        Toast.makeText(getApplicationContext(), "註冊成功!", Toast.LENGTH_SHORT).show();
//                        editor.commit();
//                        api.registerRequest();
////                        Intent intent = new Intent(MainActivity.this, CredentialsActivity.class);
////                        startActivity(intent);
//                        finish();
//                    }
//
//                    @Override
//                    public void onAuthenticationFailed() {
//                        super.onAuthenticationFailed();
//                        Toast.makeText(getApplicationContext(),"Authen Failed",Toast.LENGTH_SHORT).show();
//                    }
//                });
        init();
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.btnNext.setOnClickListener(view->{
            username=binding.etUsername.getText().toString();
            password=binding.etPassword.getText().toString();
            if(!"".equals(username)||!"".equals(password)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        api.username(username);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        api.password(password);
                    }
                }).start();
                editor.putString(Preferences_Username_Key,username);
                editor.putString(Preferences_Password_Key,password);
                editor.commit();
                PublicKeyCredentialCreationOptions options = new PublicKeyCredentialCreationOptions.Builder()
                        .setRp(rpEntity)
                        .setUser(userEntity)
                        .setChallenge(challenge())
                        .setParameters(parametersList).build();
                Fido2ApiClient fido2ApiClient = Fido.getFido2ApiClient(getApplicationContext());
                Task<Fido2PendingIntent> fido2PendingIntent= fido2ApiClient.getRegisterIntent(options);
                fido2PendingIntent.addOnSuccessListener(new OnSuccessListener<Fido2PendingIntent>() {
                    @Override
                    public void onSuccess(Fido2PendingIntent fido2PendingIntent) {
//                      try {
//                            activity.startIntentSenderForResult(
//                                    pendingIntent.getIntentSender(),
//                                    1,
//                                    null, // fillInIntent,
//                                    0, // flagsMask,
//                                    0, // flagsValue,
//                                    0); //extraFlags);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.d("SenderError",""+e.getMessage());
//                        }
                        if (fido2PendingIntent.hasPendingIntent()) {
                            Log.d("LOGTAG", "launching Fido2 Pending Intent");
                            try {
                                fido2PendingIntent.launchPendingIntent(MainActivity.this, REQUEST_CODE_REGISTER);
                            } catch (IntentSender.SendIntentException e) {
                                Log.d("LOGTAG", ""+e.getMessage());
                            }
                        }
                    }
                });
//                biometricPrompt.authenticate(prompt);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(senderRequest==null) {
//                            try {
//                                senderRequest = new IntentSenderRequest.Builder(Tasks.await(fido2PendingIntent)).build();
//                            } catch (ExecutionException e) {
//                                e.printStackTrace();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        try {
//                            registerRequest.launch(senderRequest);
//                        } catch (Exception e) {
//                            Log.d("Errorcode!", e.getMessage());
//                        }
//                    }
//                }).start();
//            }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LOG_TAG",""+data.hasExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)+"\n" +
                data.hasExtra(Fido.FIDO2_KEY_ERROR_EXTRA)+"\n"+
                data.hasExtra(Fido.KEY_RESPONSE_EXTRA));
        if (resultCode == RESULT_OK) {
            if (data.hasExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)) {
                byte[] fido2Response = data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA);
                Log.d("Response Extra",""+fido2Response);
                if (requestCode==1) {
                    handleRegisterResponse(fido2Response);
                }
                else if(requestCode==2){
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

    private void handleRegisterResponse(byte[] fido2Response) {
        AuthenticatorAttestationResponse response = AuthenticatorAttestationResponse.deserializeFromBytes(fido2Response);
        String keyHandleBase64 = Base64.encodeToString(response.getKeyHandle(), Base64.DEFAULT);
        String clientDataJson = new String(response.getClientDataJSON(), Charsets.UTF_8);
        String attestationObjectBase64 = Base64.encodeToString(response.getAttestationObject(), Base64.DEFAULT);

        Log.d("LOG_TAG", "keyHandleBase64: $keyHandleBase64");
        Log.d("LOG_TAG", "clientDataJSON: $clientDataJson");
        Log.d("LOG_TAG", "attestationObjectBase64: $attestationObjectBase64");

        String registerFido2Result = "Authenticator Attestation Response\n\n" +
                "keyHandleBase64:\n" +
                "$keyHandleBase64\n\n" +
                "clientDataJSON:\n" +
                "$clientDataJson\n\n" +
                "attestationObjectBase64:\n" +
                "$attestationObjectBase64\n";

        Log.d("FidoResult:",""+registerFido2Result);
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
    private byte[] challenge() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] challenge = new byte[16];
        secureRandom.nextBytes(challenge);
        return challenge;
    }
    //判斷有否帳號密碼
    public void init(){
        savedUsername =getSharedPreferences("Save",0).getString(Preferences_Username_Key,"");
        password =getSharedPreferences("Save",0).getString(Preferences_Password_Key,"");
        credentials = getSharedPreferences("Save",0).getInt(Credentials_Key,0);
        Log.d("Main","User:"+username+"\n"+"Pass:"+password);
        if(rpEntity==null) {
            rpEntity = new PublicKeyCredentialRpEntity("strategics-fido2.firebaseapp.com", "Fido2Demo", null);
        }
        if (userEntity==null) {
            userEntity= new PublicKeyCredentialUserEntity(
                    "demo@example.com".getBytes(),
                    "demo@example.com",
                    null,
                    "Demo User"
            );
        }
        parametersList = Collections.singletonList(new PublicKeyCredentialParameters(
                PublicKeyCredentialType.PUBLIC_KEY.toString(),
                EC2Algorithm.ES256.getAlgoValue()
        ));
        //需+驗證id是否為空的判斷
//        if(credentials==1){
//            Intent intent = new Intent(MainActivity.this, CredentialsActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }
}