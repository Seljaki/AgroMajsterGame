package com.seljaki.AgroMajsterGame.Helpers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimatedActor extends Actor {
    private final Animation<TextureRegion> animation;
    private float stateTime = 0f;

    public AnimatedActor(Animation<TextureRegion> animation) {
        this.animation = animation;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        batch.setColor(getColor());
        batch.draw(
            currentFrame,
            getX(), getY(),
            getWidth(), getHeight()
        );
    }
}
