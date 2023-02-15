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
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getSceneX1() {
        return sceneX1;
    }

    public int getSceneY1() {
        return sceneY1;
    }

    public void setPressedCoords(int x, int y, int sceneX, int sceneY){
        this.x1 = x;
        this.y1 = y;
        this.sceneX1 = sceneX;
        this.sceneY1 = sceneY;
    }

    public void setReleasedCoords(int x, int y){
        this.x2 = x;
        this.y2 = y;
    }

    public Rectangle getRectangle(){
        Rectangle rect = new Rectangle();
        if (x2 < x1){
            rect.x = x2;
            rect.width = x1 - x2;
        } else if (x2 > x1){
            rect.x = x1;
            rect.width = x2 - x1;
        }
        if (y2 < y1){
            rect.y = y2;
            rect.height = y1 - y2;
        } else if (y2 > y1){
            rect.y = y1;
            rect.height = y2 - y1;
        }

        return rect;
    }
}
