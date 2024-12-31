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

    public static final AssetDescriptor<Skin> SKIN = new AssetDescriptor<Skin>(AssetPaths.SKIN, Skin.class);

    public static final AssetDescriptor<Sound> MOLE_SQUEAK_SOUND = new AssetDescriptor<Sound>(AssetPaths.MOLE_SQUEAK_SOUND, Sound.class);
    public static final AssetDescriptor<Music> WHACK_A_MOLE_MUSIC = new AssetDescriptor<Music>(AssetPaths.WHACK_A_MOLE_MUSIC, Music.class);


    private AssetDescriptors() {
    }
}
