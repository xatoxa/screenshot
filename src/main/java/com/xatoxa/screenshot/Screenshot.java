package com.xatoxa.screenshot;

import java.awt.*;

public class Screenshot {
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public void setPressedCoords(int x, int y){
        this.x1 = x;
        this.y1 = y;
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
