package com.seljaki.AgroMajsterGame.screens.DuckHuntMagpie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.GameManager;
import com.seljaki.AgroMajsterGame.Helpers.MagpieBird;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.AssetPaths;
import com.seljaki.AgroMajsterGame.assets.RegionNames;
import com.seljaki.AgroMajsterGame.screens.MiniGameGameOverScreen;
import com.seljaki.AgroMajsterGame.screens.MiniGameSettingsScreen;
import org.jetbrains.annotations.NotNull;
import java.util.Random;

public class DuckHuntMagpie extends ScreenAdapter {

    private final Skin skin;
    private final AssetManager assetManager;
    private TextureAtlas gameplayAtlas;
    private TextureRegion crosshairRegion;
    private TextureRegion heartFull;
    private TextureRegion heartEmpty;
    private float scopeWidth, scopeHeight;
    private final Sound gunShot;
    private final Sound emptyGunShot;
    private final Sound reloadSound;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean showReloadMessage = false;
    private float reloadMessageTimer = 100f;
    private final BitmapFont messageFont;
    private Viewport viewport;
    private boolean paused = false;
    private Animation<TextureRegion> magpieAnimationLeft;
    private Animation<TextureRegion> magpieAnimationRight;
    private int maxBirdsAtOnce;
    private float birdBaseSpeed;
    private ShapeRenderer shapeRenderer;
    private float magpieScale = 0.19f;
    private float spawnTimer = 0f;
    private float spawnInterval = 2f;
    private final Array<MagpieBird> activeBirds = new Array<>();

    private final SeljakiMain game;
    private SpriteBatch batch;
    private Random random;
    private float crosshairX, crosshairY;
    private int score = 0;
    private final String difficulty;
    private final Array<TextureRegion> framesLeft = new Array<>();
    private final Array<TextureRegion> framesRight = new Array<>();
    float heartSize = 25;
    private int maxAmmo;
    private int ammo = 6;
    private final Sound gameOverSound;
    private final Skin skinForWindow;
    private float multiplier;


    public DuckHuntMagpie(SeljakiMain game) {
        this.game = game;
        assetManager = game.getAssetManager();
        messageFont = assetManager.get(AssetPaths.GAME_FONT);
        this.skin = assetManager.get(AssetPaths.UI_SKIN);
        gunShot = assetManager.get(AssetDescriptors.GUN_SHOT);
        emptyGunShot = assetManager.get(AssetDescriptors.EMPTY_GUN_SHOT);
        reloadSound = assetManager.get(AssetDescriptors.RELOAD_SOUND);
        gameOverSound = assetManager.get(AssetDescriptors.GAME_OVER_SOUND);
        //scoreSound = assetManager.get(AssetDescriptors.SCORE_SOUND);
        difficulty = GameManager.INSTANCE.getDifficultyMagpie();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        viewport = game.viewport;
        skinForWindow = assetManager.get(AssetPaths.SKIN);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        switch (difficulty) {
            case "Medium":
                multiplier =2f;
                magpieScale = 0.22f;
                maxBirdsAtOnce = 20;
                birdBaseSpeed = 200f;
                spawnInterval = 0.7f;
                maxAmmo = 8;
                ammo = 8;
                break;
            case "Hard":
                multiplier = 3f;
                magpieScale = 0.18f;
                maxBirdsAtOnce = 30;
                birdBaseSpeed = 250f;
                spawnInterval = 0.4f;
                maxAmmo = 6;
                ammo = 6;
                break;
            default:
                multiplier =1f;
                magpieScale = 0.26f;
                maxBirdsAtOnce = 5;
                birdBaseSpeed = 150f;
                maxAmmo = 10;
                ammo = 10;
        }

        random = new Random();
        heartFull = new TextureRegion(gameplayAtlas.findRegion(RegionNames.HEART));
        heartEmpty = new TextureRegion(gameplayAtlas.findRegion(RegionNames.NO_HEART));

        for (String magpieSpriteName : RegionNames.MAGPIE_SPRITES_LEFT) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            framesLeft.add(tex);
        }
        magpieAnimationLeft = new Animation<>(0.1f, framesLeft, Animation.PlayMode.LOOP);


