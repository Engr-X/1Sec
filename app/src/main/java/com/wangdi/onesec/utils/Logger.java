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

package com.wangdi.onesec.utils;

import static android.Manifest.permission_group.CALENDAR;

import android.annotation.SuppressLint;
import android.util.Log;

import com.wangdi.onesec.AsyncTask;
import com.wangdi.onesec.SyncTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;

/**
 * Logger class is used to print error messages or logs in console,
 * Also writing these error messages or logs into files for better management.
 * This class's method contain I / O operations, so it should not be called in Main thread.
 *
 *
 * @author Di Wang
 * @version 1.0
 */

public final class Logger
{
    public static final byte DEBUG = 0;
    public static final byte INFO = 1;
    public static final byte WARNING = 2;
    public static final byte ERROR = 3;
    public static final byte FATAL = 4;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final String DEBUG_STRING = "DEBUG";
    private static final String INFO_STRING = "INFO";
    private static final String WARNING_STRING = "WARNING";
    private static final String ERROR_STRING = "ERROR";
    private static final String FATAL_STRING = "FATAL";

    private static final String YEAR_KEY = "yyyy";
    private static final String MONTH_KEY = "MM";
    private static final String DAY_KEY = "dd";
    private static final String HOUR_KEY = "HH";
    private static final String MINUTE_KEY = "mm";
    private static final String SECOND_KEY = "ss";
    private static final String CLASS_KEY = "CLASS";
    private static final String LEVEL_KEY = "LEVEL";
    private static final String MESSAGE_KEY = "MESSAGE";

    private static final String LOGFILE_EXTENSION = ".log";
    private static final String LOG_FORMAT = "[HH:mm:ss] [CLASS/LEVEL]: MESSAGE";
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Creates a new Logger instance in a ui thread (not recommended use directly).
     * 
     * @param directory         the directory where log files are stored
     * @param clazz             the class associated with the logger
     * 
     * @return                  a new Logger instance
     * 
     * @throws Exception        if an error occurs during the creation process, mostly permission denied
     */
    @SyncTask
    private static Logger createSync(final File directory, Class<?> clazz) throws Exception
    {
        return new Logger(directory, clazz);
    }

