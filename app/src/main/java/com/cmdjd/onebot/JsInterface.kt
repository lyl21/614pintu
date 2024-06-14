package com.cmdjd.onebot

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib


class JsInterface(private val application: Activity) {
    val TAG = "JsInterface"
    @JavascriptInterface
    fun postMessage(name: String, data: String) {
        Log.e(TAG, "name = $name    data = $data");
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(data)) {
            return
        }
        event(application, name, data)
    }

    /***
     * 上报AF数据
     */
    fun event(context: Activity, name: String, data: String) {
        val eventValue: MutableMap<String, Any> = HashMap()
        /***
         * 开启新窗口跳转
         */
        if ("openWindow" == name) {
            try {
                //{ uid: 123456,phone:55124123,email:xxxx@xxx.com cid:64kr9, domain:'https://xxx.com/',url:”http://xxx.com/xxx/xxx”}
                val maps = JSON.parse(data) as Map<*, *>
                var url = ""
                for (map in maps.entries) {
                    val key = (map as Map.Entry<*, *>).key.toString()
                    if ("url" == key) {
                        //打开外部链接
                        url = (map as Map.Entry<*, *>).value!!.toString()
                    }
                }
                if (!TextUtils.isEmpty(url)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    context.startActivityForResult(intent,1)
                }
            } catch (e: Exception) {
            }
        } else if ("firstrecharge" == name || "recharge" == name) {
            try {
                val maps = JSON.parse(data) as Map<*, *>
                for (map in maps.entries) {
                    val key = (map as Map.Entry<*, *>).key.toString()
                    if ("amount" == key) {
                        eventValue[AFInAppEventParameterName.REVENUE] =
                            (map as Map.Entry<*, *>).value!!
                    } else if ("currency" == key) {
                        eventValue[AFInAppEventParameterName.CURRENCY] =
                            (map as Map.Entry<*, *>).value!!
                    }
                }
            } catch (e: Exception) {
            }
        } else {
            eventValue[name] = data
        }
        AppsFlyerLib.getInstance().logEvent(context, name, eventValue)
        Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
    }
}
