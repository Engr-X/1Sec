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

import java.io.File;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wangdi.onesec.utils.BasicUtils;

/**
 * This class aims to manage the directories of the app, both the internal and external ones.
 * The class contain the root directory, the data directory, the cache directory and the log directory.
 *
 * @author Di Wang
 * @version 1.0
 */

public final class DirectoryManager
{
    public static final String DATA_DIRECTORY = "data";
    public static final String LOG_DIRECTORY = "logs";

    public final File rootDirectory;
    public final File dataDirectory;
    public final File cacheDirectory;
    public final File logDirectory;

    public static DirectoryManager getInternal(final Context context)
    {
        File root = context.getFilesDir();
        return new DirectoryManager(root, new File(root, DATA_DIRECTORY), context.getCacheDir(), new File(root, LOG_DIRECTORY));
    }

    public static DirectoryManager getExternal(final Context context)
    {
        // TODO
        return new DirectoryManager(null, null, null, null);
    }

    private DirectoryManager(File rootDirectory, File dataDirectory,
                             File cacheDirectory, File logDirectory)
    {
        this.rootDirectory = rootDirectory;
        this.dataDirectory = dataDirectory;
        this.cacheDirectory = cacheDirectory;
        this.logDirectory = logDirectory;
    }

    @NonNull
    @Override
    public String toString()
    {
        return BasicUtils.combined("root dir: ", rootDirectory,
                                    "\n data dir: ", this.dataDirectory,
                                    "\n cache dir: ", this.cacheDirectory,
                                    "\n log dir: ", this.logDirectory);
    }
}