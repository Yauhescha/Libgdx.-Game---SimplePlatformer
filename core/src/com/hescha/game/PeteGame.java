package com.hescha.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class PeteGame extends Game {
	public static final AssetManager assetManager = new AssetManager();

	@Override
	public void create() {
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		setScreen(new LoadingScreen(this));
	}
}
