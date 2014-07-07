package com.vipulfb.Unjumble;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class MyAnimation extends Animation {

    int row,column;
    private float cx, cy;           // center x,y position of circular path
    private float prevX, prevY;     // previous x,y position of image during animation
    private float r;                // radius of circle
    int signX,signY;
    float angle;


    /**
     * @param r - radius of circular path
     */
    public MyAnimation(int row,int column, float r, float cx, float cy,float angle){
        this.row=row;
        this.column=column;
        this.r = r;
        this.cx=cx;
        this.cy=cy;
        this.angle=angle;
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        // set previous position to center
        switch(row){
            case 0: prevY = cy-r; break;
            case 1: prevY = cy;   break;
            case 2: prevY = cy+r; break;
        }

        switch (column){
            case 0: prevX = cx-r; break;
            case 1: prevX = cx;   break;
            case 2: prevX = cx+r; break;
        }
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if(interpolatedTime == 0){
            // I ran into some issue where interpolated would be
            return;
        }

        float angleDeg = angle +interpolatedTime * -90f;
        float angleRad = (float) Math.toRadians(angleDeg);

//        int signX=0,signY=0;

        switch(row){
            case 0: signX = +1; break;
            case 1:
                if (column == 0){signX = +1;}else{signX = -1;}
                break;
            case 2: signX = -1; break;
        }

        switch (column){
            case 0: signY = -1; break;
            case 1:if (row == 0){signY = +1;}else{signY = -1;}
            break;
            case 2: signY = +1; break;
        }

        // r = radius, cx and cy = center point, a = angle (radians)
        float x = (float) (prevX + r * Math.cos(angleRad));
        float y = (float) (prevY+ r * Math.sin(angleRad));


        float dx = x - prevX;
        float dy = y - prevY;

        prevX = x;
        prevY = y;

        t.getMatrix().setTranslate(dx, dy);
    }
}