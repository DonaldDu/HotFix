package com.dhy.hotfix.updater

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import java.util.*

internal class CurrentActivityWatcher(activity: Activity) : ActivityLifecycleCallbacks2 {
    private var current: Activity? = activity
    private val dialogs: WeakHashMap<Activity, Dialog> = WeakHashMap()
    private val application = activity.application
    private var version: IPatchVersion? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        current = activity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        current = activity
        show(version)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == current) current = null
        dialogs.remove(activity)?.dismiss()
    }

    fun show(version: IPatchVersion?) {
        this.version = version ?: return
        val activity: Activity = current ?: return
        if (activity.isFinishing) return
        activity.runOnUiThread {
            val dialog = AlertDialog.Builder(activity)
                .setMessage("补丁包有更新！")
                .setCancelable(false)
                .setPositiveButton("更新") { dialog, _ ->
                    PatchVersionUtil(activity).download(version, true)
                    hide(dialog)
                }
                .setNegativeButton("取消") { dialog, _ ->
                    hide(dialog)
                }.create()
            dialog.show()
            dialogs[activity] = dialog
        }
    }

    private fun hide(dialog: DialogInterface) {
        unregister()
        dialog.dismiss()
    }

    fun unregister() {
        application.unregisterActivityLifecycleCallbacks(this)
    }
}