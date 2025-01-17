package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.*;
import com.seljaki.AgroMajsterGame.Helpers.AnimatedActor;
import com.seljaki.AgroMajsterGame.Helpers.GameButton;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.RegionNames;
import com.seljaki.AgroMajsterGame.screens.DuckHuntMagpie.DuckHuntMagpie;
import com.badlogic.gdx.utils.Align;
import com.seljaki.AgroMajsterGame.screens.WhackAMole.WhackAMoleScreen;

public class MiniGameSettingsScreen extends ScreenAdapter {
    private Stage stage;
    private final SeljakiMain game;
    private final BitmapFont gameFont;
    private final AssetManager assetManager;
    private final TextureAtlas gameplayAtlas;
    private final Viewport viewport;
    private AnimatedActor leftMagpieActor;
    private AnimatedActor rightMagpieActor;
    private final GameButton buttons;
    private final boolean miniGame;

    public MiniGameSettingsScreen(SeljakiMain game, boolean miniGame) { //miniGame: true = Magpie, false = mole
        this.game = game;
        assetManager = game.getAssetManager();
        viewport = game.viewport;
        buttons = new GameButton(game);
        gameplayAtlas = game.gameplayAtlas;
        gameFont = assetManager.get(AssetDescriptors.GAME_FONT);
        this.miniGame = miniGame;
    }

    @Override
    public void show() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        stage = new Stage(viewport, game.batch);
        settingsUi();
        Gdx.input.setInputProcessor(stage);
    }

    private void settingsUi() {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = gameFont;
        style.fontColor = new Color(Color.BLACK);
        Label title;
        TextureRegion backgroundRegion;
        if (miniGame) {
            title = new Label("MAGPIE MINI-GAME", style);
            backgroundRegion = gameplayAtlas.findRegion(RegionNames.BG_DG);
        } else {
            title = new Label("WHACK-A-MOLE\n MINI-GAME", style);
            backgroundRegion = gameplayAtlas.findRegion(RegionNames.GRASSY_BACKGROUND);
        }

        Skin uiSkin = game.skin;
        float stageWidth = stage.getWidth();
        float stageHeight = stage.getHeight();


        Table table = new Table();
        float tableWidth = stageWidth * 0.8f;
        float tableHeight = stageHeight * 0.6f;
        table.setSize(tableWidth, tableHeight);
        //table.debugCell();
        table.setBackground(new TextureRegionDrawable(backgroundRegion));
        table.setFillParent(true);
        stage.addActor(table);

        //Label title = new Label("MAGPIE MINI-GAME", style);
        title.setFontScale(1f);
        title.setAlignment(Align.center);
        table.add(title).padBottom(20).expand().colspan(3).row();

        Label chooseDifficultyLabel = new Label("Choose difficulty:", style);
        chooseDifficultyLabel.setFontScale(0.7f);
        table.add(chooseDifficultyLabel).pad(0).expand().colspan(3).row();

        new SelectBox.SelectBoxStyle();
        SelectBox.SelectBoxStyle selectBoxStyle;
        selectBoxStyle = uiSkin.get("default", SelectBox.SelectBoxStyle.class);

        SelectBox chooseDifficultySelectBox = new SelectBox(selectBoxStyle);
        String[] difficulties = {"Easy", "Medium", "Hard"};
        chooseDifficultySelectBox.setItems(difficulties);
        if (miniGame) {
            chooseDifficultySelectBox.setSelected(GameManager.INSTANCE.getDifficultyMagpie());

        } else {
            chooseDifficultySelectBox.setSelected(GameManager.INSTANCE.getDifficultyMole());
        }

        chooseDifficultySelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedDifficulty = chooseDifficultySelectBox.getSelected().toString();
                if (miniGame) {
                    GameManager.INSTANCE.setDifficultyMagpie(selectedDifficulty);

                } else {
                    GameManager.INSTANCE.setDifficultyMole(selectedDifficulty);
                }
            }
        });

        if (miniGame) {
            createMagpieAnimations();
            table.add(rightMagpieActor).pad(10);
            table.add(chooseDifficultySelectBox).top().center();
            table.add(leftMagpieActor).pad(10);
            table.row();
        } else {

            Image mole1 = new Image(gameplayAtlas.findRegion(RegionNames.MOLE1));
            Image mole2 = new Image(gameplayAtlas.findRegion(RegionNames.MOLE2));
            table.add(mole1);
            table.add(chooseDifficultySelectBox).top().center();
            table.add(mole2);
            table.row();
        }

        table.add(buttons.getGameButton(RegionNames.BACK, RegionNames.BACK_HOVER,
            () -> new MapScreen(game)
        )).padBottom(20).right().expandY();


        table.add(buttons.getGameButton(RegionNames.PLAY_TEXT, RegionNames.PLAY_TEXT_HOVER,
            () -> {
                if (miniGame) {
                    return new DuckHuntMagpie(game);
                } else {
                    return new WhackAMoleScreen(game);
                }
            }
        )).padBottom(20).expandY();


        table.add(buttons.getGameButton(RegionNames.LEADERBOARD, RegionNames.LEADERBOARD_HOVER,
            () ->{
                if (miniGame) {
                    return new LeaderboardScreen(game, SeljakiMain.PreviousScreen.MAGPIE);
                } else {
                    return new LeaderboardScreen(game, SeljakiMain.PreviousScreen.MOLE);
                }
            }
        )).padBottom(20).left().expandY().row();
    }

    private void createMagpieAnimations() {
        Array<TextureRegion> framesLeft = new Array<>();
        for (String magpieSpriteName : RegionNames.MENU_MAGPIE_SPRITES_LEFT) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            framesLeft.add(tex);
        }
        Animation<TextureRegion> magpieAnimationLeft = new Animation<>(0.085f, framesLeft, Animation.PlayMode.LOOP);

        Array<TextureRegion> framesRight = new Array<>();
        for (String magpieSpriteName : RegionNames.MENU_MAGPIE_SPRITES_RIGHT) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            framesRight.add(tex);
        }
        Animation<TextureRegion> magpieAnimationRight = new Animation<>(0.085f, framesRight, Animation.PlayMode.LOOP);

        leftMagpieActor = new AnimatedActor(magpieAnimationLeft);
        rightMagpieActor = new AnimatedActor(magpieAnimationRight);

        leftMagpieActor.setSize(150, 150);
        rightMagpieActor.setSize(150, 150);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        //stage.setDebugAll(true);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
