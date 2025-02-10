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
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.view.WindowManager;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCData;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.SystemPropTool;

import java.lang.reflect.Method;
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
    private static final String TAG = "SwitchFreeForm";
    private static final Point mPoint = new Point();
    private static boolean mLastIsLandscape = false;
    private static boolean isReadyExpandToFullScreen = false;
    private static boolean isReadySwitchFreeForm = false;
    private static String mPackageName = null;
    private static Object mMiuiFreeformModeTaskInfo;
    private static Object mForegroundInfoListener = null;
    private static boolean isAnimatorStarted = false;
    private static boolean isAlwaysSwitchFreeForm = false;
    private static int mSwitchFreeFormThreshold = 800;
    private static int SWITCH_FREEFORM_START_DELAY = 450;
    private static int SWITCH_FREEFORM_READY_DELAY = 600;
    private static int mScreenY = -1;
    private static final int SWITCH_FREEFORM_READY = 1;
    private final static ConcurrentLinkedQueue<Runnable> mRunnableQueue = new ConcurrentLinkedQueue<>();
    private static final Runnable QUEUE_PROCESSOR = SwitchFreeForm::processNextTask;
    private final static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (SWITCH_FREEFORM_READY == msg.what) {
                Context context = (Context) msg.obj;
                final String mPackageName = (String) getField(mForegroundInfoListener, "mLastForegroundPackageName");
                AndroidLog.logI(TAG, "mLastForegroundPackageName: " + mPackageName + ", time: " + System.currentTimeMillis());
                mRunnableQueue.offer(new Runnable() {
                    @Override
                    public void run() {
                        if (mPackageName == null) return;
                        if (mPackageName.equals("com.miui.home")) return;

                        startFreeForm(context, mPackageName);
                    }
                });

                if (!isAnimatorStarted)
                    mHandler.postDelayed(QUEUE_PROCESSOR, SWITCH_FREEFORM_START_DELAY);
            }
        }
    };
    private final static HashMap<String, Pair<Rect, Float>> mFreeFormRect2Scale = new HashMap<>();

    private static String mMiuiFreeformModeMoveHandlerClass = "com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeMoveHandler";
    private static String mMiuiFreeformModeTaskInfoClass = "com.android.wm.shell.multitasking.taskmanager.MiuiFreeformModeTaskInfo";
    private static String mMultiTaskingDisplayInfoClass = "com.android.wm.shell.multitasking.common.MultiTaskingDisplayInfo";
    private static String mMiuiFreeformModeVibrateHelperClass = "com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVibrateHelper";
    private static String mMiuiFreeformModeVisualIndicatorClass = "com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVisualIndicator";
    private static String mVisualIndicatorAnimator$2Class = "com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModeVisualIndicator.VisualIndicatorAnimator$2";

    @Override
    protected void onApplicationAfter(Context context) {
        try {
            getScreenSize(context);
            registerForegroundWindowListener();
        } catch (Throwable e) {
            logE(TAG, e);
        }
    }

    @Override
    protected void init() {
        boolean isOS1 = existsClass("com.android.wm.shell.miuifreeform.MiuiFreeformModeMoveHandler");
        if (isOS1) updateClassPathToOS1();

        if (!existsClass(mMiuiFreeformModeMoveHandlerClass)) {
            logW(TAG, "Your Device Not Support Switch FreeForm!!!");
            return;
        }

        try {
            isAlwaysSwitchFreeForm = SystemPropTool.getProp("persist.hchen.switch.freeform.always", false);
            mSwitchFreeFormThreshold = SystemPropTool.getProp("persist.hchen.switch.freeform.threshold", 800);
            SWITCH_FREEFORM_START_DELAY = SystemPropTool.getProp("persist.hchen.switch.freeform.debug.start.delay", SWITCH_FREEFORM_START_DELAY);
            SWITCH_FREEFORM_READY_DELAY = SystemPropTool.getProp("persist.hchen.switch.freeform.debug.ready.delay", SWITCH_FREEFORM_READY_DELAY);
        } catch (Throwable e) {
            isAlwaysSwitchFreeForm = false;
            mSwitchFreeFormThreshold = 800;
            SWITCH_FREEFORM_START_DELAY = 450;
            SWITCH_FREEFORM_READY_DELAY = 600;
            logE(TAG, e);
        }

        logI(TAG, "Config: isAlwaysSwitchFreeForm: " + isAlwaysSwitchFreeForm + ", mSwitchFreeFormThreshold: " + mSwitchFreeFormThreshold +
            ", SWITCH_FREEFORM_START_DELAY: " + SWITCH_FREEFORM_START_DELAY + ", SWITCH_FREEFORM_READY_DELAY: " + SWITCH_FREEFORM_READY_DELAY);

        Method onBottomCaptionHandleMotionEventsMethod;
        if (isOS1)
            onBottomCaptionHandleMotionEventsMethod = findMethod(mMiuiFreeformModeMoveHandlerClass,
                "onBottomCaptionHandleMotionEvents",
                float.class, float.class, PointF.class, "com.android.wm.shell.miuifreeform.VelocityMonitor", mMiuiFreeformModeTaskInfoClass, int.class
            );
        else
            onBottomCaptionHandleMotionEventsMethod = findMethod(mMiuiFreeformModeMoveHandlerClass,
                "onBottomCaptionHandleMotionEvents",
                float.class, float.class, PointF.class, mMiuiFreeformModeTaskInfoClass, int.class
            );

        hook(onBottomCaptionHandleMotionEventsMethod,
            new IHook() {
                @Override
                public void after() {
                    if (isOS1) {
                        mMiuiFreeformModeTaskInfo = getArgs(4);
                        mPackageName = (String) getField(mMiuiFreeformModeTaskInfo, "mPackageName");
                    }
                    if (isAlwaysSwitchFreeForm) return;
                    Context mContext = (Context) getThisField("mContext");

                    float expandValue;
                    float y = (float) getArgs(1);
                    PointF pointF = (PointF) getArgs(2);
                    int round = Math.round(y - pointF.y);
                    boolean isLandscape = false;
                    if (isOS1) {
                        Object displayLayout = callMethod(getThisField("mMiuiFreeformModeDisplayInfo"), "getDisplayLayout");
                        isLandscape = ((int) getField(displayLayout, "mWidth")) > ((int) getField(displayLayout, "mHeight"));
                    } else
                        isLandscape = (boolean) callStaticMethod("com.android.wm.shell.multitasking.common.MultiTaskingDisplayInfo", "isLandscape");
                    updateScreenSizeIfNeed(mContext, isLandscape);

                    expandValue = isLandscape ? 25.0f : 300.f;
                    isReadyExpandToFullScreen = round >= expandValue;
                    int computeSwitchFreeFormThreshold = 800;
                    if (isLandscape) {
                        Object miuiFreeformModeTaskInfo = isOS1 ? getArgs(4) : getArgs(3);
                        float mScale = (float) getField(miuiFreeformModeTaskInfo, "mScale");
                        float baseMultiplier = 255f;
                        float offset = 330f;

                        if (mScale > 0.1f) {
                            computeSwitchFreeFormThreshold = (int) (baseMultiplier / mScale - offset);

                            int MAX_THRESHOLD = 450;
                            if (computeSwitchFreeFormThreshold > MAX_THRESHOLD) {
                                computeSwitchFreeFormThreshold = MAX_THRESHOLD;
                            }
                        } else {
                            computeSwitchFreeFormThreshold = mSwitchFreeFormThreshold / 8;
                        }
                    }
                    if (isReadyExpandToFullScreen && !isReadySwitchFreeForm) {
                        isReadySwitchFreeForm = round >= (computeSwitchFreeFormThreshold);
                        if (!isReadySwitchFreeForm)
                            isReadySwitchFreeForm = (mScreenY - 100 < y);

                        if (isReadySwitchFreeForm) {
                            if (isOS1) {
                                callMethod(
                                    getThisField("mMiuiFreeformModeVibrateHelper"),
                                    "hapticFeedback",
                                    callMethod(
                                        callStaticMethod(
                                            "com.xiaomi.freeform.MiuiFreeformStub",
                                            "getInstance"
                                        ),
                                        "getHapticNormal"
                                    ),
                                    false,
                                    mContext
                                );
                            } else {
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
                        }
                    } else if (round < computeSwitchFreeFormThreshold && mScreenY - 100 > y)
                        isReadySwitchFreeForm = false;
                }
            }
        );

        if (!isOS1) {
            hookConstructor(mMiuiFreeformModeVisualIndicatorClass,
                Context.class, "com.android.wm.shell.common.DisplayController",
                mMiuiFreeformModeTaskInfoClass,
                "com.android.wm.shell.RootTaskDisplayAreaOrganizer",
                new IHook() {
                    @Override
                    public void after() {
                        mMiuiFreeformModeTaskInfo = getArgs(2);
                        mPackageName = (String) getField(mMiuiFreeformModeTaskInfo, "mPackageName");
                    }
                }
            );
        }

        hookMethod(mMiuiFreeformModeVisualIndicatorClass,
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
                    AndroidLog.logI(TAG, "Add rect map! Package: " + mPackageName + ", Rect: " + rect + ", Scale: " + scale);

                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SWITCH_FREEFORM_READY, context), SWITCH_FREEFORM_READY_DELAY);
                }
            }
        );

        hookMethod(mVisualIndicatorAnimator$2Class,
            "onAnimationEnd", Animator.class,
            new IHook() {
                @Override
                public void after() {
                    if (!isReadySwitchFreeForm && !isAlwaysSwitchFreeForm) return;
                    if (!isAnimatorStarted) return;

                    if (!mHandler.hasMessages(SWITCH_FREEFORM_READY)) {
                        mHandler.postDelayed(QUEUE_PROCESSOR, SWITCH_FREEFORM_START_DELAY);
                    }
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
                logE(TAG, e);
            } finally {
                mHandler.postDelayed(QUEUE_PROCESSOR, SWITCH_FREEFORM_START_DELAY);
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private static void startFreeForm(Context context, String packageName) {
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

    private static void registerForegroundWindowListener() {
        PathClassLoader pathClassLoader = new PathClassLoader(HCData.getModulePath(), classLoader);
        mForegroundInfoListener = newInstance("com.hchen.switchfreeform.hook.ForegroundInfoListener", pathClassLoader);
        if (mForegroundInfoListener == null) return;

        callStaticMethod("miui.process.ProcessManager", "registerForegroundInfoListener", mForegroundInfoListener);
    }

    private static void updateScreenSizeIfNeed(Context context, boolean isLandscape) {
        if (mLastIsLandscape == isLandscape) return;
        getScreenSize(context);
        mLastIsLandscape = isLandscape;
    }

    private static void getScreenSize(Context context) {
        Rect bounds = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getMaximumWindowMetrics().getBounds();
        mPoint.x = bounds.width();
        mPoint.y = bounds.height();
        mScreenY = mPoint.y;
    }

    private static void updateClassPathToOS1() {
        mMiuiFreeformModeMoveHandlerClass = "com.android.wm.shell.miuifreeform.MiuiFreeformModeMoveHandler";
        mMiuiFreeformModeTaskInfoClass = "com.android.wm.shell.miuifreeform.MiuiFreeformModeTaskInfo";
        // mMultiTaskingDisplayInfoClass = "com.android.wm.shell.common.MultiTaskingDisplayInfo";
        mMiuiFreeformModeVibrateHelperClass = "com.android.wm.shell.miuifreeform.MiuiFreeformModeVibrateHelper";
        mMiuiFreeformModeVisualIndicatorClass = "com.android.wm.shell.miuifreeform.MiuiFreeformModeVisualIndicator";
        mVisualIndicatorAnimator$2Class = "com.android.wm.shell.miuifreeform.MiuiFreeformModeVisualIndicator.VisualIndicatorAnimator$2";
    }
}
