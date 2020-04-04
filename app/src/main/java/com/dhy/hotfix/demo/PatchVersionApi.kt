package com.dhy.hotfix.demo

import android.app.Activity
import android.content.Context
import com.dhy.hotfix.updater.*
import io.reactivex.Observable

class PatchVersionApi(context: Context) : IPatchVersionApi {
    private val androidId = context.androidId
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

fun Activity.checkPatchVersion() {
    checkPatchVersion(PatchVersionApi(this), "")
}