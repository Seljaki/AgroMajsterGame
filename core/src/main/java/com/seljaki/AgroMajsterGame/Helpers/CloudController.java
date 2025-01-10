package com.seljaki.AgroMajsterGame.Helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class CloudController {

    private static class Cloud {
        private TextureRegion texture;
        private float x, y;
        private float moveSpeedX, moveSpeedY;
        private boolean movingRight = true;
        private boolean movingUp = true;

        public Cloud(TextureRegion texture, float x, float y, float moveSpeedX, float moveSpeedY) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.moveSpeedX = moveSpeedX;
            this.moveSpeedY = moveSpeedY;
        }

        public void update(float delta, float screenWidth, float screenHeight, float verticalLimit) {

        }

        public void render(Batch batch) {
            batch.draw(texture, x, y, texture.getRegionWidth()*0.4f, texture.getRegionWidth()*0.4f );
        }
    }

    private List<Cloud> clouds;
    private float screenWidth, screenHeight;
    private float verticalLimit;

    public CloudController(TextureAtlas textureAtlas, float verticalLimit, Camera camera, int numberOfClouds) {
        this.clouds = new ArrayList<>();
        this.screenWidth = camera.viewportWidth;
        this.screenHeight = camera.viewportHeight;
        this.verticalLimit = verticalLimit;

        TextureRegion cloudTexture = textureAtlas.findRegion("cloud");

        for (int i = 0; i < numberOfClouds; i++) {
            float x = MathUtils.random(0, screenWidth - cloudTexture.getRegionWidth()*0.4f);
            float y = MathUtils.random(screenHeight - cloudTexture.getRegionHeight()*0.4f / 2f +200 - verticalLimit, screenHeight - cloudTexture.getRegionHeight()*0.4f + verticalLimit +200);
            float moveSpeedX = MathUtils.random(10, 30) / 100f;
            float moveSpeedY = MathUtils.random(10, 30) / 100f;

            clouds.add(new Cloud(cloudTexture, x, y, moveSpeedX, moveSpeedY));
        }
    }

    public void update(float delta) {
        for (Cloud cloud : clouds) {
            cloud.update(delta, screenWidth, screenHeight, verticalLimit);
        }
    }

    public void render(Batch batch) {
        for (Cloud cloud : clouds) {
            cloud.render(batch);
        }
    }
}
