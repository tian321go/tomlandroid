package com.axeac.app.client.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.axeac.app.client.activity.InitActivity;
import com.axeac.app.client.service.MsgCountTaskService;
import com.axeac.app.client.service.TaskCountTaskService;
import com.axeac.app.client.utils.update.PollingUtils;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * Created by Administrator on 2018/1/2.
 */

public class BootBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PollingUtils.startPollingService(context, StaticObject.read.getLong(StaticObject.SYSTEMSETUPS_MSGTIMES,10)*60000, TaskCountTaskService.class, TaskCountTaskService.ACTION);
            PollingUtils.startPollingService(context, StaticObject.read.getLong(StaticObject.SYSTEMSETUPS_MSGTIMES,10)*60000, MsgCountTaskService.class, MsgCountTaskService.ACTION);
        }else{
            PollingUtils.startPollingService(context, StaticObject.read.getLong(StaticObject.SYSTEMSETUPS_MSGTIMES,10)*60000, TaskCountTaskService.class, TaskCountTaskService.ACTION);
            PollingUtils.startPollingService(context, StaticObject.read.getLong(StaticObject.SYSTEMSETUPS_MSGTIMES,10)*60000, MsgCountTaskService.class, MsgCountTaskService.ACTION);
        }
    }
}
