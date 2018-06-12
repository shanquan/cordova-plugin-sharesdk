package behring.cordovasharesdk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import cn.sharesdk.onekeyshare.OnekeyShare;


/**
 * This class ShareSDKPlugin a string called from JavaScript.
 */
public class ShareSDKPlugin extends CordovaPlugin {
    private static final int SINA_WEIBO_CLIENT = 1;
    private static final int WECHAT_CLIENT = 2;
    private static final int QQ_CLIENT = 3;
    private static final int Q_ZONE_CLIENT = 6;

    /**平台和分享类型的值参考ShareSDK ios源码中的值*/
    /** 新浪微博 */
    private final int SSDKPlatformTypeWeibo = 1;
    /** QQ空间 */
    private final int SSDKPlatformTypeQZone = 6;
    /** QQ 好友 */
    private final int SSDKPlatformTypeQQFriend = 24;
    /** 拷贝 */
    private final int SSDKPlatformTypeCopy = 21;
    /** 微信好友 */
    private final int SSDKPlatformTypeWechatSession = 22;
    /** 微信朋友圈 */
    private final int SSDKPlatformTypeWechatTimeline = 23;

    private static final int SHARE_TEXT = 1;
    private static final int SHARE_IMAGE = 2;
    private static final int SHARE_WEBPAGE = 3;

    /**参考sharesdk中ios枚举型:SSDKResponseState*/
    private static final int RESPONSE_STATE_BEGIN = 0;
    private static final int RESPONSE_STATE_SUCCESS = 1;
    private static final int RESPONSE_STATE_FAIL = 2;
    private static final int RESPONSE_STATE_CANCEL = 3;

