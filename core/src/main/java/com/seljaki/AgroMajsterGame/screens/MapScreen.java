package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.Helpers.CloudController;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.http.Plot;
import com.seljaki.AgroMajsterGame.http.Weather;
import com.seljaki.AgroMajsterGame.utils.Constants;
import com.seljaki.AgroMajsterGame.utils.Geolocation;
import com.seljaki.AgroMajsterGame.utils.MapRasterTiles;
import com.seljaki.AgroMajsterGame.utils.ZoomXY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MapScreen extends ScreenAdapter {
    private ShapeRenderer shapeRenderer;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile
    private SeljakiMain game;
    private Plot[] plots;
    private Polygon[] plotsBounds;
    private Plot selectedPlot;
    private Stage stage;
    private Skin skin;
    InputMultiplexer inputMultiplexer;
    private Vector2 cameraVelocity = new Vector2(0, 0);
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.4129955, 16.06006619);
    private Viewport viewport;
    private ParticleEffect weatherEffect;
    private CloudController cloudController;
    private Window plotWindow;
    private Boolean isDialogOpen = false;

    public MapScreen(SeljakiMain game) {
        this.game = game;
    }

    Weather weather;

    @Override
    public void show() {
        viewport = new FitViewport(1280, 960);
        stage = new Stage(viewport);
        //stageUI = new Stage(viewport);
        skin = game.skin;
        //testSkin =  new Skin(Gdx.files.internal("ui/skin/flat-earth-ui.json"));
        plots = game.seljakiClient.getPlots();
        Plot p = plots[0];
        //p.note = "changed from libgdx!";
        //weather = Weather.CLOUDY;
        weather = game.seljakiClient.getWeather();
        System.out.println(weather);

        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();


        TextButton leaderboardButton = new TextButton("Leaderboard", skin);
        leaderboardButton.setPosition(stage.getWidth() - leaderboardButton.getWidth() - 20, stage.getHeight() - leaderboardButton.getHeight() - 20);
        leaderboardButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LeaderboardScreen(game, SeljakiMain.PreviousScreen.MAP));
                System.out.println(game.seljakiClient.getUsername());
            }
        });

        TextButton logOutButton = new TextButton("Log Out", skin);
        logOutButton.setPosition(stage.getWidth() - logOutButton.getWidth() - 20, leaderboardButton.getY() - logOutButton.getHeight() - 20);
        logOutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.seljakiClient.logOut();
                game.setScreen(new LoginScreen(game));
            }
        });

        TextButton quitButton = new TextButton("Quit", skin);
        quitButton.setPosition(logOutButton.getX() - quitButton.getWidth() - 20, leaderboardButton.getY() - quitButton.getHeight() - 20);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        stage.addActor(logOutButton);
        stage.addActor(quitButton);
        stage.addActor(leaderboardButton);


        try {
            //in most cases, geolocation won't be in the center of the tile because tile borders are predetermined (geolocation can be at the corner of a tile)
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            //you need the beginning tile (tile on the top left corner) to convert geolocation to a location in pixels.
            beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - ((Constants.NUM_TILES - 1) / 2), centerTile.y - ((Constants.NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES, Constants.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        plotsBounds = getPlotsBounds();

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputAdapter() {
            float deltaX;
            float deltaY;

            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += amountY * Gdx.graphics.getDeltaTime() * 10f;
                return true;
            }

            private float lastX, lastY;
            private boolean posSet = false;

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                deltaX = 0;
                deltaY = 0;

                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (posSet) {
                    deltaX = (lastX - screenX) * camera.zoom * (camera.viewportWidth / Gdx.graphics.getWidth());
                    deltaY = -(lastY - screenY) * camera.zoom * (camera.viewportHeight / Gdx.graphics.getHeight());

                    cameraVelocity.x = 0;
                    cameraVelocity.y = 0;
                    camera.translate(deltaX, deltaY);
                }

                lastX = screenX;
                lastY = screenY;
                posSet = true;
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                posSet = false;

                System.out.println(deltaX);
                if (Math.abs(deltaX) >= 2)
                    cameraVelocity.x = deltaX;
                if (Math.abs(deltaY) >= 2)
                    cameraVelocity.y = deltaY;
                System.out.println(cameraVelocity);

                return true;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);

        setWeather();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.translate(cameraVelocity);
        cameraVelocity.x = MathUtils.lerp(cameraVelocity.x, 0, 0.05f); // Adjust 0.1f for smoothing factor
        cameraVelocity.y = MathUtils.lerp(cameraVelocity.y, 0, 0.05F);
        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        //drawMarkers();
        Color selectedColor = Color.valueOf("00c3ff");
        selectedColor.a=0.2f;
        Color generalColor = Color.valueOf("ff8b00");
        generalColor.a=0.2f;
        drawPlots(true, selectedColor, generalColor);
        drawPlots(false,Color.valueOf("00c3ff"), Color.valueOf("ff8b00"));
        drawWeather(delta);

        stage.draw();
        stage.act(delta);
        //stageUI.draw();
    }

    private void drawPlots(boolean filled, Color selected, Color unselected) {
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl20.glLineWidth(4f / camera.zoom); // line width
        shapeRenderer.setProjectionMatrix(camera.combined);
        if(filled)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        else
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Plot p : plots) {
            if (p == selectedPlot)
                shapeRenderer.setColor(selected);
            else
                shapeRenderer.setColor(unselected);

            float[] loc = Geolocation.getPolygon(p.coordinates, beginTile);
            if (filled)
            {
                if (loc.length >= 6) {
                    for (int i = 2; i < loc.length - 2; i += 2) {
                        shapeRenderer.triangle(
                            loc[0], loc[1],       // (x0, y0)
                            loc[i], loc[i + 1],   // (xi, yi)
                            loc[i + 2], loc[i + 3] //(xi+1, yi+1)
                        );
                    }
                }
            }
            else {
                shapeRenderer.polygon(loc);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    private Polygon[] getPlotsBounds() {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (Plot p : plots) {
            Geolocation[] loc = p.coordinates;
            float[] vertices = Geolocation.getPolygon(loc, beginTile);
            polygons.add(new Polygon(vertices));
        }
        return polygons.toArray(new Polygon[0]);
    }

    private void drawWeather(float delta) {
        if (weather == Weather.CLOUDY) {
            game.batch.begin();
            cloudController.update(delta);
            cloudController.render(game.batch);
            game.batch.end();
        } else if (weather == Weather.RAINY) {
            float cameraX = camera.position.x;
            float cameraY = camera.position.y;

            weatherEffect.setPosition(0 - cameraX, viewport.getScreenHeight() * 2 - cameraY);
            game.batch.begin();
            weatherEffect.update(delta);
            weatherEffect.draw(game.batch);
            game.batch.end();
        }
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(null);
        stage.clear();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
        stage.dispose();
        tiledMap.dispose();
        for (Texture mapTile : mapTiles)
            mapTile.dispose();
    }

    private void handleInput() {
        if(isDialogOpen) return;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }
        if (Gdx.input.justTouched()) {
            Vector3 point = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            //System.out.println(Gdx.input.getX() +" "+ Gdx.input.getY());
            for (int i = 0; i < plotsBounds.length; i++) {
                if (plotsBounds[i].contains(point.x, point.y)) {
                    System.out.println("CONTAINS " + i);
                    onPlotClicked(plots[i]);
                    break;
                }
            }
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        //camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Constants.MAP_WIDTH - effectiveViewportWidth / 2f);
        //camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Constants.MAP_HEIGHT - effectiveViewportHeight / 2f);
    }

    void onPlotClicked(Plot plot) {
        selectedPlot = plot;
        //stage.clear();
        if (plotWindow != null) {
            plotWindow.remove();
        }
        plotWindow = createPlotInfoWindow(plot);
        stage.addActor(plotWindow);
    }

    private Window createPlotInfoWindow(Plot plot) {
        Dialog dialog = new Dialog("Plot Info", skin);

        dialog.row();
        dialog.add(new Label("Title: " + plot.title, skin)).left().pad(5).row();
        dialog.add(new Label("Note: " + plot.note, skin)).left().pad(5).row();
        dialog.add(new Label("Plot Number: " + plot.plotNumber, skin)).left().pad(5).row();
        dialog.add(new Label("Cadastral Municipality: " + plot.cadastralMunicipality, skin)).left().pad(5).row();
        dialog.add(new Label("Archived: " + (plot.archived ? "Yes" : "No"), skin)).left().pad(5).row();

        TextButton closeButton = new TextButton("Close", skin);
        TextButton playGameButton = new TextButton("Play Game", skin);
        TextButton editButton = new TextButton("Edit", skin);

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedPlot = null;
                dialog.remove();
            }
        });

        playGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
                Random random = new Random();
                game.setScreen(new MiniGameSettingsScreen(game, random.nextBoolean()));

            }
        });
        editButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createPlotEditWindow(dialog.getX()+dialog.getWidth()+20,dialog.getY(),plot);
            }
        });

        Table buttonTable = new Table();
        buttonTable.add(closeButton).pad(10);
        buttonTable.add(playGameButton).pad(10);
        buttonTable.add(editButton).pad(10);
        dialog.add(buttonTable).row();

        dialog.pack();
        dialog.setMovable(true);
        dialog.setModal(false);
        dialog.setResizable(false);
        dialog.setPosition(20, viewport.getWorldHeight() - dialog.getHeight() - 20);

        return dialog;
    }

    public void createPlotEditWindow(float x, float y,Plot plot){

        isDialogOpen = true;

        Dialog dialog = new Dialog("Edit Plot", skin);

        TextField titleField = new TextField(plot.title, skin);

        TextArea noteArea = new TextArea(plot.note, skin);
        noteArea.setPrefRows(4);
        noteArea.setAlignment(Align.topLeft);

        CheckBox archivedCheckBox = new CheckBox("", skin);
        archivedCheckBox.setChecked(plot.archived);

        dialog.row();
        dialog.add(new Label("Title: ", skin)).right().pad(5);
        dialog.add(titleField).width(200).pad(5).row();

        dialog.add(new Label("Note: ", skin)).right().top().pad(5);
        dialog.add(noteArea).width(200).height(100).pad(5).row();

        dialog.add(new Label("Archived: ", skin)).right().pad(5);
        dialog.add(archivedCheckBox).pad(5).left().row();
        TextButton cancelButton = new TextButton("Cancel", skin);
        TextButton saveButton = new TextButton("Save", skin);

        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
                isDialogOpen = false;
            }
        });

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
                isDialogOpen = false;
            }
        });

        Table buttonTable = new Table();
        buttonTable.add(cancelButton).pad(10);
        buttonTable.add(saveButton).pad(10);
        dialog.add(buttonTable).colspan(2).row();

        dialog.pack();
        dialog.setMovable(true);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setPosition(x, y);

        stage.addActor(dialog);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        viewport.update(width, height);
    }

    private void setWeather() {
        if (weather == Weather.RAINY) {
            weatherEffect = game.getAssetManager().get(AssetDescriptors.PARTICLE_EFFECT_RAIN);
            weatherEffect.setPosition(0, viewport.getScreenHeight());
        } else if (weather == Weather.CLOUDY) {
            cloudController = new CloudController(
                game.getAssetManager().get(AssetDescriptors.GAMEPLAY),
                100,
                camera,
                30
            );
        }
    }
}
