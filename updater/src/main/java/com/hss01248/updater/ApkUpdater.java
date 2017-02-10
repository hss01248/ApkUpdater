package com.hss01248.updater;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.config.ConfigBean;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.hss01248.net.builder.DownloadBuilder;
import com.hss01248.net.builder.JsonRequestBuilder;
import com.hss01248.net.wrapper.MyNetApi;
import com.hss01248.net.wrapper.MyNetApi2;
import com.hss01248.net.wrapper.MyNetListener;

/**
 * Created by Administrator on 2017/2/10 0010.
 */

public class ApkUpdater<T> {

    private  Context context;
    private  String url;
    private  ObjectCopyable<T> copyable;
    private  Class<T> updateBean;
    private static ApkUpdater instance;
    public     void init(Context context,String url,Class<T> updateBean,ObjectCopyable<T> copyable){
        if(MyNetApi2.context == null){
            MyNetApi2.init(context,"http://api.qxinli.com/",null);
            MyNetApi.context = context;

        }
        StyledDialog.init(context);
        this.context =  context;
        this.url = url;
        this.copyable = copyable;
        this.updateBean = updateBean;

    }
    private ApkUpdater(){

    }
    public static <T> ApkUpdater getInstance(){
        if(instance == null){
            synchronized (ApkUpdater.class){
                if(instance == null){
                    instance = new ApkUpdater();
                }
            }
        }
        return instance;
    }


    public interface ObjectCopyable<T>{
        UpdateInfo copyValues(T obj);
    }

    private   int getAPPVersionCodeFromAPP(Context ctx) {
        int currentVersionCode = 0;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            String appVersionName = info.versionName; // 版本名
            currentVersionCode = info.versionCode; // 版本号
            System.out.println(currentVersionCode + " " + appVersionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return currentVersionCode;
    }
    private  void toast(String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
    private boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public   void update(final boolean quitely, @Nullable final Activity activity){
      JsonRequestBuilder builder =  MyNetApi2.buildJsonRequest(url,updateBean);
        if(!quitely){
            builder.showLoadingDialog(activity,"获取更新信息");
        }

        builder.callback(new MyNetListener<T>() {


            @Override
            public void onSuccess(T bean, String s) {
                StyledDialog.dismissLoading();
                UpdateInfo info = copyable.copyValues(bean);

                int versionCode = getAPPVersionCodeFromAPP(context);
                if(versionCode >=info.versionCode){
                    if(!quitely){
                        toast("已经是最新版本");
                    }
                    return;
                }
                //有新版本
                boolean isWifiConnected = isWifiConnected(context);


                if(isWifiConnected){
                    if( info.isForceUpdate){
                        download(info,quitely,activity);
                    }else {
                        showInfoDialog(info,quitely,isWifiConnected,activity);
                    }
                }else {//不是wifi
                    if(!quitely){
                        showInfoDialog(info,quitely,isWifiConnected,activity);
                    }
                }



            }

            @Override
            public void onError(String msgCanShow) {
                super.onError(msgCanShow);
                StyledDialog.dismissLoading();
                toast(msgCanShow);
            }
        }).get();


    }

    private  void showInfoDialog(final UpdateInfo bean, final boolean quitely, boolean isWifiConnected, final Activity activity) {
        String title = TextUtils.isEmpty(bean.title) ? "检测到新版本:"+bean.versionName:bean.title;
        StringBuilder builder = new StringBuilder()
                .append("\n");
        if(!title.contains(bean.versionName)){
            builder.append("版本号:")
                    .append(bean.versionName)
                    .append("\n");
        }
        if(bean.apkSize >0){
            builder.append("安装包大小:")
                    .append(bean.apkSize)
                    .append("\n");
        }

        if(!isWifiConnected){
            builder.append("注意:当前不是wifi环境,您可以去切换到wifi或者直接下载")
                    .append("\n");
        }


        builder.append(bean.dec);
        Context context1 = context;
        if(activity != null ){
            context1 = activity;
        }


      ConfigBean configBean =  StyledDialog.buildMdAlert(activity, title, builder.toString(), new MyDialogListener() {
            @Override
            public void onFirst() {
                download(bean,quitely, activity);
            }

            @Override
            public void onSecond() {


            }

          @Override
          public void onThird() {
              toSettings();
          }
      });
        if(isWifiConnected){
            configBean.setBtnText("确定","取消","");
        }else {
            configBean.setBtnText("确定","取消","去开启wifi");
        }

        configBean.show();



    }

    private void toSettings() {

    }

    private   void download(UpdateInfo bean, boolean quitely, Activity activity) {
       DownloadBuilder builder =  MyNetApi2.buildDownloadRequest(bean.downloadUrl);
        if(!TextUtils.isEmpty(bean.md5)){
            builder.verifyMd5(bean.md5);
        }
        if(!quitely){
            builder.showLoadingDialog(activity);
        }
            builder.setOpenAfterSuccess()
                    .callback(new MyNetListener() {
                        @Override
                        public void onSuccess(Object o, String s) {
                            toast("下载完成");

                        }

                        @Override
                        public void onError(String msgCanShow) {
                            super.onError(msgCanShow);
                            toast(msgCanShow);
                        }
                    })
                    .get();

    }

}
