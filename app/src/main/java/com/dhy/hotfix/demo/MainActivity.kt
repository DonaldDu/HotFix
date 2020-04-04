package com.dhy.hotfix.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhy.hotfix.HotFix
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

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
        btSaveFile.setOnClickListener {
            val apk = assets.list("patch")!!.first()
            val inputStream = assets.open("patch/$apk")
            val folder = HotFix.getHotFixFolder(this)

            val apkFile = File(folder, HotFix.formatPatchFileName(BuildConfig.VERSION_CODE + 1))
            if (!folder.exists()) folder.mkdirs()
            if (apkFile.exists()) apkFile.delete()
            apkFile.createNewFile()

            HotFix.copyStream(inputStream, FileOutputStream(apkFile))
        }
        btLoadPatch.setOnClickListener {
            HotFix.init(application, null)
        }
        btCheckPatchVersion.setOnClickListener {
            checkPatchVersion()
        }
    }
}
