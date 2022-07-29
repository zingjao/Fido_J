package com.example.fido_j.username;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.fido_j.R;
import com.example.fido_j.api.AuthApi;
import com.example.fido_j.auth.AuthActivity;
import com.example.fido_j.credentials.CredentialsActivity;
import com.example.fido_j.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AuthApi api=new AuthApi();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String Preferences_Username_Key="USER_NAME_KEY";
    private String Preferences_Password_Key="USER_PASSWORD_KEY";
    private String Credentials_Key="CREDENTIALS_KEY";
    private String username,password;
    private int credentials;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        preferences= getSharedPreferences("Save",MODE_PRIVATE);
        editor=preferences.edit();
        init();
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.btnNext.setOnClickListener(view->{
            username=binding.etUsername.getText().toString();
            if(!"".equals(username)) {
                api.username();
                editor.putString(Preferences_Username_Key,username);
                editor.commit();
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    //判斷有否帳號密碼
    public void init(){
        username =getSharedPreferences("Save",0).getString(Preferences_Username_Key,"");
        password =getSharedPreferences("Save",0).getString(Preferences_Password_Key,"");
        credentials = getSharedPreferences("save",0).getInt(Credentials_Key,0);
        Log.d("Main","User:"+username+"\n"+"Pass:"+password);
        //需+驗證id是否為空的判斷
        if(credentials==1){
            Intent intent = new Intent(MainActivity.this, CredentialsActivity.class);
            startActivity(intent);
            finish();
        }
    }
}