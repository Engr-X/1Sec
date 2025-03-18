package com.wangdi.onesec.views;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.wangdi.onesec.R;
import com.wangdi.onesec.core.BasicActivity;
import com.wangdi.onesec.core.ExceptionHandler;

public final class HomePage extends BasicActivity
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
    }

    /**
     * Initializes the views for the activity eg: position, size of widgets.
     * This method is abstract and should be implemented by subclasses
     * to set up the user interface components specific to the activity.
     */
    @Override
    protected void initializeViews()
    {
        this.setContentView(R.layout.home_page);
    }
}