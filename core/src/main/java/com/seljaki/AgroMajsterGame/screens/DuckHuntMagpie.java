package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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

    // Referenca na glavno igro
    private SeljakiMain game;

    // Za risanje (lahko uporabljaš tudi svoj Stage in Scene2D, če želiš)
    private SpriteBatch batch;

    // Animacija srake (12 sličic)
    private Animation<TextureRegion> magpieAnimation;
    private float animationTimer = 0f;

    // Trenutna pozicija "leteče srake"
    private float magpieX, magpieY;
    private float magpieSpeed = 100f; // hitrost premikanja srake
    private Random random;

    // Crosshair namesto miške
    private Texture crosshairTexture;
    private float crosshairX, crosshairY;

    // Logika za streljanje
    private int ammo = 6;        // Trenutna količina nabojev
    private final int MAX_AMMO = 6;
    private int score = 0;       // Koliko srak smo zadeli

    // Dimenzije okna
    private int screenWidth, screenHeight;

    public DuckHuntMagpie(SeljakiMain game) {
        this.game = game;
        assetManager = game.getAssetManager();
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.finishLoading();
        this.skin = assetManager.get(AssetPaths.UI_SKIN);
    }

    @Override
    public void show() {
        // Ustvari batch (če še nisi)
        batch = new SpriteBatch();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        // Nastavimo začetne dimenzije
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // Ustvari animacijo srake
        // Recimo, da imamo datoteke sraka1.png ... sraka12.png v assets/
        // Če imaš TextureAtlas, lahko podobno pridobiš regije iz Atlasa.
        Array<TextureRegion> frames = new Array<>();
        for (String magpieSpriteName : RegionNames.MAGPIE_SPRITES) {
            TextureRegion tex = gameplayAtlas.findRegion(magpieSpriteName);
            frames.add(tex);
        }
        // Ustvari animacijo (0.1s na sličico, ponavljajoče (Loop))
        magpieAnimation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);

        // Začetni naključni položaj srake
        random = new Random();
        respawnMagpie();

        // Naložimo teksturo za crosshair
        TextureRegion crosshairRegion = gameplayAtlas.findRegion(RegionNames.CROSSHAIR);
        crosshairTexture = crosshairRegion.getTexture();

        // Skrij sistemski kazalec
        Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.None);

        // Postavimo crosshair na sredino
        crosshairX = screenWidth / 2f - crosshairTexture.getWidth() / 2f;
        crosshairY = screenHeight / 2f - crosshairTexture.getHeight() / 2f;
    }

    @Override
    public void render(float delta) {
        // 1) Počistimo zaslon
        Gdx.gl.glClearColor(0, 0.6f, 0, 1); // recimo zelena podlaga
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2) Posodabljamo animacijo in položaj srake
        animationTimer += delta;
        float frameWidth = magpieAnimation.getKeyFrame(animationTimer).getRegionWidth();
        float frameHeight = magpieAnimation.getKeyFrame(animationTimer).getRegionHeight();

        // Premik srake
        magpieX += magpieSpeed * delta;
        // Če sraka "odleti" iz zaslona, jo respawnamo
        if (magpieX > screenWidth) {
            respawnMagpie();
        }

        // 3) Posodobimo položaj crosshaira glede na miško
        // Ker smo skriti sistemski kazalec, lahko enostavno beremo Gdx.input.getX/Y
        // in obrnemo Y, da ustreza koordinatnemu sistemu od spodaj navzgor
        crosshairX = Gdx.input.getX() - crosshairTexture.getWidth() / 2f;
        crosshairY = (screenHeight - Gdx.input.getY()) - crosshairTexture.getHeight() / 2f;

        // 4) Preverimo input za streljanje
        handleInput();

        // 5) Izrišemo vse
        batch.begin();

        // Izrišemo srako
        TextureRegion currentFrame = magpieAnimation.getKeyFrame(animationTimer);
        batch.draw(currentFrame, magpieX, magpieY);

        // Izrišemo crosshair
        batch.draw(crosshairTexture, crosshairX, crosshairY);

        // Izrišemo score in ammo (zelo preprosto, brez Scene2D UI)
        skin.getFont("default").draw(batch, "Score: " + score, 10, screenHeight - 10);
        skin.getFont("default").draw(batch, "Ammo: " + ammo, 10, screenHeight - 30);
        skin.getFont("default").draw(batch, "[R] za reload", 10, screenHeight - 50);

        batch.end();
    }

    private void handleInput() {
        // Če je levi gumb miške pritisnjen in imamo še naboje
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (ammo > 0) {
                // Kliknili smo na zaslon -> preverimo, ali smo zadeli srako
                if (isMagpieHit()) {
                    score++;
                    // Lahko jo respawnamo ali pustimo, da odleti
                    respawnMagpie();
                }
                // Zmanjšamo strel
                ammo--;
            }
        }

        // Reload ob pritisku na tipko R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            ammo = MAX_AMMO;
        }
    }

    private boolean isMagpieHit() {
        // Ugotovimo, ali je crosshair znotraj okvirja srake
        TextureRegion frame = magpieAnimation.getKeyFrame(animationTimer);
        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        // Prilagodimo, da je "klik" nekje znotraj srake
        if (crosshairX + crosshairTexture.getWidth() / 2f >= magpieX &&
            crosshairX + crosshairTexture.getWidth() / 2f <= magpieX + frameWidth &&
            crosshairY + crosshairTexture.getHeight() / 2f >= magpieY &&
            crosshairY + crosshairTexture.getHeight() / 2f <= magpieY + frameHeight) {
            return true;
        }
        return false;
    }

    private void respawnMagpie() {
        // Srado postavimo nazaj na levo stran ekrana na naključno višino
        magpieX = -50; // malo izven leve strani
        magpieY = random.nextFloat() * (screenHeight - 100) + 50; // med 50 in screenHeight-50
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        // Sprosti vire
        batch.dispose();
        crosshairTexture.dispose();
        for (TextureRegion reg : magpieAnimation.getKeyFrames()) {
            reg.getTexture().dispose();
        }
    }
}
