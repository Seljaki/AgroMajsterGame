package com.seljaki.AgroMajsterGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameManager {

    public static final GameManager INSTANCE = new GameManager();
    private static final String PREFS_NAME = "mySettingsPrefs";

    private static final String KEY_DIFFICULTY_MAGPIE = "difficultiesMagpieGame";
    private static final String KEY_DIFFICULTY_MOLE = "difficultiesMoleGame";

    private Preferences prefs;

    private GameManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void setDifficultyMagpie(String difficulty) {
        prefs.putString(KEY_DIFFICULTY_MAGPIE, difficulty);
        prefs.flush();
    }

    public String getDifficultyMagpie() {
        return prefs.getString(KEY_DIFFICULTY_MAGPIE, "Easy");
    }

    public void setDifficultyMole(String difficulty) {
        prefs.putString(KEY_DIFFICULTY_MOLE, difficulty);
        prefs.flush();
    }

    public String getDifficultyMole() {
        return prefs.getString(KEY_DIFFICULTY_MOLE, "Easy");
    }
}
