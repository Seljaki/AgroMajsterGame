package com.seljaki.AgroMajsterGame.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetDescriptors {
    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
            new AssetDescriptor<TextureAtlas>(AssetPaths.GAMEPLAY, TextureAtlas.class);
    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<>(AssetPaths.UI_SKIN, Skin.class);
    public static final AssetDescriptor<Skin> SKIN =
        new AssetDescriptor<>(AssetPaths.SKIN, Skin.class);
    public static final AssetDescriptor<BitmapFont> GAME_FONT =
        new AssetDescriptor<>(AssetPaths.GAME_FONT,BitmapFont.class);
    public static final AssetDescriptor<Sound> GUN_SHOT =
        new AssetDescriptor<>(AssetPaths.GUN_SHOT, Sound.class);
    public static final AssetDescriptor<Sound>  RELOAD_SOUND =
        new AssetDescriptor<>(AssetPaths.RELOAD_SOUND, Sound.class);
    public static final AssetDescriptor<Sound>  SELECT_SOUND =
        new AssetDescriptor<>(AssetPaths.SELECT_SOUND, Sound.class);
    public static final AssetDescriptor<Sound>  GAME_OVER_SOUND =
        new AssetDescriptor<>(AssetPaths.GAME_OVER_SOUND, Sound.class);
    public static final AssetDescriptor<Sound>  SCORE_SOUND =
        new AssetDescriptor<>(AssetPaths.SCORE_SOUND, Sound.class);
    public static final AssetDescriptor<Sound> EMPTY_GUN_SHOT =
        new AssetDescriptor<>(AssetPaths.EMPTY_GUN_SHOT, Sound.class);
    public static final AssetDescriptor<Sound> MOLE_SQUEAK_SOUND = new AssetDescriptor<Sound>(AssetPaths.MOLE_SQUEAK_SOUND, Sound.class);
    public static final AssetDescriptor<Music> WHACK_A_MOLE_MUSIC = new AssetDescriptor<Music>(AssetPaths.WHACK_A_MOLE_MUSIC, Music.class);

    // PARTICLES
    public static final AssetDescriptor<ParticleEffect> PARTICLE_EFFECT_MOLE_BLOOD =
        new AssetDescriptor<ParticleEffect>(AssetPaths.PARTICLE_EFFECT_MOLE_BLOOD, ParticleEffect.class);
    public static final AssetDescriptor<ParticleEffect> PARTICLE_EFFECT_RAIN =
        new AssetDescriptor<ParticleEffect>(AssetPaths.PARTICLE_EFFECT_RAIN, ParticleEffect.class);

    private AssetDescriptors() {
    }
}
