package com.seljaki.AgroMajsterGame;

public class MagpieBird {
    float x, y;         // pozicija
    float speed;        // lahko +/- (smer)
    boolean flyingLeftToRight;
    float animationTimer;

    public MagpieBird(float x, float y, float speed, boolean flyingLeftToRight) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.flyingLeftToRight = flyingLeftToRight;
        this.animationTimer = 0f;
    }
}
