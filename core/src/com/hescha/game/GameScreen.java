package com.hescha.game;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 480;
    private static final float CELL_SIZE = 16;

    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TiledMap tiledMap;
    private Pete pete;

    private Array<Acorn> acorns = new Array<Acorn>();

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

        pete = new Pete(
                PeteGame.assetManager.get("pete.png", Texture.class),
                PeteGame.assetManager.get("jump.wav", Sound.class)
                );
        pete.setPosition(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        populateAcorns();

        PeteGame.assetManager.get("peteTheme.mp3", Music.class).setLooping(true);
        PeteGame.assetManager.get("peteTheme.mp3", Music.class).play();
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }

    private void update(float delta) {
        pete.update(delta);
        stopPeteLeavingTheScreen();
        handlePeteCollision();
        handlePeteCollisionWithAcorn();
        updateCameraX();
    }

    private void clearScreen() {
        ScreenUtils.clear(Color.BLACK);
    }

    private void draw() {
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        orthogonalTiledMapRenderer.render();
        batch.begin();
        pete.draw(batch);
        for (Acorn acorn : acorns) {
            acorn.draw(batch);
        }
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
        TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer)
                tiledMap.getLayers().get(0);
        float levelWidth = tiledMapTileLayer.getWidth() *
                tiledMapTileLayer.getTileWidth();
        if (pete.getX() + Pete.WIDTH > levelWidth) {
            pete.setPosition(levelWidth - Pete.WIDTH, pete.getY());
        }
    }

    private Array<CollisionCell> whichCellsDoesPeteCover() {
        float x = pete.getX();
        float y = pete.getY();
        Array<CollisionCell> cellsCovered = new Array<CollisionCell>();
        float cellX = x / CELL_SIZE;
        float cellY = y / CELL_SIZE;
        int bottomLeftCellX = MathUtils.floor(cellX);
        int bottomLeftCellY = MathUtils.floor(cellY);

        TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        TiledMapTileLayer.Cell cell = tiledMapTileLayer.getCell(bottomLeftCellX, bottomLeftCellY);
        cellsCovered.add(new CollisionCell(cell, bottomLeftCellX, bottomLeftCellY));

        if (cellX % 1 != 0 && cellY % 1 != 0) {
            int topRightCellX = bottomLeftCellX + 1;
            int topRightCellY = bottomLeftCellY + 1;
            cellsCovered.add(new
                    CollisionCell(tiledMapTileLayer.getCell(topRightCellX,
                    topRightCellY), topRightCellX, topRightCellY));
        }
        if (cellX % 1 != 0) {
            int bottomRightCellX = bottomLeftCellX + 1;
            int bottomRightCellY = bottomLeftCellY;
            cellsCovered.add(new
                    CollisionCell(tiledMapTileLayer.getCell(bottomRightCellX,
                    bottomRightCellY), bottomRightCellX, bottomRightCellY));
        }
        if (cellY % 1 != 0) {
            int topLeftCellX = bottomLeftCellX;
            int topLeftCellY = bottomLeftCellY + 1;
            cellsCovered.add(new
                    CollisionCell(tiledMapTileLayer.getCell(topLeftCellX,
                    topLeftCellY), topLeftCellX, topLeftCellY));
        }
        return cellsCovered;
    }

    private Array<CollisionCell> filterOutNonTiledCells(Array<CollisionCell> cells) {
        for (Iterator<CollisionCell> iter = cells.iterator(); iter.hasNext(); ) {
            CollisionCell collisionCell = iter.next();
            if (collisionCell.isEmpty()) {
                iter.remove();
            }
        }
        return cells;
    }

    private void handlePeteCollision() {
        Array<CollisionCell> peteCells = whichCellsDoesPeteCover();
        filterOutNonTiledCells(peteCells);
        for (CollisionCell cell : peteCells) {
            float cellLevelX = cell.getCellX() * CELL_SIZE;
            float cellLevelY = cell.getCellY() * CELL_SIZE;
            Rectangle intersection = new Rectangle();
            Rectangle rectangle2 = new Rectangle(cellLevelX, cellLevelY, CELL_SIZE, CELL_SIZE);
            Intersector.intersectRectangles(pete.getCollisionRectangle(), rectangle2, intersection);

            if (intersection.getHeight() < intersection.getWidth()) {
                pete.setPosition(pete.getX(), intersection.getY() + intersection.getHeight());
                pete.landed();
            } else if (intersection.getWidth() < intersection.getHeight()) {
                if (intersection.getX() == pete.getX()) {
                    pete.setPosition(intersection.getX() + intersection.getWidth(), pete.getY());
                }
                if (intersection.getX() > pete.getX()) {
                    pete.setPosition(intersection.getX() - Pete.WIDTH, pete.getY());
                }
            }
        }
    }

    private void populateAcorns() {
        MapLayer mapLayer = tiledMap.getLayers().get("Collectables");
        for (MapObject mapObject : mapLayer.getObjects()) {
            Texture texture = PeteGame.assetManager.get("acorn.png", Texture.class);
            Acorn value = new Acorn(texture,
                    mapObject.getProperties().get("x", Float.class),
                    mapObject.getProperties().get("y", Float.class)
            );
            acorns.add(value);
        }
    }

    private void handlePeteCollisionWithAcorn() {
        for (Iterator<Acorn> iter = acorns.iterator(); iter.hasNext(); ) {
            Acorn acorn = iter.next();
            if (pete.getCollisionRectangle().
                    overlaps(acorn.getCollision())) {
                PeteGame.assetManager.get("acorn.wav", Sound.class).play();
                iter.remove();
            }
        }
    }

    private void updateCameraX() {
        TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer)
                tiledMap.getLayers().get(0);
        float levelWidth = tiledMapTileLayer.getWidth() *
                tiledMapTileLayer.getTileWidth();
        if ((pete.getX() > WORLD_WIDTH / 2f) && (pete.getX() <
                (levelWidth - WORLD_WIDTH / 2f))) {
            camera.position.set(pete.getX(), camera.position.y,
                    camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);

        }
    }
}
