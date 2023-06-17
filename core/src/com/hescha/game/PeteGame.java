package com.hescha.game;

import com.badlogic.gdx.Game;

public class PeteGame extends Game {

	@Override
	public void create() {
		setScreen(new GameScreen(this));
	}
}
