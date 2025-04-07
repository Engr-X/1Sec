/*
 *                        _oo0oo_
 *                       o8888888o
 *                       88" . "88
 *                       (| -_- |)
 *                       0\  =  /0
 *                     ___/`---'\___
 *                   .' \\|     |// '.
 *                  / \\|||  :  |||// \
 *                 / _||||| -:- |||||- \
 *                |   | \\\  - /// |   |
 *                | \_|  ''\---/''  |_/ |
 *                \  .-\__  '-'  ___/-. /
 *              ___'. .'  /--.--\  `. .'___
 *           ."" '<  `.___\_<|>_/___.' >' "".
 *          | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *          \  \ `_.   \_ __\ /__ _/   .-` /  /
 *      =====`-.____`.___ \_____/___.-`___.-'=====
 *                        `=---='
 * 
 * 
 *      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 *            佛祖保佑     永不宕机     永无BUG
 * 
 *        佛曰:  
 *                写字楼里写字间，写字间里程序员；  
 *                程序人员写程序，又拿程序换酒钱。  
 *                酒醒只在网上坐，酒醉还来网下眠；  
 *                酒醉酒醒日复日，网上网下年复年。  
 *                但愿老死电脑间，不愿鞠躬老板前；  
 *                奔驰宝马贵者趣，公交自行程序员。  
 *                别人笑我忒疯癫，我笑自己命太贱；  
 *                不见满街漂亮妹，哪个归得程序员？
 */

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
import android.view.View;

import androidx.annotation.Nullable;

import com.wangdi.onesec.R;
import com.wangdi.onesec.core.BasicActivity;
import com.wangdi.onesec.core.ConfigManager;
import com.wangdi.onesec.core.ViewManager;
import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.data.Recorder;

import org.json.JSONObject;

/**
 * <p>
 * The Homepage class is a final class that extends BasicActivity. It appears to be a main activity class for an Android app, responsible for setting up the user interface and handling user interactions.
 * </p>
 *
 * <p>
 * Methods:
 * </p>
 *
 * <p>
 * onCreate(Bundle savedInstanceState): Initializes the activity, including calling initializeViews() and retrieving screen size from the preference file.
 * initViewsForNightTheme(): Initializes the views for the activity in dark theme mode, setting up the layout and styling of various UI components.
 * initViewsForLightTheme(): Initializes the views for the activity in light theme mode, which currently just calls initViewsForNightTheme().
 * onSettingClick(View v): Handles the click event for the settings button, logging a message to the console.
 * onSoloClassicalClick(View v): Handles the click event for the solo classical button, starting the SoloClassic activity.
 * </p>
 * 
 * @author Di Wang
 * @version 1.0
 */
public final class Homepage extends BasicActivity
{
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
        System.out.println(this.screenWidth + " " + this.screenHeight + " " + this.scaledDensity);
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets, when
     * the system is in dark theme mode. This method is abstract and should be
     * implemented by subclasses to set up the user interface components
     * specific to the activity. The method is called in the onCreate() method
     * of the activity, when the system is in dark theme mode.
     */
    @Override
    @SuppressLint("DefaultLocale")
    protected void initViewsForNightTheme()
    {
        this.setLightTheme();
        this.setContentView(R.layout.homepage);

        new ViewManager(this, R.id.homepage).applyChange();

        new ViewManager(this, R.id.homepage_head).setSize(0.85, 0.07).applyChange();
        new ViewManager(this, R.id.homepage_head_app_icon).setSize(ViewManager.RELATIVE_TO_HEIGHT, 0.5).applyChange();
        new ViewManager(this, R.id.homepage_head_app_name).translateText().setTextSizeBias(0.3).setTextColor(112, 112, 112).applyChange();
        new ViewManager(this, R.id.homepage_head_settings).setSize(ViewManager.RELATIVE_TO_HEIGHT, 0.5).applyChange();

        new ViewManager(this, R.id.homepage_personal).setSize(0.94, 0.3).applyChange();
        new ViewManager(this, R.id.homepage_personal_background).setSize(1.0, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_personal_welcome_back).translateText().setTextSizeBias(0.3).setTextColor(Color.WHITE).applyChange();
        new ViewManager(this, R.id.homepage_personal_name).formatText(this.userConfigs.get(ConfigManager.USER_CONFIGS_NAME_KEY)).setTextSize(this.screenWidth / 15.0).setTextColor(Color.WHITE).applyChange();
        new ViewManager(this, R.id.homepage_personal_level).formatText(this.gameData.get(ConfigManager.EXPERIENCE_KEY)).setTextSizeBias(0.1).setTextColor(Color.WHITE).applyChange();
        new ViewManager(this, R.id.homepage_personal_avatar_background).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.26).applyChange();
        new ViewManager(this, R.id.homepage_personal_avatar).setSize(ViewManager.RELATIVE_TO_HEIGHT, 0.9).applyChange();

