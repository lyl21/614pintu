package com.cmdjd.onebot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import java.util.UUID
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        next(this)
    }

    private fun startMain(activity: Activity){
        val flyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(applicationContext)
        val appsflyer_appId: String = applicationContext.getPackageName()
        val appsflyer_auth = "v58iGq7RjNvC2RRjfAxNVc"
        val appsflyer_id= "1234567890123-1234567"
        val gameCategoryId= "3"
        getGoogleAdId(applicationContext) { gaid ->
            println("Ad ID: $gaid")
            val url = "https://9000hhh.com/#/launch?ch=5531&sd=6"
            activity.startActivity(Intent(activity, WebActivity::class.java).apply { putExtra("url",url) })
            activity.finish()
        }

    }

    fun next(activity: Activity){
        activity.window.decorView.postDelayed({
            startMain(activity)
        }, 0)
    }


    private fun getGoogleAdId(context: Context, callback: (String?) -> Unit) {
        Executors.newCachedThreadPool().execute {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var adid: String? = sharedPreferences.getString("adid", null)

            if (adid == null) {
                try {
                    val idInfo: AdvertisingIdClient.Info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                    adid = idInfo.id
                    if (adid != null && adid.startsWith("00000000")) {
                        adid = UUID.randomUUID().toString()
                        sharedPreferences.edit().putString("adid", adid).apply()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Use callback to return the adid, either from shared preferences or newly fetched/generated
            callback(adid)
        }
    }

}
