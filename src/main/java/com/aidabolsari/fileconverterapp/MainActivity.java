package com.aidabolsari.fileconverterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myIntent = new Intent(MainActivity.this, LoginActivity.class);
    }

    public void onClickButtonSkipTutorial(View view) {
        MainActivity.this.startActivity(myIntent);
    }
}