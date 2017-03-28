package com.wholdus.wholduscontactsimport;

import android.app.Application;

/**
 * Created by kaustubh on 15/2/17.
 */

public class WholdusContactsImportApplication extends Application {

    private String mAccessToken;

    public void setTokens(String aToken) {
        mAccessToken = aToken;
    }

    public String getAccessToken() {
        return mAccessToken;
    }


}
