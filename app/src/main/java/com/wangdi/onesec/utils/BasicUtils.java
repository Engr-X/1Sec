/**
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

import java.util.List;
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
     * Perform a binary search on a sorted list of Double values.
     * 
     * @param arr               the list to search
     * @param value             the value to search for
     * 
     * @return                  the index of the value if found, -1 otherwise
     */
    public static int binarySearch(final List<Double> arr, double value)
    {
        int start = 0, end = arr.size() - 1;

        while (start <= end)
        {
            if (start == end)
                return (arr.get(start) == value) ? start : -1;

            final int mid = (end + start) >> 1;
            final double midVal = arr.get(mid);

            if (midVal == value)
                return mid;

            if (midVal > value)
                end = mid - 1;
            else
                start = mid + 1;
        }

        return -1;
    }

    /**
     * Find the index at which a given value should be inserted into a sorted
     * list to maintain sorted order.
     * 
     * @param arr               the list to search
     * @param value             the value to search for
     * 
     * @return                  the index at which the value should be inserted
     *                          if not found, -1 otherwise
     */
    public static int searchInsert(final List<Double> arr, double value)
    {
        if (arr.isEmpty())
            return 0;

        return searchInsert(arr, 0, arr.size(), value);
    }

    /**
     * Determines the index at which a given value should be inserted into a sorted
     * sublist to maintain sorted order. If the value already exists, returns the
     * index of the first occurrence.
     * 
     * @param arr               the list to search
     * @param start             the starting index of the sublist (inclusive)
     * @param end               the ending index of the sublist (exclusive)
     * @param value             the value to search for
     * 
     * @return                  the index at which the value should be inserted
     */
    public static int searchInsert(final List<Double> arr, int start, int end, double value)
    {
        int left = start, right = end - 1, ans = end;

        while (left <= right)
        {
            final int mid = ((right - left) >> 1) + left;

            if (value <= arr.get(mid))
            {
                ans = mid;
                right = mid - 1;
            }
            else
            {
                left = mid + 1;
            }
        }

        return ans;
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