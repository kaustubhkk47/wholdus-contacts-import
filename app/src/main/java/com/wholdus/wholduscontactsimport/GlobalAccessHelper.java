package com.wholdus.wholduscontactsimport;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by kaustubh on 15/2/17.
 */

public final class GlobalAccessHelper {

    public static String getAccessToken(Context context) {
        return "access_token=" + ((WholdusContactsImportApplication) context).getAccessToken();
    }

    public static String generateUrl(String endPoint, @Nullable HashMap<String, String> params) {
        return APIConstants.API_BASE + endPoint + getUrlStringFromHashMap(params);
    }

    public static String getUrlStringFromHashMap(@Nullable HashMap<String, String> params) {
        String url = "?";
        if (params != null) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                url += "&" + entry.getKey() + "=" + entry.getValue();
            }
        }
        return url;
    }
}