package com.seljaki.AgroMajsterGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.net.HttpRequestBuilder.json;

public class GameManager {

    public static final GameManager INSTANCE = new GameManager();
    private static final String PREFS_NAME = "mySettingsPrefs";

    private static final String KEY_DIFFICULTY_MAGPIE = "difficultiesMagpieGame";
    private static final String KEY_DIFFICULTY_MOLE = "difficultiesMoleGame";

    private static final String FILE_NAME_MAGPIE = "leaderboardMagpie.json";
    private static final String FILE_NAME_MOLE = "leaderboardMole.json";

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


    public static class PlayerScore {
        public int highScore;
        public String playerName;
    }

    public static class Leaderboard {
        public List<PlayerScore> playerScores  = new ArrayList<>();

        public void sortByHighScore() {
            if (playerScores != null) {
                playerScores.sort((p1, p2) -> Integer.compare(p2.highScore, p1.highScore));
            }
        }
    }

    public static Leaderboard loadLeaderboard(boolean minigameType) {
        FileHandle file;
        if (minigameType) {
            file = Gdx.files.local(FILE_NAME_MAGPIE);
        } else {
            file = Gdx.files.local(FILE_NAME_MOLE);
        }
        if (file.exists()) {
            String jsonStr = file.readString();
            Leaderboard leaderboard = json.fromJson(Leaderboard.class, jsonStr);
            System.out.println("Leaderboard loaded from " + file.nameWithoutExtension());
            leaderboard.sortByHighScore();
            return leaderboard;
        } else {
            System.out.println("Leaderboard file not found, returning a new empty leaderboard.");
            return new Leaderboard();
        }
    }

    public static void saveLeaderboard(Leaderboard leaderboard, boolean minigameType) {
        FileHandle file;
        if (minigameType) {
            file = Gdx.files.local(FILE_NAME_MAGPIE);
        } else {
            file = Gdx.files.local(FILE_NAME_MOLE);
        }
        String jsonTable = json.toJson(leaderboard);
        file.writeString(jsonTable, false);
        System.out.println("Leaderboard saved to " + file.nameWithoutExtension());
    }
}
