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

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * <p>
 * This data provided a stream method to analyse a series of data efficiently.
 * The MO class is aimed to calculate the mean of a continuous series of data.
 * The AO class is aimed to calculate the average value in a continuous series of data
 * but minus the maximum and minimum value.
 * </p>
 *
 * <p>
 * While record the data, also record it average, maximum and minimum in all MO or AO
 * </p>
 *
 * @see StreamingAggregator.AO
 * @see StreamingAggregator.MO
 * @author Di Wang
 * @version 1.0
 */

public abstract class StreamingAggregator<T extends Comparable<T>>
{
    protected final int size;
    protected final Comparator<T> comparator;
    protected final Deque<T> stream;

    protected int index;
    protected T min;
    protected T max;

    public StreamingAggregator(Comparator<T> comparator, int size)
    {
        this.size = size;
        this.comparator = comparator;
        this.stream = new LinkedList<>();

        this.index = 0;
        this.max = this.min = null;
    }

    /**
     * Adds the given value to the stream and updates the minimum and maximum
     * values if necessary. This method is abstract and should be implemented
     * by subclasses to define the specific behavior of adding a value to the
     * stream.
     *
     * @param value             The value to be added to the stream.
     * 
     * @return                  The result of adding the value, which may vary depending on the
     *                          implementation in the subclass.
     */

    public T add(T value)
    {
        this.stream.add(value);
        this.index++;
        return null;
    }

    /**
     * Returns the minimum value in the current stream of data.
     *
     * @return                  the minimum value, or null if the stream is empty
     */

    public T getMin()
    {
        return this.min;
    }


    /**
     * Returns the maximum value in the current stream of data.
     *
     * @return                  the maximum value, or null if the stream is empty
     */
    public T getMax()
    {
        return this.max;
    }

    public static final class MO extends StreamingAggregator<Double>
    {
        private int effectiveIndex;
        private double partSum1;
        private int partSum2;
        private Double average;

        public MO(int size)
        {
            super(Double::compare, size);

            this.effectiveIndex = 0;
            this.partSum1 = 0.0;
            this.partSum2 = 0;
            this.average = null;
        }

        /**
         * Adds the given value to the stream and updates the minimum, maximum, and average
         * values if necessary. This method is overridden to define the specific behavior of
         * adding a value to the MO stream.
         *
         * @param value             The value to be added to the stream.
         * 
         * @return                  The average of the current stream of data, or null if the stream is empty.
         */
        @Override
        public Double add(Double value)
        {
            if (this.index < this.size)
            {
                super.add(value);
                this.updateSum(value, true);
                final Double returnValue = (this.index != this.size) ? null : this.parseSum() / this.size;
                this.updatePeak(returnValue);
                this.updateAverage(returnValue);
                return returnValue;
            }

            this.updateSum(this.stream.pop(), false);
            super.add(value);
            this.updateSum(value, true);
            final Double returnValue = this.parseSum() / this.size;
            this.updatePeak(returnValue);
            this.updateAverage(returnValue);
            return returnValue;
        }

        /**
         * Returns the average value in the all available of MO.
         * 
         * @return                  the average value, or 0 if the stream is empty
         */
        public Double getAverage()
        {
            return this.average;
        }

        /**
         * Updates the peak values in the current stream of data.
         * This method checks the given value and updates the maximum and minimum
         * values of the stream. If the provided value is greater than the current
         * maximum or less than the current minimum, the respective peak value is
         * updated.
         * 
         * @param value         the value to be considered for updating the peak values,
         *                      ignored if null or Data.DNF
         */
        private void updatePeak(Double value)
        {
            if (value == null || value == Data.DNF) return;

            if (this.max == null || this.comparator.compare(value, this.max) > 0)
                this.max = value;

            if (this.min == null || this.comparator.compare(value, this.min) < 0)
                this.min = value;
        }

        /**
         * Updates the average value in the current stream of data.
         * This method accumulates the given value to the running average and
         * updates the average value of the stream. If the provided value is null
         * or Data.DNF, it is ignored.
         * 
         * @param value         the value to be accumulated to the running average,
         *                      ignored if null or Data.DNF
         */
        private void updateAverage(Double value)
        {
            if (value == null || value == Data.DNF) return;

            if (this.average == null) this.average = 0.0;
            this.average = this.average * (this.effectiveIndex++) + value;
            this.average /= this.effectiveIndex;
        }

        /**
         * Updates the running sum of the stream of data.
         * This method updates the running sum of the stream of data by either adding
         * or subtracting the given value. If the given value is Data.DNF, the
         * running sum is not updated, but the count of DNF values is incremented or
         * decremented.
         * 
         * @param value         the value to be added or subtracted from the running sum
         * @param add           whether to add (true) or subtract (false) the value from
         *                      the running sum
         */
        private void updateSum(double value, boolean add)
        {
            if (value == Data.DNF)
            {
                if (add) this.partSum2++;
                else this.partSum2--;
            }
            else
            {
                if (add) this.partSum1 += value;
                else this.partSum1 -= value;
            }
        }

