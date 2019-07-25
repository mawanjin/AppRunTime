package com.test.apprun.permission.rom;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;


import com.test.apprun.permission.utils.SystemTool;

import java.lang.reflect.Method;

/**
 * Description:
 *
 * @author Shawn_Dut
 * @since 2018-02-01
 */
public class VivoUtils {

    private static final String TAG = "OppoUtils";

    /**
     * 检测 悬浮窗权限
     */
    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkVivo(context); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean checkVivo(Context context) {
        int status =  SystemTool.getFloatPermissionStatus(context);
        if(status==0){
            return true;
        }
        return false;
    }

    /**
     * oppo ROM 权限申请
     */
    public static void applyVivoPermission(Context context) {
        //merge request from https://github.com/zhaozepeng/FloatWindowPermission/pull/26
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //com.coloros.safecenter/.sysfloatwindow.FloatWindowListActivity
//            ComponentName comp = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager");//悬浮窗管理页面
//            intent.setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager");
//            intent.putExtra("packagename", context.getPackageName());
//            if(hasIntent(context,intent)){
//                context.startActivity(intent);
//                return;
//            }

            ComponentName comp = new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            context.startActivity(intent);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private static boolean hasIntent(Context context,Intent intent){
        return !context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
    }
}
