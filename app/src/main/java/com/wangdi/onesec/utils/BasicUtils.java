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

import java.io.PrintWriter;
import java.io.StringWriter;
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
}