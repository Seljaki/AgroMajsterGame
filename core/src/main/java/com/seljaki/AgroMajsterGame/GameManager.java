package com.seljaki.AgroMajsterGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameManager {

    public static final GameManager INSTANCE = new GameManager();
    private static final String PREFS_NAME = "mySettingsPrefs";

    private static final String KEY_DIFFICULTY = "difficulties";

    private Preferences prefs;

    private GameManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void setDifficulty(String player) {
        prefs.putString(KEY_DIFFICULTY, player);
        prefs.flush();
    }

    public String getDifficulty() {
        return prefs.getString(KEY_DIFFICULTY, "Easy");
    }
}
