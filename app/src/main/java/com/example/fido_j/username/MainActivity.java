package com.example.fido_j.username;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.fido_j.BuildConfig;
import com.example.fido_j.R;
import com.example.fido_j.api.AuthApi;
import com.example.fido_j.databinding.ActivityMainBinding;
import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.EC2Algorithm;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import kotlin.text.Charsets;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Task<PendingIntent> fido2PendingIntent;
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
    private ActivityResultLauncher<IntentSenderRequest> registerRequest;
    private IntentSenderRequest senderRequest;
    private Activity activity;
    private PublicKeyCredentialCreationOptions options;
    private PublicKeyCredential credential;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        activity=this;
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
                            Thread.sleep(3000);
                            api.password(password);
                            Thread.sleep(5000);
                            options = api.registerRequest();
                            Log.d("Options:",""+options);

                            Fido2ApiClient fido2ApiClient = Fido.getFido2ApiClient(getApplicationContext());
                            fido2PendingIntent = fido2ApiClient.getRegisterPendingIntent(options);

                            fido2PendingIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {

                                @Override
                                public void onSuccess(PendingIntent fido2PendingIntent) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Log.d("IntentSenderrrrr",""+fido2PendingIntent.getIntentSender().toString());
                                                activity.startIntentSenderForResult(
                                                        fido2PendingIntent.getIntentSender(),
                                                        1,
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
                        } catch (Exception e) {
                            Log.d("ErrMessage",""+e.getMessage());
                        }

                    }
                }).start();
                editor.putString(Preferences_Username_Key,username);
                editor.putString(Preferences_Password_Key,password);
                editor.commit();


//                biometricPrompt.authenticate(prompt);
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
                byte[] credentialByte = data.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA);
                Log.d("Response Extra",""+fido2Response);

                if (requestCode==1) {
                    credential =PublicKeyCredential.deserializeFromBytes(credentialByte);
                    handleRegisterResponse(fido2Response,credential);
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
        Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();
    }

    private void handleRegisterResponse(byte[] fido2Response,PublicKeyCredential credential) {
        AuthenticatorAttestationResponse response = AuthenticatorAttestationResponse.deserializeFromBytes(fido2Response);
        String keyHandleBase64 = Base64.encodeToString(response.getKeyHandle(), Base64.NO_WRAP);
        String clientDataJsonBody = new String(response.getClientDataJSON(), Charsets.UTF_8);
        String clientDataJson = Base64.encodeToString(response.getClientDataJSON(),Base64.NO_WRAP);
        String attestationObjectBase64 = Base64.encodeToString(response.getAttestationObject(), Base64.NO_WRAP);
        api.registerResponse(keyHandleBase64,clientDataJson,attestationObjectBase64,credential);
        Log.d("LOG_TAG", "keyHandleBase64:"+keyHandleBase64);
        Log.d("LOG_TAG", "clientDataJSON:"+clientDataJson);
        Log.d("LOG_TAG", "attestationObjectBase64:"+attestationObjectBase64);
    }
    private void handleSignResponse(byte[] fido2Response) {
        AuthenticatorAssertionResponse response = AuthenticatorAssertionResponse.deserializeFromBytes(fido2Response);
        String keyHandleBase64 = Base64.encodeToString(response.getKeyHandle(), Base64.NO_WRAP);
        String clientDataJson = new String(response.getClientDataJSON(), Charsets.UTF_8);
        String authenticatorDataBase64 = Base64.encodeToString(response.getAuthenticatorData(), Base64.NO_WRAP);
        String signatureBase64 = Base64.encodeToString(response.getSignature(), Base64.NO_WRAP);

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
            rpEntity = new PublicKeyCredentialRpEntity("entertaining-maddening-beluga.glitch.me", "WebAuthn Codelab", null);
        }
        if (userEntity==null) {
            userEntity= new PublicKeyCredentialUserEntity(
                    "AR1SPUq7H6u5uKONczmR3r3r3vso6X0VamBTDq8QgVA".getBytes(),
                    "uu",
                    null,
                    "uu"
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