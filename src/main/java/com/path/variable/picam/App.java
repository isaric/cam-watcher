package com.path.variable.picam;

import com.path.variable.picam.recorder.Recorder;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.path.variable.commons.properties.Configuration.getConfiguration;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        var threads = new ArrayList<Thread>();
        var recs = new ArrayList<Recorder>();
        var numberOfCameras = getConfiguration().getInteger("cameras.number");
        for (int i = 0; i < numberOfCameras; i++) {
            var recorder = new Recorder(i,getConfiguration().getInteger("device.{0}", -1, i),
                    getConfiguration().getString("location.name.{0}",null, i),
                    getConfiguration().getString("path.{0}",null, i));
            LOG.info("Initialized camera recorder for location {} at number {}",
                    recorder.getLocationName(), recorder.getCameraNumber());
            recs.add(recorder);
            Runnable run = recorder::record;
            var t = new Thread(run);
            t.setName(format("Recorder-%d", i));
            threads.add(t);
        }
        LOG.info("initialized all threads for recording. starting now.");
        threads.forEach(Thread::start);
        getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook activated. Shutting down gracefully.");
            recs.forEach(Recorder::stop);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOG.error("Sleep interrupted!");
            }
        }));
        var stop = false;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOG.error("Sleep was interrupted!", ex);
            }
            stop = threads.stream().noneMatch(Thread::isAlive);
        } while (!stop);
        LOG.info("Execution of recording program(s) concluded normally");
    }
}
