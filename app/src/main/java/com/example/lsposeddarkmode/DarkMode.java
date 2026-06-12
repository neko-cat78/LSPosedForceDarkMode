package com.example.lsposeddarkmode;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DarkMode implements IXposedHookLoadPackage {

    private static final String PREFS_FILE = "dark_mode_prefs";
    private static final String MODULE_PACKAGE = "com.example.lsposeddarkmode";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(MODULE_PACKAGE)) return;

        if (!isPackageSelected(lpparam.packageName)) return;

        XposedBridge.log("ForceDarkMode: enabling dark mode for " + lpparam.packageName);
        hookDarkMode(lpparam.classLoader);
    }

    private boolean isPackageSelected(String pkg) {
        XSharedPreferences prefs = new XSharedPreferences(MODULE_PACKAGE, PREFS_FILE);
        try {
            prefs.makeWorldReadable();
            prefs.reload();
            return prefs.getBoolean(pkg, false);
        } catch (Exception e) {
            return false;
        }
    }

    private void hookDarkMode(ClassLoader cl) {
        hookUiModeManager(cl);
        hookResourcesImpl(cl);
        hookActivityAttach(cl);
        hookOnConfigurationChanged(cl);
        hookActivityOnCreate(cl);
    }

    private void hookUiModeManager(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod("android.app.UiModeManager", cl,
                    "getNightMode", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int result = (int) param.getResult();
                            if (result != UiModeManager.MODE_NIGHT_YES) {
                                param.setResult(UiModeManager.MODE_NIGHT_YES);
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log("ForceDarkMode: UiModeManager hook failed: " + e);
        }
    }

    private void hookResourcesImpl(ClassLoader cl) {
        try {
            Class<?> resImpl = XposedHelpers.findClass("android.content.res.ResourcesImpl", cl);
            XposedBridge.hookAllMethods(resImpl, "updateConfiguration", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    for (Object arg : param.args) {
                        if (arg instanceof Configuration) {
                            Configuration cfg = (Configuration) arg;
                            cfg.uiMode = (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                                    | Configuration.UI_MODE_NIGHT_YES;
                        }
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ForceDarkMode: ResourcesImpl hook failed: " + e);
        }
    }

    private void hookActivityAttach(ClassLoader cl) {
        try {
            XposedBridge.hookAllMethods(Activity.class, "attach", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    for (Object arg : param.args) {
                        if (arg instanceof Configuration) {
                            Configuration cfg = (Configuration) arg;
                            cfg.uiMode = (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                                    | Configuration.UI_MODE_NIGHT_YES;
                        }
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ForceDarkMode: Activity.attach hook failed: " + e);
        }
    }

    private void hookOnConfigurationChanged(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod("android.app.Activity", cl,
                    "onConfigurationChanged", Configuration.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Configuration cfg = (Configuration) param.args[0];
                            if (cfg != null) {
                                cfg.uiMode = (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                                        | Configuration.UI_MODE_NIGHT_YES;
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log("ForceDarkMode: onConfigurationChanged hook failed: " + e);
        }
    }

    private void hookActivityOnCreate(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod("android.app.Activity", cl,
                    "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                Class<?> delegate = XposedHelpers.findClass(
                                        "androidx.appcompat.app.AppCompatDelegate", cl);
                                if (delegate != null) {
                                    int mode = XposedHelpers.getStaticIntField(delegate, "MODE_NIGHT_YES");
                                    XposedHelpers.callStaticMethod(delegate, "setDefaultNightMode", mode);
                                }
                            } catch (Exception ignored) {}
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log("ForceDarkMode: Activity.onCreate hook failed: " + e);
        }
    }
}
