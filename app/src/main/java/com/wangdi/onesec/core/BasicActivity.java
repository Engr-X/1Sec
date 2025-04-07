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

package com.wangdi.onesec.core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wangdi.onesec.R;
import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.data.Recorder;
import com.wangdi.onesec.utils.BasicUtils;
import com.wangdi.onesec.utils.Constants;
import com.wangdi.onesec.utils.FileHelper;

import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * <p>
 * BasicActivity is an abstract class that extends AppCompatActivity and
 * provides a basic structure for activities in an Android application.
 * </p>
 *
 * <p>
 * Note that this class is designed to be extended by other activities,
 * which will implement the abstract methods initializeViews() and getLogger().
 * </p>
 *
 * @author Di Wang
 * @version 1.0
 */
public abstract class BasicActivity extends AppCompatActivity
{
    private Handler mHandler;

    public boolean isNightTheme;

    public int screenHeight;
    public int screenWidth;
    public float scaledDensity;

    public DirectoryManager internal;
    public DirectoryManager external;

    public File soloDirectory;
    public File multiplayerDirectory;

    public Logger logger;
    public Resources resources;
    public SharedPreferences preferences;

    public Recorder classicalRecorder;
    public Recorder challengeRecorder;
    public Recorder blindRecorder;

    public ConfigManager userConfigs;
    public ConfigManager userSettings;
    public ConfigManager gameData;

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

        this.mHandler = new Handler(Looper.getMainLooper());

        this.resources = this.getResources();

        this.isNightTheme = this.isSystemNightTheme();
        if (isNightTheme) this.setNightTheme();
        else this.setLightTheme();

        final DisplayMetrics metrics = this.resources.getDisplayMetrics();
        this.screenWidth =  metrics.widthPixels;
        this.screenHeight = metrics.heightPixels;
        this.scaledDensity = metrics.scaledDensity;

        this.internal = DirectoryManager.getInternal(this);
        this.external = DirectoryManager.getExternal(this);

        this.soloDirectory = new File(this.internal.dataDirectory, "solo");
        this.multiplayerDirectory = new File(this.internal.dataDirectory, "multiplayer");

        try
        {
            this.logger = this.getLogger();
        }
        catch (Exception e)
        {

        }

        this.preferences = this.getSharedPreferences(Constants.GLOBAL_PREFERENCE_KEY, Context.MODE_PRIVATE);

