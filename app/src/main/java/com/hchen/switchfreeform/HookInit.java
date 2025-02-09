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
package com.hchen.switchfreeform;

import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;
import com.hchen.switchfreeform.hook.SwitchFreeForm;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook 入口
 *
 * @author 焕晨Chen
 */
public class HookInit extends HCEntrance {
    @Override
    public HCInit.BasicData initHC(HCInit.BasicData basicData) {
        return basicData.setTag("SwitchFreeForm")
            .setLogLevel(HCInit.LOG_D)
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .initLogExpand(new String[]{
                "com.hchen.switchfreeform.hook"
            });
    }

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("com.android.systemui".equals(lpparam.packageName)) {
            HCInit.initLoadPackageParam(lpparam);
            new SwitchFreeForm().onApplicationCreate().onLoadPackage();
        }
    }
}
