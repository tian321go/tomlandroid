package com.axeac.app.client.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.axeac.app.client.R;
import com.axeac.app.client.adapters.SettingsListAdapter;
import com.axeac.app.sdk.KhinfSDK;
import com.axeac.app.sdk.activity.BaseActivity;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * 设置界面
 * @author axeac
 * @version 2.3.0.0001
 * */
public class SettingsActivity extends BaseActivity {

    private Context mContext;

    private ListView mSettingsList01;
    private TextView clientIdText;
    private Button btn_isdemo;
    private boolean isdemo = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.settings_normal);
        setTitle(R.string.settings_label);
        mContext = this;
        clientIdText = (TextView) this.findViewById(R.id.clientId);
        mSettingsList01 = (ListView) this.findViewById(R.id.settings_list_1);
        btn_isdemo = (Button) this.findViewById(R.id.btn_isdemo);
        mSettingsList01.setVisibility(View.VISIBLE);
        TelephonyManager mTelephonyMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        btn_isdemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isdemo) {
                    btn_isdemo.setBackgroundResource(R.drawable.axeac_switch_close);
                    isdemo = false;
                    StaticObject.wirte.edit().putBoolean(StaticObject.SERVER_IS_DEMO,isdemo).commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERDESC, "").commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERURL, "").commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERURL_IP, "").commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERURL_SERVERNAME, "").commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERURL_HTTP_PORT, "").commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERURL_VPN_IP, "").commit();
                    StaticObject.wirte.edit().putString(StaticObject.SERVERURL_VPN_PORT, "").commit();
                    StaticObject.wirte.edit().putBoolean(StaticObject.SERVERURL_IS_HTTPS, false).commit();
                    KhinfSDK.deleteIp("https://www.axeac.com:8443/HttpServer",mContext);
                    KhinfSDK.deleteIp("https://www.axeac.cn:8443/HttpServer",mContext);
                } else {
                    btn_isdemo.setBackgroundResource(R.drawable.axeac_switch_open);
                    isdemo = true;
                    StaticObject.wirte.edit().putBoolean(StaticObject.SERVER_IS_DEMO,isdemo).commit();
                    KhinfSDK.getDefaultInfo(mContext);
                }
            }
        });
        if(StaticObject.read.getBoolean(StaticObject.SERVER_IS_DEMO,true)){
            btn_isdemo.setBackgroundResource(R.drawable.axeac_switch_open);
        }else{
            btn_isdemo.setBackgroundResource(R.drawable.axeac_switch_close);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String clientId = mTelephonyMgr.getDeviceId() == null ? "" : mTelephonyMgr.getDeviceId();
        clientIdText.setText(getResources().getString(R.string.settings_deviceid) + clientId);
        clientIdText.setAlpha(0.8f);
        String[] mSettingsLabelList01 = {
                mContext.getString(R.string.settings_systemsetups),
                mContext.getString(R.string.settings_network)};
        mSettingsList01.setAdapter(new SettingsListAdapter(mContext, mSettingsLabelList01));
        mSettingsList01.setOnItemClickListener(mListItemClickListener01());
        setListView(mSettingsList01);
        backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFuc();
            }
        });
    }

    /**
     * 设置ListView
     * */
    private void setListView(ListView listView) {
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setFocusable(true);
        listView.setFocusableInTouchMode(true);
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

    private AdapterView.OnItemClickListener mListItemClickListener01() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    mContext.startActivity(new Intent(mContext, SystemSetupsActivity.class));
                }
                if (position == 1) {
                    mContext.startActivity(new Intent(mContext, NetworkListActivity.class));
                }
                finish();
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backFuc();
        }
        return false;
    }

    private void backFuc() {
        this.finish();
    }
}