package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.seljaki.AgroMajsterGame.SeljakiMain;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WhackAMoleScreen extends ScreenAdapter {
    private SeljakiMain game;
    private Stage stage;
    private Stage stageFront;
    private Skin skin;
    private Table backgorund;
    private Table backgroundFront;
    private Random random;
    private Map<Image, Boolean> squareStateMap;
    private int activeSquares;
    private int score;
    private Label scoreLabel;
    private Image timerBar;
    private float timeRemaining;
    private final float totalTime = 30f;
    private Label finale;

    public WhackAMoleScreen(SeljakiMain game) {
        this.game = game;
        this.random = new Random();
        this.squareStateMap = new HashMap<>();
        this.activeSquares = 0;
        this.score = 0;
        this.timeRemaining = totalTime;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
        stageFront.act(delta);
        stageFront.draw();
        timeRemaining -= delta;
        if (timeRemaining < 0) timeRemaining = 0;
        updateTimerBar();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        stageFront.getViewport().update(width, height);
    }

    @Override
    public void show() {
        stage = new Stage(game.viewport);
        stageFront = new Stage(game.viewport);
        skin = game.skin;
        Gdx.input.setInputProcessor(stage);

        backgorund = new Table();
        backgorund.setSize(stage.getWidth(), stage.getHeight());
        backgorund.setPosition((stage.getWidth() - stage.getWidth()) / 2,
            (stage.getHeight() - stage.getHeight()) / 2);
        backgorund.setBackground(skin.getDrawable("window"));

        backgroundFront = new Table();
        backgroundFront.setSize(stage.getWidth(), stage.getHeight());
        backgroundFront.setPosition((stage.getWidth() - stage.getWidth()) / 2,
            (stage.getHeight() - stage.getHeight()) / 2 - 10);

        for (int i = 0; i < 12; i++) {
            final Image brownSquare = createTempSquare();
            final Image brownSquareFront = createTempFrontSquare();
            squareStateMap.put(brownSquare, false);
            backgorund.add(brownSquare).pad(20).padTop(60);
            backgroundFront.add(brownSquareFront).pad(20).padTop(60);
            if ((i + 1) % 4 == 0) {
                backgorund.row();
                backgroundFront.row();
            }

            scheduleBlueSquarePopUp(brownSquare);
        }

        scoreLabel = new Label("Score: " + score, skin);
        scoreLabel.setPosition(stage.getWidth() / 2 - scoreLabel.getWidth() / 2, stage.getHeight() - 50);
        timerBar = createTimerBar();
        timerBar.setPosition(0, 10);

        finale = new Label("Game Over! Final Score: ", skin);
        finale.setVisible(false);


        stage.addActor(backgorund);
        stage.addActor(scoreLabel);
        stageFront.addActor(backgroundFront);
        stage.addActor(timerBar);
        stageFront.addActor(finale);
    }

    private Image createTempSquare() {
        Pixmap pixmap = new Pixmap(60, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.5f, 0.2f, 0.1f,1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new Image(texture);
    }
    private Image createTempFrontSquare() {
        Pixmap pixmap = new Pixmap(60, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BROWN);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new Image(texture);
    }

    private Image createBlueSquare() {
        Pixmap pixmap = new Pixmap(30, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLUE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new Image(texture);
    }

    private void scheduleBlueSquarePopUp(final Image brownSquare) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (timeRemaining <= 0) {

                    if (!finale.isVisible()) {
                        finale.setText("Game Over! Final Score: " + score);
                        finale.setPosition(stage.getWidth() / 2 - finale.getWidth()/2, stage.getHeight() / 2 - finale.getHeight() / 2);
                        finale.setVisible(true);
                    }

                    return;
                }
                if (!squareStateMap.get(brownSquare) && shouldSpawn()) {
                    final Image blueSquare = createBlueSquare();
                    squareStateMap.put(brownSquare, true);
                    activeSquares++;

                    blueSquare.setPosition(
                        brownSquare.getX() + brownSquare.getWidth() / 2 - blueSquare.getWidth() / 2,
                        brownSquare.getY() + brownSquare.getHeight() / 2 - blueSquare.getHeight() / 2 - 10
                    );
                    stage.addActor(blueSquare);

                    blueSquare.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                        private boolean clicked = false;

                        @Override
                        public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                            float clickY = blueSquare.getY() + y;
                            float topPortionStartY = blueSquare.getY() + blueSquare.getHeight() * 0.50f;

                            if (clickY >= topPortionStartY && !clicked) {
                                clicked = true;
                                score++;
                                updateScoreLabel();
                                blueSquare.addAction(Actions.sequence(
                                    Actions.moveBy(0, -30, 0.2f),
                                    Actions.run(() -> {
                                        blueSquare.remove();
                                        squareStateMap.put(brownSquare, false);
                                        activeSquares--;
                                    })
                                ));
                            }
                            return true;
                        }
                    });

                    blueSquare.addAction(Actions.sequence(
                        Actions.delay(2),
                        Actions.moveBy(0, 30, 0.3f),
                        Actions.delay(2),
                        Actions.moveBy(0, -30, 0.3f),
                        Actions.run(() -> {
                            blueSquare.remove();
                            squareStateMap.put(brownSquare, false);
                            activeSquares--;
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
        if (chance < 50) return activeSquares < 1;
        if (chance < 75) return activeSquares < 2;
        if (chance < 90) return activeSquares < 3;
        return activeSquares < 4;
    }

    private Image createTimerBar() {
        Pixmap pixmap = new Pixmap(1, 30, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        Image bar = new Image(texture);
        bar.setSize(stage.getWidth(), 30);
        return bar;
    }

    private void updateTimerBar() {
        float width = (timeRemaining / totalTime) * stage.getWidth();
        timerBar.setSize(width, timerBar.getHeight());
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
