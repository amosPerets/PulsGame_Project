package com.amos.pulsgameproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CircleView extends View {

    private static final String COLOR_HEX = "#E74300";

    private final int WIDTH                 = getResources().getDisplayMetrics().widthPixels;
    private final int HEIGHT                =  getResources().getDisplayMetrics().heightPixels;

    private final int RADIUS                = ((WIDTH / 2) - 10);
    private final int CX                    = WIDTH / 2;
    private final int CY                    = HEIGHT / 2;
    private final int INCREASE_RADIUS_BY    = 5;
    private final int LIMIT                 = RADIUS - 10;
    private final int DELTA                 = 5;

    private Paint                       drawPaint;
    private List<CircleData>            circleDataList;
    private Map<Integer, CircleData>    radiusMap;
    private CircleViewListener          circleViewListener;


    public CircleView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.circleDataList = new ArrayList<>();
        this.radiusMap  = new HashMap<>();
        setupPaint();
    }

    /**
     * Draw circles with holes
     * @param canvas
     */
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        clear(canvas);
        //Limit
        circle(canvas);
        canvas.getClipBounds().set(500, 100, 500,100);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(Color.RED);
        // Use Color.parseColor to define HTML colors
        drawPaint.setColor(Color.parseColor("#CD5C5C"));
        int x = WIDTH/2;
        int y = HEIGHT/2;
        RectF oval = new RectF();
        for (CircleData circleData : circleDataList) {
            float radius = circleData.radius;
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setColor(Color.parseColor("#CD5C5C"));
            canvas.drawCircle(x, y, radius, drawPaint);
            drawPaint.setColor(Color.WHITE);

            oval.set(x - radius, y - radius, x + radius, y + radius);
            for (int[] angle : circleData.angleList) {
                canvas.drawArc(oval, (float) angle[0], (float) angle[1], false, drawPaint);
            }
        }

    }

    /**
     * Draw main circle
     * @param canvas
     */
    private void circle(Canvas canvas){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);

        canvas.drawCircle(WIDTH/2, HEIGHT/2, RADIUS , paint);

    }


    /**
     * Clear the board
     * @param canvas
     */
    private void clear (Canvas canvas) {
         drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), drawPaint);

    }


    /**
     * Setup paint with color and stroke styles
     */
    private void setupPaint() {
        drawPaint = new Paint();
        drawPaint.setColor(Color.parseColor("#CD5C5C"));
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Add to circleDataList and draw
     * @param circleData
     */
    public void drawCircleData (CircleData circleData) {
        this.circleDataList.add(circleData);
        invalidate();
    }


    /**
     * Increase the circles and check if the user(finger) passed the circles
     * @param x
     * @param y
     */
    public void setCircleDataList(int x, int y){
        radiusMap.clear();
        for (int i = 0; i < circleDataList.size(); i++){
            CircleData circleData = this.circleDataList.get(i);
            if( ((circleData.radius) + INCREASE_RADIUS_BY) < LIMIT ){
                circleData.radius = circleData.radius + INCREASE_RADIUS_BY;
                radiusMap.put(circleData.radius, circleData);

                if(!circleData.isChecked){
                    if(isCirclePassed(x,y,circleData.radius) ){
                        circleData.setChecked(true);
                        if(null != circleViewListener){
                            circleViewListener.circlePassed();
                        }
                    }
                }

            } else {
                this.circleDataList.remove(i);
            }
        }
        invalidate();
    }

    /**
     * Checked if the x&y(point) passed the circle.
     * @param x
     * @param y
     * @param circleRadius
     * @return true- passed false- not passed
     */
    public boolean isCirclePassed(int x, int y, int circleRadius){

        if(getDistance(x, y) < circleRadius){
            return true;
        }

        return false;
    }

    /**
     * Check if touch on circles or not
     * @param x
     * @param y
     * @return
     */
    public boolean isOnCircle(int x, int y){
        int distance = getDistance(x,y);
        double xyAngleF = Math.atan2((double)(y - CY), (double)(x - CX));;  //Radians in the normal Coordinate axis
        // Because the Coordinate axis in phone is opposite we habe to translate it
        double theta =  (double)Math.toDegrees(xyAngleF); // Degrees
        if (theta < 0) {
            theta = 360 + theta;
        }

        Log.i("***", String.format("\n\n(%d,%d) distance: %d  xtAngleF: %f   xyAngle: %f",x,y,distance,xyAngleF,theta)) ;



        for (int radius : radiusMap.keySet()) {
            if ((distance >= radius - DELTA ) && (distance <= radius + DELTA)) {

                CircleData circleData = radiusMap.get(radius);

                int [][] angleList = circleData.getAngleList();

                for(int[] anglePair : angleList){

                    int angle1 = anglePair[0];
                    int angle2 = anglePair[1] + angle1;
//                    int angle2 = anglePair[1];

                    Log.i("***", "\n\nxyAngle: " + theta);
                    Log.i("***", "angle1: " + angle1);
                    Log.i("***", "angle2: " + angle2);

                    if(theta >= angle1 && theta <= angle2){
                        Log.i("***", "result: ***false***");
                        return false;
                    }

                }
                Log.i("***", "result:                           ***true***");
                return true;
            }
        }
       Log.i("***", "result: ***false***");
        return false;
    }


    /**
     * Check if touch on main circle
     * @param x
     * @param y
     * @return true- touch false- not touch
     */
    public boolean isOnMainCircle(int x, int y){
        int distance = getDistance(x,y);
        if ((distance >= RADIUS - DELTA ) && (distance <= RADIUS + DELTA)) {
            return true;
        }

        return false;
    }



    /**
     * Get distance from a center point(radius)
     * @param x
     * @param y
     * @return ditance
     */
    public int getDistance(int x, int y){

        x = (int) Math.pow(CX - x, 2);
        y = (int) Math.pow(CY - y, 2);

        int distance = (int) Math.sqrt(x + y);

        return distance;
    }


    /**
     * Get number of range angle on circle(for hole)
     * @return
     */
    public int getNumberOfRangeHole(){
        Random rand = new Random();
        int  rangeAngle;
        rangeAngle = rand.nextInt(50) +30;
        return rangeAngle;
    }


    /**
     * Get the start angle on circle(for hole)
     * @return
     */
    public int getAngleStartHole(){
        Random rand = new Random();
        int  angleStart;
        angleStart = rand.nextInt(360) + 1;

        return angleStart;
    }

    /**
     * Number of holes on circle
     * @return
     */
    public int getNumberHoles(){
        Random rand = new Random();
        int  numberOfHoles;
        numberOfHoles = rand.nextInt(3) + 1;
        return numberOfHoles;
    }

    //=====================================================================
    // Inner Class
    //=====================================================================

    /**
     * Class save the radius of circle and degrees of holes
     */
    public static class CircleData {
        private int radius;
        private int[][] angleList;
        private boolean isChecked = false;

        public CircleData (int radius, int[] ... angle) {
            this.radius = radius;
            this.angleList = (angle == null ? new int[0][2] : angle);
        }

        public int[][] getAngleList() {
            return angleList;
        }


        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }

    public void setCircleViewListener(CircleViewListener circleViewListener) {
        this.circleViewListener = circleViewListener;
    }

    public interface CircleViewListener{
        public void circlePassed();
    }
}