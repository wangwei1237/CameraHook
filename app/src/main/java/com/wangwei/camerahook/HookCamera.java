package com.wangwei.camerahook;

import android.app.Application;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera implements IXposedHookLoadPackage {
    private final String TAG = "HookCamera";
    private SurfaceTexture st = null;

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
                            if (param.args[0] != null) {
                                st = (SurfaceTexture) param.args[0];
                                Log.d(TAG, "SurfaceTexture class is : " + st.toString());
                            }
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

                            try {
                                final Class<?> hookclass = cl.loadClass("com.ss.android.ttvecamera.a");
                                Log.d(TAG, "查找抖音类ttvecamera.a成功啦啦");
                                Log.d(TAG, "hookclass is : " + hookclass.toString());

                                XposedHelpers.findAndHookMethod(hookclass,
                                        "a",
                                        new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                if (st != null) {
                                                    // 将摄像头的previewTexture设置为空，然后将该Texture对象修改。
                                                    Field fieldA = hookclass.getDeclaredField("a");
                                                    fieldA.setAccessible(true);
                                                    Camera camera = (Camera)fieldA.get(param.thisObject);
                                                    camera.setPreviewTexture(null);
                                                    Log.d(TAG, "deattach the camera and surfacetexture first.");

                                                    // 将surfacetexture的生产者设置为video
                                                    Surface mSurface      = new Surface(st);
                                                    MediaPlayer mMediaPlayer  = new MediaPlayer();
                                                    mMediaPlayer.setVolume(0, 0);
                                                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                        @Override
                                                        public void onPrepared(MediaPlayer mp) {
                                                            mp.start();
                                                        }
                                                    });

                                                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                        @Override
                                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                                            mediaPlayer.seekTo(0);
                                                            mediaPlayer.start();
                                                        }
                                                    });

                                                    mMediaPlayer.setSurface(mSurface);
                                                    mSurface.release();

                                                    try {
                                                        mMediaPlayer.setDataSource("/sdcard/DCIM/Camera/video_20190909_172631.mp4");
                                                        mMediaPlayer.prepareAsync();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "查找抖音ttvecamera.a类出错啦", e);
                                return;
                            }


                        }
                    }
            );

        }
    }
}
