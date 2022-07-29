package com.example.fido_j.auth;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminInfo;
import android.content.Context;
import android.content.Intent;
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

import com.example.fido_j.CredentialsAdapter;
import com.example.fido_j.R;
import com.example.fido_j.api.AuthApi;
import com.example.fido_j.credentials.CredentialsActivity;
import com.example.fido_j.databinding.ActivityAuthBinding;
@RequiresApi(api = Build.VERSION_CODES.M)
public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private AuthApi api= new AuthApi();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private BiometricManager manager;
    private BiometricPrompt.PromptInfo prompt;
    private BiometricPrompt biometricPrompt;
    private String password;
    private String Preferences_Password_Key="USER_PASSWORD_KEY";
    private String Credentials_Key="CREDENTIALS_KEY";
    private int Credentials=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_auth);
        preferences= getSharedPreferences("Save",0);
        editor=preferences.edit();
        prompt=new BiometricPrompt.PromptInfo.Builder()
                .setTitle("指紋認證")
                .setSubtitle("使用掃描器認證以進行下一步")
                .setNegativeButtonText("取消")
                .build();
        biometricPrompt = new BiometricPrompt(AuthActivity.this, ContextCompat.getMainExecutor(this),
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
                        Intent intent = new Intent(AuthActivity.this, CredentialsActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(),"Authen Failed",Toast.LENGTH_SHORT).show();
                    }
                });
        init();
        binding.btnNext.setOnClickListener(view->{
            password=binding.etPassword.getText().toString();
            if(!password.equals("")) {
                api.password();
                editor.putString(Preferences_Password_Key,password);
                editor.commit();
                Intent intent = new Intent(AuthActivity.this, CredentialsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    public void init(){
        Credentials=getSharedPreferences("Save",0).getInt(Credentials_Key,0);
        password =getSharedPreferences("Save",0).getString(Preferences_Password_Key,"");
        if(Credentials==1){
            biometricPrompt.authenticate(prompt);
        }
    }
}
