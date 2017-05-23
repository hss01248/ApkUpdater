# ApkUpdater
[![](https://jitpack.io/v/hss01248/ApkUpdater.svg)](https://jitpack.io/#hss01248/ApkUpdater)

based on HttpUtil和DialogUtil

封装apk更新的逻辑



# usage

## gradle

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
```

**Step 2.** Add the dependency

```
    dependencies {
            compile 'com.github.hss01248:ApkUpdater:1.0.4'
    }
```



## 初始化-在application的oncreate方法中:

配置检查更新的url,以及json对应的javabean,并提供了一个接口,用于将用户javabean中的字段逐个拷贝到内部框架使用的bean.

```
ApkUpdater.getInstance()
                .init(this, "http://api.qxinli.com:9001/api/version/latestVersion/v1.json", UpdateModel.class,
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
```

## 同时,添加生命周期callback(必须):

> 在onActivityResumed中调用ApkUpdater.onActivityResumed(activity);

```
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
```





> 不要在activity的onstart中调用,不要在application的oncreate中调用

```
/**
     *
     * @param showLoadingInfo 是否显示"正在获取更新信息"
     * @param showAskDownload 是否询问用户"是否下载安装",如果为false,则直接下载
     * @param showProgressOrNotify  true:显示下载进度dialog,  false: 下载进度:显示notification
     */
    public   void update(final boolean showLoadingInfo, final boolean showAskDownload, final boolean showProgressOrNotify)
```

检测更新信息时弹出"loading"对话框-可以控制显示或不显示

检查到有更新时:

弹出更新的信息对话框,选择是否更新.如果不是wifi,有相关提示信息   --可以选择是否弹出

弹出进度对话框显示下载进度 --可以选择是dialog还是notification的形式

下载完成后弹出apk安装界面

## 常用场景
* splash或mainactivity界面,偷偷检查是否有更新,如果有,弹出提示,让用户选择是否更新:
  update(false,true,true)
* 手动点击按钮"检查更新": update(true,true,true)
* 强制更新: 服务器下发的 info.isForceUpdate算得为true时,即可强制更新







# UI

> 下图按钮颜色暂时写死成ios蓝

 ![wifi](img/wifi.jpg)

 ![nowifi](img/nowifi.jpg)

> 下图颜色由colorAccent决定,可自行配置

  ![progress](img/progress.jpg)
