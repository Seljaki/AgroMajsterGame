package com.seljaki.AgroMajsterGame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.http.SeljakiClient;
import com.seljaki.AgroMajsterGame.screens.LoginScreen;
import com.seljaki.AgroMajsterGame.screens.MapScreen;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;

public class SeljakiMain extends Game {
    public Skin skin;
    public Viewport viewport;
    public Batch batch;
    public SeljakiClient seljakiClient;
    private AssetManager assetManager;
    public AssetManager getAssetManager() {
        return assetManager;
    }
    public TextureAtlas gameplayAtlas;
    public Sound moleSqueak;
    public Music whackAMoleMusic;
    @Override
    public void create() {
        viewport = new FitViewport(640, 480);

        batch = new SpriteBatch();
        seljakiClient = SeljakiClient.loadData();
        assetManager = new AssetManager();
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.EMPTY_GUN_SHOT);
        assetManager.load(AssetDescriptors.GUN_SHOT);
        assetManager.load(AssetDescriptors.GAME_FONT);
        assetManager.load(AssetDescriptors.SELECT_SOUND);
        assetManager.load(AssetDescriptors.RELOAD_SOUND);
        assetManager.load(AssetDescriptors.GAME_OVER_SOUND);
        assetManager.load(AssetDescriptors.SCORE_SOUND);
        assetManager.load(AssetDescriptors.MOLE_SQUEAK_SOUND);
        assetManager.load(AssetDescriptors.WHACK_A_MOLE_MUSIC);
        assetManager.load(AssetDescriptors.PARTICLE_EFFECT_MOLE_BLOOD);
        assetManager.finishLoading();

        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        if(seljakiClient.isLoggedIn())
            setScreen(new MapScreen(this));
        else
            setScreen(new LoginScreen(this));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.6f, 0.6f, 0.6f, 1f);
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        skin.dispose();
        gameplayAtlas.dispose();
        whackAMoleMusic.dispose();
        moleSqueak.dispose();
        assetManager.dispose();
    }
}