        for (String magpieSpriteName : RegionNames.MAGPIE_SPRITES_RIGHT) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            framesRight.add(tex);
        }
        magpieAnimationRight = new Animation<>(0.1f, framesRight, Animation.PlayMode.LOOP);

        crosshairRegion = gameplayAtlas.findRegion(RegionNames.SCOPE);
        float scopeScale = 0.05f;
        scopeWidth = crosshairRegion.getRegionWidth() * scopeScale;
        scopeHeight = crosshairRegion.getRegionHeight() * scopeScale;

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();
        //System.out.println(screenWidth + " : " + screenHeight);
        ScreenUtils.clear(0, 0, 0, 1);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        if(gameOver){
            game.setScreen(new MiniGameGameOverScreen(game, score*multiplier, true));
        }

        handleInput();

        Vector3 tmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.getCamera().unproject(tmp);
        crosshairX = tmp.x - scopeWidth/2f;
        crosshairY = tmp.y - scopeHeight/2f;

       if(!paused){
           if (showReloadMessage) {
               reloadMessageTimer -= delta;
               if (reloadMessageTimer <= 0) {
                   showReloadMessage = false;
               }
           }
           spawnTimer += delta;
           //System.out.println(spawnTimer + " ? " + spawnInterval);
           if (spawnTimer >= spawnInterval) {
               //System.out.println("IN");
               spawnBird();
               spawnTimer = 0f;
           }

           for (int i = activeBirds.size - 1; i >= 0; i--) {
               MagpieBird bird = activeBirds.get(i);
               bird.animationTimer += delta;
               bird.x += bird.speed * delta;

               if (bird.flyingLeftToRight && bird.x < -250) {
                   activeBirds.removeIndex(i);
                   lives = Math.max(0, lives - 1);
                   if (lives == 0) {
                       gameOverSound.play();
                       gameOver = true;
                   }
               } else if (!bird.flyingLeftToRight && bird.x > screenWidth + 100) {
                   activeBirds.removeIndex(i);
                   lives = Math.max(0, lives - 1);
                   if (lives == 0) {
                       gameOverSound.play();
                       gameOver = true;
                   }
               }
           }
       }

        batch.begin();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        float startX = (screenWidth - (heartSize * 3)) / 2;
        float heartY = screenHeight - heartSize - 10;

        TextureRegion bg = gameplayAtlas.findRegion(RegionNames.BG_DG);
        batch.draw(bg, 0, 0, screenWidth, screenHeight);
        for (MagpieBird bird : activeBirds) {
            TextureRegion frame = bird.flyingLeftToRight
                ? magpieAnimationLeft.getKeyFrame(bird.animationTimer)
                : magpieAnimationRight.getKeyFrame(bird.animationTimer);

            float birdW = frame.getRegionWidth() * magpieScale;
            float birdH = frame.getRegionHeight() * magpieScale;

            batch.draw(frame, bird.x, bird.y, birdW, birdH);
        }

        batch.draw(crosshairRegion, crosshairX, crosshairY, scopeWidth, scopeHeight);

        BitmapFont ammoFont = assetManager.get(AssetDescriptors.GAME_FONT);
        ammoFont.getData().setScale(0.5f);
        ammoFont.setColor(Color.BLACK);

        GlyphLayout layoutScore = new GlyphLayout(ammoFont, "" + score+ " x"+multiplier);
        Image scoreImage = new Image(gameplayAtlas.findRegion(RegionNames.CROW));
        scoreImage.setSize(40,40);
        float scoreX = scoreImage.getWidth() + 70f;
        float scoreY = screenHeight - 42f;
        scoreImage.setPosition(scoreX, scoreY);
        scoreImage.draw(batch, 1);
        float sx = 20f;
        float sy = screenHeight - 20f;
        ammoFont.draw(batch, layoutScore, sx, sy);

        GlyphLayout layoutAmmo = new GlyphLayout(ammoFont, "" + ammo);
        Image ammoImage = new Image(gameplayAtlas.findRegion(RegionNames.AMMO));
        ammoImage.setSize(30,30);
        float ammoX = (screenWidth - layoutAmmo.width - ammoImage.getWidth()) - 20f;
        float ammoY = screenHeight -ammoImage.getHeight() - 5;
        ammoImage.setPosition(ammoX, ammoY);
        ammoImage.draw(batch, 1);
        float ax = (screenWidth - layoutAmmo.width) - 20f;
        float ay = screenHeight - 20f;
        ammoFont.draw(batch, layoutAmmo, ax, ay);

        GlyphLayout layoutPause = new GlyphLayout();
        String pauseMessage;
        if(!paused){
            pauseMessage = "Pause [P]";
        }else{
           pauseMessage = "Resume [P]";
           BitmapFont homeFont = skinForWindow.getFont("window");
           homeFont.getData().setScale(0.7f);
           GlyphLayout layoutHome = new GlyphLayout(homeFont, "Home [H]");
           float hx = (screenWidth - layoutHome.width) / 2f;
           float hy = screenHeight /22f;
           homeFont.getData().setScale(0.7f);
           homeFont.draw(batch, layoutHome, hx, hy);
        }
        layoutPause.setText(skinForWindow.getFont("window"), pauseMessage);
        float xPause = (screenWidth - layoutPause.width) /2;
        BitmapFont pausedFont = skinForWindow.getFont("window");
        pausedFont.getData().setScale(0.7f);
        pausedFont.draw(batch,layoutPause, xPause, screenHeight /14);

        if (showReloadMessage) {
            messageFont.setColor(1, 0, 0, 1);
            GlyphLayout layout = new GlyphLayout();
            String message = "MUST RELOAD! [R]";
            layout.setText(messageFont, message);
            float textWidth = layout.width;
            float x = (screenWidth - textWidth) / 2;
            float y = screenHeight /4;
            messageFont.draw(batch, layout, x, y);
        }
        for (int i = 0; i < 3; i++) {
            if (i < lives) {
                batch.draw(heartFull, startX + i * heartSize, heartY, heartSize, heartSize);
            } else {
                batch.draw(heartEmpty, startX + i * heartSize, heartY, heartSize, heartSize);
            }
        }

        if(paused){
            BitmapFont pauseFont = assetManager.get(AssetDescriptors.GAME_FONT);
            pauseFont.setColor(new Color(Color.BLACK));
            GlyphLayout layout = new GlyphLayout(pauseFont, "PAUSED");
            float px = (screenWidth - layout.width) / 2f;
            float py = (screenHeight - layout.height) / 2f;
            pauseFont.draw(batch, layout, px, py);
        }

        batch.end();
        shapeRenderer.end();

    }

    private void handleInput() {

        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            paused = !paused;
        }
        if(!paused){

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                if (ammo > 0) {
                    gunShot.play();
                    for (int i = activeBirds.size - 1; i >= 0; i--) {
                        if (isMagpieHit(activeBirds.get(i))) {
                            //scoreSound.play();
                            score++;
                            if(spawnInterval > 0.05f){
                                spawnInterval -= 0.003f;
                            }
                            activeBirds.removeIndex(i);
                            break;
                        }
                    }
                    ammo--;
                } else {
                    emptyGunShot.play();
                    showReloadMessage = true;
                    reloadMessageTimer = 0.7f;
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                reloadSound.play();
                ammo = maxAmmo;
            }
        }else{
            if(Gdx.input.isKeyJustPressed(Input.Keys.H)){
                game.setScreen(new MiniGameSettingsScreen(game, true));
            }
        }
    }

    private void spawnBird() {
        if (activeBirds.size >= maxBirdsAtOnce) {
            return;
        }

        boolean leftToRight = random.nextBoolean();
        float startX, chosenSpeed;
        if (leftToRight) {
            startX = viewport.getWorldWidth() + viewport.getWorldWidth()/6.4f;
            chosenSpeed = -birdBaseSpeed;
        } else {
            startX = -(viewport.getWorldWidth()/6.4f);
            chosenSpeed = birdBaseSpeed;
        }

        float startY = random.nextFloat() * (viewport.getWorldHeight() - 200) + 50;
        MagpieBird newBird = new MagpieBird(startX, startY, chosenSpeed, leftToRight);
        activeBirds.add(newBird);
    }

    private boolean isMagpieHit(@NotNull MagpieBird bird) {
        TextureRegion frame = bird.flyingLeftToRight
            ? magpieAnimationLeft.getKeyFrame(bird.animationTimer)
            : magpieAnimationRight.getKeyFrame(bird.animationTimer);

        float birdW = frame.getRegionWidth() * magpieScale;
        float birdH = frame.getRegionHeight() * magpieScale;

        float birdLeft = bird.x;
        float birdRight = bird.x + birdW;
        float birdBottom = bird.y;
        float birdTop = bird.y + birdH;

        float crosshairCenterX = crosshairX + scopeWidth / 2f;
        float crosshairCenterY = crosshairY + scopeHeight / 2f;

        return (crosshairCenterX >= birdLeft && crosshairCenterX <= birdRight
            && crosshairCenterY >= birdBottom && crosshairCenterY <= birdTop);
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (TextureRegion reg : magpieAnimationLeft.getKeyFrames()) {
            reg.getTexture().dispose();
        }
        for (TextureRegion reg : magpieAnimationRight.getKeyFrames()) {
            reg.getTexture().dispose();
        }
    }
}
