package com.hchen.switchfreeform;

import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;
import com.hchen.switchfreeform.hook.SwitchFreeForm;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookInit extends HCEntrance {
    @Override
    public HCInit.BasicData initHC(HCInit.BasicData basicData) {
        return basicData.setTag("SwitchFreeForm")
            .setLogLevel(HCInit.LOG_D)
            .setModulePackageName("com.hchen.switchfreeform")
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
