package com.seljaki.AgroMajsterGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.seljaki.AgroMajsterGame.GameManager;
import com.seljaki.AgroMajsterGame.MagpieBird;
import com.seljaki.AgroMajsterGame.SeljakiMain;
import com.seljaki.AgroMajsterGame.assets.AssetDescriptors;
import com.seljaki.AgroMajsterGame.assets.AssetPaths;
import com.seljaki.AgroMajsterGame.assets.RegionNames;
import okhttp3.internal.http2.Settings;

import java.util.Random;

public class DuckHuntMagpie extends ScreenAdapter {

    private Skin skin;
    private AssetManager assetManager;
    private TextureAtlas gameplayAtlas;

    private TextureRegion crosshairRegion;
    private TextureRegion heartFull;
    private TextureRegion heartEmpty;
    private float scopeWidth, scopeHeight;
    private Sound gunShot;
    private Sound emptyGunShot;
    private Sound reloadSound;
    private float scopeScale = 0.15f;
    private int lives = 3;

    private boolean showReloadMessage = false;
    private float reloadMessageTimer = 100f;

    private BitmapFont messageFont;
    private BitmapFont ammoFont;
    private Viewport viewport;

    // ----- NOVO -----
    private Animation<TextureRegion> magpieAnimationLeft;
    private Animation<TextureRegion> magpieAnimationRight;
    private int maxBirdsAtOnce;
    private float birdBaseSpeed;

    // Oba tipa animacij (levo-desno)
    private boolean flyingLeftToRight; // določa smer
    private ShapeRenderer shapeRenderer;

    // Scale srake, da je 0.4 = 40% original size
    private float magpieScale = 0.4f;

    // Časovni zamik (random spawn)
    private float spawnTimer = 0f;
    private float spawnInterval = 2f; // kolikokrat približno čakamo do next spawn
    private boolean birdActive = false; // ali je sraka trenutno "v zraku"

    private Array<MagpieBird> activeBirds = new Array<>();


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
        assetManager.load(AssetDescriptors.GAME_FONT);
        assetManager.load(AssetDescriptors.SELECT_SOUND);
        assetManager.load(AssetDescriptors.RELOAD_SOUND);
        assetManager.finishLoading();

        messageFont = assetManager.get(AssetPaths.GAME_FONT);

        this.skin = assetManager.get(AssetPaths.UI_SKIN);
        gunShot = assetManager.get(AssetDescriptors.GUN_SHOT);
        emptyGunShot = assetManager.get(AssetDescriptors.EMPTY_GUN_SHOT);
        reloadSound = assetManager.get(AssetDescriptors.RELOAD_SOUND);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer(); // Ustvarimo ShapeRenderer za debug
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        viewport = game.viewport;
        screenWidth = viewport.getScreenWidth();
        screenHeight = viewport.getScreenHeight();

        String difficulty = GameManager.INSTANCE.getDifficulty();  // "Easy" / "Medium" / "Hard"
        switch (difficulty) {
            case "Easy":
                maxBirdsAtOnce = 3;
                birdBaseSpeed = 100f;
                break;
            case "Medium":
                maxBirdsAtOnce = 5;
                birdBaseSpeed = 150f;
                break;
            case "Hard":
                maxBirdsAtOnce = 8;
                birdBaseSpeed = 200f;
                break;
            default:
                maxBirdsAtOnce = 3; // fallback
                birdBaseSpeed = 100f;
        }

        random = new Random();

        heartFull = new TextureRegion(gameplayAtlas.findRegion("heart"));
        heartEmpty = new TextureRegion(gameplayAtlas.findRegion("noHeart"));
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

        if (showReloadMessage) {
            reloadMessageTimer -= delta; // Odštej čas
            if (reloadMessageTimer <= 0) {
                showReloadMessage = false; // Skrij napis po 1 sekundi
            }
        }

        batch.begin();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        float heartSize = 50; // Velikost src
        float startX = (screenWidth - (heartSize * 3)) / 2; // Začetek, da so srca centrirana
        float heartY = screenHeight - heartSize - 10; // Y-koordinata (zgoraj, z 10 px odmika)




        // Narišemo ozadje
        TextureRegion bg = gameplayAtlas.findRegion(RegionNames.BG_DG);
        batch.draw(bg, 0, 0, screenWidth, screenHeight);


