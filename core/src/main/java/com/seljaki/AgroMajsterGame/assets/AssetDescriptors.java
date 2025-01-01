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
    public static final AssetDescriptor<Sound> GUN_SHOT =
        new AssetDescriptor<>(AssetPaths.GUN_SHOT, Sound.class);
    public static final AssetDescriptor<Sound> EMPTY_GUN_SHOT =
        new AssetDescriptor<>(AssetPaths.EMPTY_GUN_SHOT, Sound.class);
    private AssetDescriptors() {
    }
}
