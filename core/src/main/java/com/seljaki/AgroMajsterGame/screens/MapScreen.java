package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
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

import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.http.Plot;
import com.seljaki.AgroMajsterGame.utils.Constants;
import com.seljaki.AgroMajsterGame.utils.Geolocation;
import com.seljaki.AgroMajsterGame.utils.MapRasterTiles;
import com.seljaki.AgroMajsterGame.utils.ZoomXY;

import java.io.IOException;
import java.util.ArrayList;

public class MapScreen extends ScreenAdapter {
    private ShapeRenderer shapeRenderer;
    private Vector3 touchPosition;

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

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.4129955, 16.06006619);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.4129955, 16.06006619);

    public MapScreen(SeljakiMain game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(game.viewport);
        skin = game.skin;
        plots = game.seljakiClient.getPlots();

        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();

        touchPosition = new Vector3();

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
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += amountY * Gdx.graphics.getDeltaTime() * 10f;
                return true;
            }
            private float lastX, lastY;
            private boolean posSet = false;
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(posSet) {
                    float deltaX = (lastX - screenX) * camera.zoom * (camera.viewportWidth / Gdx.graphics.getWidth());
                    float deltaY = -(lastY - screenY) * camera.zoom * (camera.viewportHeight / Gdx.graphics.getHeight());

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
                return true;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        //drawMarkers();
        drawPlots();

        stage.draw();
    }

    private void drawPlots() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        //shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Plot p : plots) {
            if(p == selectedPlot)
                shapeRenderer.setColor(Color.GREEN);
            else
                shapeRenderer.setColor(Color.RED);
            Geolocation[] loc = p.coordinates;
            shapeRenderer.polygon(Geolocation.getPolygon(loc, beginTile));
        }
        shapeRenderer.end();
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

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private void handleInput() {
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
        if(Gdx.input.justTouched()) {
            Vector3 point = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            //System.out.println(Gdx.input.getX() +" "+ Gdx.input.getY());
            for (int i = 0; i < plotsBounds.length; i++) {
                if(plotsBounds[i].contains(point.x, point.y)) {
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
        stage.clear();
        stage.addActor(createPlotInfoWindow(plot));
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
                game.setScreen(new DuckHuntMagpie(game));
                System.out.println("Play Game clicked!");
            }
        });

        Table buttonTable = new Table();
        buttonTable.add(closeButton).pad(10);
        buttonTable.add(playGameButton).pad(10);
        dialog.add(buttonTable).row();

        dialog.pack();
        dialog.setMovable(true);
        dialog.setModal(false);
        dialog.setResizable(false);
        dialog.setPosition(20, game.viewport.getWorldHeight() - dialog.getHeight() - 20);

        return dialog;
    }
}