        // Če nimamo aktivne srake na ekranu
        if (!birdActive) {
            spawnTimer += delta;
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
            //System.out.println(magpieX + "?" + screenWidth);
            if (!flyingLeftToRight && magpieX > screenWidth + 100) {
                //System.out.println("checking left2Right " + magpieX + " > " + screenWidth);
                birdActive = false;
                lives = Math.max(0, lives - 1); // Zmanjšaj življenja, ne manj kot 0
            }
            if (flyingLeftToRight && magpieX < -250) {
                //System.out.println("checking right2Left " + magpieX + " < -250");
                birdActive = false;
                lives = Math.max(0, lives - 1);
            }

            TextureRegion currentFrame;
            if (flyingLeftToRight) {
                currentFrame = magpieAnimationLeft.getKeyFrame(animationTimer);
            } else {
                currentFrame = magpieAnimationRight.getKeyFrame(animationTimer);
            }

            float birdW = currentFrame.getRegionWidth() * magpieScale;
            float birdH = currentFrame.getRegionHeight() * magpieScale;

            batch.draw(currentFrame, magpieX, magpieY, birdW, birdH);
            shapeRenderer.rect(magpieX, magpieY, birdW, birdH); // Izris okvira okoli srake
        }

        batch.draw(crosshairRegion, crosshairX, crosshairY, scopeWidth, scopeHeight);


        ammoFont = assetManager.get(AssetDescriptors.GAME_FONT);
        ammoFont.setColor(Color.BLACK);
        float scoreX = 20;
        float scoreY = screenHeight - 90;
        Image scoreImage = new Image(gameplayAtlas.findRegion("crow"));
        scoreImage.setSize(90,90);
        scoreImage.setPosition(scoreX, scoreY);
        scoreImage.draw(batch, 1);
        ammoFont.draw(batch, "" + score, scoreX + scoreImage.getWidth() + 10, scoreY + scoreImage.getHeight() / 2);
        float ammoX = screenWidth - 120;
        float ammoY = screenHeight - 90;


        Image ammoImage = new Image(gameplayAtlas.findRegion("ammo"));
        ammoImage.setSize(70,70);
        ammoImage.setPosition(ammoX, ammoY);
        ammoImage.draw(batch, 1);
        BitmapFont font = new BitmapFont();
        font.setColor(new Color(Color.BLACK));
        font.getData().setScale(3f);

        ammoFont.draw(batch, "" + ammo, ammoX + ammoImage.getWidth() + 10, ammoY + ammoImage.getHeight() / 2);

        if (showReloadMessage) {
            messageFont.setColor(1, 0, 0, 1);
            GlyphLayout layout = new GlyphLayout();
            String message = "must reload [R]";
            layout.setText(messageFont, message);
            float textWidth = layout.width;
            float x = (screenWidth - textWidth) / 2;
            float y = (float) screenHeight /4;
            messageFont.draw(batch, layout, x, y);
        }
        for (int i = 0; i < 3; i++) {
            if (i < lives) {
                batch.draw(heartFull, startX + i * heartSize, heartY, heartSize, heartSize);
            } else {
                batch.draw(heartEmpty, startX + i * heartSize, heartY, heartSize, heartSize);
            }
        }

        batch.end();


        shapeRenderer.end();

    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (ammo > 0) {
                gunShot.play();
                if (birdActive && isMagpieHit()) {
                    score++;
                    birdActive = false;
                }
                ammo--;
            } else {
                emptyGunShot.play();
                showReloadMessage = true;
                reloadMessageTimer = 0.7f;
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            // Pause
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)){
            game.setScreen(new DuckHuntMagpieSettings(game));
        }

        // Reload ob pritisku na tipko R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reloadSound.play();
            ammo = MAX_AMMO;
        }
    }

    private void spawnBird() {
        if (activeBirds.size >= maxBirdsAtOnce) {
            // Že imamo max dovolj ptic, ne ustvarjaj nove
            return;
        }

        boolean leftToRight = random.nextBoolean();
        float startX, chosenSpeed;
        if (leftToRight) {
            startX = screenWidth + 200;
            chosenSpeed = -birdBaseSpeed;  // negativna, leti levo
        } else {
            startX = -200;
            chosenSpeed = birdBaseSpeed;   // pozitivna, leti desno
        }

        float startY = random.nextFloat() * (screenHeight - 200) + 50;
        MagpieBird newBird = new MagpieBird(startX, startY, chosenSpeed, leftToRight);
        activeBirds.add(newBird);
    }

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
