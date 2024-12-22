package com.seljaki.AgroMajsterGame.utils;

import com.badlogic.gdx.math.Vector2;

public class Geolocation {
    public double lat;
    public double lng;

    public Geolocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static float[] getPolygon(Geolocation[] loc, ZoomXY beginTile) {
        float[] vertices = new float[loc.length*2];
        for (int i = 0; i < loc.length; i++) {
            Vector2 v = MapRasterTiles.getPixelPosition(loc[i].lat, loc[i].lng, beginTile.x, beginTile.y);
            vertices[i*2] = v.x;
            vertices[i*2 + 1] = v.y;
        }
        return vertices;
    }
}
