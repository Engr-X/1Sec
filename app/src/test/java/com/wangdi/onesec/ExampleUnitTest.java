package com.wangdi.onesec;

import com.wangdi.onesec.data.Analyser;
import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.data.Recorder;

import org.junit.Test;
import org.junit.internal.management.ManagementFactory;

import java.io.File;
import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    public static Thread MAIN_THREAD = Thread.currentThread();

    public static boolean isDebugMode() {
        for (final String arg : ManagementFactory.getRuntimeMXBean().getInputArguments())
        {
            if (arg.contains("jdwp") || arg.contains("Xdebug") || arg.contains("Xrunjdwp"))
                return true;
        }
        return false;
    }



    @Test
    @SuppressWarnings("null")
    public void test() throws Exception
    {
        /*double[] data = {27.8, 45.56, 29.30, 34.82, 37.17, 29.77, 26.30, 33.86, 27.20, 34.95, 29.55, 32.06, 38.75, 44.06, 26.91};
        //double[] data = {44.60};
        Recorder r = Recorder.create(new File("C:\\Users\\wd200\\Desktop\\root"), 10, Data.NUMBER_RESPONSE_TYPE);

        for (Double d : data)
            ((Recorder.NumberResponseRecorder)r).addData(d);*/

        Analyser da = new Analyser(Arrays.asList(37.23, 31.90, 32.67, 42.48, 35.29, 29.49, 26.99, 25.06, 37.18, 30.04, 39.33,
                        31.31, 34.03, 39.33, 31.31, 34.03, 39.39, 29.00, 29.34, 27.23));
        System.out.println(da.AO5);

    }
}