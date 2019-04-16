package com.darklycoder.wifitool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEvents();
    }

    private void initEvents() {
        findViewById(R.id.btn_global).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GlobalMonitorActivity.class)));

        findViewById(R.id.btn_single).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SingleMonitorActivity.class)));
    }

}
