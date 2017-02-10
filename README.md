# ApkUpdater
[![](https://jitpack.io/v/hss01248/ApkUpdater.svg)](https://jitpack.io/#hss01248/ApkUpdater)

based on netWrapper ,封装apk更新的逻辑





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
            compile 'com.github.hss01248:ApkUpdater:1.0.0'
    }
```



## 初始化

配置检查更新的url,以及json对应的javabean,并提供了一个接口,用于将用户javabean中的字段逐个拷贝到内部框架使用的bean.

```
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
```

## 手动点击"检查更新"

```
ApkUpdater.getInstance().update(false,SettingsActivity.this);
```

检测更新信息时弹出"loading"对话框

检查到有更新时:

弹出更新的信息对话框,选择是否更新.如果不是wifi,有相关提示信息

弹出进度对话框显示下载进度

下载完成后弹出apk安装界面

## 进入首页时"偷偷"拉取更新信息

```
ApkUpdater.getInstance().update(true,MainActivity.this);
```

静默检测更新信息.

检查到有更新时:

如果不是wifi,不下载.

如果是wifi,如果服务器没有要求强制更新,则弹出对话框,让用户选择是否更新,后续同上面手动点击.

如果服务器要求强制更新,则"偷偷"下载,下载完后直接弹出apk安装界面.

