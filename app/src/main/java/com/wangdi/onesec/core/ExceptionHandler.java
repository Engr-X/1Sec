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

import android.app.Activity;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * This class aimed to handle exceptions while using the app, it will pop up a window with stack trace
 * and a copy button to send the stack trace information.
 *
 *
 * @author Di Wang
 * @version 1.0
 */
public final class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final WeakReference<Activity> activityRef;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Object lock = new Object();

    public ExceptionHandler(Activity activity)
    {
        this.activityRef = new WeakReference<>(activity);
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable)
    {

    }
}
