package com.seljaki.AgroMajsterGame.Helpers;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;

import java.util.function.Supplier;

public class GameButton {
    private final SeljakiMain game;
    private final TextureAtlas gameplayAtlas;
    private final Sound selectSound;
    public GameButton(SeljakiMain game){
        this.game = game;
        AssetManager assetManager = game.getAssetManager();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        selectSound = assetManager.get(AssetDescriptors.SELECT_SOUND);
    }

    public Actor getGameButton(String normal, String hover, Supplier<Screen> nextScreen){
        TextureRegion normalButtonTR = gameplayAtlas.findRegion(normal);
        TextureRegion normalButtonHoverTR = gameplayAtlas.findRegion(hover);

        TextureRegionDrawable normalButtonDrawable = new TextureRegionDrawable(normalButtonTR);
        TextureRegionDrawable normalButtonHoverDrawable = new TextureRegionDrawable(normalButtonHoverTR);

        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = normalButtonDrawable;
        buttonStyle.imageDown = normalButtonHoverDrawable;

        ImageButton button = new ImageButton(buttonStyle);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                selectSound.play();
                game.setScreen(nextScreen.get());
            }
        });
        return button;
    }

}
