package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
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

public class DuckHuntMagpieSettings extends ScreenAdapter {
    private Stage stage;

    private final SeljakiMain game;
    private Skin uiSkin;

    private final AssetManager assetManager;
    private TextureAtlas gameplayAtlas;
    private Viewport viewport;
    private SpriteBatch batch;

    public DuckHuntMagpieSettings(SeljakiMain game){
        this.game = game;
        assetManager = game.getAssetManager();
    }
    @Override
    public void show(){
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        viewport = game.viewport;
        stage = new Stage(viewport, game.batch);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
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
        style.font = uiSkin.getFont("window");
        style.fontColor = new Color(Color.BLACK);
        Label title = new Label("Settings", style);
        title.setFontScale(2f);
        table.add(title).padBottom(20).expand().row();

        TextButton.TextButtonStyle style2 = new TextButton.TextButtonStyle();
        style2.font = uiSkin.getFont("font");
        Label chooseDifficultyLabel = new Label("Choose difficulty:", style);
        chooseDifficultyLabel.setFontScale(1.5f);
        table.add(chooseDifficultyLabel).pad(0).expand().row();

        new SelectBox.SelectBoxStyle();
        SelectBox.SelectBoxStyle selectBoxStyle;
        selectBoxStyle = uiSkin.get("default", SelectBox.SelectBoxStyle.class);

        SelectBox chooseDifficultySelectBox = new SelectBox(selectBoxStyle);
        String[] difficulties = {"Easy","Medium","Hard"};
        chooseDifficultySelectBox.setItems(difficulties);
        chooseDifficultySelectBox.setSelected(GameManager.INSTANCE.getDifficulty()); // Nalaganje nastavitve

        // Shrani izbiro ob spremembi
        chooseDifficultySelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedDifficulty = chooseDifficultySelectBox.getSelected().toString();
                GameManager.INSTANCE.setDifficulty(selectedDifficulty); // Shranjevanje nastavitve
            }
        });
        table.add(chooseDifficultySelectBox).padTop(10).expand().row();
        table.add(continueButton()).padBottom(20).expand().row();
        return table;
    }
    private Actor continueButton(){
        TextureRegion continueButtonTR = gameplayAtlas.findRegion("continue2");
        TextureRegion continueButtonHoverTR = gameplayAtlas.findRegion("continue3");

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
