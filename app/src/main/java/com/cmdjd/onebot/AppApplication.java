package com.cmdjd.onebot;

import android.app.Application;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.Map;
import java.util.Objects;

public class AppApplication extends Application {

    public Application application;
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Log.e("ASAppsFlyer","AppApplication  onCreate");
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.e("ASAppsFlyer", "onConversionDataSuccess: " + attrName + " = " + conversionData.get(attrName));
                    String status = Objects.requireNonNull(conversionData.get("af_status")).toString();
                    if (status.equals("Organic")) {
//                        isOrganic = true;
                    } else {
//                        isOrganic = false;
                    }
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.e("ASAppsFlyer", "error getting conversion data: " + errorMessage);

            }

            @Override
            public void onAppOpenAttribution(Map<String, String> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.e("ASAppsFlyer", "onAppOpenAttribution: " + attrName + " = " + conversionData.get(attrName));
                }
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.e("ASAppsFlyer", "error onAttributionFailure : " + errorMessage);
            }
        };
        AppsFlyerLib.getInstance().init("onnR4mL5tDQdGwWTwfW6HC", conversionListener, getApplicationContext());
        AppsFlyerLib.getInstance().start(this);
        AppsFlyerLib.getInstance().setDebugLog(true);
    }

}
