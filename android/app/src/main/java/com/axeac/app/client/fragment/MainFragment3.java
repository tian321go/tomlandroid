package com.axeac.app.client.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.axeac.app.client.R;
import com.axeac.app.client.activity.AboutActivity;
import com.axeac.app.client.activity.CheckCurUsersActivity;
import com.axeac.app.client.activity.LoginActivity;
import com.axeac.app.client.activity.MainActivity;
import com.axeac.app.client.activity.PwdUpdateActivity;
import com.axeac.app.client.adapters.SettingsListAdapter;
import com.axeac.app.client.service.MsgCountTaskService;
import com.axeac.app.client.service.TaskCountTaskService;
import com.axeac.app.client.utils.update.PollingUtils;
import com.axeac.app.sdk.KhinfSDK;
import com.axeac.app.sdk.fragment.BaseFragment;
import com.axeac.app.sdk.jhsp.JHSPResponse;
import com.axeac.app.sdk.retrofit.OnRequestCallBack;
import com.axeac.app.sdk.retrofit.UIHelper;
import com.axeac.app.sdk.tools.Property;
import com.axeac.app.sdk.utils.CommonUtil;
import com.axeac.app.sdk.utils.DeviceMessage;
import com.axeac.app.sdk.utils.StaticObject;
import com.axeac.app.sdk.utils.WaterMarkImage;

/**
 * describe: setting interface fragment
 * <br>设置界面fragment
 * @author axeac
 * @version 2.3.0.0001
 * */

public class MainFragment3 extends BaseFragment {

