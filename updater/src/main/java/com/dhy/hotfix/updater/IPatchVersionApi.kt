package com.dhy.hotfix.updater

import com.google.gson.Gson
import io.reactivex.Observable

interface IPatchVersionApi {
    companion object {
        const val HOTFIX_VERSION_CODE = 30_000
        const val HOTFIX_USER_CODE = 40_000
    }

    fun checkPatchVersion(): Observable<IPatchVersion>
    fun fetchPatchUsers(): Observable<List<PatchUser>>
    fun parseToPatchUsers(json: String?): List<PatchUser> {
        return if (json == null) {
            listOf(PatchUser())
        } else {
            try {
                Gson().fromJson(json, Array<PatchUser>::class.java).toList()
            } catch (e: Exception) {
                listOf(PatchUser())
            }
        }
    }
}