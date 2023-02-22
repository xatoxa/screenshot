package com.xatoxa.screenshot;

import javafx.geometry.Rectangle2D;

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

    public int getW(){
        return Math.max(x1, x2) - Math.min(x1, x2);
    }

    public int getH(){
        return Math.max(y1, y2) - Math.min(y1, y2);
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

    public Rectangle2D getRectangle(){
        return new Rectangle2D(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.max(x1, x2) - Math.min(x1, x2),
                Math.max(y1, y2) - Math.min(y1, y2));
    }
}
