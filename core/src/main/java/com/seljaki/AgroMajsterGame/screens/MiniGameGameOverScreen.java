package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.Helpers.GameButton;
import com.seljaki.AgroMajsterGame.GameManager;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.RegionNames;
import com.seljaki.AgroMajsterGame.screens.DuckHuntMagpie.DuckHuntMagpie;
import com.seljaki.AgroMajsterGame.screens.WhackAMole.WhackAMoleScreen;

public class MiniGameGameOverScreen extends ScreenAdapter {
    private Stage stage;

    private final SeljakiMain game;
    private final BitmapFont gameFont;
    private final TextureAtlas gameplayAtlas;
    private final Viewport viewport;
    private final int finalScore;
    private final GameButton buttons;
    private final boolean miniGame;

    public MiniGameGameOverScreen(SeljakiMain game, int finalScore, boolean miniGame) {
        this.game = game;
        this.finalScore = finalScore;
        AssetManager assetManager = game.getAssetManager();
        buttons = new GameButton(game);
        viewport = game.viewport;
        gameplayAtlas = game.gameplayAtlas;
        gameFont = assetManager.get(AssetDescriptors.GAME_FONT);
        this.miniGame = miniGame;

    }

    @Override
    public void show() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        stage = new Stage(viewport, game.batch);
        gameOverScreenUi();
        Gdx.input.setInputProcessor(stage);
    }

    private void gameOverScreenUi() {
        float stageWidth = stage.getWidth();
        float stageHeight = stage.getHeight();

        TextureRegion backgroundRegion;
        String diff;
        if (miniGame) {
            backgroundRegion = gameplayAtlas.findRegion(RegionNames.BG_DG);
            diff = GameManager.INSTANCE.getDifficultyMagpie();
        } else {
            backgroundRegion = gameplayAtlas.findRegion(RegionNames.GRASSY_BACKGROUND);
            diff = GameManager.INSTANCE.getDifficultyMole();

        }

        Table table = new Table();
        float tableWidth = stageWidth * 0.8f;
        float tableHeight = stageHeight * 0.6f;
        table.setSize(tableWidth, tableHeight);
        //table.debugCell();
        table.setBackground(new TextureRegionDrawable(backgroundRegion));
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = gameFont;
        style.fontColor = new Color(Color.BLACK);
        Label title = new Label("GAME OVER!", style);
        style.fontColor = new Color(Color.BLACK);
        title.setFontScale(1.2f);
        table.add(title).padBottom(20).expand().colspan(3).row();

        Label.LabelStyle style2 = new Label.LabelStyle();
        style2.font = gameFont;
        style2.fontColor = new Color(Color.BLACK);
        Label stats = new Label("Final Score: " + finalScore + "\nDifficulty: " + diff, style2);
        stats.setWrap(true);
        stats.setAlignment(Align.center);
        table.add(stats).pad(0).colspan(3).expand().growX().row();

        table.add(buttons.getGameButton(RegionNames.HOME, RegionNames.HOME_HOVER,
            () -> new MiniGameSettingsScreen(game, miniGame)
        )).padBottom(20).expand().padRight(20).right();

        if (miniGame) {
            table.add(buttons.getGameButton(RegionNames.REPEAT, RegionNames.REPEAT_HOVER,
                () -> new DuckHuntMagpie(game)
            )).padBottom(20).expandY();
        } else {
            table.add(buttons.getGameButton(RegionNames.REPEAT, RegionNames.REPEAT_HOVER,
                () -> new WhackAMoleScreen(game)
            )).padBottom(20).expandY();
        }

        table.add(buttons.getGameButton(RegionNames.LEADERBOARD, RegionNames.LEADERBOARD_HOVER,
            () -> new DuckHuntMagpie(game) // TODO: TUKAJ SE DODA LEADERBOARD SCREEN
        )).padBottom(20).expand().padLeft(20).left().row();
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        //stage.setDebugAll(true);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
