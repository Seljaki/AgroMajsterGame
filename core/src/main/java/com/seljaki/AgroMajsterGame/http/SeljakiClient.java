package com.seljaki.AgroMajsterGame.http;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.google.gson.*;
import com.seljaki.AgroMajsterGame.utils.Geolocation;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.seljaki.AgroMajsterGame.utils.Constants.SELJAKI_CHAIN_URL;
import static com.seljaki.AgroMajsterGame.utils.Constants.SELJAKI_SERVER_URL;

public class SeljakiClient {
    private static final String FILENAME = "userdata.json";
    LoginInfo loginInfo = null;
    Plot[] plots = null;
    Weather weather = null;

    public SeljakiClient(){}
    public SeljakiClient(LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    public boolean isLoggedIn() {
        return loginInfo != null;
    }

    public User getUser() {
        if(loginInfo == null)
            return null;

        return loginInfo.user;
    }

    public String getUsername() {
        if(loginInfo == null)
            return null;

        return getUser().username;
    }

    public static SeljakiClient loadData() {
        FileHandle file = Gdx.files.local(FILENAME);

        if(!file.exists())
            return new SeljakiClient();

        Json json = new Json();
        String jsonLoginInfo = file.readString();

        return new SeljakiClient(json.fromJson(LoginInfo.class, jsonLoginInfo));
    }

    public void saveData() {
        FileHandle file = Gdx.files.local(FILENAME);
        Json json = new Json();
        String jsonLoginInfo = json.toJson(loginInfo, LoginInfo.class);
        file.writeString(jsonLoginInfo, false);
    }

    public void logOut() {
        loginInfo = null;
        FileHandle file = Gdx.files.local(FILENAME);
        if(file.exists())
            file.delete();
    }

    @Nullable
    public Weather getWeather() {
        if(weather != null)
            return weather;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(SELJAKI_CHAIN_URL + "/blockchain/lastBlock")
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Gson gson = new Gson();
                // Parse the JSON response
                String responseBody = response.body().string();

                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                // Navigate to the prediction object
                JsonObject prediction = jsonObject
                    .getAsJsonObject("data")
                    .getAsJsonObject("prediction");

                float clear = prediction.get("clear").getAsFloat();
                float cloudy = prediction.get("cloudy").getAsFloat();
                float rainy = prediction.get("rainy").getAsFloat();

                // Determine the highest value and return the corresponding enum
                if (clear >= cloudy && clear >= rainy)
                    weather = Weather.CLEAR;
                else if (cloudy >= clear && cloudy >= rainy)
                    weather = Weather.CLOUDY;
                else
                    weather = Weather.RAINY;
                return weather;
            } else {
                //System.err.println("Login failed: " + response.code() + " - " + response.message());
                return null;
            }
        } catch (Exception e) {
            // Handle exceptions such as network issues
            e.printStackTrace();
            return null;
        }
    }

    public boolean logIn(String username, String password) {
        OkHttpClient client = new OkHttpClient();

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
                Gson gson = new Gson();
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

    public boolean updatePlot(Plot plot) { // ne posodobi vsega !!!
        if(loginInfo == null) return false;

        OkHttpClient client = new OkHttpClient();

        String json = "{"
            + "\"title\": \"" + plot.title + "\","
            + "\"note\": \"" + plot.note + "\","
            + "\"archived\": " + plot.archived
            + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
            .put(body)
            .addHeader("x-auth-token", loginInfo.token)
            .url(SELJAKI_SERVER_URL+ "/plots/" + plot.id)
            .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public Plot[] getPlots() {
        if(loginInfo == null) return null;
        if(this.plots != null) return this.plots;

        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        Request request = new Request.Builder()
            .get()
            .addHeader("x-auth-token", loginInfo.token)
            .url(SELJAKI_SERVER_URL+"/plots/geojson")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();

                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                JsonArray features = jsonResponse
                    .getAsJsonObject("plots")
                    .getAsJsonArray("features");

                // Convert each feature to a Plot object
                List<Plot> plots = new ArrayList<>();
                for (JsonElement featureElement : features) {
                    JsonObject feature = featureElement.getAsJsonObject();

                    // Extract properties
                    JsonObject properties = feature.getAsJsonObject("properties");
                    int id = properties.get("id").getAsInt();
                    String title = properties.get("title").getAsString();
                    String note = properties.get("note").getAsString();
                    String plotNumber = properties.get("plotNumber").getAsString();
                    int cadastralMunicipality = properties.get("cadastralMunicipality").getAsInt();
                    boolean archived = properties.get("archived").getAsBoolean();

                    // Extract geometry (coordinates)
                    JsonArray coordinatesArray = feature
                        .getAsJsonObject("geometry")
                        .getAsJsonArray("coordinates")
                        .get(0)
                        .getAsJsonArray(); // Assuming Polygon with one set of coordinates

                    // Convert coordinates to Geolocation[]
                    List<Geolocation> coordinates = new ArrayList<>();
                    for (JsonElement coordinateElement : coordinatesArray) {
                        JsonArray coordinate = coordinateElement.getAsJsonArray();
                        double lng = coordinate.get(0).getAsDouble();
                        double lat = coordinate.get(1).getAsDouble();
                        coordinates.add(new Geolocation(lat, lng));
                    }

                    // Create and add Plot to the list
                    Plot plot = new Plot();
                    plot.id = id;
                    plot.title = title;
                    plot.note = note;
                    plot.plotNumber = plotNumber;
                    plot.cadastralMunicipality = cadastralMunicipality;
                    plot.archived = archived;
                    plot.coordinates = coordinates.toArray(new Geolocation[0]);
                    plots.add(plot);
                }

                this.plots = plots.toArray(new Plot[0]);
                return this.plots;
            } else {
                System.err.println("Get failed: " + response.code() + " - " + response.message());
                return null;
            }
        } catch (Exception e) {
            // Handle exceptions such as network issues
            e.printStackTrace();
            return null;
        }
    }
}
