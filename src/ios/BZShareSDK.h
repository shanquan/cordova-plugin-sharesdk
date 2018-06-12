#import <Cordova/CDVPlugin.h>

//继承CDVPlugin类
@interface BZShareSDK : CDVPlugin

//接口方法， command.arguments[0]获取前端传递的参数
- (void)share:(CDVInvokedUrlCommand*)command;
- (void)isInstallClient:(CDVInvokedUrlCommand*)command;
- (void)getAuthorize:(CDVInvokedUrlCommand*)command;
- (void)getUserInfo:(CDVInvokedUrlCommand*)command;
- (void)onekeyShare:(CDVInvokedUrlCommand*)command;
@end
