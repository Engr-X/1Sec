/*
 * Copyright (c) 2025, [1Sec team]. All rights reserved.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

package com.wangdi.onesec.views;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wangdi.onesec.R;
import com.wangdi.onesec.core.BasicActivity;
import com.wangdi.onesec.core.ViewManager;
import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.data.Recorder;
import com.wangdi.onesec.utils.BasicUtils;


import java.util.Random;

/**
 * <p>
 * The SoloClassic class extends BasicActivity and represents a timer-based activity with various UI elements and functionality.
 * </p>
 *
 * <p>
 * Methods:
 * </p>
 *
 * <p>
 * onCreate: Initializes the activity, calling initializeViews() and retrieving screen size from preferences.
 * startTimer: Starts the timer when the user touches the screen, setting the timer state to TIMER_RUNNING and updating the timer text every frame.
 * stopTimer: Stops the timer when the user lifts their finger off the screen, calculating the difference between the current time and the aim value, and updating the UI with the result.
 * getBest: Returns the best response time of the user in SoloClassic mode as a formatted string.
 * getAverage: Returns the average value of all available responses as a formatted string.
 * getAO5, getAO12, getAO100: Return the current AO5, AO12, and AO100 values as formatted strings.
 * updateData: Updates the UI elements with the latest statistical data.
 * initViewsForNightTheme: Initializes the views for the activity in night theme mode, setting up the UI components and their properties.
 * onDestroy: Removes the long press runnable and update timer runnable from the message queue when the activity is destroyed.
 * initViewsForLightTheme: Initializes the views for the activity in light theme mode, calling initViewsForNightTheme.
 * onBackClick: Handles the click event for the back button, starting the Homepage activity.
 * </p>
 * 
 * @author Di Wang, Yichen Leng
 * @version 1.0
 */
public class SoloClassic extends BasicActivity
{
    public static final byte TIMER_STOP = 0;
    public static final byte TIMER_ACTIVE = 1;
    public static final byte TIMER_RUNNING = 2;

    private byte timerState;

    private double aim;
    private long startTime;
    private long currentTime;

    private Random rand;

    private Runnable updateTimerRunnable;
    private Runnable longPressRunnable;
    private Handler handler;
    private TextView counterText;

    /**
     * Initializes the activity, including calling {@link #initializeViews()} and retrieving the
     * screen size from the preference file.
     *
     * @param savedInstanceState the saved instance state, or null if none
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Starts the timer of SoloClassic activity. This method is called when the user touches the screen.
     *
     * <p>The timer is stopped when the user lifts their finger off the screen.</p>
     *
     * @see #stopTimer()
     */
    private void startTimer()
    {
        if (this.timerState == TIMER_ACTIVE)
        {
            this.timerState = TIMER_RUNNING;
            this.startTime = System.nanoTime();

            this.updateTimerRunnable = new Runnable()
            {
                /**
                 * Updates the timer text every frame with the current elapsed time
                 */
                @Override
                @SuppressLint("DefaultLocale")
                public void run()
                {
                    currentTime = System.nanoTime() - startTime;
                    double elapsedTimeSeconds = currentTime / 1_000_000_000.0;
                    runOnUiThread(() -> counterText.setText(String.format("%.4f", elapsedTimeSeconds)));
                    handler.postDelayed(this, 0);
                }
            };

            this.handler.post(updateTimerRunnable);
        }
    }

    /**
     * Stops the timer of SoloClassic activity. This method is called when the user lifts their finger off the screen.\
     *
     * <p>
     * The method first stops the timer by calling {@link Handler#removeCallbacks(Runnable)} on the {@link #updateTimerRunnable}
     * and then calculates the difference between the current time and the aim value. The absolute value of the difference
     * is formatted as a string and displayed in the "Difference" field of the UI. The method also formats the average value
     * of the past 10 differences and displays it in the "Average" field of the UI.
     * </p>
     *
     * @see #startTimer()
     */
    @SuppressLint("DefaultLocale")
    private void stopTimer()
    {
        this.timerState = TIMER_STOP;
        this.handler.removeCallbacks(updateTimerRunnable);
        final double difference = this.aim - this.currentTime / 1_000_000_000.0;
        final double absDifference = Math.abs(difference);
        final String temp = difference < 0 ? String.format("+%.4f", absDifference) : String.format("-%.4f", absDifference);

        new Thread(() ->
        {
            try {((Recorder.NumberResponse)(this.classicalRecorder)).addData(difference);} catch (Exception e)
            {this.logger.error(BasicUtils.getStackTraceAsString(e));}

            this.aim = this.rand.nextInt(50001) / 10000.0;

            this.runOnUiThread(() ->
            {
                new ViewManager(SoloClassic.this, R.id.solo_classic_timer_aim)
                        .setText(R.string.solo_classic_timer_aim).translateText().formatText(aim).applyChange();

                new ViewManager(SoloClassic.this, R.id.solo_classic_timer_difference)
                        .setText(R.string.solo_classic_timer_difference).formatText(temp).applyChange();

                new ViewManager(SoloClassic.this, R.id.solo_classic_timer_average)
                        .setText(R.string.solo_classic_timer_average).formatText(this.getAverage()).translateText().applyChange();

                updateData();
            });
        }).start();
    }

