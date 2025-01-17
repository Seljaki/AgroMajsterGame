package com.seljaki.AgroMajsterGame.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Keys {
    public static final Keys INSTANCE = new Keys();
    private Keys() {
        FileHandle keyFile = Gdx.files.local("mapbox.key");
        if (!keyFile.exists()) {
            Gdx.app.error("mapbox key", "Error, no file 'mapbox.key' found");
            Gdx.app.exit();
        }
        String key = keyFile.readString();
        if(key.isEmpty()) {
            Gdx.app.error("mapbox key", "Error, no key found");
            Gdx.app.exit();
        }
        MAPBOX = key;
    }

    public String getMAPBOX() {
        return MAPBOX;
    }

    public String getGEOAPIFY() {
        return GEOAPIFY;
    }

    private String MAPBOX = "";
    private String GEOAPIFY = "";
}
