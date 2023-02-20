package com.xatoxa.screenshot;

import java.awt.*;

public class ScreenshotRect {
    private int x1;
    private int y1;
    private int sceneX1;
    private int sceneY1;
    private int x2;
    private int y2;

    public int getX1() {
        return Math.min(x1, x2);
    }

    public int getY1() {
        return Math.min(y1, y2);
    }

    public int getSceneX1() {
        return sceneX1;
    }

    public int getSceneY1() {
        return sceneY1;
    }

    public void setPressedCoordinates(int x, int y, int sceneX, int sceneY){
        //FIXME написать min/max функции сюда, а не в get
        this.x1 = x;
        this.y1 = y;
        this.sceneX1 = sceneX;
        this.sceneY1 = sceneY;
    }

    public void setReleasedCoordinates(int x, int y){
        this.x2 = x;
        this.y2 = y;
    }

    public Rectangle getRectangle(){
        Rectangle rect = new Rectangle();
        rect.x = Math.min(x1, x2);
        rect.y = Math.min(y1, y2);
        rect.width = Math.max(x1, x2) - rect.x;
        rect.height = Math.max(y1, y2) - rect.y;

        return rect;
    }
}
