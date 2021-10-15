package com.path.variable.picam;

import com.path.variable.picam.recorder.ConfigurationFileWatcher;
import com.path.variable.picam.recorder.Recorder;
import com.path.variable.picam.recorder.RecorderConfigurationReader;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.path.variable.commons.properties.Configuration.getConfiguration;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private static boolean globalStop = false;

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        RecorderConfigurationReader slurper = new RecorderConfigurationReader();
        var threads = new ArrayList<Thread>();
        String configFolder = getConfiguration().getString("recorder.config.folder");
        var recs = slurper.loadRecordersFromFolder(new File(configFolder));
        ConfigurationFileWatcher watcher = new ConfigurationFileWatcher(configFolder, threads, recs);
        LOG.info("initialized all threads for recording. starting now.");
        watcher.init();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                watcher.watch();
            }
        }, 1000, 1000);
        addShutdownHook(recs);
        waitForSafeShutdown(threads);
        timer.cancel();
        LOG.info("Execution of recording program(s) concluded normally");
    }

    private static void addShutdownHook(List<Recorder> recorders) {
        getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook activated. Shutting down gracefully.");
            recorders.forEach(Recorder::stop);
            globalStop = true;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOG.error("Sleep interrupted!");
            }
        }));
    }

    private static void waitForSafeShutdown(List<Thread> threads) {
        var stop = false;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOG.error("Sleep was interrupted!", ex);
            }
            stop = threads.stream().noneMatch(Thread::isAlive) && globalStop;

        } while (!stop);
    }
}
