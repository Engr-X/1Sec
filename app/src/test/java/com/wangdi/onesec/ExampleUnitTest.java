package com.wangdi.onesec;

import com.wangdi.onesec.data.Analyser;
import com.wangdi.onesec.data.Data;
import com.wangdi.onesec.data.Recorder;
import com.wangdi.onesec.core.Logger;

import org.junit.Test;
import org.junit.internal.management.ManagementFactory;

import java.io.File;
import java.util.List;
import java.util.Random;


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

    public double random()
    {
        // 方法 2
        Random rand = new Random();
        int intValue = rand.nextInt(50001);
        return intValue / 10000.0;
    }

    @Test
    @SuppressWarnings("null")
    public void test() throws Exception
    {


        List<Double> data =  List.of(Data.DNF, 0.79, -0.57, 100.0, 12.0);

        // test analyser
        Analyser analyser = new Analyser(data);
        //System.out.println(analyser.all.average);

        // test recorder
        final Recorder r = Recorder.auto(new File("C:\\Users\\wd200\\Desktop\\root"), 10, Data.NUMBER_RESPONSE_TYPE);

        for (Double d : data)
            ((Recorder.NumberResponse)(r)).addData(d);

        System.out.println(((Recorder.NumberResponse)(r)).all.average);

        // test logger
        final Logger logger = Logger.create(new File("C:\\Users\\wd200\\Desktop\\root"), this.getClass());
        logger.debug("debug");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.fatal("fatal");
    }
}