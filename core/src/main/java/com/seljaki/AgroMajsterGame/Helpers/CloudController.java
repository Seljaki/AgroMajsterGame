package com.seljaki.AgroMajsterGame.Helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.seljaki.AgroMajsterGame.assets.RegionNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudController {

    private class Cloud {
        private TextureRegion texture;
        private float x, y;
        private float moveSpeedX, moveSpeedY;
        private float scale;
        private boolean movingRight = MathUtils.random(0, 10) % 2 == 0;
        private boolean movingUp = true;

        public Cloud(TextureRegion texture, float x, float y, float moveSpeedX, float moveSpeedY, float scale) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.moveSpeedX = moveSpeedX;
            this.moveSpeedY = moveSpeedY;
            this.scale = scale;
        }

        public void update(float delta, float screenWidth, float screenHeight, float verticalLimit) {
            if (movingRight) {
                x += moveSpeedX * delta * 25;
                if (x >= screenWidth) {
                    movingRight = false;
                }
            } else {
                x -= moveSpeedX * delta * 25;
                if (x <= 0) {
                    movingRight = true;
                    x = 0;
                }
            }
        }

        public void render(Batch batch) {
            batch.draw(texture, x, y, texture.getRegionWidth() * scale, texture.getRegionHeight() * scale);
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

        TextureRegion cloudTexture = textureAtlas.findRegion(RegionNames.CLOUD);

        for (int i = 0; i < numberOfClouds; i++) {
            float x = MathUtils.random(0, screenWidth);
            float y = MathUtils.random(screenHeight - cloudTexture.getRegionHeight() / 2f + 200 - verticalLimit, screenHeight - cloudTexture.getRegionHeight() + verticalLimit + 200);
            float moveSpeedX = MathUtils.random(0.1f, 0.3f);
            float moveSpeedY = MathUtils.random(0.1f, 0.3f);
            float scale = MathUtils.random(0.6f, 1.3f);

            clouds.add(new Cloud(cloudTexture, x, y, moveSpeedX, moveSpeedY, scale));
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
