package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.RegionNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DuckHuntMagpie extends ScreenAdapter {
    private SeljakiMain game;
    private Stage stage;
    private Stage stageFront;
    private Stage stageScore;
    private Skin skin;
    private Table background;
    private Table backgroundFront;
    private Random random;
    private Map<Image, Boolean> moleHillStateMap;
    private int activeMoles;
    private int score;
    private Label scoreLabel;
    private Image timerBar;
    private float timeRemaining;
    private final float totalTime = 5f;
    TextureRegion mole1Texture;
    TextureRegion mole2Texture;

    private Table resultTable;
    private TextureAtlas gameplayAtlas;

    public DuckHuntMagpie(SeljakiMain game) {
        this.game = game;
        this.random = new Random();
        this.moleHillStateMap = new HashMap<>();
        this.activeMoles = 0;
        this.score = 0;
        this.timeRemaining = totalTime;
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height);
        stageFront.getViewport().update(width, height);
        stageScore.getViewport().update(width, height);
    }

    @Override
    public void show() {
    }

    @Override
    public void dispose() {
        Timer.instance().clear();
        stage.dispose();
        stageFront.dispose();
        stageScore.dispose();
    }
}
