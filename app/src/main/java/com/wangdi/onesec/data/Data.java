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

package com.wangdi.onesec.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import com.wangdi.onesec.NotImplementException;
import com.wangdi.onesec.utils.BasicUtils;

import androidx.annotation.NonNull;

import org.json.JSONObject;

/**
 * <p>
 * Data class is aimed to record people's sessions in 1Sec APP.
 * The Data class can be initialised by Map or by a Json object.
 * The NumberResponse class record people's reaction time in number test session
 *
 * @see NumberResponse
 * </p>
 *
 * @author Di Wang
 * @version 1.0
 */

public class Data
{
    public static final class Statics<T>
    {
        public T current;
        public T best;
        public T worst;
        public T average;

        public Statics()
        {
            this(null, null, null, null);
        }

        public Statics(T best, T worst, T average)
        {
            this(null, best, worst, average);
        }

        public Statics(T current, T best, T worst, T average)
        {
            this.current = current;
            this.best = best;
            this.worst = worst;
            this.average = average;
        }

        /**
         * Updates the statistics with the provided values.
         * This method sets the current, best, worst, and average values of the statistics
         * to the specified values.
         *
         * @param current       the current value to set
         * @param best          the best value to set
         * @param worst         the worst value to set
         * @param average       the average value to set
         */
        public void update(T current, T best, T worst, T average)
        {
            this.current = current;
            this.best = best;
            this.worst = worst;
            this.average = average;
        }
    }

    public static final byte GENERAL_DATA_TYPE = -1;
    public static final byte NUMBER_RESPONSE_TYPE = 0;

    public static final byte BETTER = 1;
    public static final byte EQUALS = 0;
    public static final byte WORSE = -1;

    public static final double DNF = Double.POSITIVE_INFINITY;

    public static final String NUMBER_RESPONSE_SERIAL = "serial";
    public static final String NUMBER_RESPONSE_TIME = "time";
    private static final Set<String> NUMBER_RESPONSE_PARAM = new HashSet<>() {{this.add(NUMBER_RESPONSE_SERIAL); this.add(NUMBER_RESPONSE_TIME);}};

    /**
     * If the given Double value is null or DNF, return null; otherwise return the value itself.
     *
     * @param value             the Double value to be formatted
     * @return                  the formatted Double value
     */
    public static Double format(Double value)
    {
        return (value == null || value == DNF) ? null : value;
    }

    /**
     * Create a Data object according to the given parameters and data type.
     * 
     * @param params            parameters to create the Data object
     * @param dataType          type of the Data object
     * @return                  the created Data object
     * 
     * @throws Exception        if error occurs, for example no such data type
     */
    public static Data create(Map<String, Object> params, byte dataType) throws Exception
    {
        switch (dataType)
        {
            case NUMBER_RESPONSE_TYPE: return new NumberResponse(params);
            default: throw new DataFormatException(BasicUtils.combined("Cannot format to ", dataType, ". No such data type"));
        }
    }

    /**
     * Load a Data object from a JSONObject.
     * 
     * @param json              the JSONObject to load from
     * @param dataType          the type of the Data object
     * @return                  the loaded Data object
     * 
     * @throws Exception        if error occurs, for example no such data type
     */
    public static Data load(JSONObject json, byte dataType) throws Exception
    {
        switch (dataType)
        {
            case NUMBER_RESPONSE_TYPE: return new NumberResponse().load(json);
            default: throw new DataFormatException(BasicUtils.combined("Cannot format to ", dataType, ". No such data type"));
        }
    }

    protected final Set<String> parameters;
    protected final Map<String, Object> content;
    protected final JSONObject json;

    private Data(final Set<String> parameters)
    {
        this.parameters = parameters;
        this.content = new HashMap<>();
        this.json = new JSONObject();
    }

    private Data(final Set<String> parameters, final Map<String, Object> content) throws Exception
    {
        this(parameters);

        for (String key : content.keySet())
        {
            if (parameters.contains(key))
            {
                final Object value = content.get(key);
                this.json.put(key, value);
                this.content.put(key, value);
            }
            else
                throw new DataFormatException(BasicUtils.combined("Data format error: additional ", key, " is given."));
        }
    }

    /**
     * Returns the JSONObject representation of this Data object.
     * 
     * @return                  the JSONObject representation of this Data object
     */
    public final JSONObject toJson()
    {
        return this.json;
    }

    /**
     * Sets a value for a given key in the Data object.
     * 
     * @param key              the key for which the value needs to be set
     * @param value            the value to be set for the specified key
     * 
     * @throws Exception       if the key is not part of the predefined parameters
     */
    public void set(final String key, final Object value) throws Exception
    {
        if (this.parameters.contains(key))
        {
            this.json.put(key, value);
            this.content.put(key, value);
        }
        else
            throw new DataFormatException(BasicUtils.combined("Data format error: additional ", key, " is given."));
    }

    /**
     * Returns whether the Data object is valid or not.
     * 
     * @return                  whether the Data object is valid or not
     */
    public boolean isValid() throws Exception
    {
        return true;
    }

    /**
     * Gets the type of the Data object.
     * 
     * @return                  the type of the Data object
     */
    public byte getDataType()
    {
        return GENERAL_DATA_TYPE;
    }

