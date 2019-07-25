package com.test.service;

import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.apprun.R;
import com.test.apprun.permission.FloatWindowManager;
import com.test.apprun.utils.ApiUtils;
import com.test.apprun.utils.SystemTool;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/12/20.
 */
public class MyAppService extends Service {
    private static final String TAG = "MyAppService";

    private MyAppBinder binder = new MyAppBinder();
    private Handler handler;

    private Timer timer;
    private TextView tvLeft;

    private TextView tvCenter;

    private int time = 0;
    private int targetTime = 20;

    private boolean runInBackground = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"MyAppService----onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"MyAppService----onCreate");
        handler = new Handler();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"MyAppService----onStartCommand");
        showMyInfo();
        return super.onStartCommand(intent, flags, startId);
    }

    public void showMyInfo(){
        Log.i(TAG,"MyAppService----showMyInfo");
        if (timer != null){
            return;
        }
        FloatWindowManager.getInstance().showWindow(this);
        tvLeft = FloatWindowManager.getInstance().getTvLeft();
        tvCenter = FloatWindowManager.getInstance().getTvCenter();
//        showWindow();
        tvLeft.setVisibility(View.GONE);
        tvCenter.setVisibility(View.GONE);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                Log.i(TAG,"Service在运行");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(getApplicationContext(),"Service在运行",Toast.LENGTH_SHORT).show();
//                        dialog.show();

                        if (time > targetTime){
                            return;
                        }

                        Log.i(TAG,"MyAppService----showMyInfo-----timer1");

                        if (time >= targetTime){
                            tvLeft.setVisibility(View.GONE);
                            tvCenter.setVisibility(View.VISIBLE);
                            showCenterView("恭喜您，试玩" + targetTime + "秒");
                            time++;
                            timer.schedule(new TimerTask() {
                                @Override public void run() {
                                    endSevice();
                                }
                            },3000);
                            return;
                        }

                        boolean isRunForeground = SystemTool.isRunningForeground(MyAppService.this, ApiUtils.TARGET_PACKGAGE_NAME);

                        if (isRunForeground){
                            if (runInBackground){
                                runInBackground = false;
                                tvLeft.setVisibility(View.GONE);
                                tvCenter.setVisibility(View.VISIBLE);
                                if (time > 0){
                                    tvCenter.setText("暂停回来，上次玩了:" + time+"秒");
                                }else{
                                    tvCenter.setText("开始试玩，试玩时间：" + targetTime  + "秒");
                                }
                            }else{
                                tvLeft.setVisibility(View.VISIBLE);
                                tvCenter.setVisibility(View.GONE);
                                tvLeft.setText("已试玩：" + (time++) + "秒");
                            }
                        }else{
                            if (!runInBackground){
                                runInBackground = true;
                                showCenterView("试玩暂停，试玩" + time + "秒");
                            }
                        }
                    }
                });
            }
        },5*1000,1*1000);
    }

    @Override
    public void onDestroy() {
//        Log.i(TAG,"MyAppService----onDestroy");
        super.onDestroy();
        if (timer != null){
            timer.cancel();
            timer = null;
        }

        FloatWindowManager.getInstance().dismissWindow();
//        if (mWindowManager != null){
//            mWindowManager.removeViewImmediate(view);
//        }
    }

    private void showCenterView(String message){
        if (timer == null){
            return;
        }
        tvLeft.setVisibility(View.GONE);
        tvCenter.setVisibility(View.VISIBLE);
        tvCenter.setText(message);
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvCenter.setText("closed");
                        tvCenter.setVisibility(View.GONE);
                        FloatWindowManager.getInstance().dismissWindow();
//                        if (removeView){
////                            mWindowManager.removeViewImmediate(view);
////                            timer.cancel();
////                            timer = null;
////                            MyAppService.this.onDestroy();
//                            endSevice();
//                        }else {
//                            tvCenter.setVisibility(View.GONE);
//                        }
                    }
                });
            }
        },3*1000);
    }

//    private void showCenterView(String message, final boolean removeView){
//        if (timer == null){
//            return;
//        }
//        tvLeft.setVisibility(View.GONE);
//        tvCenter.setVisibility(View.VISIBLE);
//        tvCenter.setText(message);
//        Timer timer1 = new Timer();
//        timer1.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        tvCenter.setVisibility(View.GONE);
////                        if (removeView){
//////                            mWindowManager.removeViewImmediate(view);
//////                            timer.cancel();
//////                            timer = null;
//////                            MyAppService.this.onDestroy();
////                            endSevice();
////                        }else {
////                            tvCenter.setVisibility(View.GONE);
////                        }
//                    }
//                });
//            }
//        },3*1000);
//    }

    /**
     * 手动调用解除绑定
     */
    private void endSevice(){
        Intent intent = new Intent();
        intent.setAction("com.test.MainAcitiviy.EndReceiver");
        sendBroadcast(intent);
    }

    public class MyAppBinder extends Binder{
        public MyAppService getService(){
            return MyAppService.this;
        }
    }
}
