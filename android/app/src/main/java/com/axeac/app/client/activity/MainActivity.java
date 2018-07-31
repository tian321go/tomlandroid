package com.axeac.app.client.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axeac.app.client.service.MsgCountTaskService;
import com.github.ikidou.fragmentBackHandler.BackHandlerHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.axeac.app.client.R;
import com.axeac.app.client.fragment.MainFragment3;
import com.axeac.app.client.service.TaskCountTaskService;
import com.axeac.app.sdk.KhinfSDK;
import com.axeac.app.sdk.activity.BaseActivity;
import com.axeac.app.sdk.customview.MyTabHost;
import com.axeac.app.sdk.fragment.MainFragment1;
import com.axeac.app.sdk.fragment.MainFragment2;
import com.axeac.app.sdk.tools.Property;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * 主界面，在主界面中包含三个Fragment，即主页Fragment（MainFragment1）、
 * <br>导航Fragment（MainFragment2）、设置Fragment（MainFragment3），下方
 * <br>为TabHost选项标签
 * @author axeac
 * @version 2.3.0.0001
 * */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    /** TabHost标签1布局*/
    @Bind(R.id.layout1)
    LinearLayout layout1;
    /** TabHost标签2布局*/
    @Bind(R.id.layout2)
    LinearLayout layout2;
    /** TabHost标签3布局*/
    @Bind(R.id.layout3)
    LinearLayout layout3;
    @Bind(android.R.id.tabhost)
    MyTabHost mTabHost;
    /** 包含Fragment的Class类型数组*/
    private final Class[] fragments = {MainFragment1.class,
            MainFragment2.class, MainFragment3.class};

    /** 存储DFS脚本数据的Property*/
    private Property navLists;

    /**
     * 返回存有DFS脚本数据的Property
     * @return
     * 存有DFS脚本数据Property对象
     * */
    public Property getNavLists() {
        return navLists;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BackHandlerHelper.handleBackPress(MainActivity.this)) {
                    backToHome();
                }
            }
        });
        initComp(getIntent());
    }

    private MenuItem switchbtn;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_frag2, menu);
        switchbtn = toolbar.getMenu().getItem(0);
        StaticObject.isGrid = StaticObject.read.getBoolean("isGrid", false);
        if (StaticObject.isGrid) {
            switchbtn.setIcon(R.drawable.axeac_index02);
        } else {
            switchbtn.setIcon(R.drawable.axeac_index01);
        }
        hiddenEditMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action:
                if (StaticObject.isGrid) {
                    StaticObject.isGrid = false;
                    StaticObject.wirte.edit().putBoolean("isGrid", false).commit();
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(StaticObject.CHANGE_GRID_OR_LIST_ACTION));
                    switchbtn.setIcon(R.drawable.axeac_index02);
                } else {
                    StaticObject.isGrid = true;
                    StaticObject.wirte.edit().putBoolean("isGrid", true).commit();
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(StaticObject.CHANGE_GRID_OR_LIST_ACTION));
                    switchbtn.setIcon(R.drawable.axeac_index01);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * describe:initalize
     *
     * 初始化主界面，并切换到TabHost标签1
     * */
    private void initView(Property p) {
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("0").setIndicator("0"), KhinfSDK.getInstance().getCustomMainFramentClass(p), null);
        mTabHost.addTab(mTabHost.newTabSpec("1").setIndicator("1"), KhinfSDK.getInstance().getNavFramentClass(p), null);
        mTabHost.addTab(mTabHost.newTabSpec("2").setIndicator("2"), MainFragment3.class, null);
        mTabHost.setCurrentTab(0);
        backPage.setVisibility(View.INVISIBLE);
        layout1.setOnClickListener(this);
        layout2.setOnClickListener(this);
        layout3.setOnClickListener(this);
    }

    /**
     * 从Intent中获取数据，显示视图
     * @param intent
     * 传入参数为getIntent()
     * */
    private void initComp(Intent intent) {
        if (intent.getAction().equals("TaskCountTaskService")){
            MainFragment2.navMap.clear();
        }
        navLists = new Property();
        navLists.setSplit("\n");
        navLists.load(intent.getStringExtra("parms"));
        setTitle(navLists.getProperty("Form.title", "WorkCenter"));

        initView(navLists);

        if (!"".equals(navLists.getProperty("Form", ""))) {
            changeBg(0);
            mTabHost.setCurrentTab(0);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(StaticObject.NOTIFYCATION_ACTION));
        } else {
            changeBg(1);
            mTabHost.setCurrentTab(1);
            showEditMenu();
        }
    }

    /**
     * describe:Show the middle fragment
     *
     *显示TabHost标签中间位置fragment
     * */
    public void showMidView() {
        changeBg(1);
        mTabHost.setCurrentTab(1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        View v = findViewById(com.axeac.app.sdk.R.id.toolbar);
        if (v != null) {
            backPage = (ImageButton) v.findViewById(com.axeac.app.sdk.R.id.layout_button);
        }
        StaticObject.ismenuclick = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout1:
                changeBg(0);
                mTabHost.setCurrentTab(0);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(StaticObject.NOTIFYCATION_ACTION));
                break;
            case R.id.layout2:
                changeBg(1);
                mTabHost.setCurrentTab(1);
                break;
            case R.id.layout3:
                changeBg(2);
                mTabHost.setCurrentTab(2);
                break;
            default:
                break;
        }
    }

    /**
     * describe:Toggle the bottom icon
     *
     * 切换底部显示图标
     *
     * @param arg0
     * 0代表TabHost标签1,1代表TabHost标签2,2代表TabHost标签3
     *
     * */
    public void changeBg(int arg0) {
        backPage.setVisibility(View.INVISIBLE);
        if (arg0==1&&MainFragment2.navMap.size()>1){
            backPage.setVisibility(View.VISIBLE);
        }
        int imgId;
        int tvId;
        ImageView iv;
        TextView tv;
        for (int i = 0; i < fragments.length; i++) {
            imgId = getResources().getIdentifier("img_icon" + (i + 1),
                    "id", MainActivity.this.getPackageName());
            tvId = getResources().getIdentifier("tv" + (i + 1),
                    "id", MainActivity.this.getPackageName());
            iv = (ImageView) findViewById(imgId);
            tv = (TextView) findViewById(tvId);
            int resId = getResources().getIdentifier("index0" + (i + 1),
                    "drawable", MainActivity.this.getPackageName());
            int resId2 = getResources().getIdentifier("index" + (i + 1),
                    "drawable", MainActivity.this.getPackageName());
            if (arg0 == i) {
                iv.setImageResource(resId);
                tv.setTextColor(getResources().getColor(R.color.tv_main_bottom1));
            } else {
                iv.setImageResource(resId2);
                tv.setTextColor(getResources().getColor(R.color.tv_main_bottom));
            }
        }
    }

    /**
     * 获取TabHost高度
     * @return
     * TabHost的高度
     * */
    public int getTabHeight() {
        return findViewById(R.id.tab_rg_menu).getHeight();
    }


    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            backToHome();
        }
    }

    /**
     * describe:Return to the main interface of the phone
     *
     * 返回手机主界面
     * */
    private void backToHome(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * describe:Whether to exit the system method, select OK to close the service, clear the push
     *          message, and clear the password, and jump to the login interface.Choose to cancel,
     *          directly close the dialog, the other do nothing.
     *
     * 是否退出系统方法，选择确定时关闭服务，清除推送消息，并清除密码，
     * <br>并跳转到登录界面。选择取消时，直接关闭dialog，其他什么也不做。
     * */
    public void backFuc() {

        showDialog(R.string.axeac_msg_prompt, R.string.axeac_msg_loginout, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                StaticObject.wirte.edit().remove(StaticObject.PASSWORD).commit();
                Intent iService = new Intent(act, TaskCountTaskService.class);
                act.stopService(iService);
                Intent msgService = new Intent(act, MsgCountTaskService.class);
                act.stopService(msgService);
                MainFragment2.navMap.clear();
                NotificationManager mNotificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(0);
                mNotificationManager.cancel(1);
                StaticObject.loginFlag = false;
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }


}
