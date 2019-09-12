package com.darklycoder.wifitool

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initEvents()
    }

    private fun initEvents() {
        btn_global.setOnClickListener { startActivity(Intent(this@MainActivity, GlobalMonitorActivity::class.java)) }

        btn_single.setOnClickListener { startActivity(Intent(this@MainActivity, SingleMonitorActivity::class.java)) }
    }

}
