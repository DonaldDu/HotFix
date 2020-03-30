package com.dhy.hotfix.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dhy.hotfix.HotFix
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btShowSetting.setOnClickListener {
            Toast.makeText(this, Setting().name, Toast.LENGTH_LONG).show()
        }
        btClearBuffer.setOnClickListener {
            HotFix.clearBuffer(this)
        }
    }
}
