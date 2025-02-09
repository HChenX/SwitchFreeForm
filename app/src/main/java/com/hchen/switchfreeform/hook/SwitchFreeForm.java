/*
 * This file is part of SwitchFreeForm.

 * SwitchFreeForm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.switchfreeform.hook;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.StaticLayout;
import android.util.Pair;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCData;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.SystemPropTool;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import dalvik.system.PathClassLoader;

/**
 * 自动切换小窗
 *
 * @author 焕晨HChen
 */
public class SwitchFreeForm extends BaseHC {
    private static boolean isReadyExpandToFullScreen = false;
    private static boolean isReadySwitchFreeForm = false;
    @Deprecated
    private static Object mMiuiFreeformAnimation;
    @Deprecated
    private static Object mMultiTaskingTaskRepository;
    private static String mPackageName = null;
    private static Object mMiuiFreeformModeTaskInfo;
    private static Object mForegroundInfoListener = null;
    private static boolean isAnimatorStarted = false;
    private static boolean isAlwaysSwitchFreeForm = false;
    private static int mSwitchFreeFormThreshold = 800;
    @Deprecated
    private static StaticLayout mFullscreenStaticLayout;
    private final static ConcurrentLinkedQueue<Runnable> mRunnableQueue = new ConcurrentLinkedQueue<>();
    private static final Runnable QUEUE_PROCESSOR = SwitchFreeForm::processNextTask;
    private final static Handler mHandler = new Handler(Looper.getMainLooper());
    private final static HashMap<String, Pair<Rect, Float>> mFreeFormRect2Scale = new HashMap<>();

    @Override
    protected void onApplicationAfter(Context context) {
        try {
            registerForegroundWindowListener();
        } catch (Throwable e) {
            logE(TAG, e);
        }
    }

