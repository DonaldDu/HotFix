package com.dhy.hotfix.updater

import io.reactivex.Observable

interface IPatchVersionApi {
    companion object {
        const val HOTFIX_VERSION_CODE = 30_000
    }

    fun checkPatchVersion(): Observable<IPatchVersion>
    fun fetchPatchUsers(): Observable<List<PatchUser>>
}