    /**
     * Compares this Data object with the given Data object.
     * 
     * @param other             the Data object to compare with
     * @return                  a negative integer, zero, or a positive integer as this Data object is less than, equal to, or greater than the given Data object
     * 
     * @throws Exception        if error occurs, for example no such data type
     */
    public byte compare(final Data other) throws Exception
    {
        throw new NotImplementException("Function byte Data.compare[Data] is not implemented");
    }

    /**
     * Loads the Data object from the given JSONObject.
     * 
     * @param json              the JSONObject to load the Data object from
     * @return                  the loaded Data object
     * 
     * @throws Exception        if error occurs, for example no such data type
     */
    public Data load(final JSONObject json) throws Exception
    {
        final Iterator<String> it = json.keys();

        while (it.hasNext())
        {
            final String key = it.next();
            final Object value = json.get(key);

            if (this.parameters.contains(key))
            {
                this.json.put(key, value);
                this.content.put(key, value);
            }
            else
                throw new DataFormatException(BasicUtils.combined("Data load error: additional ", key, " is given."));
        }

        return this;
    }

    /**
     * Returns the value of the given key in the Data object.
     * 
     * @param key               the key for which the value needs to be retrieved
     * 
     * @return                  the value of the given key
     */
    public final Object get(String key)
    {
        return this.content.get(key);
    }

    /**
     * Returns the string representation of this Data object.
     *
     * @return                  the string representation of this Data object in JSON format
     */
    @NonNull
    @Override
    public final String toString()
    {
        return this.json.toString();
    }

    // if the time is Double.Max, it will be DNF
    public static final class NumberResponse extends Data
    {
        private NumberResponse()
        {
            super(NUMBER_RESPONSE_PARAM);
        }

        private NumberResponse(Map<String, Object> content) throws Exception
        {
            super(NUMBER_RESPONSE_PARAM, content);
        }

        public NumberResponse(int numberSerial, double time) throws Exception
        {
            super(NUMBER_RESPONSE_PARAM, new HashMap<>() {{
                this.put(NUMBER_RESPONSE_SERIAL, numberSerial);
                this.put(NUMBER_RESPONSE_TIME, time);
            }});
        }

        public NumberResponse(int numberSerial) throws Exception
        {
            super(NUMBER_RESPONSE_PARAM, new HashMap<>() {{
                this.put(NUMBER_RESPONSE_SERIAL, numberSerial);
                this.put(NUMBER_RESPONSE_TIME, null);
            }});
        }

        /**
         * Returns whether the Data object is valid or not.
         * A Data object is valid if it is not DNF.
         * 
         * @return                  whether the Data object is valid or not
         * @throws Exception        if error occurs, for example no such data type
         */
        @Override
        public boolean isValid() throws Exception
        {
            return !this.getIsDNF();
        }

        /**
         * Gets the data type of this NumberResponse object.
         *
         * @return              the data type, which is NUMBER_RESPONSE_TYPE
         */
        @Override
        public byte getDataType()
        {
            return NUMBER_RESPONSE_TYPE;
        }

        /**
         * Compares this NumberResponse object with the given NumberResponse object.
         * 
         * @param other         the NumberResponse object to compare with
         * @return              a negative integer, zero, or a positive integer as this NumberResponse object is less than, equal to, or greater than the given NumberResponse object
         * 
         * @throws Exception    if error occurs, for example no such data type
         */
        @Override
        public byte compare(final Data other) throws Exception
        {
            final double thisTime = Math.abs(this.getTime()),
                         otherTime = Math.abs(((NumberResponse)(other)).getTime());

            if (thisTime < otherTime)
                return BETTER;

            if (thisTime > otherTime)
                return WORSE;


            final double thisSerial = Math.abs(this.getSerial()),
                         otherSerial = Math.abs(((NumberResponse)(other)).getSerial());

            if (thisSerial > otherSerial)
                return BETTER;

            if (thisSerial < otherSerial)
                return WORSE;

            return EQUALS;
        }

        /**
         * Gets the serial number of the number in the number test session.
         *
         * @return              the serial number of the number in the number test session
         * @throws Exception    if error occurs, for example JSON does not contain the key
         */
        public int getSerial() throws Exception
        {
            return this.json.getInt(NUMBER_RESPONSE_SERIAL);
        }

        /**
         * Gets the time cost to finish the number in the number test session.
         *
         * @return              the time cost to finish the number in the number test session
         * @throws Exception    if error occurs, for example JSON does not contain the key
         */
        public double getTime() throws Exception
        {
            if (this.content.containsKey(NUMBER_RESPONSE_TIME))
            {
                final Object value = this.content.get(NUMBER_RESPONSE_TIME);
                return value == null ? DNF : (Double)(value);
            }

            return DNF;
        }

        /**
         * Gets whether the user did not finish the session.
         *
         * @return              whether the user did not finish the session
         * @throws Exception    if error occurs, for example JSON does not contain the key
         */
        private boolean getIsDNF() throws Exception
        {
            if (this.content.containsKey(NUMBER_RESPONSE_TIME))
                return this.content.get(NUMBER_RESPONSE_TIME) == null;

            return true;
        }
    }
}