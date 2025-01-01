package com.seljaki.AgroMajsterGame.lwjgl3;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
public class AssetPacker {

    private static final boolean DRAW_DEBUG_OUTLINE = false;

    private static final String RAW_ASSETS_PATH = "assets/assets_raw";
    private static final String ASSETS_PATH = "assets/images";

    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 8192;
        settings.maxHeight = 8192;
        settings.debug = DRAW_DEBUG_OUTLINE;



        TexturePacker.process(settings,
            RAW_ASSETS_PATH,   // the directory containing individual images to be packed
            ASSETS_PATH,   // the directory where the pack file will be written
            "assetsAtlas"   // the name of the pack file / atlas name
        );
    }
}
