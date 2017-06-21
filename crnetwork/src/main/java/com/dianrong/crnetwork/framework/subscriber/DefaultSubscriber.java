package com.dianrong.crnetwork.framework.subscriber;

import android.support.annotation.NonNull;
import android.util.Log;

import com.dianrong.crnetwork.framework.ObservableHandler;
import com.dianrong.crnetwork.framework.adapter.ExceptionAdapter;
import com.dianrong.crnetwork.framework.error.ErrorHandler;
import com.dianrong.crnetwork.framework.view.IBaseView;
import com.dianrong.crnetwork.response.RequestException;

/**
 * Created by PengFeifei on 17-6-19.
 */

public abstract class DefaultSubscriber<T> extends BaseSubscriber<T> {

    private ErrorHandler errorHandler;
    private IBaseView baseView;

    public DefaultSubscriber(@NonNull IBaseView baseView, ErrorHandler errorHandler) {
        super(baseView);
        this.errorHandler = errorHandler;
        this.baseView = baseView;
    }

    @Override
    public void onError(Throwable e) {
        try {
            RequestException requestException = ExceptionAdapter.toRequestException(e, ObservableHandler.getHttpUrl());
            if (errorHandler != null && errorHandler.onErrorOccurs(requestException)) {
                super.onError(e,true);
            } else {
                super.onError(e,false);
                baseView.onRequestError(ExceptionAdapter.toRequestException(e, ObservableHandler.getHttpUrl()));
            }
        } catch (Exception e1) {
            baseView.onRequestError(ExceptionAdapter.toRequestException(e, ObservableHandler.getHttpUrl()));
            Log.e("DefaultSubscriber-->", e1.getMessage());
        } finally {
            ObservableHandler.setHttpUrl(null);
        }
    }

}
