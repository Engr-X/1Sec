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

package com.wangdi.onesec.exceptions;

/**
 * Indicates that a method or constructor need to be replace by meaningful logic.
 * It can used by checking override function is correct or not.
 *
 *
 * @author Di Wang
 * @version 1.0
 */

public final class NotImplementedException extends Exception
{
    public NotImplementedException()
    {
        super();
    }

    public NotImplementedException(String message)
    {
        super(message);
    }

    @SuppressWarnings("unused")
    public NotImplementedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public NotImplementedException(Throwable cause)
    {
        super(cause);
    }
}