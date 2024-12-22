package com.seljaki.AgroMajsterGame.http;

import com.google.gson.Gson;
import okhttp3.*;

import static com.seljaki.AgroMajsterGame.utils.Constants.SELJAKI_SERVER_URL;

public class SeljakiClient {

    LoginInfo loginInfo = null;
    public boolean isLoggedIn() {
        return loginInfo != null;
    }

    public User getUser() {
        if(loginInfo == null)
            return null;

        return loginInfo.user;
    }

    public boolean logIn(String username, String password) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        String jsonBody = "{"
            + "\"username\":\"" + username + "\","
            + "\"password\":\"" + password + "\""
            + "}";

        RequestBody body = RequestBody.create(
            jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
            .url(SELJAKI_SERVER_URL+"/auth/login") // Replace with your actual server URL
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Parse the JSON response
                String responseBody = response.body().string();
                loginInfo = gson.fromJson(responseBody, LoginInfo.class);

                return true;
            } else {
                System.err.println("Login failed: " + response.code() + " - " + response.message());
                return false;
            }
        } catch (Exception e) {
            // Handle exceptions such as network issues
            e.printStackTrace();
            return false;
        }
    }
}