    /**
     * Creates a new Logger instance in specific thread (recommended).
     * 
     * @param directory         the directory where log files are stored
     * @param clazz             the class associated with the logger
     * 
     * @return                  a new Logger instance
     * 
     * @throws Exception        if an error occurs during the creation process, mostly permission denied
     * @see #createSync(File, Class)
     */
    @AsyncTask
    public static Logger create(final File directory, Class<?> clazz) throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(() -> createSync(directory, clazz)));
    }

    /**
     * Converts the given level to a string, given the following mapping:
     *
     * <ul>
     *     <li>{@link #DEBUG} -> {@link #DEBUG_STRING}</li>
     *     <li>{@link #INFO} -> {@link #INFO_STRING}</li>
     *     <li>{@link #WARNING} -> {@link #WARNING_STRING}</li>
     *     <li>{@link #ERROR} -> {@link #ERROR_STRING}</li>
     *     <li>{@link #FATAL} -> {@link #FATAL_STRING}</li>
     *     <li>Otherwise -> "UNKNOWN"</li>
     * </ul>
     *
     * @param level the level to convert
     * 
     * @return the string associated with the given level
     */
    @SyncTask
    private static String levelToString(byte level)
    {
        return switch (level)
        {
            case DEBUG -> DEBUG_STRING;
            case INFO -> INFO_STRING;
            case WARNING -> WARNING_STRING;
            case ERROR -> ERROR_STRING;
            case FATAL -> FATAL_STRING;
            default -> "UNKNOWN";
        };
    }

    /**
     * Shuts down the executor service, and all the threads in it.
     *
     * <p>
     * This method is intended to be called in the main thread, before the application is
     * exited.
     * </p>
     */
    @SyncTask
    private static void shutdown()
    {
        EXECUTOR.shutdown();
    }

    private final Calendar calendar;
    private final String logFormat;
    private final File currentFile;
    private final File directory;
    private final Class<?> clazz;

    static
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutting down logger's executor...");
            shutdown();

            try
            {
                if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS))
                    EXECUTOR.shutdownNow();

            }
            catch (InterruptedException e)
            {
                EXECUTOR.shutdownNow();
            }
        }));
    }

    @SyncTask
    private Logger(final File directory, Class<?> clazz) throws Exception
    {
        this(directory, clazz, LOG_FORMAT);
    }

    @SyncTask
    private Logger(final File directory, Class<?> clazz, String format) throws Exception
    {
        this.calendar = Calendar.getInstance();
        this.clazz = clazz;
        this.logFormat = format;
        this.directory = directory;
        this.currentFile = new File(directory, DATA_FORMAT.format(new Date()) + LOGFILE_EXTENSION);

        if (!this.currentFile.exists() && !FileHelper.createNewFile(this.currentFile))
            throw new IOException("Create file false: " + this.currentFile.getAbsolutePath());
    }

    /**
     * Clears all log files in specific thread (recommended).
     * 
     * <p>
     * This method submits a task to the executor to delete all directories
     * within the logger's directory. The operation is performed asynchronously
     * and the method returns a boolean indicating the success of the operation.
     * </p>
     * 
     * @return                  true if all log files were successfully deleted; false otherwise
     * 
     * @throws Exception        if an error occurs during the deletion process
     */
    @AsyncTask
    public boolean clearAllLogs() throws Exception
    {
        return BasicUtils.handleFutureResult(EXECUTOR.submit(this::clearAllLogsSync));
    }

    /**
     * Clears all log files in ui thread (not recommended use directly).
     * 
     * @return                  true if all log files were successfully deleted; false otherwise
     * 
     * @throws Exception        if an error occurs during the deletion process
     */
    @SyncTask
    private boolean clearAllLogsSync() throws Exception
    {
        return FileHelper.deleteDirectories(this.directory, false);
    }

    /**
     * Logs the given messages at the debug level in a specific thread (recommended).
     *
     * <p>
     * This method submits a task to the executor to write the messages
     * asynchronously, improving performance by not blocking the calling
     * thread. The messages are written to both the standard output and
     * the current log file.
     * </p>
     *
     * @param messages          the messages to be logged
     */
    @AsyncTask
    public void debug(Object... messages)
    {
        EXECUTOR.submit(() -> this.debugSync(messages));
    }

    /**
     * Writes the given messages to the standard output and the current log file as
     * debug messages in ui thread (not recommended use directly).
     * 
     * @param messages          the messages to write
     * 
     * @throws Exception        if an error occurs during writing
     */
    @SyncTask
    private void debugSync(Object... messages)
    {
        final String message = this.getString(DEBUG, messages);
        System.out.println(message);
        try {FileHelper.writeln(this.currentFile, message, true);}
        catch (Exception e) {System.err.println(this.getString(FATAL, BasicUtils.getStackTraceAsString(e)));}
    }

    /**
     * Writes the given messages to the standard output and the current log file as
     * info messages in ui thread (not recommended use directly).
     * 
     * @param messages          the messages to write
     * 
     * @throws Exception        if an error occurs during writing
     */
    @SyncTask
    private void infoSync(Object... messages)
    {
        final String message = this.getString(INFO, messages);
        System.out.println(message);
        try {FileHelper.writeln(this.currentFile, message, true);}
        catch (Exception e) {System.err.println(this.getString(FATAL, BasicUtils.getStackTraceAsString(e)));}
    }

    /**
     * Writes the given messages to the standard output and the current log file as
     * info messages in specific thread (recommended).
     * 
     * @param messages          the messages to write
     * 
     * @throws Exception        if an error occurs during writing
     */
    @AsyncTask
    public void info(Object... messages)
    {
        EXECUTOR.submit(() -> this.infoSync(messages));
    }

    /**
     * Writes the given messages to the standard error and the current log file as
     * warning messages in ui thread (not recommended use directly).
     * 
     * @param messages          the messages to write
     * 
     * @throws Exception        if an error occurs during writing
     */
    @SyncTask
    private void warningSync(Object... messages)
    {
        final String message = this.getString(WARNING, messages);
        System.err.println(message);
        try {FileHelper.writeln(this.currentFile, message, true);}
        catch (Exception e) {System.err.println(this.getString(FATAL, BasicUtils.getStackTraceAsString(e)));}
    }

    /**
     * Logs the given messages at the warning level in a specific thread (recommended).
     * 
     * <p>
     * This method submits a task to the executor to write the messages
     * asynchronously, improving performance by not blocking the calling
     * thread. The messages are written to both the standard error and
     * the current log file.
     * </p>
     * 
     * @param messages          the messages to be logged
     */
    @AsyncTask
    public void warning(Object... messages)
    {
        EXECUTOR.submit(() -> this.warningSync(messages));
    }

    /**
     * Writes the given messages to the standard error and the current log file as
     * error messages in ui thread (not recommended use directly).
     * 
     * @param messages          the messages to write
     * 
     * @throws Exception        if an error occurs during writing
     */
    @SyncTask
    private void errorSync(Object... messages)
    {
        final String message = this.getString(ERROR, messages);
        System.err.println(message);
        try {FileHelper.writeln(this.currentFile, message, true);}
        catch (Exception e) {System.err.println(this.getString(FATAL, BasicUtils.getStackTraceAsString(e)));}
    }

    /**
     * Logs the given messages at the fatal level in a specific thread (recommended).
     * 
     * <p>
     * This method submits a task to the executor to write the messages
     * asynchronously, improving performance by not blocking the calling
     * thread. The messages are written to both the standard error and
     * the current log file.
     * </p>
     * 
     * @param messages          the messages to be logged
     */
    @AsyncTask
    public void fatal(Object... messages)
    {
        EXECUTOR.submit(() -> this.errorSync(messages));
    }

    /**
     * Writes the given messages to the standard error and the current log file as
     * fatal messages in ui thread (not recommended use directly).
     * 
     * @param messages          the messages to write
     * 
     * @throws Exception        if an error occurs during writing
     */
    @SyncTask
    private void fatalSync(Object... messages)
    {
        final String message = this.getString(FATAL, messages);
        System.err.println(message);
        try {FileHelper.writeln(this.currentFile, message, true);}
        catch (Exception e) {System.err.println(this.getString(FATAL, BasicUtils.getStackTraceAsString(e)));}
    }

    /**
     * Logs the given messages at the fatal level in a specific thread (recommended).
     * 
     * <p>
     * This method submits a task to the executor to write the messages
     * asynchronously, improving performance by not blocking the calling
     * thread. The messages are written to both the standard error and
     * the current log file.
     * </p>
     * 
     * @param messages          the messages to be logged
     */
    @AsyncTask
    public void error(Object... messages)
    {
        EXECUTOR.submit(() -> this.fatalSync(messages));
    }

    @SyncTask
    private String getString(byte level, Object... messages)
    {
        final int year = this.calendar.get(Calendar.YEAR);
        final int month = this.calendar.get(Calendar.MONTH) + 1;
        final int day = this.calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = this.calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = this.calendar.get(Calendar.MINUTE);
        final int second = this.calendar.get(Calendar.SECOND);

        final String monthStr = month < 10 ? "0" + month : Integer.toString(month);
        final String dayStr = day < 10 ? "0" + day : Integer.toString(day);
        final String hourStr = hour < 10 ? "0" + hour : Integer.toString(hour);
        final String minuteStr = minute < 10 ? "0" + minute : Integer.toString(minute);
        final String secondStr = second < 10 ? "0" + second : Integer.toString(second);

        final String className = clazz.getName();
        final String levelStr = levelToString(level);

        final StringBuilder sb = new StringBuilder(logFormat);

        replaceAll(sb, YEAR_KEY, String.valueOf(year));
        replaceAll(sb, MONTH_KEY, monthStr);
        replaceAll(sb, DAY_KEY, dayStr);
        replaceAll(sb, HOUR_KEY, hourStr);
        replaceAll(sb, MINUTE_KEY, minuteStr);
        replaceAll(sb, SECOND_KEY, secondStr);
        replaceAll(sb, CLASS_KEY, className);
        replaceAll(sb, LEVEL_KEY, levelStr);
        replaceAll(sb, MESSAGE_KEY, BasicUtils.combined(messages));

        return sb.toString();
    }

    /**
     * Replace all occurrences of a key in a StringBuilder with a value.
     * 
     * @param sb      the StringBuilder to replace in
     * @param key     the key to replace
     * @param value   the value to replace with
     */
    @SyncTask
    private void replaceAll(StringBuilder sb, String key, String value)
    {
        int index = sb.indexOf(key);

        while (index != -1)
        {
            sb.replace(index, index + key.length(), value);
            index = sb.indexOf(key, index + value.length());
        }
    }
}