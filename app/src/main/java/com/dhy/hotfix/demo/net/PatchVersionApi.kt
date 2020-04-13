package com.dhy.hotfix.demo.net

import android.app.Activity
import com.dhy.hotfix.demo.BuildConfig.APPLICATION_ID
import com.dhy.hotfix.demo.BuildConfig.VERSION_TYPE
import com.dhy.hotfix.updater.IPatchVersion
import com.dhy.hotfix.updater.IPatchVersionApi
import com.dhy.hotfix.updater.IPatchVersionApi.Companion.HOTFIX_USER_CODE
import com.dhy.hotfix.updater.IPatchVersionApi.Companion.HOTFIX_VERSION_CODE
import com.dhy.hotfix.updater.PatchUser
import com.dhy.hotfix.updater.checkPatchVersion
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class PatchVersionApi : IPatchVersionApi {
    private val api = getApi()
    override fun fetchPatchUsers(): Observable<List<PatchUser>> {
        return api.checkVersion(APPLICATION_ID, HOTFIX_USER_CODE + VERSION_TYPE)
            .map {
                parseToPatchUsers(it.data?.message)
            }
    }

    override fun checkPatchVersion(): Observable<IPatchVersion> {
        return api.checkVersion(APPLICATION_ID, HOTFIX_VERSION_CODE + VERSION_TYPE)
            .map {
                it.data ?: AppVersion()
            }
    }

    private fun getApi(): Api {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(OkHttpClient())
            .baseUrl("https://www.baidu.com/")
            .build()
            .create(Api::class.java)
    }
}

fun Activity.checkPatchVersion() {
    checkPatchVersion(PatchVersionApi(), "0")
}

interface Api {
    @GET("http://apk.wwvas.com:9999/admin/versioninfo/get")
    fun checkVersion(@Query("packagename") packageName: String, @Query("versiontype") versionType: Int): Observable<Response<AppVersion>>
}