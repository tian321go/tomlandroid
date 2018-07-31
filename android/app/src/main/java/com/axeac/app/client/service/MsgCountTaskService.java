package com.axeac.app.client.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.axeac.app.client.R;
import com.axeac.app.client.activity.LoginActivity;
import com.axeac.app.client.activity.MainActivity;
import com.axeac.app.client.utils.update.PollingUtils;
import com.axeac.app.sdk.activity.BaseActivity;
import com.axeac.app.sdk.activity.ComponentActivity;
import com.axeac.app.sdk.fragment.MainFragment2;
import com.axeac.app.sdk.jhsp.JHSPResponse;
import com.axeac.app.sdk.retrofit.OnRequestCallBack;
import com.axeac.app.sdk.retrofit.UIHelper;
import com.axeac.app.sdk.tools.LinkedHashtable;
import com.axeac.app.sdk.tools.Property;
import com.axeac.app.sdk.utils.CommonUtil;
import com.axeac.app.sdk.utils.DeviceMessage;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * describe:service to listen pushing messages
 * <br>监听推送消息的后台服务
 * @author axeac
 * @version 2.3.0.0001
 * */
public class MsgCountTaskService extends Service {
    public static final String ACTION = "com.axeac.app.client.service.MsgCountTaskService";
    private Property items;
    private Timer timer;
    /**
     * 轮询时间
     * */
    private long times = 10000 * 60;
    private static BaseActivity activity;
    private static NotificationCompat.Builder builder;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // describe:Request data once every 59 seconds to the server
    /**
     * 每59秒向服务器请求一次数据
     * @param intent
     * Intent对象
     * @param flags
     * 有关此开始请求的附加数据。 目前为0，{@link #START_FLAG_REDELIVERY}或{@link #START_FLAG_RETRY}
     * @param startId
     * 一个独特的整数，表示这个特定的启动请求。 与{@link #stopSelfResult（int）}一起使用
     * @return
     * 返回值表示系统应该为服务当前启动状态使用什么语义。
     * <br>可能是与{@link #START_CONTINUATION_MASK}位相关联的常量之一。
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        StaticObject.initStaticObject(getApplicationContext());
        DeviceMessage.init(getApplicationContext());
        activity = BaseActivity.act;
        times = StaticObject.read.getLong(StaticObject.SYSTEMSETUPS_MSGTIMES,10)*60000;
        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PollingUtils.startPollingService(activity, times, MsgCountTaskService.class, MsgCountTaskService.ACTION);

        }
                Log.w("Backlog.service", "Backlog service run...."+times);
                UIHelper.sendRequestMsgService(getApplicationContext(), new OnRequestCallBack() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onSuccesed(JHSPResponse response) {
                        boolean iscoming = false;
                        items = new Property();
                        items.setSplit("\n");
                        items.load(response.getData());
                        String res = StaticObject.read.getString("msgcount","");

                        if (res.length()>0){
                            if (res.substring(0,res.lastIndexOf("=")).equals(response.getData().substring(0,response.getData().lastIndexOf("=")))){
                                iscoming = true;
                            }
                        }
                        if (items.getProperty("count.text").equals("0")){
                            iscoming = true;
                        }
                        StaticObject.wirte.edit().putString("msgcount", response.getData()).commit();
                        Property items = new Property();
                        items.setSplit("\n");
                        items.load(response.getData());
                        LinkedHashtable listscount = items.searchSubKeyDeep("var.", 1);
                        String countProperty = "";
                        if (listscount != null && listscount.size() > 0) {
                            Vector<?> v = listscount.linkedKeys();
                                String keykey = (String) v.elementAt(0);
                                if (!"0".equals(items.getProperty(keykey + ".text")) && !"".equals(items.getProperty(keykey + ".text")))
                                    countProperty += getString(R.string.backlog_info1, items.getProperty(keykey + ".text"));
                        }
                        if (!"".equals(countProperty)&&!iscoming)
                            setNoti(getApplicationContext(), null, getString(R.string.backlog_tip1), countProperty, true);
                    }

                    @Override
                    public void onfailed(Throwable e) {
                    }
                });

//            }
//        }, 0, times);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Backlog.service", "Backlog service Start.");

    }

    @Override
    public void onDestroy() {
        timer.cancel();
        Log.d("Backlog.service", "Backlog service shutdown.");
    }


    /**
     * describe:Set the notification
     * 描述：设置通知栏
     * */
    public static void setNoti(Context ctx, Class<? extends Activity> cls, String info, String content, boolean sound) {

        builder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.mipmap.icon_trans)
                .setContentTitle(info)
                .setContentText(content)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.icon))
                .setAutoCancel(true);
        // requires VIBRATE permission
        // 需要VIBRATE权限

        login(ctx,sound);

    }

    /**
     * describe:Request the data, and btn_add the click jump action after the success
     * 描述：请求数据，并在成功后加入点击跳转动作
     *
     * @param context
     * @param b
     * */
    private static void login(final Context context, final boolean b) {
        final String username = StaticObject.read.getString(StaticObject.USERNAME,"");
        String password = StaticObject.read.getString(StaticObject.PASSWORD,"");
        UIHelper.init(username, password);

        UIHelper.send(activity, "MEIP_PAGE=msg_list", new OnRequestCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onSuccesed(JHSPResponse response) {
                if (response.getCode() == 0) {
                    if (!CommonUtil.isResponseNoToast(response.getMessage())) {
                        Toast.makeText(context, response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if (DeviceMessage.getParams().get("MEIP_CURRENT_USER") == null || DeviceMessage.getParams().get("MEIP_CURRENT_USER").equals("")) {
                        DeviceMessage.getParams().put("MEIP_CURRENT_USER",username);
                    }
                    Intent intent = new Intent(context, ComponentActivity.class);
                    intent.setAction("MsgCountTaskService");
                    intent.putExtra("parms", response.getData());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    builder.setContentIntent(pendingIntent);
                    if (b) {
                        builder.setDefaults(Notification.DEFAULT_ALL);
                    }

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, builder.build());

                } else {
                    StaticObject.loginFlag = false;
                    DeviceMessage.getParams().clear();
                }
            }

            @Override
            public void onfailed(Throwable e) {

            }
        });

    }



}
