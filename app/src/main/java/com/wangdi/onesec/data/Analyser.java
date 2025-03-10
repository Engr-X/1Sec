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

package com.wangdi.onesec.data;

import com.wangdi.onesec.utils.BasicUtils;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provided methods to analyse data, including the average value, the best data
 *
 *
 * @author Di Wang
 * @version 1.0
 */

public final class Analyser
{
    public static class Tracker
    {
        public Double min;

        protected final int size;
        protected final Deque<Double> dq;

        protected int sizeCount;
        protected double sum;

        public Tracker(int size)
        {
            this.sum = 0.0;
            this.size = size;
            this.sizeCount = 0;
            this.dq = new LinkedList<>();
        }

        public Double add(double value)
        {
            return null;
        }

        public static final class MO extends Tracker
        {
            public MO(int size)
            {
                super(size);
            }

            @Override
            public Double add(double value)
            {
                dq.add(value);
                this.sum += value;
                this.sizeCount++;

                if (this.sizeCount == this.size)
                {
                    final double result = this.sum / this.size;

                    if (this.min == null || result < this.min)
                        this.min = result;

                    return result;
                }

                if (this.sizeCount > this.size)
                {
                    this.sum -= this.dq.pop();

                    final double result = this.sum / this.size;

                    if (result < this.min)
                        this.min = result;

                    return result;
                }

                return null;
            }
        }

        public static final class AO extends Tracker
        {
            private final int realSize;
            private final List<Double> sort;

            public AO(int size)
            {
                super(size);
                this.realSize = size - 2;
                this.sort = new ArrayList<>(size);
            }

            @Override
            public Double add(double value)
            {
                if (this.sizeCount < this.size)
                {
                    this.sizeCount++;
                    this.sum += value;
                    this.dq.add(value);
                    this.sort.add(BasicUtils.searchInsert(this.sort, value), value);

                    if (this.sizeCount == this.size)
                    {
                        final double result = (this.sum - this.sort.get(0) - this.sort.get(this.size - 1)) / this.realSize;

                        if (this.min == null || result < this.min)
                            this.min = result;

                        return result;
                    }
                }

                if (this.sizeCount >= this.size)
                {
                    final double popValue = this.dq.pop();
                    this.sort.remove(BasicUtils.binarySearch(this.sort, popValue));
                    this.sum += value - popValue;

                    this.dq.add(value);
                    this.sort.add(BasicUtils.searchInsert(this.sort, value), value);

                    final double result = (this.sum - this.sort.get(0) - this.sort.get(this.size - 1)) / this.realSize;

                    if (result < this.min)
                        this.min = result;

                    return result;
                }

                return null;
            }
        }
    }

    public Double best;
    public Double average;

    public Double bestMO3;
    public Double bestAO5;
    public Double bestAO12;
    public Double bestAO100;
    public final List<Double> MO3;
    public final List<Double> AO5;
    public final List<Double> AO12;
    public final List<Double> AO100;

    public Analyser(final List<Double> data)
    {
        final Iterator<Double> it = data.iterator();

        this.MO3 = new LinkedList<>();
        this.AO5 = new LinkedList<>();
        this.AO12 = new LinkedList<>();
        this.AO100 = new LinkedList<>();

        final Tracker mo3 = new Tracker.MO(3);
        final Tracker ao5 = new Tracker.AO(5);
        final Tracker ao12 = new Tracker.AO(12);
        final Tracker ao100 = new Tracker.AO(100);

        while (it.hasNext())
        {
            final double value = it.next();

            if (this.best == null || value > this.best)
                this.best = value;

            this.average = (this.average == null) ? value : this.average + value;

            MO3.add(mo3.add(value));
            AO5.add(ao5.add(value));
            AO12.add(ao12.add(value));
            AO100.add(ao100.add(value));
        }

        this.bestMO3 = mo3.min;
        this.bestAO5 = ao5.min;
        this.bestAO12 = ao12.min;
        this.bestAO100 = ao100.min;

        if (this.average != null)
            this.average /= data.size();
    }
}