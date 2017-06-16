package com.dianrong.crnetwork.response;

import android.support.annotation.NonNull;

import com.dianrong.crnetwork.dataformat.DrList;
import com.dianrong.crnetwork.dataformat.DrRoot;
import com.dianrong.crnetwork.dataformat.Entity;
import com.dianrong.crnetwork.error.DrErrorMsgHelper;
import com.dianrong.crnetwork.error.ErrorCode;

import java.util.ArrayList;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;
import util.Strings;

/**
 * Created by PengFeifei on 17-4-21.
 * 只对content,result字段做了处理
 * 根数据结构
 * {
 * "content": {},
 * "result":  "...",//login or success ...
 * "message": "...",
 * "message-args": "...",
 * "errors": [ ]
 * "code": 3456
 * }
 */
//Note T=ContentWrapper<TemplateEntityList<ErrorItem>>>
public class DrResponse<T extends Entity> {

    public DrResponse() {
    }

    /**
     * @return 点融统一的数据格式 && 数据非空
     */
    private boolean checkRootData(final Call<T> call, Response<T> response) {
        if (response == null || !response.isSuccessful()) {
            return false;
        }
        T root = response.body();
        //是否是点融统一的数据格式
        boolean isDianrongDataFormat = root instanceof DrRoot;
        DrRoot drRoot = null;
        try {
            drRoot = (DrRoot) response.body();
        } catch (ClassCastException e) {
            throwRequestException(call.request().url(), "can not cast to dainrong root data");
        }
        boolean result = isDianrongDataFormat && drRoot != null;
        if (!result) {
            throwRequestException(call.request().url(), "can not cast to dainrong root data");
        }
        return true;
    }

    /**
     * @param response
     * @param call
     * @return
     */
    private DrRoot getRootData(Response<T> response, final Call<T> call) {
        checkRootData(call, response);
        DrRoot drRoot = (DrRoot) response.body();
        if (drRoot != null) {
            checkRootData(drRoot, call.request().url());
        }
        return drRoot;
    }

    public static boolean checkRootData(@NonNull DrRoot drRoot, HttpUrl url) {
        boolean result = false;
        if (!Strings.isEmpty(drRoot.getResult())) {
            result = dispatchResult(drRoot, url);
        }
        if (!result) {
            int code = drRoot.getCode();
            String drCode = Integer.toString(code);
            String drErrMsg = DrErrorMsgHelper.getErrorMsg(drCode);
            if (drErrMsg.contains("登录") || drErrMsg.contains("登陆")) {
                throw new RequestException(url, ErrorCode.DR_INTERCEPTION_LOGIN_ERR, drErrMsg);
            }
            throw new RequestException(url, code, drErrMsg);
        }
        return true;
    }

    public <Content extends Entity> Content getContentData(Response<T> response, final Call<T> call) {
        DrRoot drRoot = getRootData(response, call);
        if (drRoot == null) {
            throwRequestException(call.request().url(), "drRoot data retuns null");
        }
        Content contentData;
        try {
            contentData = (Content) drRoot.getContent();
        } catch (ClassCastException e) {
            throw new RequestException(call.request().url(), ErrorCode.DR_CAST_ERR, e);
        }
        return contentData;
    }

    public <Item extends Entity> ArrayList<Item> getListData(Response<T> response, final Call<T> call) {
        DrRoot drRoot = getRootData(response, call);
        if (drRoot == null) {
            return null;
        }
        DrList drList = (DrList) drRoot.getContent();
        if (drRoot.getContent() instanceof DrList) {
            drList = (DrList) drRoot.getContent();
        }
        if (drList == null) {
            throwRequestException(call.request().url(), "drList data retuns null");
        }
        ArrayList<Item> list = null;
        try {
            list = drList.getList();
        } catch (ClassCastException e) {
            throw new RequestException(call.request().url(), ErrorCode.DR_CAST_ERR, e);
        }
        return list;
    }

    /**
     * //NOTE 只处理login和Auth
     * 解析Response的result字段
     *
     * @return
     */
    private static boolean dispatchResult(DrRoot drRoot, HttpUrl url) {
        String result = drRoot.getResult();
        ErrorCode.DrResultCode drResultCode = getDrResultCode(result);
        if (drResultCode == ErrorCode.DrResultCode.Login || drResultCode == ErrorCode.DrResultCode.AuthFirst) {
            String errMsg = DrErrorMsgHelper.getErrorMsg(Integer.toString(drRoot.getCode()));
            throw new RequestException(url, ErrorCode.DR_INTERCEPTION_LOGIN_ERR, errMsg);
        }
        return drResultCode.equals(ErrorCode.DrResultCode.Success);
    }

    private static ErrorCode.DrResultCode getDrResultCode(String result) {
        ErrorCode.DrResultCode drResultCode;
        if (result == null) {
            drResultCode = ErrorCode.DrResultCode.Unknown;
        } else if (result.equals("success")) {
            drResultCode = ErrorCode.DrResultCode.Success;
        } else if (result.equals("error")) {
            drResultCode = ErrorCode.DrResultCode.Error;
        } else if (result.equals("login")) {
            drResultCode = ErrorCode.DrResultCode.Login;
        } else if (result.equals("service_disabled")) {
            drResultCode = ErrorCode.DrResultCode.ServiceDisabled;
        } else if (result.equals("session_timeout")) {
            drResultCode = ErrorCode.DrResultCode.SessionTimeout;
        } else if (result.equals("lender_not_a_member")) {
            drResultCode = ErrorCode.DrResultCode.LenderNotAMember;
        } else {
            drResultCode = ErrorCode.DrResultCode.Unknown;
        }
        return drResultCode;
    }

    private void throwRequestException(HttpUrl url, String cause) {
        throw new RequestException(url, ErrorCode.DR_CAST_ERR, cause);
    }


}
