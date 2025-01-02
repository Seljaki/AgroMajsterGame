package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.GameManager;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.RegionNames;

public class DuckHuntMagpieGameOverScreen extends ScreenAdapter {
    private Stage stage;

    private final SeljakiMain game;
    private Skin uiSkin;
    private BitmapFont gameFont;

    private final AssetManager assetManager;
    private TextureAtlas gameplayAtlas;
    private Viewport viewport;
    private SpriteBatch batch;
    private int finalScore;

    public DuckHuntMagpieGameOverScreen(SeljakiMain game, int finalScore){
        this.game = game;
        this.finalScore = finalScore;
        assetManager = game.getAssetManager();
    }
    @Override
    public void show(){
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        viewport = game.viewport;
        stage = new Stage(viewport, game.batch);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        gameFont = assetManager.get(AssetDescriptors.GAME_FONT);
        batch = new SpriteBatch();
        settingsUi();
        Gdx.input.setInputProcessor(stage);
    }

    private Actor settingsUi(){
        uiSkin = assetManager.get(AssetDescriptors.UI_SKIN);
        float stageWidth = stage.getWidth();
        float stageHeight = stage.getHeight();

        TextureRegion backgroundRegion = gameplayAtlas.findRegion(RegionNames.BG_DG);

        Table table = new Table();
        float tableWidth = stageWidth * 0.8f; // 80% širine odra
        float tableHeight = stageHeight * 0.6f; // 60% višine odra
        table.setSize(tableWidth,tableHeight);
        //table.debugCell();
        table.setBackground(new TextureRegionDrawable(backgroundRegion));
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = gameFont;
        style.fontColor = new Color(Color.BLACK);
        Label title = new Label("GAME OVER!", style);
        style.fontColor = new Color(Color.RED);
        title.setFontScale(1.2f);
        table.add(title).padBottom(20).expand().row();

        Label.LabelStyle style2 = new Label.LabelStyle();
        style2.font = gameFont;
        style2.fontColor = new Color(Color.BLACK);
        Label stats = new Label("Final Score: " + finalScore, style2);
        table.add(stats).pad(0).expand().row();

        table.add(continueButton()).padBottom(20).expand().row();
        return table;
    }
    private Actor continueButton(){
        TextureRegion continueButtonTR = gameplayAtlas.findRegion("repeat");
        TextureRegion continueButtonHoverTR = gameplayAtlas.findRegion("repeatHover");

        TextureRegionDrawable continueButtonDrawable = new TextureRegionDrawable(continueButtonTR);
        TextureRegionDrawable continueButtonHoverDrawable = new TextureRegionDrawable(continueButtonHoverTR);

        ImageButton.ImageButtonStyle continueButtonStyle = new ImageButton.ImageButtonStyle();
        continueButtonStyle.imageUp = continueButtonDrawable;
        continueButtonStyle.imageDown = continueButtonHoverDrawable;

        ImageButton continueButton = new ImageButton(continueButtonStyle);

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new DuckHuntMagpie(game));
            }
        });
        return continueButton;
    }
    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.setDebugAll(true);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
