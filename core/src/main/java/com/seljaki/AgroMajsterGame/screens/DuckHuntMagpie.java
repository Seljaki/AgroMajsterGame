package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.AssetPaths;
import com.seljaki.AgroMajsterGame.assets.RegionNames;

import java.util.Random;

public class DuckHuntMagpie extends ScreenAdapter {

    private Skin skin;
    private AssetManager assetManager;
    private TextureAtlas gameplayAtlas;

    private TextureRegion crosshairRegion;
    private float scopeWidth, scopeHeight;
    private Sound gunShot;
    private Sound emptyGunShot;
    private float scopeScale = 0.15f;

    // ----- NOVO -----
    private Animation<TextureRegion> magpieAnimationLeft;
    private Animation<TextureRegion> magpieAnimationRight;

    // Oba tipa animacij (levo-desno)
    private boolean flyingLeftToRight; // določa smer

    // Scale srake, da je 0.4 = 40% original size
    private float magpieScale = 0.4f;

    // Časovni zamik (random spawn)
    private float spawnTimer = 0f;
    private float spawnInterval = 2f; // kolikokrat približno čakamo do next spawn
    private boolean birdActive = false; // ali je sraka trenutno "v zraku"

    // ----------------

    // Referenca na glavno igro
    private SeljakiMain game;
    private SpriteBatch batch;

    private float animationTimer = 0f;

    // Trenutna pozicija "leteče srake"
    private float magpieX, magpieY;
    private float magpieSpeed = 100f;
    private Random random;

    // Crosshair namesto miške
    private float crosshairX, crosshairY;

    // Logika za streljanje
    private int ammo = 6;
    private final int MAX_AMMO = 6;
    private int score = 0;

    // Dimenzije okna
    private int screenWidth, screenHeight;

    public DuckHuntMagpie(SeljakiMain game) {
        this.game = game;
        assetManager = game.getAssetManager();

        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.EMPTY_GUN_SHOT);
        assetManager.load(AssetDescriptors.GUN_SHOT);
        assetManager.finishLoading();

        this.skin = assetManager.get(AssetPaths.UI_SKIN);
        gunShot = assetManager.get(AssetDescriptors.GUN_SHOT);
        emptyGunShot = assetManager.get(AssetDescriptors.EMPTY_GUN_SHOT);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        random = new Random();

