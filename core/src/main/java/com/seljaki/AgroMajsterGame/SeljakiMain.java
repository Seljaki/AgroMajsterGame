package com.seljaki.AgroMajsterGame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.screens.MapScreen;

public class SeljakiMain extends Game {
    public Skin skin;
    public Viewport viewport;

    @Override
    public void create() {
        viewport = new FitViewport(640, 480);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        setScreen(new MapScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }
}
