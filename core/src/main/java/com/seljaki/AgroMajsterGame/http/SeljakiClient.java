package com.seljaki.AgroMajsterGame.http;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.google.gson.Gson;
import okhttp3.*;

import static com.seljaki.AgroMajsterGame.utils.Constants.SELJAKI_SERVER_URL;

public class SeljakiClient {
    private static final String FILENAME = "userdata.json";
    LoginInfo loginInfo = null;
    public boolean isLoggedIn() {
        return loginInfo != null;
    }

    public User getUser() {
        if(loginInfo == null)
            return null;

        return loginInfo.user;
    }

    public static SeljakiClient loadData() {
        FileHandle file = Gdx.files.local(FILENAME);

        if(!file.exists())
            return new SeljakiClient();

        Json json = new Json();
        String jsonGameData = file.readString();
        return json.fromJson(SeljakiClient.class, jsonGameData);
    }

    public void saveData() {
        FileHandle file = Gdx.files.local(FILENAME);

        Json json = new Json();
        String jsonGameData = json.toJson(this, SeljakiClient.class);
        file.writeString(jsonGameData, false);
    }

    public void logOut() {
        loginInfo = null;
        FileHandle file = Gdx.files.local(FILENAME);
        if(file.exists())
            file.delete();
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
