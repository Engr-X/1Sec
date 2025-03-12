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

import java.io.File;

/**
 * Logger class is used to print error messages or logs in console,
 * Also writing these error messages or logs into files for better management.
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
    public static final byte CRITICAL = 4;

    private static final String FILE_EXTENSION = ".log";

    private final File directory;

    public Logger(final File directory)
    {
        this.directory = directory;
    }
}