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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.os.RemoteException;

import com.hchen.hooktool.log.AndroidLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import miui.app.MiuiFreeFormManager;
import miui.process.ForegroundInfo;
import miui.process.IForegroundInfoListener;

/**
 * 监听前台应用切换
 *
 * @author 焕晨HChen
 */
public class ForegroundInfoListener extends IForegroundInfoListener.Stub {
    private static final String TAG = "ForegroundWindowListener";
    @Deprecated
    public Object mAtm;
    @Deprecated
    public Method mGetTasks;
    @Deprecated
    public Field mMiuiFreeFormStackInfo;
    public String mForegroundPackageName;
    public String mLastForegroundPackageName = null;

    @SuppressLint("BlockedPrivateApi")
    public ForegroundInfoListener() {
        // try {
        //     Class<?> atm = getClass().getClassLoader().loadClass("android.app.ActivityTaskManager");
        //     Method getInstance = atm.getDeclaredMethod("getInstance");
        //     getInstance.setAccessible(true);
        //     mAtm = getInstance.invoke(null);
        //
        //     mGetTasks = atm.getDeclaredMethod("getTasks", int.class, boolean.class);
        //     mGetTasks.setAccessible(true);
        //
        //     mMiuiFreeFormStackInfo = ActivityManager.RunningTaskInfo.class.getSuperclass().getDeclaredField("miuiFreeFormStackInfo");
        //     mMiuiFreeFormStackInfo.setAccessible(true);
        // } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
        //          InvocationTargetException | NoSuchFieldException e) {
        //     AndroidLog.logE(TAG, e);
        // }
    }

    @Override
    public void onForegroundInfoChanged(ForegroundInfo foregroundInfo) throws RemoteException {
        try {
            // if (shouldSkip(foregroundInfo.mLastForegroundPackageName)) return;
            if (foregroundInfo.mForegroundPackageName == null || foregroundInfo.mLastForegroundPackageName == null)
                return;
            if (foregroundInfo.mForegroundPackageName.equals(foregroundInfo.mLastForegroundPackageName))
                return;

            mForegroundPackageName = foregroundInfo.mForegroundPackageName;
            mLastForegroundPackageName = foregroundInfo.mLastForegroundPackageName;
            AndroidLog.logI(TAG, "ForegroundWindowListener: cur: " + foregroundInfo.mForegroundPackageName +
                ", last: " + foregroundInfo.mLastForegroundPackageName + ", time: " + System.currentTimeMillis());
        } catch (Throwable e) {
            AndroidLog.logE(TAG, e);
        }
    }

    @Deprecated
    public boolean shouldSkip(String mPackageName) {
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = getTasks(Integer.MAX_VALUE, true);
        for (ActivityManager.RunningTaskInfo info : runningTaskInfos) {
            if (mMiuiFreeFormStackInfo != null) {
                try {
                    MiuiFreeFormManager.MiuiFreeFormStackInfo miuiFreeFormStackInfo = (MiuiFreeFormManager.MiuiFreeFormStackInfo) mMiuiFreeFormStackInfo.get(info);
                    if (miuiFreeFormStackInfo == null) continue;
                    if (mPackageName.equals(miuiFreeFormStackInfo.packageName)) {
                        boolean isFreeFormMode = miuiFreeFormStackInfo.isNormalFreeForm ||
                            miuiFreeFormStackInfo.inPinMode ||
                            miuiFreeFormStackInfo.isInFreeFormMode() ||
                            miuiFreeFormStackInfo.isInMiniFreeFormMode();
                        AndroidLog.logI(TAG, "package: " + mPackageName + " is in freeform mode.");
                        return isFreeFormMode;
                    }
                } catch (Throwable e) {
                    AndroidLog.logE(TAG, e);
                }
            }
        }
        return false;
    }

    @Deprecated
    public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, boolean filterOnlyVisibleRecents) {
        try {
            if (mAtm == null) return null;
            return (List<ActivityManager.RunningTaskInfo>) mGetTasks.invoke(mAtm, maxNum, filterOnlyVisibleRecents);
        } catch (IllegalAccessException | InvocationTargetException e) {
            AndroidLog.logE(TAG, e);
        }
        return null;
    }
}
