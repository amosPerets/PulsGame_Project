package com.amos.pulsgameproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CircleView.CircleViewListener{

    public static final int DEFUALT_RADIUS       = 30;
    public static final int INTERVAL             = 2;
    public static final int SECOND_BY_COUNTER    = 500;
    public static final int COUNTER_SET_CIRCLES  = 20;


    private Timer                   timerNewCircle;
    private MyTimerTask             myTimerTask;

    private TextView                textViewIncreasePassedCircle;
    private CircleView              circleView;

    private int counterRun          = 0;
    private int countPassed         = 0;
    private int x,y;

    private boolean isStartGame     = false;
    private FloatingActionButton    fab;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        circleView = findViewById(R.id.circle);
        circleView.setCircleViewListener(this);

        myTimerTask = new MyTimerTask();
        timerNewCircle = new Timer();

        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(isStartGame){
                    x = (int)motionEvent.getX();
                    y = (int)motionEvent.getY();

                    boolean onCircle = circleView.isOnCircle(x, y);
                    boolean onMainCircle = circleView.isOnMainCircle(x, y);

                    // Check if the user up tje finger or touch the circles or main circle
                    if((motionEvent.getAction() == MotionEvent.ACTION_UP) || onCircle || onMainCircle){
                        isStartGame = false;
                        showAlertDialogFinish();
                        timerNewCircle.cancel();
                        myTimerTask.cancel();
                    }

                }

                return true;
            }
        };

        circleView.setOnTouchListener(listener);

        textViewIncreasePassedCircle = (TextView)findViewById(R.id.time1);

         fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }


    /**
     * Notify circle passed
     */
    @Override
    public void circlePassed() {

        //Start delay for update boolean isStartGame.
        new CountDownTimer(100, 50){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if(isStartGame){
                    countPassed++;
                    textViewIncreasePassedCircle.setText(String.format(getString(R.string.circles_passed),countPassed));
                }
            }
        }.start();

    }

    /**
     * Set the first circle and isStartGame and start the timer
     */
    private void startGame(){
        isStartGame = true;
        circleView.drawCircleData(getCircleData(circleView.getNumberHoles()));
        timerNewCircle.schedule(myTimerTask, 0, INTERVAL);
        fab.setVisibility(View.INVISIBLE);
    }

    /**
     * get circle data with number of holes anf radius
     * @param numberOfCircle
     * @return
     */
    private CircleView.CircleData getCircleData(int numberOfCircle){
        CircleView.CircleData circleData = null;
        int[][] angleList = new int[numberOfCircle][2];

        for(int i = 0; i < numberOfCircle; i++){

            int angleStart = circleView.getAngleStartHole();
            int rangeAngle = circleView.getNumberOfRangeHole();

            angleList[i][0] = angleStart;
            angleList[i][1] = rangeAngle;
        }
        circleData = new CircleView.CircleData(DEFUALT_RADIUS, angleList);

        return circleData;
    }

    /**
     * Show the AlertDialog if the game finished
     */
    private void showAlertDialogFinish(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

        String circlePassed = getString(R.string.passed) + String.format(" %d ",countPassed) + getString(R.string.circles);
        builder1.setMessage(getString(R.string.game_over) + "\n" + circlePassed);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                getString(R.string.play_again),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(refresh);
                        finish();

                    }
                });

        builder1.setNegativeButton(
                getString(R.string.exit),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    //=====================================================================
    // Inner Class
    //=====================================================================

    /**
     * Class for Timer
     * In the run method add new circle if passed one second and increase the circles.
     */
    class MyTimerTask extends TimerTask {

        final Handler handler = new Handler();

        @Override
        public void run() {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    counterRun++;

                    if(counterRun % COUNTER_SET_CIRCLES == 0){
                        circleView.setCircleDataList(x, y);
                    }

                    if(counterRun == SECOND_BY_COUNTER){
                        circleView.drawCircleData(getCircleData(circleView.getNumberHoles()));
                        counterRun = 0;
                    }

                }
            });

        }
    }

}
