package com.xfunforx.luckymoney;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class Main implements IXposedHookLoadPackage {
    boolean newmsg = false; //new message is coming
    int curmax = -1;
    View lastview = null; //luckymoney item view
    PendingIntent pendingIntent = null;//save intent for jump
    Object chattingui = null;
    int hongbaopfuck = 2131624863;//hongbaoviewid
    boolean hasgothongbao = true; //has got it
    boolean robot = false;

    private void log(String tag, Object msg) {
        XposedBridge.log(tag + " " + msg.toString());
    }

    private View getAllChildrenBFS(View v) {
        //get all the views in listview item
        //this function thanks to stackoverflow user
        // " MH " http://stackoverflow.com/users/1029225/mh
        //
        List<View> visited = new ArrayList<View>();
        List<View> unvisited = new ArrayList<View>();
        unvisited.add(v);
        View foundview = null;
        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);
            log("view classname", child.getClass().getName());
            if (child.getClass().getName().equals("TextView")) {
                log("view text", ((TextView) child).getText());
            }
            log("viewid", child.getId());
            int a = child.getId() - hongbaopfuck;
            if (a == 0) {
                foundview = child;
                return foundview;
            }
            if (!(child instanceof ViewGroup)) {
                continue;
            }
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                unvisited.add(group.getChildAt(i));
            }
        }
        return foundview;
    }

    private void showview() {
        // message doing here
        if (lastview != null) {
            View v = getAllChildrenBFS(lastview);
            if (v != null) {
                log("yes found right view", v.toString());
                robot = true;
                v.performClick();
//                Object cj = XposedHelpers.getObjectField(chattingui, "kbM");
//                Object ck = XposedHelpers.getObjectField(cj, "keC");

//                XposedHelpers.callMethod(ck, "onClick", v);
            }
        }
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

//        ..system app
        if (!lpparam.packageName.contains("tencent")) {
            return;
        }
//            try{
//            for debug log here
//            Class xlog = findClass("com.tencent.mm.xlog.Xlog",lpparam.classLoader);
//            XposedBridge.hookAllMethods(xlog, "logD", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    log("log", param.args[1] + "-" + param.args[2] + "-" + param.args[7]);
//                }
//            });
//            XposedBridge.hookAllMethods(xlog, "logF", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    log("log", param.args[1] + "-" + param.args[2]+"-"+param.args[7]);
//                }
//            });
//            XposedBridge.hookAllMethods(xlog, "logI", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    log("log", param.args[1] + "-" + param.args[2]+"-"+param.args[7]);
//                }
//            });
//            XposedBridge.hookAllMethods(xlog, "logV", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    log("log",param.args[1]+"-"+param.args[2]+"-"+param.args[7]);
//                }
//            });
//            XposedBridge.hookAllMethods(xlog, "logW", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    log("log", param.args[1] + "-" + param.args[2] + "-" + param.args[7]);
//                }
//            });
//        }catch (Exception e){e.printStackTrace();}
//        findAndHookMethod("com.tencent.mm.ao.c", lpparam.classLoader, "yU", String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    log("load yU",param.args[0]);
//            }
//        });


        // must hook late func ai for plugin luckymoney
        findAndHookMethod("com.tencent.mm.model.ba", lpparam.classLoader, "ai", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class j = findClass("com.tencent.mm.q.j", lpparam.classLoader);
                // button is ready for click here
                findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", lpparam.classLoader, "e", int.class, int.class, String.class, j, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (robot) {
                            log("if robot so hook for auto click", "hongbao");
                            Class receiveui = findClass("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI", lpparam.classLoader);
                            Button button = (Button) XposedHelpers.callStaticMethod(receiveui, "e", param.thisObject);
                            log("the button text will change ,so get the text for use", button.getText());
                            if (button.getText() != "") {
                                hasgothongbao = false;
                                button.performClick();
                            }
                            robot = false;
                        }
                    }
                });
                //auto close the LuckyMoneyDetailUI activity
                findAndHookMethod("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!hasgothongbao) {
                            log("hongbao result ", "whill auto finish if have got");
                            XposedHelpers.callMethod(param.thisObject, "finish");
                            hasgothongbao = true;
                        }
                    }
                });
            }
        });
        try {
            //new message is coming here
            Class b = findClass("com.tencent.mm.booter.notification.b", lpparam.classLoader);
            findAndHookMethod("com.tencent.mm.booter.notification.b", lpparam.classLoader, "a", b, String.class, String.class, int.class, int.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("fuckmm id", param.args[1].toString());
                    log("fuckmm content", param.args[2].toString());
                    log("fuckmm mess type", param.args[3].toString());
                    int msgtype = Integer.valueOf(param.args[3].toString());
                    if (msgtype != 436207665){
                        return;
                    }
                    newmsg = true;
                    //436207665 hongbao type
                    if (null != pendingIntent) {
                        log("jump to ", "pendding intent");
                        pendingIntent.send();
                    } else {
                        // current chatting can click luckymoney
                        showview();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //save intent for jump
            findAndHookMethod("com.tencent.mm.booter.notification.a.d", lpparam.classLoader, "a", Context.class, int.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    pendingIntent = (PendingIntent) param.getResult();
                    param.setResult(null);
                    log("save intent for jump", "pendding intent");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//      for debug function
//        try {
//            findAndHookMethod("com.tencent.mm.ui.chatting.ck", lpparam.classLoader, "onClick", View.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    log("com.tencent.mm.ui.chatting.ck", "chatting item click");
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            Class ad = findClass("com.tencent.mm.storage.ad", lpparam.classLoader);
//            Class a = findClass("com.tencent.mm.ui.chatting.ChattingUI$a", lpparam.classLoader);
//            findAndHookMethod("com.tencent.mm.ui.chatting.z", lpparam.classLoader, "a", View.class, a, ad, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    log("com/tencent/mm/ui/chatting/z", "hongbao clicked");
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        // save listview item view here the get the first itemview
        try {
            findAndHookMethod("com.tencent.mm.ui.chatting.cj", lpparam.classLoader, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("fuckmm ", "listview getView");
                    int tempmax = (int) param.args[0];
                    log("temp id", tempmax);
                    if (tempmax > curmax) {
                        curmax = tempmax;
                        log("lastmax update to", curmax);
                        lastview = (View) param.getResult();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a", lpparam.classLoader, "onPause", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("ChattingUI$a", "after onresume set curmax to -1");
                    curmax = -1;
                }
            });
            findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a", lpparam.classLoader, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("ChattingUI$a", "after onresume set curmax to -1");
                    chattingui = param.thisObject;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //must wait the chattingui scroll to the last ,so luckymoney ui is visable
            findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a$55", lpparam.classLoader, "run", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("ChattingUI$a$55", "scroll to last");
                    if (newmsg) {
                        showview();
                        newmsg = false;
                        pendingIntent = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //for get notification inten
            findAndHookMethod("com.tencent.mm.g.a", lpparam.classLoader, "pi", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
