package com.wholdus.wholduscontactsimport;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText mEMailEditText, mPasswordEditText;

    public static final String EMAIL_KEY = "email", PASSWORD_KEY = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoginHelper loginHelper = new LoginHelper(getApplicationContext());
        if (loginHelper.checkIfLoggedIn()) {
            startHomeActivity();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEMailEditText = (EditText) findViewById(R.id.email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLoginButtonClick();
            }
        });
    }

    private void handleLoginButtonClick() {
        String password = mPasswordEditText.getText().toString();
        String email = mEMailEditText.getText().toString();

        if (!(password.equals("") || email.equals(""))) {
            try {
                JSONObject loginData = new JSONObject();
                loginData.put(EMAIL_KEY, email);
                loginData.put(PASSWORD_KEY, password);
                new LoginTask().execute(loginData.toString());
            } catch (Exception e) {
                Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.login_incorrect_input, Toast.LENGTH_SHORT).show();
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Response response = OkHttpHelper.makePostRequest(getApplicationContext(),
                        OkHttpHelper.generateUrl(APIConstants.LOGIN_URL), strings[0]);
                String responseBody = response.body().string();
                response.body().close();
                JSONObject data = new JSONObject(responseBody);
                if (response.isSuccessful()) {
                    LoginHelper loginHelper = new LoginHelper(getApplicationContext());
                    return loginHelper.login(data);
                }else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                startHomeActivity();
            } else {
                Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
