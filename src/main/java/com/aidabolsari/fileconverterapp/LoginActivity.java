package com.aidabolsari.fileconverterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {
    Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myIntent = new Intent(LoginActivity.this, ConverterActivity.class);
    }

    public void onClickLoginButton(View view) {
        LoginActivity.this.startActivity(myIntent);
    }
}