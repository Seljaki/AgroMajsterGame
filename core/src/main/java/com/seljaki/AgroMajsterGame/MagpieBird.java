package com.seljaki.AgroMajsterGame;

public class MagpieBird {
    public float x;
    public float y;
    public float speed;
    public boolean flyingLeftToRight;
    public float animationTimer;

    public MagpieBird(float x, float y, float speed, boolean flyingLeftToRight) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.flyingLeftToRight = flyingLeftToRight;
        this.animationTimer = 0f;
    }
}
