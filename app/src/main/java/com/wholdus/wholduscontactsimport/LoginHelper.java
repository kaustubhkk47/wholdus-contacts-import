package com.wholdus.wholduscontactsimport;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.wholdus.wholduscontactsimport.WholdusContactsImportApplication;

import org.json.JSONObject;

/**
 * Created by aditya on 29/12/16.
 */

public class LoginHelper {

    private static final String ACCESS_TOKEN_KEY = "token";

    private Context mContext;

    public LoginHelper(Context context) {
        mContext = context;
    }

    public boolean checkIfLoggedIn() {
        SharedPreferences sp = getSharedPreference();

        try {
            final String aToken = sp.getString(ACCESS_TOKEN_KEY, null);

            if (aToken == null) {
                return false;
            } else {
                setTokens(aToken);
                return true;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(JSONObject apiResponse) {
        SharedPreferences.Editor editor = getSharedPreference().edit();

        try {
            final String aToken = apiResponse.getString(ACCESS_TOKEN_KEY);

            editor.putString(ACCESS_TOKEN_KEY, aToken);
            editor.apply();

            setTokens(aToken);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean logout() {
        SharedPreferences.Editor editor = getSharedPreference().edit();

        try {

            editor.remove(ACCESS_TOKEN_KEY);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private SharedPreferences getSharedPreference() {
        return mContext.getSharedPreferences("LoginHelperSharedPreference", Context.MODE_PRIVATE);
    }

    private void setTokens(String aToken) {
        try {
            WholdusContactsImportApplication wholdusApplication = ((WholdusContactsImportApplication) mContext);
            wholdusApplication.setTokens(aToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
