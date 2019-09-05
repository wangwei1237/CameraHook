package com.wangwei.camerahook;

import android.app.Application;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera implements IXposedHookLoadPackage {
    private final String TAG = "HookCamera";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("Loaded app: " + lpparam.packageName);
        Log.d(TAG, "Loaded app: " + lpparam.packageName );

        // Xposed模块自检测
        if (lpparam.packageName.equals("com.wangwei.camerahook")){
            XposedHelpers.findAndHookMethod("com.wangwei.camerahook.MainActivity",
                    lpparam.classLoader,
                    "isModuleActive",
                    XC_MethodReplacement.returnConstant(true));
        }

        if (lpparam.packageName.equals("com.wangwei.camerahook")) {
            XposedHelpers.findAndHookMethod("com.wangwei.camerahook.MainActivity",
                    lpparam.classLoader,
                    "getMessageInfo",
                    String.class,
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            //param.setResult("i am new result! before. ");
                            String para1 = (String)param.args[0];
                            String para2 = (String)param.args[1];

                            Log.d(TAG, "para1 before hook is : " + para1);
                            Log.d(TAG, "para2 before hook is : " + para2);

                            param.args[0] = para1 + '1';
                            param.args[1] = para2 + '2';
                        }
                    }
            );
        }



        if (lpparam.packageName.equals("com.ss.android.ugc.aweme")) {
            XposedHelpers.findAndHookMethod("android.hardware.Camera",
                    lpparam.classLoader,
                    "setPreviewTexture",
                    SurfaceTexture.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            //param.setResult("i am new result! before. ");
                            SurfaceTexture para1 = (SurfaceTexture) param.args[0];

                            Log.d(TAG, "SurfaceTexture class is : " + para1.toString());
                        }
                    }
            );

            XposedHelpers.findAndHookMethod("android.graphics.SurfaceTexture",
                    lpparam.classLoader,
                    "setOnFrameAvailableListener",
                    SurfaceTexture.OnFrameAvailableListener.class,
                    Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            //param.setResult("i am new result! before. ");

                            Log.d(TAG, " setOnFrameAvailableListener handler is : " );
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(Application.class,
                    "attach",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            ClassLoader cl     = ((Context)param.args[0]).getClassLoader();
                            Class<?> hookclass = null;

                            try {
                                hookclass = cl.loadClass("com.ss.android.ttvecamera.f.c");
                                Log.d(TAG, "查找抖音Camera类成功啦啦");
                                Log.d(TAG, "hookclass is : " + hookclass.toString());
                            } catch (Exception e) {
                                Log.e(TAG, "查找抖音Camera类出错啦", e);
                                return;
                            }

                            XposedHelpers.findAndHookMethod(hookclass,
                                    "a",
                                    new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            super.beforeHookedMethod(param);

                                            /*
                                            byte[] data = (byte[])param1.args[0];
                                            Camera camera = (Camera)param1.args[1];

                                            Camera.Parameters cp = camera.getParameters();
                                            Camera.Size size     = cp.getPreviewSize();
                                            int width  = size.width;
                                            int height = size.height;
                                            */

                                            Log.d(TAG, "The preview width is : ");
                                            Log.d(TAG, "The preview height is : ");
                                            Log.d(TAG, "this this.b.c() is : ");
                                        }
                                    });
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(Application.class,
                    "attach",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            ClassLoader cl     = ((Context)param.args[0]).getClassLoader();
                            Class<?> hookclass = null;

                            try {
                                hookclass = cl.loadClass("com.ss.android.ttvecamera.f.e$1");
                                Log.d(TAG, "查找抖音f.e$1类成功啦啦");
                                Log.d(TAG, "hookclass is : " + hookclass.toString());
                            } catch (Exception e) {
                                Log.e(TAG, "查找抖音f.e$1类出错啦", e);
                                return;
                            }

                            XposedHelpers.findAndHookMethod(hookclass,
                                    "onFrameAvailable",
                                    SurfaceTexture.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            super.beforeHookedMethod(param);
                                            SurfaceTexture st = (SurfaceTexture) param.args[0];
                                            Log.d(TAG, "onFrameAvailable is called.");
                                            Log.d(TAG, "onFrameAvailable(SurfaceTexture) st is: " + st.toString());
                                        }
                                    });
                        }
                    }
            );

        }
    }
}
