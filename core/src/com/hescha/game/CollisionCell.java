package com.hescha.game;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class CollisionCell {
    private final TiledMapTileLayer.Cell cell;
    private final int cellX;
    private final int cellY;

    public CollisionCell(TiledMapTileLayer.Cell cell, int cellX, int cellY) {
        this.cell = cell;
        this.cellX = cellX;
        this.cellY = cellY;
    }

    public boolean isEmpty() {
        return cell == null;
    }

    public TiledMapTileLayer.Cell getCell() {
        return cell;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
    }
}
