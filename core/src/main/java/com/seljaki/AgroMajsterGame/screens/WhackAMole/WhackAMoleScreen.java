package com.seljaki.AgroMajsterGame.screens.WhackAMole;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.RegionNames;
import com.seljaki.AgroMajsterGame.screens.MapScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WhackAMoleScreen extends ScreenAdapter {
    private final SeljakiMain game;
    private Stage stage;
    private Stage stageFront;
    private Stage stageScore;
    private final Skin skin;
    private final Random random;
    private final Map<Image, Boolean> moleHillStateMap;
    private int activeMoles;
    private int score;
    private Label scoreLabel;
    private Image timerBar;
    private float timeRemaining;
    private final float totalTime = 30f;
    TextureRegion mole1Texture;
    TextureRegion mole2Texture;

    private Table resultTable;
    private final TextureAtlas gameplayAtlas;
    private final Sound moleSqueak;
    private final Music gameMusic;
    private final ParticleEffect particleEffectMoleBlood;
    private AssetManager assetManager;


    public WhackAMoleScreen(SeljakiMain game) {
        this.game = game;
        this.random = new Random();
        this.moleHillStateMap = new HashMap<>();
        this.activeMoles = 0;
        this.score = 0;
        this.timeRemaining = totalTime;
        assetManager = game.getAssetManager();
        skin = game.skin;
        gameplayAtlas = game.gameplayAtlas;
        moleSqueak = assetManager.get(AssetDescriptors.MOLE_SQUEAK_SOUND);
        gameMusic = assetManager.get(AssetDescriptors.WHACK_A_MOLE_MUSIC);
        gameMusic.setLooping(true);
        particleEffectMoleBlood = assetManager.get(AssetDescriptors.PARTICLE_EFFECT_MOLE_BLOOD);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
        stageFront.act(delta);
        stageFront.draw();
        stageScore.act(delta);
        stageScore.draw();

        if (particleEffectMoleBlood.isComplete()) {
            particleEffectMoleBlood.reset();
        }

        timeRemaining -= delta;
        if (timeRemaining < 0) timeRemaining = 0;
        updateTimerBar();
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
        if (!gameMusic.isPlaying()) gameMusic.play();
        stage = new Stage(game.viewport);
        stageFront = new Stage(game.viewport);
        stageScore = new Stage(game.viewport);
        Gdx.input.setInputProcessor(stage);


        TextureRegion frontHillTexture = gameplayAtlas.findRegion(RegionNames.MOLEHILL_FRONT);
        TextureRegion backHillTexture = gameplayAtlas.findRegion(RegionNames.MOLEHILL_BACK);
        TextureRegion backgroundTexture = gameplayAtlas.findRegion(RegionNames.GRASSY_BACKGROUND);
        mole1Texture = gameplayAtlas.findRegion(RegionNames.MOLE1);
        mole2Texture = gameplayAtlas.findRegion(RegionNames.MOLE2);

        Image grassyBackground = new Image(backgroundTexture);
        grassyBackground.setSize(stage.getWidth(), stage.getHeight());
        grassyBackground.setPosition(0, 0);

        stage.addActor(grassyBackground);
        Table background = new Table();
        background.setSize(stage.getWidth(), stage.getHeight());
        background.setPosition((stage.getWidth() - stage.getWidth()) / 2,
            (stage.getHeight() - stage.getHeight()) / 2);

        Table backgroundFront = new Table();
        backgroundFront.setSize(stage.getWidth(), stage.getHeight());
        backgroundFront.setPosition((stage.getWidth() - stage.getWidth()) / 2,
            (stage.getHeight() - stage.getHeight()) / 2 - 10);


        for (int i = 0; i < 12; i++) {
            Image molehillBack = new Image(backHillTexture);
            Image molehillFront = new Image(frontHillTexture);
            moleHillStateMap.put(molehillBack, false);
            background.add(molehillBack).pad(20).padTop(60);
            backgroundFront.add(molehillFront).pad(20).padTop(60);
            if ((i + 1) % 4 == 0) {
                background.row();
                backgroundFront.row();
            }

            scheduleMolePopUp(molehillBack);
        }

        scoreLabel = new Label("Score: " + score, skin);
        scoreLabel.setPosition(stage.getWidth() / 2 - scoreLabel.getWidth() / 2, stage.getHeight() - 50);
        timerBar = createTimerBar();

        Label timeLabel = new Label("Time: ", skin);
        timeLabel.setPosition(5,20);
        timerBar.setPosition(0, 0);

        stage.addActor(background);
        stage.addActor(scoreLabel);
        stage.addActor(timeLabel);
        stageFront.addActor(backgroundFront);
        stage.addActor(timerBar);
    }

    private void scheduleMolePopUp(final Image molehillBack) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (timeRemaining <= 0) {
                    showResultTable();
                    cancel();
                    return;
                }
                if (!moleHillStateMap.get(molehillBack) && shouldSpawn()) {
                    Image mole = new Image(mole1Texture);
                    moleHillStateMap.put(molehillBack, true);
                    activeMoles++;

                    mole.setPosition(
                        molehillBack.getX() + molehillBack.getWidth() / 2 - mole.getWidth() / 2,
                        molehillBack.getY() + molehillBack.getHeight() / 2 - mole.getHeight() / 2 - 15
                    );
                    stage.addActor(mole);

                    mole.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                        private boolean clicked = false;

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            if (!clicked) {
                                ParticleEffect particleEffectInstance = new ParticleEffect(particleEffectMoleBlood);
                                particleEffectInstance.setPosition(mole.getX() + mole.getWidth() / 2, mole.getY() + mole.getHeight() / 2);
                                particleEffectInstance.start();
                                Actor particleActor = new Actor() {
                                    @Override
                                    public void draw(Batch batch, float parentAlpha) {
                                        particleEffectInstance.draw(batch, Gdx.graphics.getDeltaTime());
                                    }

                                    @Override
                                    public void act(float delta) {
                                        super.act(delta);
                                        if (particleEffectInstance.isComplete()) {
                                            particleEffectInstance.dispose();
                                            this.remove();
                                        }
                                    }
                                };
                                stageFront.addActor(particleActor);
                                mole.setDrawable(new TextureRegionDrawable(new TextureRegion(mole2Texture)));
                                clicked = true;
                                score++;
                                updateScoreLabel();
                                mole.clearActions();
                                moleSqueak.play();
                                mole.addAction(Actions.sequence(
                                    Actions.delay(0.2f),
                                    Actions.moveBy(0, -30, 0.1f),
                                    Actions.run(() -> {
                                        mole.remove();
                                        moleHillStateMap.put(molehillBack, false);
                                        activeMoles--;
                                    })
                                ));
                            }
                            return true;
                        }

                    });

                    mole.addAction(Actions.sequence(
                        Actions.delay(2),
                        Actions.moveBy(0, 45, 0.3f),
                        Actions.delay(2),
                        Actions.moveBy(0, -45, 0.3f),
                        Actions.run(() -> {
                            mole.remove();
                            moleHillStateMap.put(molehillBack, false);
                            activeMoles--;
                        })
                    ));
                }
            }
        }, 0, 0.3f);
    }


    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
        scoreLabel.setPosition(stage.getWidth() / 2 - scoreLabel.getWidth() / 2, stage.getHeight() - 50);
    }
    private boolean shouldSpawn() {
        int chance = random.nextInt()%100;
        if (chance < 50) return activeMoles < 1;
        if (chance < 75) return activeMoles < 2;
        if (chance < 90) return activeMoles < 3;
        return activeMoles < 4;
    }

    private Image createTimerBar() {
        Pixmap pixmap = new Pixmap(1, 20, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.ORANGE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        Image bar = new Image(texture);
        bar.setSize(stage.getWidth(), 20);
        return bar;
    }

    private void updateTimerBar() {
        float width = (timeRemaining / totalTime) * stage.getWidth();
        timerBar.setSize(width, timerBar.getHeight());
    }

    private void showResultTable(){
        if (resultTable != null) {
            resultTable.remove();
        }
        resultTable = new Table();
        resultTable.setSize(200, 170);
        resultTable.setBackground(skin.getDrawable("window"));
        resultTable.setPosition((stageScore.getWidth() - resultTable.getWidth()) / 2,
            (stageScore.getHeight() - resultTable.getHeight()) / 2);

        Label OverLabel = new Label("Game Over!", skin);
        Label scoreLabel = new Label("Final Score: " + score, skin);
        TextButton backButton = new TextButton("Return to map", skin);
        TextButton againButton = new TextButton("Play Again", skin);

        resultTable.add(OverLabel).pad(10).colspan(2).padBottom(25).row();
        resultTable.add(scoreLabel).pad(10).colspan(2).row();
        resultTable.add(againButton).pad(10);
        resultTable.add(backButton).pad(10).row();

        stageScore.addActor(resultTable);
        Gdx.input.setInputProcessor(stageScore);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resultTable.remove();
                gameMusic.stop();
                game.setScreen(new MapScreen(game));
            }
        });
        againButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resultTable.remove();
                gameMusic.stop();
                dispose();
                game.setScreen(new WhackAMoleScreen(game));
            }
        });
    }

    @Override
    public void dispose() {
        Timer.instance().clear();
        stage.dispose();
        stageFront.dispose();
        stageScore.dispose();
    }
}