    @Override
    protected void init() {
        try {
            isAlwaysSwitchFreeForm = SystemPropTool.getProp("persist.hchen.switch.freeform.always", false);
            mSwitchFreeFormThreshold = SystemPropTool.getProp("persist.hchen.switch.freeform.threshold", 800);
        } catch (Throwable e) {
            isAlwaysSwitchFreeForm = false;
            mSwitchFreeFormThreshold = 800;
            logE(TAG, e);
        }

        hookMethod("com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeMoveHandler",
            "onBottomCaptionHandleMotionEvents",
            float.class, float.class, PointF.class,
            "com.android.wm.shell.multitasking.taskmanager.MiuiFreeformModeTaskInfo",
            int.class,
            new IHook() {
                @Override
                public void after() {
                    if (isAlwaysSwitchFreeForm) return;

                    float f6;
                    float f5 = (float) getArgs(1);
                    PointF pointF = (PointF) getArgs(2);
                    int round = Math.round(f5 - pointF.y);

                    boolean isLandscape = (boolean) callStaticMethod("com.android.wm.shell.multitasking.common.MultiTaskingDisplayInfo", "isLandscape");
                    f6 = isLandscape ? 25.0f : 300.f;
                    isReadyExpandToFullScreen = round >= f6;
                    if (isReadyExpandToFullScreen && !isReadySwitchFreeForm) {
                        isReadySwitchFreeForm = round >= mSwitchFreeFormThreshold;
                        Context mContext = (Context) getThisField("mContext");
                        if (isReadySwitchFreeForm) {
                            callMethod(
                                callStaticMethod(
                                    "com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVibrateHelper",
                                    "getInstance",
                                    mContext
                                ),
                                "hapticFeedback",
                                callStaticMethod("android.util.MiuiMultiWindowUtils", "getHapticNormal"),
                                false,
                                mContext
                            );
                        }
                    } else if (round < mSwitchFreeFormThreshold)
                        isReadySwitchFreeForm = false;
                }
            }
        );

        hookConstructor("com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVisualIndicator",
            Context.class, "com.android.wm.shell.common.DisplayController",
            "com.android.wm.shell.multitasking.taskmanager.MiuiFreeformModeTaskInfo",
            "com.android.wm.shell.RootTaskDisplayAreaOrganizer",
            new IHook() {
                @Override
                public void after() {
                    mMiuiFreeformModeTaskInfo = getArgs(2);
                    mPackageName = (String) getField(mMiuiFreeformModeTaskInfo, "mPackageName");
                }
            }
        );

        hookMethod("com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVisualIndicator",
            "startReleaseFullscreenIndicatorAnimation",
            new IHook() {
                @Override
                public void after() {
                    if (!isReadySwitchFreeForm && !isAlwaysSwitchFreeForm) return;

                    isAnimatorStarted = true;
                    final Context context = (Context) getThisField("mContext");
                    Rect rect = (Rect) getField(mMiuiFreeformModeTaskInfo, "mBounds");
                    float scale = (float) getField(mMiuiFreeformModeTaskInfo, "mScale");
                    mFreeFormRect2Scale.put(mPackageName, new Pair<>(rect, scale));
                    AndroidLog.logI(TAG, "add rect map! Package: " + mPackageName + ", Rect: " + rect + ", Scale: " + scale);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final String mPackageName = (String) getField(mForegroundInfoListener, "mLastForegroundPackageName");
                            mRunnableQueue.offer(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPackageName == null) return;
                                    if (mPackageName.equals("com.miui.home")) return;
                                    AndroidLog.logI(TAG, "mLastForegroundPackageName: " + mPackageName + ", time: " + System.currentTimeMillis());

                                    startFreeForm(context, mPackageName);
                                }
                            });
                        }
                    }, 80);

                }
            }
        );

        hookMethod("com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVisualIndicator.VisualIndicatorAnimator$2",
            "onAnimationEnd", Animator.class,
            new IHook() {
                @Override
                public void after() {
                    if (!isReadySwitchFreeForm && !isAlwaysSwitchFreeForm) return;
                    if (!isAnimatorStarted) return;

                    mHandler.postDelayed(QUEUE_PROCESSOR, 300);
                    isAnimatorStarted = false;
                }
            }
        );
    }

    private static void processNextTask() {
        final Runnable task = mRunnableQueue.poll();
        if (task != null) {
            try {
                task.run();
            } catch (Throwable e) {
                logE("SwitchFreeForm", e);
            } finally {
                mHandler.postDelayed(QUEUE_PROCESSOR, 300);
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void startFreeForm(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(packageName);
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        if (queryIntentActivities.isEmpty())
            return;
        ResolveInfo resolveInfo = queryIntentActivities.get(0);

        Intent intent2 = new Intent();
        ComponentName componentName = new ComponentName(packageName, resolveInfo.activityInfo.name);
        intent2.setAction("android.intent.action.MAIN");
        intent2.addCategory("android.intent.category.LAUNCHER");
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2.setComponent(componentName);

        ActivityOptions options;
        if (mFreeFormRect2Scale.get(packageName) != null) {
            Pair<Rect, Float> pair = mFreeFormRect2Scale.get(packageName);
            assert pair != null;
            Rect rect = pair.first;
            int top = rect.top;
            int left = rect.left;
            options = (ActivityOptions) callStaticMethod(
                "android.util.MiuiMultiWindowUtils",
                "getActivityOptions",
                context, packageName, true, left, top, true
            );
            if (options == null) return;
            options.setLaunchBounds(rect);
            Object activityOptionsInjector = callMethod(options, "getActivityOptionsInjector");
            callMethod(activityOptionsInjector, "setFreeformScale", pair.second);
            mFreeFormRect2Scale.remove(packageName);
        } else
            options = (ActivityOptions) callStaticMethod(
                "android.util.MiuiMultiWindowUtils",
                "getActivityOptions",
                context, packageName, true, -1, -1, true
            );

        if (options == null) return;
        context.startActivity(intent2, (Bundle) callMethod(options, "toBundle"));
    }

    private void registerForegroundWindowListener() {
        PathClassLoader pathClassLoader = new PathClassLoader(HCData.getModulePath(), classLoader);
        mForegroundInfoListener = newInstance("com.hchen.switchfreeform.hook.ForegroundInfoListener", pathClassLoader);
        if (mForegroundInfoListener == null) return;

        callStaticMethod("miui.process.ProcessManager", "registerForegroundInfoListener", mForegroundInfoListener);
    }
}