        new Thread(() ->
        {
            try
            {
                if (!this.soloDirectory.exists() && !FileHelper.createNewDirectories(this.soloDirectory))
                    throw new IOException("Create directory false: " + this.soloDirectory.getAbsolutePath());

                if (this.multiplayerDirectory.exists() && !FileHelper.createNewDirectories(this.multiplayerDirectory))
                    throw new IOException("Create directory false: " + this.multiplayerDirectory.getAbsolutePath());

                Pair<ConfigManager, Boolean> temp = ConfigManager.autoSync(this.internal.dataDirectory, ConfigManager.USER_CONFIGS_FILE_NAME);
                this.userConfigs = temp.getValue0();
                if (temp.getValue1()) this.initUserConfig();

                temp = ConfigManager.autoSync(this.internal.dataDirectory, ConfigManager.APP_SETTINGS_FILE_NAME);
                this.userSettings = temp.getValue0();
                if (temp.getValue1()) this.initSettings();

                temp = ConfigManager.autoSync(this.internal.dataDirectory, ConfigManager.GAME_DATA_FILE_NAME);
                this.gameData = temp.getValue0();
                if (temp.getValue1()) this.initGameData();

                if (!I18n.isInit())
                {
                    if (I18n.init(this) == null)
                        throw new RuntimeException("Cannot read language file");
                }

                this.classicalRecorder = Recorder.autoSync(new File(this.soloDirectory, "classic"), 1000, Data.NUMBER_RESPONSE_TYPE);
                this.challengeRecorder = Recorder.autoSync(new File(this.soloDirectory, "challenge"), 1000, Data.NUMBER_RESPONSE_TYPE);
                this.blindRecorder = Recorder.autoSync(new File(this.soloDirectory, "blind"), 1000, Data.NUMBER_RESPONSE_TYPE);

                System.out.println(this.classicalRecorder);
                System.out.println(this.challengeRecorder);
                System.out.println(this.blindRecorder);
            }
            catch (Exception e) {this.logger.fatal(BasicUtils.getStackTraceAsString(e));}

            mHandler.post(this::initializeViews);
        }).start();
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets.
     * This method is abstract and should be implemented by subclasses
     * to set up the user interface components specific to the activity.
     */
    protected void initializeViews()
    {
        if (this.isNightTheme) this.initViewsForNightTheme();
        else this.initViewsForLightTheme();
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets, when
     * the system is in dark theme mode. This method is abstract and should be
     * implemented by subclasses to set up the user interface components
     * specific to the activity. The method is called in the onCreate() method
     * of the activity, when the system is in dark theme mode.
     */
    protected abstract void initViewsForNightTheme();

    /**
     * Initializes the views for the activity eg: position, size of widgets, when
     * the system is in light theme mode. This method is abstract and should be
     * implemented by subclasses to set up the user interface components
     * specific to the activity. The method is called in the onCreate() method
     * of the activity, when the system is in light theme mode.
     */
    protected abstract void initViewsForLightTheme();

    /**
     * Sets the theme of this activity to the light theme.
     * This method is intended to be called in the onCreate() method of
     * activities that extend this class.
     */
    protected void setLightTheme()
    {
        this.setTheme(R.style.Theme_1Sec_Light);
    }

    /**
     * Sets the theme of this activity to the dark theme.
     * This method is intended to be called in the onCreate() method of
     * activities that extend this class.
     */
    protected void setNightTheme()
    {
        this.setTheme(R.style.Theme_1Sec_Dark);
    }

    /**
     * Starts an activity of type {@code clazz}, which should be a subclass of
     * {@link BasicActivity}. This method is a convenience method that
     * simplifies the process of starting an activity.
     *
     * @param clazz             the class of the activity to start
     */
    protected void startActivity(Class<? extends BasicActivity> clazz)
    {
        this.logger.info("Jump to new page: ", clazz);
        final Intent intent = new Intent(this, clazz);
        this.startActivity(intent);
    }

    /**
     * Returns whether the system is in dark theme mode.
     *
     * @return  true if the system is in dark theme mode, false otherwise
     */
    protected boolean isSystemNightTheme()
    {
        int currentNightMode = this.resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Retrieves the logger instance specific to this activity. This method is abstract
     * and must be implemented by subclasses to return their own logger instance.
     *
     * @return                  the logger instance for this activity
     */
    protected Logger getLogger() throws Exception
    {
        return Logger.create(this.internal.logDirectory, this.getClass());
    }

    /**
     * Initializes the user configuration by generating a unique user ID and name,
     * and saving them to the userConfig. This method writes the data to a file
     * asynchronously.
     *
     * @throws Exception        if an error occurs while updating the user configuration
     *                          or saving it to the file.
     */
    private void initUserConfig() throws Exception
    {
        final int id = (int)(Math.random() * Integer.MAX_VALUE);
        final String name = "Zeeeeeeeeeeeeeee";
        this.userConfigs.put(ConfigManager.USER_CONFIGS_NAME_KEY, name)
                        .put(ConfigManager.USER_CONFIGS_ID_KEY, id)
                        .saveToFile();
    }

    /**
     * Initializes the settings by generating a default settings configuration and
     * saving it to the settingsConfig. This method writes the data to a file
     * asynchronously.
     *
     * @throws Exception        if an error occurs while updating the settings configuration
     *                          or saving it to the file.
     */
    private void initSettings() throws Exception
    {
        this.userSettings.put(ConfigManager.APP_SETTINGS_LANGUAGE_KEY, I18n.getSystemLanguage())
                         .put(ConfigManager.USER_AVATAR_ID, new File(this.internal.dataDirectory, "aviator.png").getAbsolutePath())
                         .saveToFile();
    }

    /**
     * Initializes the game data by setting default values for coins and experience,
     * and saves them to the gameData configuration. This method writes the data
     * to a file asynchronously.
     *
     * @throws Exception        if an error occurs while updating the game data
     *                          or saving it to the file.
     */
    private void initGameData() throws Exception
    {
        this.gameData.put(ConfigManager.COINS_KEY, 520)
                     .put(ConfigManager.EXPERIENCE_KEY, 1314)
                     .saveToFile();
    }
}