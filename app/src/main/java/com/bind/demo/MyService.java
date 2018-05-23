package com.bind.demo;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class MyService extends Service {

    private Work work;
    //有可能会内存泄漏,采用虚引用
    private WeakReference<MyService> myService = new WeakReference<>(MyService.this);
    private static final String TAG = "MyService";

    public MyService() {
        threadFlag = true;
        workThread.start();
    }

    private Work getWork() {
        if (work == null) {
            work = new Work();
        }
        return work;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return getWork();
    }

    @Override
    public void onDestroy() {
        threadFlag = false;
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public class Work extends Binder {
        public void startWork() {
            //开始工作
            flag = true;
        }

        public void stopWork() {
            //停止工作
            flag = false;
        }

        public MyService getMyService() {
            return myService.get();
        }
    }

    //工作标识符
    private boolean flag = true;

    //线程工作标识符
    private boolean threadFlag = true;

    private int i = 0;

    //模拟后台持续工作
    private Thread workThread = new Thread() {
        @Override
        public void run() {
            while (threadFlag) {
                if (flag) {
                    //模拟子线程后台工作 然后用handler发送给主线程
                    i++;
                    SystemClock.sleep(2000);
                    Message message = handler.obtainMessage();
                    message.obj = i;
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int message = (int) msg.obj;
                    //将信息反馈给接口
                    if (callBacks != null && callBacks.size() > 0) {
                        for (CallBack callBack :
                                callBacks) {
                            callBack.postMessage(message);
                        }
                    }
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 提供给activity的接口 因为存在一个服务绑定多个activity的情况 所以监听接口采用list装起来
     */

    public interface CallBack {
        void postMessage(int message);
    }

    private List<CallBack> callBacks = new LinkedList<>();

    //注册接口
    public void registerCallBack(CallBack callBack) {
        if (callBacks != null) {
            callBacks.add(callBack);
        }
    }

    /**
     * 注销接口 false注销失败
     *
     * @param callBack
     * @return
     */
    public boolean unRegisterCallBack(CallBack callBack) {
        if (callBacks != null && callBacks.contains(callBack)) {
            return callBacks.remove(callBack);
        }
        return false;
    }
}
