package com.example.fido_j.credentials;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fido_j.CredentialsAdapter;
import com.example.fido_j.R;
import com.example.fido_j.api.AuthApi;
import com.example.fido_j.auth.AuthActivity;
import com.example.fido_j.databinding.ActivityAuthBinding;
import com.example.fido_j.databinding.ActivityCreditialsBinding;
import com.example.fido_j.username.MainActivity;
import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.EC2Algorithm;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;

import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CredentialsActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ActivityCreditialsBinding binding;
    private Context context;
    private BiometricManager manager;
    private BiometricPrompt.PromptInfo prompt;
    private BiometricPrompt biometricPrompt;
    private CredentialsAdapter adapter;
    private ArrayList<String> id=new ArrayList<>(),publicKey=new ArrayList<>();
    private String Preferences_Username_Key="USER_NAME_KEY";
    private String Preferences_Password_Key="USER_PASSWORD_KEY";
    private String Credentials_Key="CREDENTIALS_KEY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        preferences= getSharedPreferences("Save",0);
        editor=preferences.edit();
        context=this;
        manager=BiometricManager.from(context);
        switch (manager.canAuthenticate()){
            case BiometricManager.BIOMETRIC_SUCCESS:
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                break;
        }
        prompt=new BiometricPrompt.PromptInfo.Builder()
                .setTitle("指紋認證")
                .setSubtitle("使用掃描器認證以進行下一步")
                .setNegativeButtonText("取消")
                .build();
        biometricPrompt = new BiometricPrompt(CredentialsActivity.this, ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),"Authen Error:"+errString,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(),"Authen Succeed",Toast.LENGTH_SHORT).show();
                        id.add("id");
                        publicKey.add("public_keys");
                        adapter = new CredentialsAdapter(id,publicKey);
                        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));//使用LinearLayout布局
                        //分割線套件
                        binding.RecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                                DividerItemDecoration.VERTICAL));
                        binding.RecyclerView.setAdapter(adapter);//將資料給recyclerView顯示
                        editor.putInt(Credentials_Key,1);
                        editor.commit();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(),"Authen Failed",Toast.LENGTH_SHORT).show();
                    }
                });
        binding= DataBindingUtil.setContentView(this,R.layout.activity_creditials);
        binding.btnAdd.setOnClickListener(view->{
            biometricPrompt.authenticate(prompt);
        });
        binding.btnLogout.setOnClickListener(view->{
//            editor.remove(Preferences_Username_Key);
            editor.remove(Preferences_Password_Key);
//            editor.remove(Credentials_Key);
            editor.commit();
            Intent intent = new Intent(CredentialsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
