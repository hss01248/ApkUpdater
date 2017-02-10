package com.hss01248.apkupdater;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.hss01248.updater.ApkUpdater;
import com.hss01248.updater.UpdateInfo;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        update();
    }

    private void update() {
         ApkUpdater.getInstance()
                 .init(getApplication(), "http://api.qxinli.com:9001/api/version/latestVersion/v1.json", UpdateModel.class,
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
                           info.isForceUpdate = obj.data.forceUpdate==1;
                           info.downloadUrl = obj.data.url;
                           info.dec = obj.data.description;
                           return info;
                       }
                   });
        ApkUpdater.getInstance().update(false,this);
    }
}