        /**
         * Parses the running sum into an average value.
         * This method takes the running sum of the stream of data and returns the average value. If
         * the count of DNF values is greater than zero, the method returns Data.DNF.
         * 
         * @return              the average value, or Data.DNF if the count of DNF values is greater than
         *                      zero
         */
        private double parseSum()
        {
            return (this.partSum2 == 0) ? this.partSum1 : Data.DNF;
        }
    }

    public static final class AO extends StreamingAggregator<Double>
    {
        private int effectiveIndex;
        private double partSum1;
        private int partSum2;
        private Double average;

        private final int realSize;
        private final TreeMap<Double, Integer> map;

        public AO(int size)
        {
            super(Double::compare, size);

            this.realSize = size - 2;
            this.effectiveIndex = 0;
            this.partSum1 = 0.0;
            this.partSum2 = 0;
            this.average = null;

            this.map = new TreeMap<>();
        }

        /**
         * Adds the given value to the stream and updates the minimum, maximum, and average
         * values if necessary. This method is overridden to define the specific behavior of
         * adding a value to the AO stream.
         * 
         * @param value             The value to be added to the stream.
         * 
         * @return                  The average of the current stream of data, or null if the stream is empty.
         */
        @Override
        public Double add(Double value)
        {
            if (this.index < this.size)
            {
                super.add(value);
                this.updateSum(value, true);
                this.updateMap(value, true);
                final Double returnValue = (this.index != this.size) ? null : this.parseSum() / this.realSize;
                this.updatePeak(returnValue);
                this.updateAverage(returnValue);
                return returnValue;
            }

            final Double removeValue = this.stream.pop();
            this.updateSum(removeValue, false);
            this.updateMap(removeValue, false);
            super.add(value);
            this.updateSum(value, true);
            this.updateMap(value, true);
            final Double returnValue = this.parseSum() / this.realSize;
            this.updatePeak(returnValue);
            this.updateAverage(returnValue);
            return returnValue;
        }

        /**
         * Returns the average value of the all available of AO.
         * 
         * @return              the average value of the current stream of data
         */
        public Double getAverage()
        {
            return this.average;
        }

        /**
         * Updates the map of values in the stream to their respective counts.
         * If the given value is being added, the count of that value in the map is incremented.
         * If the given value is being removed, the count of that value in the map is decremented.
         * If the count of the given value in the map is zero, the entry is removed from the map.
         * 
         * @param value             the value to be added or removed from the map
         * @param add               whether to add (true) or remove (false) the value from the map
         */
        private void updateMap(Double value, boolean add)
        {
            if (add)
                this.map.merge(value, 1, Integer::sum);
            else
            {
                this.map.compute(value, (k, currentCount) -> {
                    if (currentCount == null || currentCount <= 0)
                        throw new RuntimeException("No such value in map: " + value);

                    return (currentCount > 1) ? currentCount - 1 : null;
                });
            }
        }

        /**
         * Updates the maximum and minimum value in the current stream of data.
         * This method checks the given value and updates the maximum and minimum
         * values of the stream. If the provided value is greater than the current
         * maximum or less than the current minimum, the respective peak value is
         * updated.
         * 
         * @param value         the value to be considered for updating the peak values,
         *                      ignored if null or Data.DNF
         */
        private void updatePeak(Double value)
        {
            if (value == null || value == Data.DNF) return;

            if (this.max == null || this.comparator.compare(value, this.max) > 0)
                this.max = value;

            if (this.min == null || this.comparator.compare(value, this.min) < 0)
                this.min = value;
        }

        /**
         * Updates the average value of the stream.
         * This method accumulates the given value to the running average and
         * updates the average value of the stream. If the provided value is null
         * or Data.DNF, it is ignored.
         * 
         * @param value         the value to be accumulated to the running average,
         *                      ignored if null or Data.DNF
         */
        private void updateAverage(Double value)
        {
            if (value == null || value == Data.DNF) return;

            if (this.average == null) this.average = 0.0;
            this.average = this.average * (this.effectiveIndex++) + value;
            this.average /= this.effectiveIndex;
        }

        /**
         * Updates the running sum of the stream of data.
         * This method updates the running sum of the stream of data by either adding
         * or subtracting the given value. If the given value is Data.DNF, the
         * running sum is not updated, but the count of DNF values is incremented or
         * decremented.
         * 
         * @param value         the value to be added or subtracted from the running sum
         * @param add           whether to add (true) or subtract (false) the value from
         *                      the running sum
         */
        private void updateSum(double value, boolean add)
        {
            if (value == Data.DNF)
            {
                if (add) this.partSum2++;
                else this.partSum2--;
            }
            else
            {
                if (add) this.partSum1 += value;
                else this.partSum1 -= value;
            }
        }

        /**
         * Parses the running sum into an average value.
         * This method takes the running sum of the stream of data and returns the average value. If
         * the count of DNF values is greater than zero, the method returns Data.DNF.
         * 
         * @return              the average value, or Data.DNF if the count of DNF values is greater than
         *                      zero
         */
        private double parseSum()
        {
            final Double first = this.map.firstKey(), last = this.map.lastKey();

            if (first == null || last == null)
                throw new RuntimeException("Cannot find maximum or minimum");

            if (this.partSum2 == 0) return this.partSum1 - first - last;
            if (this.partSum2 == 1) return this.partSum1 - Math.min(last, first);
            return Data.DNF;
        }
    }
}