package com.demo.videodemo.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class AppUtils {
    private static final String TAG = "AppUtils";

    private static Application application;

    private static ActivityLifecycleImpl sActivityLifecycle = new ActivityLifecycleImpl();

    public static void setApp(Application app) {
        init(app);
    }

    public static void shutApp() {
        sActivityLifecycle.clear();
        sActivityLifecycle = null;
        application = null;
    }

    public static Application app() {
        return application;
    }


     /*
      简单总结一下为了返回上层的Activity或者是App, 经过的过程。
      1. 首先我们需要自定义一个ActivityLifecycleCallback来回调管理Activity的生命周期, 在里面实现返回上层
         activity的函数. ActivityLifecycleCallback的主要工作就是使用一个list来管理Activity. 使用两个计数
         值来统计位于前台的Activity的数量和判断是否是因为configuration的改变而造成Activity的重建的.(然后说
         一个在这个过程比较重要的函数, 通过反射来获取top Activity)
      2. 如果上个步骤返回的是null, 这时我们就需要返回Application了. 使用一个static变量sApplication来存储,
         如果该值不是null, 那么就直接返回. 如果是null就通过反射来获取Application, 并对获取到的Application
         进行注册ActivityLifecycleCallback, 最后保存到Application的静态变量中
      3. 为了更好的使用1步骤, 我们需要判断一下当前应用是否是位于前台, 如果是的话进行1步骤, 如果不是的话进行2步骤
     */


    public static Context getTopActivity() {
        return sActivityLifecycle.getTopActivity();
    }

    /**
     * 优先返回上层的Activity而不是Application, 如果上层的Activity是null就返回App
     */
    public static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Log.d(TAG, "getTopActivityOrApp: is foreground");
            return sActivityLifecycle.getTopActivity();
        } else {
            Log.d(TAG, "getTopActivityOrApp: not foreground");
            return app();
        }
    }


    /**
     * 其实用这个方法来判断应用是否处于前台是有局限的, 如果这个应用的Service被设置成START_STICKY
     * `appInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND` 这个判断是
     * 始终成立的.
     * 还是比较推荐使用ActivityLifecycle来判断是否是前台应用的
     */
    /**
     * 通过runningProcess获取到一个当前正在运行的进程list, 我们遍历这个list的每一个进程, 判断这个进程的
     * importance属性是否是IMPORTANCE_FOREGROUND, 并且包名是否与我们的app的包名一样, 如果这两个条件都
     * 符合, 那么这个app就处于前台运行
     */
    public static boolean isAppForeground(){
        ActivityManager am = (ActivityManager) app().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appInfo : info) {
            if (appInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return appInfo.processName.equals(app().getPackageName());
            }
        }
        return false;
    }




    /**
     * 通过反射来获取application
     * @return application
     */
    private static Application getApplicationByReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            return (Application) app;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void init(Application app) {
        if (application == null) {
            if (app == null) {
                application = getApplicationByReflect();
            } else {
                application = app;
            }
            if (application != null) {
                application.registerActivityLifecycleCallbacks(sActivityLifecycle);
            }
        } else {
            if (app != null && app.getClass() != application.getClass()) {
                // 重新注册
                application.unregisterActivityLifecycleCallbacks(sActivityLifecycle);
                sActivityLifecycle.getActivityList().clear();
                application = app;
                application.registerActivityLifecycleCallbacks(sActivityLifecycle);
            }
        }
    }


    public static void addStateListener(Object object, ActivityLifecycleImpl.OnAppStatusChangedListener listener) {
        sActivityLifecycle.addListener(object, listener);
    }

    public static void removeStateListener(Object object) {
        sActivityLifecycle.removeListener(object);
    }
}
