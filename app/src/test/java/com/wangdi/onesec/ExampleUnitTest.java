package com.wangdi.onesec;

import android.content.res.AssetManager;

import com.wangdi.onesec.data.Analyser;
import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.data.Recorder;
import com.wangdi.onesec.utils.Logger;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.internal.management.ManagementFactory;

import java.io.File;
import java.util.List;


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
        List<Double> data =  List.of(Data.DNF, 0.79, 0.57, 0.32, 1.37, 1.48, Data.DNF, Data.DNF, 1.04, 0.85,
                                     1.13, 0.98, 1.44, 0.99, 0.94, 1.25, 1.36, 1.26, 1.31, 1.54, 2.36);

        // test analyser
        Analyser analyser = new Analyser(data);
        System.out.println(analyser.MO3data);

        // test recorder
        final Recorder r = Recorder.auto(new File("C:\\Users\\wd200\\Desktop\\root"), 10, Data.NUMBER_RESPONSE_TYPE);

        for (Double d : data)
            ((Recorder.NumberResponse)(r)).addData(d);

        // test logger
        final Logger logger = Logger.create(new File("C:\\Users\\wd200\\Desktop\\root"), this.getClass());
        logger.debug("debug");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.fatal("fatal");
    }
}