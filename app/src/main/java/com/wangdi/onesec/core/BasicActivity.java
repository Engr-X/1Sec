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
 */

package com.wangdi.onesec.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wangdi.onesec.R;
import com.wangdi.onesec.utils.Constants;
import com.wangdi.onesec.utils.DirectoryManager;
import com.wangdi.onesec.utils.Logger;

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
    protected boolean isDarkTheme;

    protected int screenHeight;
    protected int screenWidth;

    protected DirectoryManager internal;
    protected DirectoryManager external;

    protected Logger logger;
    protected Resources resources;
    protected SharedPreferences preferences;

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

        this.resources = this.getResources();

        this.isDarkTheme = this.isSystemDarkTheme();
        if (isDarkTheme) this.setDarkTheme();
        else this.setLightTheme();

        final DisplayMetrics displayMetrics = this.resources.getDisplayMetrics();
        this.screenWidth =  displayMetrics.widthPixels;
        this.screenHeight = displayMetrics.heightPixels;

        this.internal = DirectoryManager.getInternal(this);
        this.external = DirectoryManager.getExternal(this);

        try
        {
            this.logger = this.getLogger();

            if (!I18n.isInit() && I18n.init(this) == null)
                throw new RuntimeException("Cannot read language file");
        }
        catch (Exception e)
        {

        }

        this.preferences = this.getSharedPreferences(Constants.GLOBAL_PREFERENCE_KEY, Context.MODE_PRIVATE);

        this.initializeViews();
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets.
     * This method is abstract and should be implemented by subclasses
     * to set up the user interface components specific to the activity.
     */
    protected abstract void initializeViews();

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
    protected void setDarkTheme()
    {
        this.setTheme(R.style.Theme_1Sec_Dark);
    }

    /**
     * Returns whether the system is in dark theme mode.
     *
     * @return  true if the system is in dark theme mode, false otherwise
     */
    protected boolean isSystemDarkTheme()
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
}