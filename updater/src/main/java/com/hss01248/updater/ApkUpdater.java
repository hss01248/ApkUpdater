package com.hss01248.updater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.config.ConfigBean;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.hss01248.net.builder.DownloadBuilder;
import com.hss01248.net.builder.JsonRequestBuilder;
import com.hss01248.net.util.MyActyManager;
import com.hss01248.net.wrapper.HttpUtil;
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

    public static void onActivityResumed(Activity activity){
        MyActyManager.getInstance().setCurrentActivity(activity);
        com.hss01248.dialog.MyActyManager.getInstance().setCurrentActivity(activity);
    }


    public  void init(Context context,String url,Class<T> updateBean,ObjectCopyable<T> copyable){
        if(HttpUtil.context == null){
            HttpUtil.init(context,"http://api.qxinli.com/");
            HttpUtil.context = context;
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
                return mWiFiNetworkInfo.isConnected();
            }
        }
        return false;
    }

    /**
     *
     * @param showLoadingInfo 是否显示"正在获取更新信息"
     * @param showAskDownload 是否询问用户"是否下载安装",如果为false,则直接下载
     * @param showProgressOrNotify  true:显示下载进度dialog,  false: 下载进度:显示notification
     */
    public   void update(final boolean showLoadingInfo, final boolean showAskDownload, final boolean showProgressOrNotify){
      JsonRequestBuilder builder =  HttpUtil.buildJsonRequest(url,updateBean);
        if(showLoadingInfo){
            builder.showLoadingDialog("获取更新信息");
        }

        builder.getAsync(new MyNetListener<T>() {


            @Override
            public void onSuccess(T bean, String s,boolean iscache) {
                StyledDialog.dismissLoading();
                UpdateInfo info = copyable.copyValues(bean);

                int versionCode = getAPPVersionCodeFromAPP(context);
                if(versionCode >=info.versionCode){
                    if(!showLoadingInfo){
                        toast("已经是最新版本");
                    }
                    return;
                }
                //有新版本
                boolean isWifiConnected = isWifiConnected(context);

                Log.e("dd","是不是wifi:"+isWifiConnected);


                if(isWifiConnected){
                    if( info.isForceUpdate || !showAskDownload){
                        download(info,false);
                    }else {
                        showInfoDialog(info,showProgressOrNotify,isWifiConnected);
                    }
                }else {//不是wifi
                    if(showAskDownload){
                        showInfoDialog(info,showProgressOrNotify,isWifiConnected);
                    }
                }



            }

            @Override
            public void onError(String msgCanShow) {
                super.onError(msgCanShow);
                if(!showLoadingInfo)
                toast(msgCanShow);
            }
        });


    }

    private  void showInfoDialog(final UpdateInfo bean, final boolean showProgressOrNotify, boolean isWifiConnected) {
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

        builder.append(bean.dec).append("\n\n");

        if(!isWifiConnected){
            builder.append("注意:当前不是wifi环境,您可以去切换到wifi或者直接下载");
        }



      ConfigBean configBean =  StyledDialog.buildMdAlert( title, builder.toString(), new MyDialogListener() {
            @Override
            public void onFirst() {
                download(bean,showProgressOrNotify);
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
            configBean.setBtnText("下载安装","取消","");
        }else {
            configBean.setBtnText("下载安装","取消","去开启wifi");
        }

        configBean.show();



    }

    private void toSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private   void download(UpdateInfo bean, final boolean showProgressOrNotify) {
       DownloadBuilder builder =  HttpUtil.buildDownloadRequest(bean.downloadUrl);
        if(!TextUtils.isEmpty(bean.md5)){
            builder.verifyMd5(bean.md5);
        }
        if(showProgressOrNotify){
            builder.showLoadingDialog();
        }
            builder.setOpenAfterSuccess()
                    .getAsync(new MyNetListener() {
                        @Override
                        public void onSuccess(Object o, String s,boolean iscache) {
                            if(showProgressOrNotify)
                            toast("下载完成");

                        }

                        @Override
                        public void onError(String msgCanShow) {
                            super.onError(msgCanShow);
                            if(showProgressOrNotify)
                            toast(msgCanShow);
                        }
                    });


    }

}
