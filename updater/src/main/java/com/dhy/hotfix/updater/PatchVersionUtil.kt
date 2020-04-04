@file:Suppress("DEPRECATION")

package com.dhy.hotfix.updater

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.dhy.hotfix.HotFix
import com.dhy.xintent.Waterfall
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class PatchVersionUtil(val context: Context) {
    private val TAG = "HotFix"
    private fun IPatchVersion.toDownloadTask(context: Context): DownloadTask.Builder {
        val patchFolder = HotFix.getHotFixFolder(context).absolutePath
        val patchFileName = HotFix.formatPatchFileName(versionCode)
        return DownloadTask.Builder(url, patchFolder, patchFileName)
    }

    private lateinit var version: IPatchVersion
    fun download(version: IPatchVersion, withUI: Boolean) {
        this.version = version
        val task = version.toDownloadTask(context)
            .setPassIfAlreadyCompleted(true)
            .build()
        task.enqueue(getDownloadListener(withUI))
    }

    @Suppress("DEPRECATION")
    private fun getDownloadListener(withUI: Boolean): DownloadListener? {
        return if (withUI) {
            val progressDialog = ProgressDialog(context).apply {
                setMessage("下载中。。。")
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                isIndeterminate = false
                max = 100
                show()
            }
            PatchDownloadListener({
                progressDialog.dismiss()
                AlertDialog.Builder(context)
                    .setMessage(it)
                    .setPositiveButton("OK", null)
                    .show()
            }, {
                progressDialog.progress = it
            })
        } else PatchDownloadListener({
            Log.i(TAG, "download end: $it")
        }, {
            Log.i(TAG, "download progress: $it")
        })
    }

    private inner class PatchDownloadListener(val onEnd: (String) -> Unit, val onProgress: (Int) -> Unit) : DownloadListener1() {
        override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
            if (realCause != null) onEnd("error\n" + realCause.message)
            else {
                HotFix.updatePatchVersion(context, version.versionCode)
                onEnd("更新成功")
            }
        }

        override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
            val progress = ((currentOffset.toFloat() / totalLength) * 100).toInt()
            onProgress(progress)
            Log.i(TAG, "download progress $progress")
        }

        override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {}
        override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {}
        override fun retry(task: DownloadTask, cause: ResumeFailedCause) {}
    }
}

fun Activity.checkPatchVersion(api: IPatchVersionApi, user: String) {
    val watcher = CurrentActivityWatcher(this)
    val activity: Activity = this
    var version: IPatchVersion? = null
    var patchUser: PatchUser? = null
    Waterfall.flow {
        api.checkPatchVersion().subscribeX {
            if (it.isNewPatch(activity)) {
                version = it
                next()
            } else end()
        }
    }.flow {
        api.fetchPatchUsers().subscribeX {
            patchUser = it?.find { u -> u.match(activity, user) }
            if (patchUser != null) next() else end()
        }
    }.flow {
        if (patchUser?.showTip == true) {
            watcher.show(version)
        } else {
            PatchVersionUtil(applicationContext).download(version!!, false)
        }
    }.onEnd {
        watcher.unregister()
    }
}

fun IPatchVersion?.isNewPatch(context: Context): Boolean {
    return if (this != null && versionCode > 0) {
        val pv = HotFix.getPatchVersion(context)
        val vc = HotFix.getAppVersionCode(context)
        val localVersionCode = kotlin.math.max(pv, vc)
        versionCode > localVersionCode
    } else false
}

/**
 *  @return vn==version && (users.contains(user) || uuids.contains(uuid))
 * */
fun PatchUser.match(context: Context, user: String): Boolean {
    if (version != "*") {
        val vn = HotFix.getAppVersionName(context)
        if (version != vn) return false
    }
    if (users == null && uuids == null) return true

    if (users.containsWithRemark(user)) return true
    return uuids.containsWithRemark(context.androidId)
}

/**
 * 带注释的匹配检测："uuids": ["123@Donald"]
 * */
private fun List<String>?.containsWithRemark(content: String, marker: String = "@"): Boolean {
    return this?.find {
        val start = it.indexOf(marker)
        if (start != -1) it.substring(0, start) == content else it == content
    } != null
}

val Context.androidId: String
    get() {
        return Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

fun Context.copy2clipboard(text: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(null, text))
}

@SuppressLint("CheckResult")
private fun <T> Observable<T>.subscribeX(callback: (T?) -> Unit) {
    subscribeOn(Schedulers.io())
        .subscribe({
            callback(it)
        }, {
            it.printStackTrace()
            callback(null)
        })
}