    /**
     * Returns the best response time of the user in SoloClassic mode formatted as a string.
     * If an error occurs, returns "-"
     *
     * @return                  the best response time formatted as a string, or "-" if an error occurs
     */
    @SuppressLint("DefaultLocale")
    private String getBest()
    {
        try
        {
            final Object o = ((Recorder.NumberResponse)(this.classicalRecorder)).all.best;
            final double value = ((Data.NumberResponse)(o)).getTime();
            return String.format("%.4f", Math.abs(value));
        }
        catch (Exception e) {return "-";}
    }

    /**
     * Returns the average value of the all available of the response.
     * If error occurs, return "-"
     * 
     * @return                  the average value, or "-" if error occurs
     */
    @SuppressLint("DefaultLocale")
    private String getAverage()
    {
        try
        {
            final Object o = ((Recorder.NumberResponse)(this.classicalRecorder)).all.average;
            final double value = ((Number)(o)).doubleValue();
            return String.format("%.4f", Math.abs(value));
        }
        catch (Exception e) {return "-";}
    }

    /**
     * Returns the current AO5 value formatted as a string.
     * If an error occurs, returns "-".
     *
     * @return                  the formatted AO5 value, or "-" if an error occurs
     */
    @SuppressLint("DefaultLocale")
    private String getAO5()
    {
        try
        {
            final Double value = ((Recorder.NumberResponse)(this.classicalRecorder)).AO5.current;
            return String.format("%.4f", Math.abs(value));
        }
        catch (Exception e) {return "-";}
    }

    /**
     * Returns the current AO12 value formatted as a string.
     * If an error occurs, returns "-".
     *
     * @return                  the formatted AO5 value, or "-" if an error occurs
     */
    @SuppressLint("DefaultLocale")
    private String getAO12()
    {
        try
        {
            final Double value = ((Recorder.NumberResponse)(this.classicalRecorder)).AO12.current;
            return String.format("%.4f", Math.abs(value));
        }
        catch (Exception e) {return "-";}
    }

    /**
     * Returns the current AO100 value formatted as a string.
     * If an error occurs, returns "-".
     *
     * @return                  the formatted AO5 value, or "-" if an error occurs
     */
    @SuppressLint("DefaultLocale")
    private String getAO100()
    {
        try
        {
            final Double value = ((Recorder.NumberResponse)(this.classicalRecorder)).AO100.current;
            return String.format("%.4f", Math.abs(value));
        }
        catch (Exception e) {return "-";}
    }

