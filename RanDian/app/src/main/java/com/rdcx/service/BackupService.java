package com.rdcx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.rdcx.tools.ServiceUtils;

public class BackupService extends Service {

    @Override
    public void onTrimMemory(int level) {
        ServiceUtils.keepMainService(this);
    }

    @Override
    public void onLowMemory() {
        ServiceUtils.keepMainService(this);
    }

    @Override
    public void onCreate() {
        ServiceUtils.keepMainService(this);
    }

    @Override
    public void onDestroy() {
        ServiceUtils.keepMainService(this);
        super.onDestroy();
    }

    public BackupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
