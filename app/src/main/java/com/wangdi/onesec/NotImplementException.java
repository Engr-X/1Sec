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

package com.wangdi.onesec;

/**
 * Indicates that a method or constructor need to be replace by meaningful logic.
 * It can used by checking override function is correct or not.
 *
 *
 * @author Di Wang
 * @version 1.0
 */

public final class NotImplementException extends Exception
{
    public NotImplementException()
    {
        super();
    }

    public NotImplementException(String message)
    {
        super(message);
    }

    @SuppressWarnings("unused")
    public NotImplementException(String message, Throwable cause)
    {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public NotImplementException(Throwable cause)
    {
        super(cause);
    }
}