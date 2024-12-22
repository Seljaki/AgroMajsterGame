package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.SeljakiMain;

public class LoginScreen extends ScreenAdapter {
    private SeljakiMain game;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;

    public LoginScreen(SeljakiMain game) {
        this.game = game;
        viewport = game.viewport;
        skin = game.skin;
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        stage = new Stage(viewport, game.batch);

        stage.addActor(createUi());
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private Actor createUi() {
        Table table = new Table();
        table.defaults().pad(20);

        TextField usernameInput = new TextField("", skin);
        TextField passwordInput = new TextField("", skin);
        passwordInput.setPasswordMode(true);
        passwordInput.setPasswordCharacter('*');

        Label errorLabel = new Label("The username or password is inccorect", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setVisible(false);

        TextButton backButton = new TextButton("Exit", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        TextButton loginButton = new TextButton("Login", skin);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameInput.getText();
                String password = passwordInput.getText();

                if(game.seljakiClient.logIn(username, password)) {
                    game.seljakiClient.saveData();
                    game.setScreen(new MapScreen(game));
                } else {
                    errorLabel.setVisible(true);
                }
            }
        });

        Table contentTable = new Table(skin);
        contentTable.setDebug(false);

        contentTable.add(new Label("Login", skin)).padBottom(10).colspan(2).row();
        contentTable.add(new Label("Username", skin)).pad(10);
        contentTable.add(usernameInput).pad(10).row();
        contentTable.add(new Label("Password", skin)).pad(10);
        contentTable.add(passwordInput).pad(10).row();
        contentTable.add(errorLabel).fillX().colspan(2).row();

        contentTable.add(backButton).width(100).padTop(10);
        contentTable.add(loginButton).width(100).padTop(10);

        table.add(contentTable);
        table.center();
        table.setFillParent(true);
        table.pack();

        return table;
    }
}