    /**
     * Updates the UI elements with the latest statistical data.
     *
     * <p>This method retrieves the best, AO5, AO12, and AO100 values and updates
     * their respective text views. Each value is formatted, translated, and the
     * changes are applied to the UI.</p>
     */
    private void updateData()
    {
        new ViewManager(this, R.id.solo_classic_data_best).setText(R.string.solo_classic_data_best).formatText(this.getBest()).translateText().applyChange();
        new ViewManager(this, R.id.solo_classic_data_current_ao5).setText(R.string.solo_classic_data_current_ao5).formatText(this.getAO5()).translateText().applyChange();
        new ViewManager(this, R.id.solo_classic_data_current_ao12).setText(R.string.solo_classic_data_current_ao12).formatText(this.getAO12()).translateText().applyChange();
        new ViewManager(this, R.id.solo_classic_data_current_ao100).setText(R.string.solo_classic_data_current_ao100).formatText(this.getAO100()).translateText().applyChange();
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets, when
     * the system is in dark theme mode. This method is abstract and should be
     * implemented by subclasses to set up the user interface components
     * specific to the activity. The method is called in the onCreate() method
     * of the activity, when the system is in dark theme mode.
     */
    @Override
    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    protected void initViewsForNightTheme()
    {
        this.rand = new Random();
        this.aim = this.rand.nextInt(50001) / 10000.0;

        this.setLightTheme();
        this.setContentView(R.layout.solo_classical);

        new ViewManager(this, R.id.classic_solo).applyChange();

        new ViewManager(this, R.id.solo_classic_head).setSize(1.0, 0.07).applyChange();
        new ViewManager(this, R.id.solo_classic_head_back).setSize(ViewManager.RELATIVE_TO_HEIGHT, 0.5).applyChange();
        new ViewManager(this, R.id.solo_classic_head_title).translateText().setTextSize(this.screenWidth / 20.0).setTextColor(191, 191, 191).applyChange();
        new ViewManager(this, R.id.solo_classic_head_help).setSize(ViewManager.RELATIVE_TO_HEIGHT, 0.4).applyChange();
        new ViewManager(this, R.id.solo_classic_head_line).setSize(1.0, 0.01).applyChange();

        new ViewManager(this, R.id.solo_classic_timer).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.9).applyChange();
        new ViewManager(this, R.id.solo_classic_timer_counter).setTextFamily("consola").formatText(0.0).setTextSizeBias(0.72).setTextColor(Color.WHITE).applyChange();
        new ViewManager(this, R.id.solo_classic_timer_aim).translateText().formatText(this.aim).setTextSizeBias(0.35).setTextColor(Color.WHITE).applyChange();
        new ViewManager(this, R.id.solo_classic_timer_difference).formatText("-").setTextSize(this.screenWidth / 20.0).setTextColor(Color.WHITE).applyChange();
        new ViewManager(this, R.id.solo_classic_timer_average).formatText(this.getAverage()).translateText().setTextSize(screenWidth / 20.0).setTextColor(Color.WHITE).applyChange();

        final int dataColor = Color.rgb(137, 135, 135);
        new ViewManager(this, R.id.solo_classic_data).setSize(1.0, 0.3).applyChange();
        new ViewManager(this, R.id.solo_classic_data_best).formatText(this.getBest()).translateText().setTextColor(dataColor).setTextSize(screenWidth / 20.0).applyChange();
        new ViewManager(this, R.id.solo_classic_data_current_ao5).formatText(this.getAO5()).translateText().setTextColor(dataColor).setTextSize(screenWidth / 20.0).applyChange();
        new ViewManager(this, R.id.solo_classic_data_current_ao12).formatText(this.getAO12()).translateText().setTextColor(dataColor).setTextSize(screenWidth / 20.0).applyChange();
        new ViewManager(this, R.id.solo_classic_data_current_ao100).formatText(this.getAO100()).translateText().setTextColor(dataColor).setTextSize(screenWidth / 20.0).applyChange();

        this.handler = new Handler();
        this.counterText = this.findViewById(R.id.solo_classic_timer_counter);
        this.longPressRunnable = () -> {this.counterText.setTextColor(Color.WHITE); this.timerState = TIMER_ACTIVE;};

        final ConstraintLayout touchArea = this.findViewById(R.id.solo_classic_timer);
        touchArea.setOnTouchListener(new View.OnTouchListener()
        {
            /**
             * Handles touch events on the timer area of the SoloClassic activity.
             *
             * <p>
             * When the user touches down on the timer area, the timer text is set to
             * red color and a delayed runnable is posted to be executed after a
             * short delay. If the user lifts their finger off the screen before the
             * delayed runnable is executed, the timer text is reset to white color
             * and the runnable is removed from the handler's message queue.
             * </p>
             *
             * <p>
             * If the delayed runnable is executed, the timer is started if it is not
             * already running. If the timer is already running when the user lifts
             * their finger off the screen, the timer is stopped.
             * </p>
             *
             * @param v         the view that was touched
             * @param event     the touch event
             * 
             * @return true     if the touch event is handled, false otherwise
             */
            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        counterText.setTextColor(Color.RED);
                        handler.postDelayed(longPressRunnable, 600);
                        break;
                    }

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    {
                        handler.removeCallbacks(longPressRunnable);
                        counterText.setTextColor(Color.WHITE);
                        if (timerState == TIMER_RUNNING) stopTimer();
                        else if (timerState == TIMER_ACTIVE) startTimer();
                        break;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Removes the long press runnable and the update timer runnable from the
     * message queue when the activity is being destroyed. This is to prevent
     * memory leaks and to make sure that the activity can be garbage collected
     * properly.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.handler.removeCallbacks(this.longPressRunnable);
        this.handler.removeCallbacks(this.updateTimerRunnable);
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets, when
     * the system is in light theme mode. This method is abstract and should be
     * implemented by subclasses to set up the user interface components
     * specific to the activity. The method is called in the onCreate() method
     * of the activity, when the system is in light theme mode.
     */
    @Override
    protected void initViewsForLightTheme()
    {
        this.initViewsForNightTheme();
    }

    /**
     * Handles the click event for the back button. This method is called when the
     * user clicks on the back button in the SoloClassic activity. It starts the
     * Homepage activity.
     *
     * @param v                 the view that was clicked
     */
    public void onBackClick(View v)
    {
        this.startActivity(Homepage.class);
    }
}