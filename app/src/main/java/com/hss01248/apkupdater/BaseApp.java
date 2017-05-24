package com.hss01248.apkupdater;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.hss01248.updater.ApkUpdater;
import com.hss01248.updater.UpdateInfo;

/**
 * Created by Administrator on 2017/5/22 0022.
 */

public class BaseApp extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ApkUpdater.getInstance()
                .init(this, false,"http://api.qxinli.com:9001/api/version/latestVersion/v1.json", UpdateModel.class,
                        new ApkUpdater.ObjectCopyable<UpdateModel>() {
                            @Override
                            public UpdateInfo copyValues(UpdateModel obj) {
                                if(obj.code !=0){
                                    Toast.makeText(getApplicationContext(),"不成功",Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                                UpdateInfo info = new UpdateInfo();
                                info.versionName = obj.data.version;
                                info.versionCode = obj.data.code;
                                info.isForceUpdate = obj.data.forceUpdate==1;//服务器的强制更新配置
                                //info.isForceUpdate = true;
                                info.downloadUrl = obj.data.url;
                                info.dec = obj.data.description;
                                //info.apkSize = obj.data.size;
                                return info;
                            }
                        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                ApkUpdater.onActivityResumed(activity);

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
