package com.dhy.hotfix.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhy.hotfix.HotFix
import com.dhy.hotfix.updater.*
import io.reactivex.Observable
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
            checkPatchVersion(api, "")
        }
    }

    private val api = object : IPatchVersionApi {
        override fun fetchPatchUsers(): Observable<List<PatchUser>> {
            val user = PatchUser().apply {
                version = "*"
                users = listOf("10086@Donald")
                uuids = listOf("$androidId@Donald")
                showTip = true
            }
            val user2 = PatchUser().apply {
                version = "1.0.0"
                users = listOf("10086")
            }
            return Observable.just(listOf(user, user2))
        }

        override fun checkPatchVersion(): Observable<IPatchVersion> {
            return Observable.just(AppVersion().apply {
                url = "https://gitee.com/88911006/patch/raw/master/README.md"
                versionCode = BuildConfig.VERSION_CODE + 1
            })
        }
    }
}
