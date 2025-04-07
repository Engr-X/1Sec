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

package com.wangdi.onesec.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Some method, include better combined string and Handles the result of a future.
 *
 *
 * @author Di Wang
 * @version 1.0
 */

public final class BasicUtils
{
    /**
     * Replaces all occurrences of a specified key within a StringBuilder
     * with the provided replacement object.
     *
     * @param sb                The StringBuilder in which to perform replacements.
     * @param key               The substring to be replaced.
     * 
     * @param replacement       The object whose string representation will replace the key.
     */
    public static void replaceAll(StringBuilder sb, String key, Object replacement)
    {
        int start = 0;
        final String replacementStr = replacement.toString();
        while ((start = sb.indexOf(key, start)) != -1)
        {
            sb.replace(start, start + key.length(), replacementStr);
            start += replacementStr.length();
        }
    }

    /**
     * Replace all occurrence in given StringBuilder with the given Map.
     * The key of the Map is the pattern to be replaced, and the value is the replacement.
     *
     * @param sb                The StringBuilder to be modified.
     * @param map               The Map containing the pattern and replacement.
     */
    public static void replaceAll(StringBuilder sb, Map<String, Object> map)
    {
        for (Map.Entry<String, Object> entry : map.entrySet())
            replaceAll(sb, entry.getKey(), entry.getValue());
    }

    /**
     * Replace all occurrence in given string with the given key and replacement.
     * 
     * @param str               The string to be modified.
     * @param key               The substring to be replaced.
     * @param replacement       The object whose string representation will replace the key.
     * @return                  The modified string.
     */
    public static String replaceAll(String str, String key, Object replacement)
    {
        final StringBuilder sb = new StringBuilder(str);
        replaceAll(sb, key, replacement);
        return sb.toString();
    }

    /**
     * Replaces all occurrence in given string with the given Map.
     * The key of the Map is the pattern to be replaced, and the value is the replacement.
     *
     * @param str               The String to be modified.
     * @param map               The Map containing the pattern and replacement.
     * 
     * @return                  The modified String
     */
    public static String replaceAll(String str, Map<String, Object> map)
    {
        final StringBuilder sb = new StringBuilder(str);
        replaceAll(sb, map);
        return sb.toString();
    }

    /**
     * Combines the given objects into a single string. The objects are appended
     * together without any separator. The order of the objects is preserved.
     *
     * @param objects           the objects to combine
     * @return                  the combined string
     */
    public static String combined(Object... objects)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Object obj : objects)
            sb.append(obj);

        return sb.toString();
    }

    /**
     * Returns the stack trace of the given exception as a string.
     *
     * @param e                the exception whose stack trace is to be returned
     * @return                 the stack trace of the exception as a string
     */
    public static String getStackTraceAsString(Exception e)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Handles the result of a future, by blocking until the result is available
     * and then returning the result. If the future is interrupted, the calling
     * thread is also interrupted and an exception is thrown.
     *
     * @param future            the future to get the result from
     * @return                  the result of the future
     * 
     * @throws Exception        if the future is interrupted
     */
    public static <T> T handleFutureResult(final Future<T> future) throws Exception
    {
        try
        {
            return future.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new Exception("Operation interrupted", e);
        }
    }

    /**
     * Extracts a sublist from the given list, starting from the specified start index (inclusive) 
     * to the specified end index (exclusive).
     *
     * @param array             The list from which to extract the sublist.
     * @param start             The starting index (inclusive) of the sublist.
     * @param end               The ending index (exclusive) of the sublist.
     * @return                  A list containing the elements from the start index to the end index.
     */
    public static <T> LinkedList<T> at(List<T> array, int start, int end)
    {
        int index = 0;
        final LinkedList<T> result = new LinkedList<>();

        for (T t : array)
        {
            if (start <= index && index < end)
                result.add(t);

            index++;
        }

        return result;
    }


    /**
     * Returns a list containing the last 'number' elements of the given list.
     *
     * @param array             The list from which to extract the sublist.
     * @param number            The number of elements to return from the end of the list.
     * @return                  A list containing the last 'number' elements of the given list.
     */
    public static <T> LinkedList<T> last(List<T> array, int number)
    {
        final int size = array.size();
        return (number > size) ? new LinkedList<>(array) : at(array, size - number, size);
    }

    public BasicUtils()
    {

    }
}