package com.seljaki.AgroMajsterGame.utils;

import com.badlogic.gdx.Gdx;

public class Constants {
    public static final int NUM_TILES = 3;
    public static final int ZOOM = 15;
    public static final int MAP_WIDTH = MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int MAP_HEIGHT = MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int HUD_WIDTH = Gdx.graphics.getWidth();
    public static final int HUD_HEIGHT = Gdx.graphics.getHeight();
    public static final String SELJAKI_SERVER_URL = "https://seljaki-server.schnapsen66.eu";
    public static final String SELJAKI_CHAIN_URL = "http://smd.schnapsen66.eu:3336";
}
