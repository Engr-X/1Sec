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

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;

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
        System.out.println("HelloWorld");

        new Handler(Looper.getMainLooper()).post(() -> {
            System.out.println("HelloWorld1");
            Activity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) {
                defaultHandler.uncaughtException(Thread.currentThread(), throwable);
                return;
            }

            // 显示弹窗
            showCrashDialog(activity);

            // 关键：重启消息循环以处理后续事件
            Looper.loop();
        });
    }

    private void showCrashDialog(Activity activity)
    {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle("程序崩溃")
                    .setMessage("检测到崩溃，是否重启应用？")
                    .setPositiveButton("send", (d, which) -> copy())
                    .setNegativeButton("close", (d, which) -> close())
                    .create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void close()
    {

    }

    private void copy()
    {

    }
}