    private CallbackContext callbackContext;
    private PlatformActionListener platformActionStateListener = new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            if (i == Platform.ACTION_USER_INFOR) {
                Log.v("sq",platform.getDb().exportData());
                if(callbackContext!=null)
                    callbackContext.success(platform.getDb().exportData());
            }else{
                if(callbackContext!=null)
                    callbackContext.success();
            }
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            if(callbackContext!=null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.putOpt("state", RESPONSE_STATE_FAIL);
                    jsonObject.putOpt("error", throwable.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    callbackContext.error(jsonObject);
                }
            }
        }

        @Override
        public void onCancel(Platform platform, int i) {
            if(callbackContext!=null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.putOpt("state", RESPONSE_STATE_CANCEL);
                    jsonObject.putOpt("error","cancel");
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    callbackContext.error(jsonObject);
                }
            }
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        ShareSDK.initSDK(cordova.getActivity());
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("share")) {
            int platformType = args.optInt(0);
            int shareType = args.optInt(1);
            JSONObject shareInfo = args.optJSONObject(2);
            this.share(platformType, shareType, shareInfo, callbackContext);
            return true;
        } else if(action.equals("isInstallClient")) {
            int clientType = args.optInt(0);
            isInstallClient(clientType, callbackContext);
            return true;
        } else if(action.equals("onekeyShare")){
            OnekeyShare oks = new OnekeyShare();
            oks.setSilent(false);
            // oks.disableSSOWhenAuthorize();
            View view = webView.getView();
            oks.setViewToShare(view);
            oks.setCallback(platformActionStateListener);
            oks.show(cordova.getActivity());
            return true;
        } else if(action.equals("getAuthorize")||action.equals("getUserInfo")){
            int clientType = args.optInt(0);
            Platform platform = null;
            switch (clientType) {
                case SINA_WEIBO_CLIENT:
                    platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                    break;
                case WECHAT_CLIENT:
                    platform = ShareSDK.getPlatform(Wechat.NAME);
                    break;
                case QQ_CLIENT:
                    platform = ShareSDK.getPlatform(QQ.NAME);
                    break;
                default:
                    break;
            }
            if(platform!=null){
                platform.setPlatformActionListener(platformActionStateListener);
                if(action.equals("getAuthorize")){
                    platform.authorize();
                }else if(action.equals("getUserInfo")){
                    platform.showUser(null);
                }
            }else if(callbackContext!=null){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.putOpt("error", "wrong platform！");
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    callbackContext.error(jsonObject);
                }
            }
            return true;
        }
        return false;
    }

    private void share(int platformType, int shareType, JSONObject shareInfo, CallbackContext callbackContext) {

        switch (shareType) {
            case SHARE_TEXT:
                if(platformType == SSDKPlatformTypeCopy) {
                    this.copyLink(shareInfo, callbackContext);
                }else {
                    this.shareText(platformType, shareInfo, callbackContext);
                }

                break;
            case SHARE_IMAGE:
                this.shareImage(platformType, shareInfo, callbackContext);
                break;
            case SHARE_WEBPAGE:
                this.shareWebPage(platformType, shareInfo, callbackContext);
                break;
            default:
                break;
        }
    }

    private void isInstallClient(int clientType, CallbackContext callbackContext) {
        boolean isInstallClient;
        Platform platform = null;
        switch (clientType) {
            case SINA_WEIBO_CLIENT:
                platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                break;
            case WECHAT_CLIENT:
                platform = ShareSDK.getPlatform(Wechat.NAME);
                break;
            case QQ_CLIENT:
                platform = ShareSDK.getPlatform(QQ.NAME);
                break;
            case Q_ZONE_CLIENT:
                platform = ShareSDK.getPlatform(QZone.NAME);
            break;
            default:
                break;
        }

        if (platform != null) {
            isInstallClient = platform.isClientValid();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, isInstallClient);
            callbackContext.sendPluginResult(pluginResult);
        }else {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            callbackContext.sendPluginResult(pluginResult);
        }

    }

    private void copyLink(JSONObject shareInfo, final CallbackContext callbackContext) {
        ClipboardManager myClipboard = (ClipboardManager)cordova.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        String text = shareInfo.optString("text");
        ClipData clipData = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(clipData);
        callbackContext.success();
    }

    private void shareText(int platformType, JSONObject shareInfo, final CallbackContext callbackContext) {
        Platform.ShareParams sp = null;
        Platform platform = null;
        switch (platformType) {
            case SSDKPlatformTypeWechatSession:
                sp = new Wechat.ShareParams();
                sp.setShareType(Platform.SHARE_TEXT);
                platform = ShareSDK.getPlatform(Wechat.NAME);
                break;
            case SSDKPlatformTypeWechatTimeline:
                sp = new WechatMoments.ShareParams();
                sp.setShareType(Platform.SHARE_TEXT);
                platform = ShareSDK.getPlatform(WechatMoments.NAME);
                break;
            case SSDKPlatformTypeWeibo:
                sp = new SinaWeibo.ShareParams();
                platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                break;
            case SSDKPlatformTypeQQFriend:
                sp = new QQ.ShareParams();
                platform = ShareSDK.getPlatform(QQ.NAME);
                break;
            case SSDKPlatformTypeQZone:
                sp = new QQ.ShareParams();
                platform = ShareSDK.getPlatform(QZone.NAME);
                break;
            default:
                break;
        }

        sp.setText(shareInfo.optString("text"));
        platform.setPlatformActionListener(platformActionStateListener);
        platform.share(sp);
    }

    private void shareImage(int platformType, JSONObject shareInfo, final CallbackContext callbackContext) {
        Platform.ShareParams sp = null;
        Platform platform = null;
        switch (platformType) {
            case SSDKPlatformTypeWechatSession:
                sp = new Wechat.ShareParams();
                sp.setShareType(Platform.SHARE_IMAGE);
                platform = ShareSDK.getPlatform(Wechat.NAME);
                break;
            case SSDKPlatformTypeWechatTimeline:
                sp = new WechatMoments.ShareParams();
                sp.setShareType(Platform.SHARE_IMAGE);
                platform = ShareSDK.getPlatform(WechatMoments.NAME);
                break;
            case SSDKPlatformTypeWeibo:
                sp = new SinaWeibo.ShareParams();
                platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                break;
            case SSDKPlatformTypeQQFriend:
                sp = new QQ.ShareParams();
                platform = ShareSDK.getPlatform(QQ.NAME);
                break;
            case SSDKPlatformTypeQZone:
                sp = new QQ.ShareParams();
                platform = ShareSDK.getPlatform(QZone.NAME);
                break;
            default:
                break;
        }

        String [] imagePath = shareInfo.optString("image").split(":");
        if(imagePath[0].substring(0,4).equals("http")){
            sp.setImageUrl(shareInfo.optString("image"));
        }else{
            sp.setImagePath(shareInfo.optString("image"));
        }
        platform.setPlatformActionListener(platformActionStateListener);
        platform.share(sp);
    }

    private void shareWebPage(int platformType, JSONObject shareInfo, final CallbackContext callbackContext) {
        Platform.ShareParams sp = null;
        Platform platform = null;
        switch (platformType) {
            case SSDKPlatformTypeWechatSession:
                sp = new Wechat.ShareParams();
                sp.setShareType(Platform.SHARE_WEBPAGE);
                platform = ShareSDK.getPlatform(Wechat.NAME);
                break;
            case SSDKPlatformTypeWechatTimeline:
                sp = new WechatMoments.ShareParams();
                sp.setShareType(Platform.SHARE_WEBPAGE);
                platform = ShareSDK.getPlatform(WechatMoments.NAME);
                break;
            case SSDKPlatformTypeWeibo:
                sp = new SinaWeibo.ShareParams();
                platform = ShareSDK.getPlatform(SinaWeibo.NAME);
                break;
            case SSDKPlatformTypeQQFriend:
                sp = new QQ.ShareParams();
                platform = ShareSDK.getPlatform(QQ.NAME);
                break;
            case SSDKPlatformTypeQZone:
                sp = new QQ.ShareParams();
                platform = ShareSDK.getPlatform(QZone.NAME);
                break;
            default:
                break;
        }

        sp.setImageUrl(shareInfo.optString("image"));
        sp.setTitle(shareInfo.optString("title"));
        sp.setUrl(shareInfo.optString("url"));
        sp.setText(shareInfo.optString("text"));
        platform.setPlatformActionListener(platformActionStateListener);
        platform.share(sp);
    }
}