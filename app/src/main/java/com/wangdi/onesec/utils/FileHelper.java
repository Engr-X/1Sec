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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.wangdi.onesec.interfaces.SyncTask;

/**
 * FileHelper provided some methods related to IO operations.
 * For example write content to files, and read content in a file etc.
 * Remember that all the functions are suggested running in thread pool
 *
 *
 * @author YuanZi Jing
 * @author Di Wang
 * @version 1.0
 */

public final class FileHelper
{
    public static final Charset STANDARD_FORMAT = StandardCharsets.UTF_8;

    /**
     * Checks whether the current thread is the Main thread, and if so, throw a RuntimeException.
     * This is because IO operations should not be performed in the Main thread, as it can
     * cause the application to hang or crash.
     * <p>
     * This method is private, and should only be called within this class.
     * <p>
     */
    private static void threadCheck()
    {
        return;
    }

    /**
     * Write the given content to the given file in ui thread.
     * 
     * @param file              target file
     * @param content           content to write
     * @param append            whether to append to the end of the file, or overwrite it
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    public static void write(final File file, final String content, boolean append) throws Exception
    {
        threadCheck();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), STANDARD_FORMAT));
        writer.write(content);
        writer.flush();
    }

    /**
     * Write the given content to the given file, append a newline character at the end of the content.
     * 
     * @param file              target file
     * @param content           content to write
     * @param append            whether to append to the end of the file, or overwrite it
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    public static void writeln(final File file, final String content, boolean append) throws Exception
    {
        threadCheck();

        write(file, content + "\n", append);
    }


    /**
     * Create a new file.
     * 
     * @param file              target file
     * @return                  whether the file is created successfully
     * @throws Exception        if error occurs, for example no such parent directory, permission denied
     */
    @SyncTask
    public static boolean createNewFile(final File file) throws Exception
    {
        threadCheck();

        return file.createNewFile();
    }

    /**
     * Create a new directory or directories in ui thread. eg a/b/c, will create folder a, folder a/b, folder a/b/c.
     * 
     * @param directory         target file
     * @return                  whether the directory is created successfully
     */
    @SyncTask
    public static boolean createNewDirectories(final File directory)
    {
        threadCheck();

        return directory.mkdirs();
    }

    /**
     * Delete the given file in ui thread.
     * 
     * @param file              target file
     * @return                  whether the file is deleted successfully
     */
    @SyncTask
    public static boolean deleteFile(final File file)
    {
        threadCheck();

        return file.delete();
    }

    /**
     * Delete the specified directory and all of its contents in ui thread.
     * 
     * @param directory         target directory to delete
     * @param deleteRoot        if true, delete all the content in the folder include the root folder.
     *                          if false, delete all the content in the folder, not include the root folder
     *
     * @return                  whether the directory was deleted successfully
     */
    @SyncTask
    public static boolean deleteDirectories(final File directory, boolean deleteRoot)
    {
        threadCheck();

        final File[] files = directory.listFiles();

        if (files == null)
            return false;

        for (File child : files)
        {
            if (child.isDirectory())
                deleteDirectories(child, true);
            else
            {
                if (!child.delete())
                    return false;
            }
        }

        return !deleteRoot || directory.delete();
    }

    /**
     * Reads the given file and returns its content as a string in ui thread.
     * 
     * @param file              target file to read
     * @return                  the content of the file as a string
     * @throws Exception        if error occurs, for example no such file, permission denied
     */
    @SyncTask
    public static String read(final File file) throws Exception
    {
        threadCheck();

        final StringBuilder content = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), STANDARD_FORMAT));

        String line;
        while ((line = reader.readLine()) != null)
            content.append(line).append(System.lineSeparator());

        return content.toString();
    }

    /**
     * Lists the files in the given directory in ui thread.
     * 
     * @param directory         target directory to list files from
     * @return                  an array of files in the directory
     */
    @SyncTask
    public static File[] listFiles(final File directory)
    {
        threadCheck();

        return directory.listFiles();
    }

    /**
     * Lists the files in the given directory with the given regex in ui thread
     * 
     * @param directory         target directory to list files from
     * @param regex             regex to filter the files
     * @return                  an array of files in the directory that match the regex
     */
    @SyncTask
    public static File[] listFiles(final File directory, String regex)
    {
        threadCheck();

        final Pattern pattern = Pattern.compile(regex);
        return directory.listFiles((dir, name) -> pattern.matcher(name).matches());
    }

    public FileHelper()
    {

    }
}