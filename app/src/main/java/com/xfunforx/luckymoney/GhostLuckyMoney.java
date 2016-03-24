package com.xfunforx.luckymoney;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class GhostLuckyMoney implements IXposedHookLoadPackage {

    boolean robot = false;
    boolean hasgothongbao = true;
    boolean toggle = false;
    Context context;

    private void log(String tag, Object msg) {

        ;
//        XposedBridge.log(tag + " " + msg.toString());
    }

    private String readxml(String xmlstr) {
        int i = xmlstr.indexOf("<msg>");
        String xml = xmlstr.substring(i);
        String keyurl = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xml));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("nativeurl")) {
                        xpp.nextToken();
                        keyurl = xpp.getText();
                        break;
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyurl;
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {



        try {if (!loadPackageParam.packageName.contains("tencent.mm")) {
            return;
        }
            //new message is coming here
            Class b = findClass("com.tencent.mm.booter.notification.b", loadPackageParam.classLoader);
            findAndHookMethod("com.tencent.mm.booter.notification.b", loadPackageParam.classLoader, "a", b, String.class, String.class, int.class, int.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    toggle = (Boolean) XposedHelpers.callStaticMethod(findClass("com.tencent.mm.g.a", loadPackageParam.classLoader), "pb");
                    // FIXME: 15/12/28 for wechat 6.3.8 pb
                    log("toggle is:" , String.valueOf(toggle));
                    if (toggle && param.args[3].toString().equals("436207665")) {
                        String nativeurl = readxml(param.args[2].toString());
                        context = (Context) XposedHelpers.callStaticMethod(findClass("com.tencent.mm.sdk.platformtools.z", loadPackageParam.classLoader), "getContext");
                        Intent intent = new Intent();
                        intent.setClassName("com.tencent.mm", "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI");
                        int key;
                        if (param.args[1].toString().endsWith("@chatroom")) {
                            key = 0;
                        } else {
                            key = 1;
                        }
                        intent.putExtra("key_way", key);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("key_native_url", nativeurl);
                        intent.putExtra("key_username", param.args[1].toString());
                        log("made intent :", intent.getExtras().toString());
                        robot = true;
                        context.startActivity(intent);
                    }
                }
            });
            // FIXME: 15/12/28 for wechat 6.3.8
            findAndHookMethod("com.tencent.mm.model.bc", loadPackageParam.classLoader, "ai", boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

                    findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            if (robot){
                                Activity activity = (Activity) methodHookParam.thisObject;
                                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            }
                            XposedBridge.invokeOriginalMethod(methodHookParam.method,methodHookParam.thisObject,methodHookParam.args);
                            return null;
                        }
                    });
                    findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!hasgothongbao) {
                                hasgothongbao = true;
                                XposedHelpers.callMethod(param.thisObject,"finish");
                            }
                        }
                    });
                    Class j = findClass("com.tencent.mm.q.j", loadPackageParam.classLoader);
                    // button is ready for click here
                    findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", loadPackageParam.classLoader, "e", int.class, int.class, String.class, j, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (robot) {
                                log("if robot so hook for auto click", "hongbao");
                                Class receiveui = findClass("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", loadPackageParam.classLoader);
                                Button button = (Button) XposedHelpers.callStaticMethod(receiveui, "e", param.thisObject);
                                log("the button text will change ,so get the text for use", button.getText());
                                if (button.getText() != "" && button.isShown()) {
                                    hasgothongbao = false;
                                     Random rd = new Random();
                                   
                                   Thread.sleep(rd.nextInt(1600));
                                    button.performClick();
<<<<<<< HEAD
                                    log("click", "get luckymoney button");
                                    // FIXME: 15/12/30 maybe fix the luckymoney is over,so maybe didnot close the window
                                }else{
//                                } else if (!button.isShown()) {
=======
                                } else if (!button.isShown()) {
>>>>>>> origin/master
                                    XposedHelpers.callMethod(param.thisObject, "finish");
                                    Toast.makeText(context, "(^O^)", Toast.LENGTH_SHORT).show();
                                }
                                robot = false;
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
