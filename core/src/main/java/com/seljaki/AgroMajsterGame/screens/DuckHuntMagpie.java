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
import org.w3c.dom.Text;

import java.util.Random;

public class DuckHuntMagpie extends ScreenAdapter {

    private Skin skin;
    private AssetManager assetManager;
    private TextureAtlas gameplayAtlas;

    private TextureRegion crosshairRegion;
    private float scopeWidth, scopeHeight;
    private float scopeScale = 0.15f; // prilagodi po želji

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
        //assetManager.load(AssetDescriptors.DUCK_MAGPIE_BACKGROUND);
        assetManager.finishLoading();
        this.skin = assetManager.get(AssetPaths.UI_SKIN);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

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
        crosshairRegion = gameplayAtlas.findRegion(RegionNames.SCOPE);
        scopeWidth = crosshairRegion.getRegionWidth() * scopeScale;
        scopeHeight = crosshairRegion.getRegionHeight() * scopeScale;

        // Skrij sistemski kazalec
        Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.None);

        // Postavimo crosshair na sredino
        crosshairX = Gdx.input.getX() - scopeWidth / 2f;
        crosshairY = (screenHeight - Gdx.input.getY()) - scopeHeight / 2f;
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

        crosshairX = Gdx.input.getX() - scopeWidth / 2f;
        crosshairY = (screenHeight - Gdx.input.getY()) - scopeHeight / 2f;

        // 4) Preverimo input za streljanje
        handleInput();

        // 5) Izrišemo vse
        batch.begin();
        batch.draw(gameplayAtlas.findRegion(RegionNames.BG_DG),
            0, 0,
            screenWidth, screenHeight);
        // Izrišemo srako
        TextureRegion currentFrame = magpieAnimation.getKeyFrame(animationTimer);
        batch.draw(currentFrame, magpieX, magpieY);

        float scale = 0.15f; // 50 % velikost

        batch.draw(
            crosshairRegion,
            crosshairX,        // x (levega spodnjega kota izrisa)
            crosshairY,        // y (levega spodnjega kota izrisa)
            scopeWidth,
            scopeHeight
        );

        skin.getFont("window").draw(batch, "Score: " + score, 10, screenHeight - 10);
        skin.getFont("window").draw(batch, "Ammo: " + ammo, 10, screenHeight - 30);
        skin.getFont("window").draw(batch, "[R] za reload", 10, screenHeight - 50);

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
        if (crosshairX + crosshairRegion.getTexture().getWidth() / 2f >= magpieX &&
            crosshairX + crosshairRegion.getTexture().getWidth() / 2f <= magpieX + frameWidth &&
            crosshairY + crosshairRegion.getTexture().getHeight() / 2f >= magpieY &&
            crosshairY + crosshairRegion.getTexture().getHeight() / 2f <= magpieY + frameHeight) {
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
        for (TextureRegion reg : magpieAnimation.getKeyFrames()) {
            reg.getTexture().dispose();
        }
    }
}
