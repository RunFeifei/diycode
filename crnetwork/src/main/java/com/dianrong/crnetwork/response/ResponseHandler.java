package com.dianrong.crnetwork.response;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import com.dianrong.android.common.AppContext;
import com.dianrong.crnetwork.error.ErrorCode;
import com.dianrong.crnetwork.dataformat.Entity;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by PengFeifei on 17-4-20.
 */

public class ResponseHandler {

    private static final String TAG = ResponseHandler.class.getSimpleName();

    /**
     * 同步方法
     * 勿在主线程执行此方法
     */
    public static <T extends Entity> Response<T> getSyncResponse(Call<T> call) {
        HttpUrl url;
        try {
            url = call.request().url();
        } catch (NullPointerException e) {
            url = HttpUrl.parse("https://www.null.com/");
        }

        Response<T> response = null;
        try {
            response = call.execute();
        } catch (Exception e) {
            throw new RequestException(url, ErrorCode.NETWORK_ERR, e);
        }

        if (response == null) {
            throw new RequestException(url, ErrorCode.RESPONSE_NULL_ERR, "response is null");
        }
        if (!response.isSuccessful()) {
            throw new RequestException(url, response.code(), "response is failed");
        }
        return response;
    }


    /**
     * 异步方法
     */
    public static <T extends Entity> void getAsyncResponse(Call<T> call, ResponseCallback<T> callback) {
        call.enqueue(callback);
    }


    private static boolean isMainProcess() {
        int pid = Process.myPid();
        String processName = "";
        ActivityManager mActivityManager = (ActivityManager) AppContext.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                processName = appProcess.processName;
                break;
            }
        }
        if (processName.equals(AppContext.getInstance().getPackageName())) {
            return true;
        }
        return false;
    }

}