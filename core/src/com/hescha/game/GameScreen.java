package com.hescha.game;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 480;

    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TiledMap tiledMap;
    private Pete pete;

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        tiledMap = PeteGame.assetManager.get("pete.tmx");
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
        orthogonalTiledMapRenderer.setView(camera);

        pete = new Pete();
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }

    private void update(float delta) {
        pete.update();
        stopPeteLeavingTheScreen();
    }

    private void clearScreen() {
        ScreenUtils.clear(Color.BLACK);
    }

    private void draw() {
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        orthogonalTiledMapRenderer.render();
        batch.begin();
        batch.end();
    }

    private void drawDebug() {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        pete.drawDebug(shapeRenderer);
        shapeRenderer.end();
    }

    private void stopPeteLeavingTheScreen() {
        if (pete.getY() < 0) {
            pete.setPosition(pete.getX(), 0);
            pete.landed();
        }
        if (pete.getX() < 0) {
            pete.setPosition(0, pete.getY());
        }
        if (pete.getX() + Pete.WIDTH > WORLD_WIDTH) {
            pete.setPosition(WORLD_WIDTH - Pete.WIDTH,
                    pete.getY());
        }
    }
}
