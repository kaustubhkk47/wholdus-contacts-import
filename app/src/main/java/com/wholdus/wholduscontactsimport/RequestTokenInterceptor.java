package com.wholdus.wholduscontactsimport;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kaustubh on 15/2/17.
 */

public class RequestTokenInterceptor implements Interceptor {

    private Context mContext;

    public RequestTokenInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder()
                .addHeader(APIConstants.HEADER_ACCEPT, APIConstants.HEADER_ACCEPT_V1)
                .addHeader(APIConstants.HEADER_AUTH, GlobalAccessHelper.getAccessToken(mContext))
                .build();

        return chain.proceed(newRequest);
    }
}
