# Cordova Plugin ShareSDK

### 说明

本插件在[cordova-plugin-sharesdk](https://github.com/behring/cordova-plugin-sharesdk)基础上添加客户端登录功能，android增加一键分享(onekeyshare)功能

#### cordova项目

1. 进入cordova项目目录。

```powershell
cd ~/yourpath/cordovaproject
```

2. 安装cordova-plugin-sharesdk，使用申请应用信息替换xxxx

```powershell
cordova plugin add https://github.com/shanquan/cordova-plugin-sharesdk.git --variable SHARESDK_ANDROID_APP_KEY=xxxx --variable SHARESDK_IOS_APP_KEY=xxxx --variable WECHAT_APP_ID=xxxx --variable WECHAT_APP_SECRET=xxxx --variable WEIBO_APP_ID=xxxx --variable WEIBO_APP_SECRET=xxxx --variable WEIBO_REDIRECT_URL=http://xxxx --variable QQ_IOS_APP_ID=xxxx --variable QQ_IOS_APP_HEX_ID=xxxx --variable QQ_IOS_APP_KEY=xxxx --variable QQ_ANDROID_APP_ID=xxxx --variable QQ_ANDROID_APP_KEY=xxxx
```

3. 重新构建cordova项目。

```powershell
cordova build
```

4. js调用onekeyshare和授权登录

```javascript
//shareSDK一键菜单分享 仅支持android
sharesdk.onekeyShare(success,fail);

//QQ登录
sharesdk.getAuthorize(ShareSDK.ClientType.QQ, success, fail);
//微信登录
sharesdk.getAuthorize(ShareSDK.ClientType.Wechat, success, fail);
//微博登录
sharesdk.getAuthorize(ShareSDK.ClientType.SinaWeibo, success, fail);

//获取QQ用户信息
sharesdk.getUserInfo(ShareSDK.ClientType.QQ, success, fail);
//获取微信用户信息
sharesdk.getUserInfo(ShareSDK.ClientType.Wechat, success, fail);
//获取微博用户信息
sharesdk.getUserInfo(ShareSDK.ClientType.SinaWeibo, success, fail);

function success(info) {
    if (info) {
        alert('success!' + JSON.stringify(info));
    }
}

function fail(msg) {
    if (msg.state == ShareSDK.ResponseState.Cancel) {
        // alert('cancel！');
    } else {
        alert('failed！: ' + msg.error);
    }
}
```

### Demo地址

https://github.com/shanquan/cordova-sharesdk-demo

