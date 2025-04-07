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

package com.wangdi.onesec.data;

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
    public Data.Statics<Double> all;
    public Data.Statics<Double> MO3;
    public Data.Statics<Double> AO5;
    public Data.Statics<Double> AO12;
    public Data.Statics<Double> AO100;

    public final List<Double> MO3data;
    public final List<Double> AO5data;
    public final List<Double> AO12data;
    public final List<Double> AO100data;

    public Analyser(final List<Double> data)
    {
        final Iterator<Double> it = data.iterator();

        this.MO3data = new LinkedList<>();
        this.AO5data = new LinkedList<>();
        this.AO12data = new LinkedList<>();
        this.AO100data = new LinkedList<>();

        final StreamingAggregator.MO mo3 = new StreamingAggregator.MO(3);
        final StreamingAggregator.AO ao5 = new StreamingAggregator.AO(5);
        final StreamingAggregator.AO ao12 = new StreamingAggregator.AO(12);
        final StreamingAggregator.AO ao100 = new StreamingAggregator.AO(100);

        int effectiveIndex = 0;
        double average = 0.0;
        Double best = null, worse = null;

        while (it.hasNext())
        {
            final double value = Math.abs(it.next());

            if (value != Data.DNF)
            {
                average = (average * effectiveIndex + value) / (++effectiveIndex);

                if (best == null || value < best) best = value;
                if (worse == null || value > best) worse = value;
            }

            MO3data.add(mo3.add(value));
            AO5data.add(ao5.add(value));
            AO12data.add(ao12.add(value));
            AO100data.add(ao100.add(value));
        }

        this.all = new Data.Statics<>(best, worse, average);
        this.MO3 = new Data.Statics<>(mo3.getMin(), mo3.getMax(), mo3.getAverage());
        this.AO5 = new Data.Statics<>(ao5.getMin(), ao5.getMax(), ao5.getAverage());
        this.AO12 = new Data.Statics<>(ao12.getMin(), ao12.getMax(), ao12.getAverage());
        this.AO100 = new Data.Statics<>(ao100.getMin(), ao100.getMax(), ao100.getAverage());
    }
}