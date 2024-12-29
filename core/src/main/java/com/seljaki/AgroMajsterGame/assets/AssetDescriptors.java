package com.seljaki.AgroMajsterGame.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class AssetDescriptors {
    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
            new AssetDescriptor<TextureAtlas>(AssetPaths.GAMEPLAY, TextureAtlas.class);

    private AssetDescriptors() {
    }
}
