package com.example.fido_j;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Base64;

public class ShareKeyHandle {
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String keyHandle;
    public ShareKeyHandle(Context context){
        this.context=context;
        preferences= context.getSharedPreferences("Save",MODE_PRIVATE);
        editor=preferences.edit();

    }
    public void saveKeyHandle(byte[] keyHandle){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editor.putString("keyHandle",java.util.Base64.getUrlEncoder().encodeToString(keyHandle));
        }
    }
    public byte[] loadKeyHandle(){
        keyHandle=context.getSharedPreferences("Save",0).getString("keyHandle","");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getUrlDecoder().decode(keyHandle);
        }
        else{
            return null;
        }
    }

}
