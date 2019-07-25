package com.test.apprun.permission.utils;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/12/21.
 */
public class SystemTool {

    /**
     * 活动检测是否开启
     *
     * @param context
     * @return
     */
    public static boolean getUsageStatsList(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usage = (UsageStatsManager) context.getSystemService
                    (Context.USAGE_STATS_SERVICE);
//            Calendar calendar = Calendar.getInstance();
//            long endTime = calendar.getTimeInMillis();
//            calendar.add(Calendar.YEAR, -1);
//            long startTime = calendar.getTimeInMillis();
            List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    0, System.currentTimeMillis());
            if (stats != null && stats.size() > 0)
                return true;
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(context
                    .ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApp = activityManager
                    .getRunningAppProcesses();
            if (runningApp != null && runningApp.size() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含ACTION_USAGE_ACCESS_SETTINGS权限
     *
     * @param context
     * @return
     */
    public static boolean isNoOption(Context context) {
        PackageManager packageManager = context.getApplicationContext()
                .getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static long getStartTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static boolean isRunningForeground(Context context,String packageName){
        if (TextUtils.isEmpty(packageName)){
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            String topPackageName = getRunningPackageNameOver21(context);
            if (packageName.equals(topPackageName)){
                return true;
            }
        }else{
            List<String> topList = getRunningPackageName(context);
            if (topList != null && topList.size() > 0){
                for (String topPackageName:topList){
                    if (packageName.equals(topPackageName)){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 系统版本超过21时，返回的位于前台的应用
     * @param context
     * @return
     */
    public static String getRunningPackageNameOver21(Context context){
        String topPackageName = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            return topPackageName;
        }
        // 1.获取统计服务类
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long currTime = System.currentTimeMillis();
//            Log.i(TAG,"0点---" + start);
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //2.获取从今天0点到现在的使用情况
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, getStartTime(),currTime);
        //3.根据最后使用时间降序排列
        Collections.sort(usageStatsList,new UsageComparator());

        //获取前台应用，排除其他应用因通知栏而产生的统计信息
        Field mLastEventField = null;
        try {
            mLastEventField = UsageStats.class.getField("mLastEvent");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        for (UsageStats usageStats:usageStatsList){
//            long date = usageStats.getLastTimeStamp();
//                Log.i("SystemTool","包名:" + usageStats.getPackageName() + ",:" + dateFormat.format(new Date(date)));

            if (mLastEventField != null){
                int lastEvent = 0;
                try {
                    lastEvent =  mLastEventField.getInt(usageStats);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (lastEvent == 1){
                    topPackageName = usageStats.getPackageName();
//                        Log.i(TAG,"包名:" + usageStats.getPackageName() + ",:" + dateFormat.format(new Date(date)));
                    return topPackageName;
                }
            }
        }
        return topPackageName;
    }

    /**
     * 系统版本5.0以下获取前台运行程序
     * @param context
     * @return
     */
    public static List<String> getRunningPackageName(Context context){
        List<String> topList = null;
        if (context == null){
            return topList;
        }
        topList = new ArrayList<>();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo: processes) {

            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                //处于前台的有可能有系统的一些包
//                Log.i("SystemTool","packageName--->" + processInfo.processName);
                topList.add(processInfo.processName);
            }
//            if (processInfo.processName.equals(context.getPackageName())) {
//                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
////                    return true;
//                }
//            }
        }

        return topList;
    }



    /**
     * 获取悬浮窗权限状态
     *
     * @param context
     * @return 1或其他是没有打开，0是打开，该状态的定义和{@link android.app.AppOpsManager#MODE_ALLOWED}，MODE_IGNORED等值差不多，自行查阅源码
     */
    public static int getFloatPermissionStatus(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        String packageName = context.getPackageName();
        Uri uri = Uri.parse("content://com.iqoo.secure.provider.secureprovider/allowfloatwindowapp");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{packageName};
        Cursor cursor = context
                .getContentResolver()
                .query(uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            cursor.getColumnNames();
            if (cursor.moveToFirst()) {
                int currentmode = cursor.getInt(cursor.getColumnIndex("currentlmode"));
                cursor.close();
                return currentmode;
            } else {
                cursor.close();
                return getFloatPermissionStatus2(context);
            }

        } else {
            return getFloatPermissionStatus2(context);
        }
    }

    /**
     * vivo比较新的系统获取方法
     *
     * @param context
     * @return
     */
    private static int getFloatPermissionStatus2(Context context) {
        String packageName = context.getPackageName();
        Uri uri2 = Uri.parse("content://com.vivo.permissionmanager.provider.permission/float_window_apps");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{packageName};
        Cursor cursor = context
                .getContentResolver()
                .query(uri2, null, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int currentmode = cursor.getInt(cursor.getColumnIndex("currentmode"));
                cursor.close();
                return currentmode;
            } else {
                cursor.close();
                return 1;
            }
        }
        return 1;
    }
}