    @Bind(R.id.settings_list)
    ListView listview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.fragment_main_3, null);
            ButterKnife.bind(this, mView);
            initView();
        } else {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
        }
        return mView;
    }

    /**
     * 初始化
     * */
    private void initView() {
        showLinearLayout();

    }

    /**
     * 显示LinearLayout布局
     * */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showLinearLayout() {
        listview.setVisibility(View.VISIBLE);
//        listview.setBackground(WaterMarkImage.getDrawable(StaticObject.read.getString(StaticObject.USERNAME,""),240,240,30));
        String[] mSettingsLabelList01 = {
                this.getString(R.string.settings_gridview),
                this.getString(R.string.settings_listview),
                this.getString(R.string.settings_freshicon),
                this.getString(R.string.settings_updatepwd),
                this.getString(R.string.settings_about),
                this.getString(R.string.settings_exit)
        };
        listview.setAdapter(new SettingsListAdapter(act, mSettingsLabelList01));
        listview.setOnItemClickListener(mListItemClickListener());
        setListView(listview);
    }

    /**
     * 设置ListView
     * @param listView
     * ListView对象
     * */
    private void setListView(ListView listView) {
        ListAdapter mMenuListAdapter = listView.getAdapter();
        if (mMenuListAdapter != null) {
            int totalHeight = 0;
            for (int i = 0; i < mMenuListAdapter.getCount(); i++) {
                View listItem = mMenuListAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (mMenuListAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }
    }

    // describe:listen to item click event
    /**
     * item点击事件监听
     * */
    private AdapterView.OnItemClickListener mListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    StaticObject.isGrid = true;
                    StaticObject.wirte.edit().putBoolean("isGrid", true).commit();
                    LocalBroadcastManager.getInstance(act).sendBroadcast(new Intent(StaticObject.CHANGE_GRID_OR_LIST_ACTION));
                    ((MainActivity) getActivity()).showMidView();
                }
                if (position == 1) {
                    StaticObject.isGrid = false;
                    StaticObject.wirte.edit().putBoolean("isGrid", false).commit();
                    LocalBroadcastManager.getInstance(act).sendBroadcast(new Intent(StaticObject.CHANGE_GRID_OR_LIST_ACTION));
                    ((MainActivity) getActivity()).showMidView();
                }
                if (position == 3) {
                    startActivity(new Intent(act, PwdUpdateActivity.class));
                }
                if (position == 4) {
                    startActivity(new Intent(act, AboutActivity.class));
                }
                if (position == 2) {
                    freshIcon("MEIP_LOGIN=MEIP_LOGIN");

                }
                if (position == 5) {
                    ((MainActivity) getActivity()).backFuc();
                }
            }
        };
    }

    private void freshIcon(String data) {
        final String username = StaticObject.read.getString(StaticObject.USERNAME,"");
        String password = StaticObject.read.getString(StaticObject.PASSWORD,"");
        UIHelper.init(username, password);
        UIHelper.sendRequest(act, username, password, data, new OnRequestCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onSuccesed(JHSPResponse response) {

                if (response.getCode() == 0) {
                    Property navLists = new Property();
                    navLists.setSplit("\n");
                    navLists.load(response.getData());
                    KhinfSDK.getInstance().setProperty(navLists);
                }
                LocalBroadcastManager.getInstance(act).sendBroadcast(new Intent(StaticObject.CHANGE_GRID_OR_LIST_ACTION));
                ((MainActivity) getActivity()).showMidView();
            }

            @Override
            public void onfailed(Throwable e){
            }
        });

    }
    // describe:Check if apk is present
    /**
     * 检查apk是否存在
     * @param context
     * Context对象
     * @param packageName
     * The package name of the apk to check
     * <br>检查的apk的包名
     * */
    boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return false;
        }
        return true;
    }



    String test = "Form = Form\n" +
            "Form.id = Form1\n" +
            "Form.title = PI图\n" +
            "Form.layout = BoxLayoutY\n" +
            "var.HtmlListView1 = HtmlListView\n" +
            "HtmlListView1.headHtml = <body<spa>style<equ>\"height:auto;\">\n" +
            "HtmlListView1.bottomHtml = </body>\n" +
            "HtmlListView1.template = <table<spa>align<equ>\"left\"<spa>width<equ>\"100%\"<spa>height<equ>\"70px\"><tr<spa>height<equ>\"50\"><td<spa>width<equ>\"30%\"><img<spa>src<equ>\"http://10.1.16.151:8086//img/pi.png\"<spa>width<equ>\"50px\"/><input<spa>type<equ>\"hidden\"<spa>text<equ>\"@@ID@@\"/></td><td>@@NAME@@</td></tr></table>\n" +
            "HtmlListView1.addRowData100 = ID,div_pi||NAME,自定义PI图\n" +
            "HtmlListView1.addRowClick0 = ID||NAME||ICON||PAGE:@ID:id<equ>0\n" +
            "Form.btn_add.HtmlListView1 = true\n" +
            "HtmlListView1.addRowData2 = ID,pi_ghhb||NAME,全国华环保指标\n" +
            "HtmlListView1.addRowData3 = ID,pi_ghmap||NAME,全国华实时负荷\n" +
            "HtmlListView1.addRowData4 = ID,pi_guolu1||NAME,＃1锅炉\n" +
            "HtmlListView1.addRowData5 = ID,pi_guolu2||NAME,#2锅炉\n" +
            "HtmlListView1.addRowData6 = ID,pi_qiji1||NAME,＃1汽机\n" +
            "HtmlListView1.addRowData7 = ID,pi_qiji2||NAME,＃2汽机\n" +
            "HtmlListView1.addRowData8 = ID,pi_tongxintu||NAME,#值班人员实时数据检查\n" +
            "HtmlListView1.addRowData9 = ID,pi_xitong1||NAME,＃1系统图\n" +
            "HtmlListView1.addRowData10 = ID,pi_xitong2||NAME,#2系统图\n" +
            "HtmlListView1.addRowData11 = ID,pi_yanqi2||NAME,烟气排放图\n" +
            "HtmlListView1.addRowData11 = ID,pi_yanqi3||NAME,烟气排放图\n" +
            "HtmlListView1.addRowData11 = ID,pi_yanqi4||NAME,烟气排放图\n" +
            "HtmlListView1.addRowData11 = ID,pi_yanqi5||NAME,烟气排放图\n" +
            "Form.id = new_pi\n" +
            "Form.buildDate = 2016-10-20 13:27:55";


}
