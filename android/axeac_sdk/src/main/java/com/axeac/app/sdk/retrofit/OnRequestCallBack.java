package com.axeac.app.sdk.retrofit;

import com.axeac.app.sdk.jhsp.JHSPResponse;
/**
 * 网络请求回调接口
 * @author axeac
 * @version 1.0.0
 * */
public interface OnRequestCallBack {
    void onStart();

    void onCompleted();

    void onSuccesed(JHSPResponse response);

    void onfailed(Throwable e);
}