        String value1, value2 = String.valueOf(this.challengeRecorder.getSize()), value3;

        try
        {
            final JSONObject json = new JSONObject(((Recorder.NumberResponse)(this.classicalRecorder)).all.best.toString());
            final double value = ((Number)(json.get(Data.NUMBER_RESPONSE_TIME))).doubleValue();
            value1 = String.format("%.4f", Math.abs(value));
        }
        catch (Exception e) {value1 = "-";}

        new ViewManager(this, R.id.homepage_personal_classical_show).setSize(0.3, 0.3).applyChange();
        new ViewManager(this, R.id.homepage_personal_classical_show_text).translateText().setTextColor(Color.WHITE).setTextSize(this.screenWidth / 20.0).applyChange();
        new ViewManager(this, R.id.homepage_personal_classical_show_value).formatText(value1).setTextColor(Color.WHITE).setTextSize(this.screenWidth / 15.0).applyChange();

        new ViewManager(this, R.id.homepage_personal_challenge_show).setSize(0.3, 0.3).applyChange();
        new ViewManager(this, R.id.homepage_personal_challenge_show_text).translateText().setTextColor(Color.WHITE).setTextSize(this.screenWidth / 20.0).applyChange();
        new ViewManager(this, R.id.homepage_personal_challenge_show_value).formatText(value2).setTextColor(Color.WHITE).setTextSize(this.screenWidth / 15.0).applyChange();

        new ViewManager(this, R.id.homepage_personal_double_tap_show).setSize(0.3, 0.3).applyChange();
        new ViewManager(this, R.id.homepage_personal_double_tap_show_text).translateText().setTextColor(Color.WHITE).setTextSize(this.screenWidth / 20.0).applyChange();
        new ViewManager(this, R.id.homepage_personal_double_tap_show_value).formatText("-").setTextColor(Color.WHITE).setTextSize(this.screenWidth / 15.0).applyChange();

        final int tc = Color.rgb(83, 83, 83);
        final double textSize = this.screenWidth / 25.0;
        new ViewManager(this, R.id.homepage_solo).setSize(0.94, 0.24).applyChange();
        new ViewManager(this, R.id.homepage_solo_title).translateText().setTextSize(this.screenWidth / 20.0).setTextColor(209, 209, 209).applyChange();
        new ViewManager(this, R.id.homepage_solo_classical).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_solo_classical_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_solo_classical_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();

        new ViewManager(this, R.id.homepage_solo_challenge).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_solo_challenge_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_solo_challenge_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();

        new ViewManager(this, R.id.homepage_solo_blind).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_solo_blind_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_solo_blind_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();

        new ViewManager(this, R.id.homepage_solo_double_tap).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_solo_double_tap_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_solo_double_tap_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();


        new ViewManager(this, R.id.homepage_multiplayer).setSize(0.94, 0.24).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_title).translateText().setTextSize(this.screenWidth / 20.0).setTextColor(209, 209, 209).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_classical).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_classical_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_classical_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();

        new ViewManager(this, R.id.homepage_multiplayer_cps).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_cps_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_cps_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();

        new ViewManager(this, R.id.homepage_multiplayer_reaction).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_reaction_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_reaction_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();

        new ViewManager(this, R.id.homepage_multiplayer_blind).setSize(0.2, 0.9).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_blind_icon).setSize(ViewManager.RELATIVE_TO_WIDTH, 0.7).applyChange();
        new ViewManager(this, R.id.homepage_multiplayer_blind_text).translateText().setTextColor(tc).setTextSize(textSize).applyChange();
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
     * Handles the click event for the settings button. This method is called
     * when the user clicks on the settings button in the homepage. It logs
     * a message to the console indicating that the settings were clicked.
     *
     * @param v                 the view that was clicked
     */
    public void onSettingClick(View v)
    {
        System.out.println("Click settings");
    }

    /**
     * Handles the click event for the solo classical button. This method is called
     * when the user clicks on the solo classical button in the homepage. It starts
     * the SoloClassic activity.
     *
     * @param v                 the view that was clicked
     */
    public void onSoloClassicalClick(View v)
    {
        this.startActivity(SoloClassic.class);
    }
}