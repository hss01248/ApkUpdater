package com.hss01248.apkupdater;

import android.app.Activity;
import android.os.Bundle;

import com.hss01248.updater.ApkUpdater;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private void update() {

        ApkUpdater.getInstance().update(false,true,true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }
}
