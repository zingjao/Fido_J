package com.example.fido_j.credentials;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fido_j.CredentialsAdapter;
import com.example.fido_j.LoginActivity;
import com.example.fido_j.R;
import com.example.fido_j.databinding.ActivityCreditialsBinding;
import com.example.fido_j.username.MainActivity;
import com.google.android.gms.fido.Fido;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CredentialsActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ActivityCreditialsBinding binding;
    private Bundle bundle;
    private Context context;
    //    private BiometricManager manager;
//    private BiometricPrompt.PromptInfo prompt;
//    private BiometricPrompt biometricPrompt;
    private CredentialsAdapter adapter;
    private ArrayList<String> id=new ArrayList<>(),publicKey=new ArrayList<>();
    private String Preferences_Username_Key="USER_NAME_KEY";
    private String Preferences_Password_Key="USER_PASSWORD_KEY";
    private String Credentials_Key="CREDENTIALS_KEY";
    private String username,challenge;
    private ActivityResultLauncher<IntentSenderRequest> createCredentialIntentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("tag",""+result.getData().getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA));
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_creditials);
//        setContentView(R.layout.activity_auth);
//        createCredentialIntentLauncher.launch(
//                IntentSenderRequest.Builder(intent).build()
//        );
        preferences= getSharedPreferences("Save",0);
        editor=preferences.edit();
        context=this;
        init();
//        manager=BiometricManager.from(context);
//        switch (manager.canAuthenticate()){
//            case BiometricManager.BIOMETRIC_SUCCESS:
//                break;
//            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
//                break;
//            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
//                break;
//            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
//                break;
//        }
//        prompt=new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("指紋認證")
//                .setSubtitle("使用掃描器認證以進行下一步")
//                .setNegativeButtonText("取消")
//                .build();
//        biometricPrompt = new BiometricPrompt(CredentialsActivity.this, ContextCompat.getMainExecutor(this),
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
//                        Toast.makeText(getApplicationContext(),"Authen Succeed",Toast.LENGTH_SHORT).show();
//                        id.add("id");
//                        publicKey.add("public_keys");
//                        adapter = new CredentialsAdapter(id,publicKey);
//                        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));//使用LinearLayout布局
//                        //分割線套件
//                        binding.RecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
//                                DividerItemDecoration.VERTICAL));
//                        binding.RecyclerView.setAdapter(adapter);//將資料給recyclerView顯示
//                        editor.putInt(Credentials_Key,1);
//                        editor.commit();
//                    }
//
//                    @Override
//                    public void onAuthenticationFailed() {
//                        super.onAuthenticationFailed();
//                        Toast.makeText(getApplicationContext(),"Authen Failed",Toast.LENGTH_SHORT).show();
//                    }
//                });
        binding= DataBindingUtil.setContentView(this,R.layout.activity_creditials);
//        binding.btnAdd.setOnClickListener(view->{
//            biometricPrompt.authenticate(prompt);
//        });
        binding.btnLogout.setOnClickListener(view->{
//            PublicKeyCredential credential =PublicKeyCredential.deserializeFromBytes(getIntent().getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA));
//            AuthenticatorResponse response=credential.getResponse();
//            Log.d("tagggggg",""+credential.getRawId());
//            editor.remove(Preferences_Username_Key);
            editor.remove(Preferences_Password_Key);
//            editor.remove(Credentials_Key);
            editor.commit();
            Intent intent = new Intent(CredentialsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    public void init(){
//        bundle=getIntent().getExtras();
//        challenge=bundle.getString("Challenge");
        username=preferences.getString(Preferences_Username_Key,"");
        ArrayList<String> userList = new ArrayList<>();
        ArrayList<String> challengeList = new ArrayList<>();
        userList.add("username");
        challengeList.add("challenge");
        Log.d("tagggg",""+username);
        binding.txvTitle.setText("Welcome,"+username+"!");
        adapter = new CredentialsAdapter(userList,challengeList);
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));//使用LinearLayout布局
        //分割線套件
        binding.RecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        binding.RecyclerView.setAdapter(adapter);//將資料給recyclerView顯示
    }
}
