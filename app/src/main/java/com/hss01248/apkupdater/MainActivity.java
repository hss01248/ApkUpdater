package com.hss01248.apkupdater;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.hss01248.updater.ApkUpdater;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApkUpdater.getInstance().update(true,true,true);
            }
        });

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