        // Naredimo array sličic za levo
        Array<TextureRegion> framesLeft = new Array<>();
        for (String magpieSpriteName : RegionNames.MAGPIE_SPRITES_LEFT) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            framesLeft.add(tex);
        }
        magpieAnimationLeft = new Animation<>(0.1f, framesLeft, Animation.PlayMode.LOOP);

        // Naredimo array sličic za desno
        Array<TextureRegion> framesRight = new Array<>();
        for (String magpieSpriteName : RegionNames.MAGPIE_SPRITES_RIGHT) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            framesRight.add(tex);
        }
        magpieAnimationRight = new Animation<>(0.1f, framesRight, Animation.PlayMode.LOOP);

        // Crosshair
        crosshairRegion = gameplayAtlas.findRegion(RegionNames.SCOPE);
        scopeWidth = crosshairRegion.getRegionWidth() * scopeScale;
        scopeHeight = crosshairRegion.getRegionHeight() * scopeScale;

        Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.None);
    }

    @Override
    public void render(float delta) {
        // Čiščenje zaslona
        Gdx.gl.glClearColor(0, 0.6f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Vrednosti ekrana (če se med igro spreminja velikost)
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // Izračun miške + centriranje crosshair
        crosshairX = Gdx.input.getX() - scopeWidth / 2f;
        crosshairY = (screenHeight - Gdx.input.getY()) - scopeHeight / 2f;

        // Najprej posodobimo streljanje
        handleInput();

        batch.begin();

        // Narišemo ozadje
        TextureRegion bg = gameplayAtlas.findRegion(RegionNames.BG_DG);
        batch.draw(bg, 0, 0, screenWidth, screenHeight);

        // ----- SRAKA - UPRAVLJANJE spawn in premik -----

        // Če nimamo aktivne srake na ekranu
        if (!birdActive) {
            // Odbijamo čas
            spawnTimer += delta;
            // Ko preteče spawnInterval, ustvarimo srako
            if (spawnTimer >= spawnInterval) {
                spawnBird();         // ustvari srako
                birdActive = true;   // zdaj je aktivna
                spawnTimer = 0f;
                // Lahko nastavimo nov random spawnInterval
                spawnInterval = 1f + random.nextFloat() * 2f; // med 1 in 3 sek
            }
        } else {
            // Sraka je aktivna, torej jo premikamo, rišemo
            animationTimer += delta;
            magpieX += magpieSpeed * delta;

            // Ali je prišla izven ekrana (glede na smer letenja)?
            if (flyingLeftToRight && magpieX > screenWidth + 50) {
                birdActive = false;
            }
            if (!flyingLeftToRight && magpieX < -150) {
                birdActive = false;
            }

            // Izberemo ustrezno animacijo (levo/desno)
            TextureRegion currentFrame;
            if (flyingLeftToRight) {
                currentFrame = magpieAnimationLeft.getKeyFrame(animationTimer);
            } else {
                currentFrame = magpieAnimationRight.getKeyFrame(animationTimer);
            }

            // Pomanjšamo srako (0.4f)
            float birdW = currentFrame.getRegionWidth() * magpieScale;
            float birdH = currentFrame.getRegionHeight() * magpieScale;

            // Narišemo srako
            batch.draw(currentFrame, magpieX, magpieY, birdW, birdH);
        }

        // Narišemo crosshair
        batch.draw(crosshairRegion, crosshairX, crosshairY, scopeWidth, scopeHeight);

        // UI: Score in Ammo
        skin.getFont("window").draw(batch, "Score: " + score, 10, screenHeight - 10);
        skin.getFont("window").draw(batch, "Ammo: " + ammo, 10, screenHeight - 30);
        skin.getFont("window").draw(batch, "[R] za reload", 10, screenHeight - 50);

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (ammo > 0) {
                gunShot.play();
                // Preverimo zadetek samo, če je ptica aktivna
                if (birdActive && isMagpieHit()) {
                    score++;
                    // "Odstranimo" ptico takoj, da jo bo spawnTimer čez nekaj časa spet ustvaril
                    birdActive = false;
                }
                ammo--;
            } else {
                // Če ni nabojev
                emptyGunShot.play();
            }
        }

        // Reload ob pritisku na tipko R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            ammo = MAX_AMMO;
        }
    }

    /** Ustvari novo srako z naključno smerjo in y-pozicijo. */
    private void spawnBird() {
        animationTimer = 0f;

        // Naključno določimo, ali leti z leve na desno ali obratno
        flyingLeftToRight = random.nextBoolean();

        if (flyingLeftToRight) {
            // Začne izven levega roba
            magpieX = screenWidth + 100;
            magpieSpeed = -100f; // negativna hitrost

        } else {
            // Začne izven desnega roba
            magpieX = -100;
            magpieSpeed = 100f; // pozitivna hitrost
        }

        // Naključna višina
        magpieY = random.nextFloat() * (screenHeight - 200) + 50;
    }

    /** Preveri, ali je križec znotraj bounding box srake. */
    private boolean isMagpieHit() {
        // Izberemo trenutno sličico animacije
        TextureRegion frame = flyingLeftToRight
            ? magpieAnimationLeft.getKeyFrame(animationTimer)
            : magpieAnimationRight.getKeyFrame(animationTimer);

        // Velikost srake z upoštevanjem scale
        float birdW = frame.getRegionWidth() * magpieScale;
        float birdH = frame.getRegionHeight() * magpieScale;

        // Koordinate srake so (magpieX, magpieY), to je "spodnji levi" kot
        float birdLeft   = magpieX;
        float birdRight  = magpieX + birdW;
        float birdBottom = magpieY;
        float birdTop    = magpieY + birdH;

        // Koordinate središča križca
        float crosshairCenterX = crosshairX + scopeWidth / 2f;
        float crosshairCenterY = crosshairY + scopeHeight / 2f;

        return (crosshairCenterX >= birdLeft && crosshairCenterX <= birdRight
            && crosshairCenterY >= birdBottom && crosshairCenterY <= birdTop);
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        // Sprosti sličice
        for (TextureRegion reg : magpieAnimationLeft.getKeyFrames()) {
            reg.getTexture().dispose();
        }
        for (TextureRegion reg : magpieAnimationRight.getKeyFrames()) {
            reg.getTexture().dispose();
        }
    }
}
