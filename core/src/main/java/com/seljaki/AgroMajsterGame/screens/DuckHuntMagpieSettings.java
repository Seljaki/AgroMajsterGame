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
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.*;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.RegionNames;

public class DuckHuntMagpieSettings extends ScreenAdapter {
    private Stage stage;
    private final SeljakiMain game;
    private final BitmapFont gameFont;
    private final AssetManager assetManager;
    private final TextureAtlas gameplayAtlas;
    private final Viewport viewport;
    private AnimatedActor leftMagpieActor;
    private AnimatedActor rightMagpieActor;
    private final GameButton buttons;

    public DuckHuntMagpieSettings(SeljakiMain game){
        this.game = game;
        assetManager = game.getAssetManager();
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.EMPTY_GUN_SHOT);
        assetManager.load(AssetDescriptors.GUN_SHOT);
        assetManager.load(AssetDescriptors.GAME_FONT);
        assetManager.load(AssetDescriptors.SELECT_SOUND);
        assetManager.load(AssetDescriptors.RELOAD_SOUND);
        assetManager.load(AssetDescriptors.GAME_OVER_SOUND);
        assetManager.load(AssetDescriptors.SCORE_SOUND);
        assetManager.finishLoading();
        viewport = game.viewport;
        buttons = new GameButton(game);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        gameFont = assetManager.get(AssetDescriptors.GAME_FONT);
    }
    @Override
    public void show(){
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        stage = new Stage(viewport, game.batch);
        settingsUi();
        Gdx.input.setInputProcessor(stage);
    }
    private void settingsUi(){
        Skin uiSkin = assetManager.get(AssetDescriptors.UI_SKIN);
        float stageWidth = stage.getWidth();
        float stageHeight = stage.getHeight();

        TextureRegion backgroundRegion = gameplayAtlas.findRegion(RegionNames.BG_DG);

        Table table = new Table();
        float tableWidth = stageWidth * 0.8f;
        float tableHeight = stageHeight * 0.6f;
        table.setSize(tableWidth,tableHeight);
        //table.debugCell();
        table.setBackground(new TextureRegionDrawable(backgroundRegion));
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = gameFont;
        style.fontColor = new Color(Color.BLACK);
        Label title = new Label("MAGPIE MINI-GAME", style);
        title.setFontScale(1f);
        table.add(title).padBottom(20).expand().colspan(3).row();

        Label chooseDifficultyLabel = new Label("Choose difficulty:", style);
        chooseDifficultyLabel.setFontScale(0.7f);
        table.add(chooseDifficultyLabel).pad(0).expand().colspan(3).row();

        new SelectBox.SelectBoxStyle();
        SelectBox.SelectBoxStyle selectBoxStyle;
        selectBoxStyle = uiSkin.get("default", SelectBox.SelectBoxStyle.class);

        SelectBox chooseDifficultySelectBox = new SelectBox(selectBoxStyle);
        String[] difficulties = {"Easy","Medium","Hard"};
        chooseDifficultySelectBox.setItems(difficulties);
        chooseDifficultySelectBox.setSelected(GameManager.INSTANCE.getDifficulty());

        chooseDifficultySelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedDifficulty = chooseDifficultySelectBox.getSelected().toString();
                GameManager.INSTANCE.setDifficulty(selectedDifficulty);
            }
        });
        createMagpieAnimations();
        table.add(rightMagpieActor).pad(10);
        table.add(chooseDifficultySelectBox).top().center();
        table.add(leftMagpieActor).pad(10);
        table.row();
        table.add(buttons.getGameButton("back","backHover",
            () -> new MapScreen(game)
        )).padBottom(20).right().expandY();
        table.add(buttons.getGameButton("playText","playTextHover",
            () -> new DuckHuntMagpie(game)
            )).padBottom(20).expandY();
        table.add(buttons.getGameButton("leaderboard","leaderboardHover",
            () -> new DuckHuntMagpie(game) // TODO: TUKAJ SE DODA LEADERBOARD SCREEN
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
        stage.act(delta);
        //stage.setDebugAll(true);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
