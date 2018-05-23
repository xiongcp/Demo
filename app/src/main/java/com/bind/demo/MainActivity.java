package com.bind.demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean bindService;
    private MyService.Work work;
    private MyService myService;
    private TextView tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        tv_text = findViewById(R.id.tv_text);
        bindService();
    }

    //绑定service
    private void bindService() {
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindService = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //获取绑定binder 强制转化为MyService.Work
            work = (MyService.Work) iBinder;
            myService = work.getMyService();
            myService.registerCallBack(callBack);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (myService != null) {
                myService.unRegisterCallBack(callBack);
            }
        }
    };

    private MyService.CallBack callBack = new MyService.CallBack() {
        @Override
        public void postMessage(int message) {
            tv_text.setText("service do result-----> " + message);
        }
    };

    public void unBindService() {
        if (bindService && serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        unBindService();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (work!=null){
                    work.startWork();
                }
                break;
            case R.id.btn_stop:
                if (work!=null){
                    work.stopWork();
                }
                break;
        }
    }
}
