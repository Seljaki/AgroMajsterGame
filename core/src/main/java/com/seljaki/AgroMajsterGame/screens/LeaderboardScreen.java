package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.seljaki.AgroMajsterGame.GameManager;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.RegionNames;


public class LeaderboardScreen extends ScreenAdapter {
    private final SeljakiMain game;
    private Stage stage;
    private Table root;
    private Table table;
    private ScrollPane scrollPane;
    private Skin skin;
    private final TextureAtlas gameplayAtlas;
    private Image background;
    private TextureRegion bgMagpie;
    private TextureRegion bgMole;

    public LeaderboardScreen(SeljakiMain game) {
        this.game = game;
        skin = game.skin;
        gameplayAtlas = game.gameplayAtlas;
        bgMole = gameplayAtlas.findRegion(RegionNames.GRASSY_BACKGROUND);
        bgMagpie = gameplayAtlas.findRegion(RegionNames.BG_DG);
    }

    public void show() {
        stage = new Stage(game.viewport);
        Gdx.input.setInputProcessor(stage);

        background = new Image(bgMagpie);
        background.setSize(stage.getWidth(), stage.getHeight());
        background.setPosition(0, 0);
        stage.addActor(background);

        // Table to hold leaderboard and buttons
        root = new Table();
        root.setSize(stage.getWidth()-150, stage.getHeight()-100);

        // Leaderboard table
        table = new Table();
        table.top();
        scrollPane = new ScrollPane(table, skin);
        scrollPane.setScrollingDisabled(true, false);

        // Add leaderboard
        loadLeaderboardIntoTable(true);

        // Buttons for grid sizes
        TextButton magpieButton = new TextButton("Magpie Hunt", skin);
        TextButton moleButton = new TextButton("Whack-a-Mole", skin);
        TextButton backButton = new TextButton("Back", skin);

        root.add(magpieButton).fill(true,false).expandX().center().padRight(5);//.fill(true,false);
        root.add(moleButton).fill(true,false).expandX().center().padLeft(5).row();//.fill(true,false);
        root.add(scrollPane).colspan(3).expand().fill().row();

        root.add(backButton).center().colspan(3).pad(10);
        table.setBackground(skin.getDrawable("textfield-custom"));

        // Add button listeners
        magpieButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadLeaderboardIntoTable(true);
            }
        });

        moleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadLeaderboardIntoTable(false);
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MapScreen(game));
            }
        });

        stage.addActor(root);
    }

    private void loadLeaderboardIntoTable(boolean MinigameType) {
        table.clear();
        String miniGame;
        if (MinigameType) {
            miniGame = "Magpie hunt";
            background.setDrawable(new TextureRegionDrawable(bgMagpie));
        }
        else {
            miniGame = "Whack-a-mole";
            background.setDrawable(new TextureRegionDrawable(bgMole));
        }

        table.add(new Label(miniGame+" leaderboard", skin)).expandX().colspan(2).row();
        table.add(new Label("NAME", skin)).expandX().center().pad(10);
        table.add(new Label("SCORE", skin)).expandX().center().pad(10).row();

        GameManager.Leaderboard leaderboard = GameManager.loadLeaderboard(MinigameType);

        if (leaderboard.playerScores == null) return;
        for (int i = 0; i < leaderboard.playerScores.size(); i++) {
            String player = leaderboard.playerScores.get(i).playerName;
            int score = leaderboard.playerScores.get(i).highScore;
            table.add(new Label(player, skin)).fillX().center().pad(10);
            table.add(new Label(String.valueOf(score), skin)).fillX().center().pad(10).row();
        }
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        root.setPosition((stage.getWidth() - root.getWidth()) / 2, (stage.getHeight() - root.getHeight()) / 2);
    }

    @Override
    public void hide() {
        stage.dispose();
    }
}
