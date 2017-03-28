package com.wholdus.wholduscontactsimport;

import android.content.Context;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by kaustubh on 15/2/17.
 */

public class OkHttpHelper {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient mOkHttpClient;

    public static synchronized OkHttpClient getClient(Context context) {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new RequestTokenInterceptor(context))
                    .build();
        }
        return mOkHttpClient;
    }

    public static String generateUrl(String endpoint) {
        return APIConstants.API_BASE + endpoint;
    }

    public static Response makeGetRequest(Context context, String url) throws IOException {
        OkHttpClient okHttpClient = OkHttpHelper.getClient(context);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return okHttpClient.newCall(request).execute();
    }

    public static Response makePostRequest(Context context, String url, String data) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, data);
        OkHttpClient okHttpClient = OkHttpHelper.getClient(context);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        return okHttpClient.newCall(request).execute();
    }

    public static Response makePutRequest(Context context, String url, String data) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, data);
        OkHttpClient okHttpClient = OkHttpHelper.getClient(context);

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        return okHttpClient.newCall(request).execute();
    }

    public static Response makeDeleteRequest(Context context, String url, String data) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, data);
        OkHttpClient okHttpClient = OkHttpHelper.getClient(context);

        Request request = new Request.Builder()
                .url(url)
                .delete(requestBody)
                .build();

        return okHttpClient.newCall(request).execute();
    }
}
