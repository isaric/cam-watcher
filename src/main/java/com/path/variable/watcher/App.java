package com.path.variable.watcher;

import com.path.variable.watcher.config.CameraConfigurationReader;
import com.path.variable.watcher.config.ConfigurationFileWatcher;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.path.variable.watcher.config.CameraConstants.ENABLE_OPENCV;
import static com.path.variable.watcher.config.CameraConstants.RECORDER_CONFIG_FOLDER;
import static com.path.variable.watcher.util.Util.sleep;
import static java.lang.Runtime.getRuntime;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        if (ENABLE_OPENCV) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }

        CameraConfigurationReader reader = new CameraConfigurationReader();
        var cams = reader.loadRecordersFromFolder(new File(RECORDER_CONFIG_FOLDER));
        LOG.info("initialized all threads for recording. starting now.");

        ConfigurationFileWatcher watcher = new ConfigurationFileWatcher(RECORDER_CONFIG_FOLDER, cams, reader);
        watcher.init();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                watcher.watch();
            }
        }, 1000, 1000);

        addShutdownHook(cams);

        timer.cancel();
        LOG.info("Execution of recording program(s) concluded normally");
    }

    private static void addShutdownHook(Set<Camera> cameras) {
        getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook activated. Shutting down gracefully.");
            cameras.forEach(Camera::stop);
            sleep(3000);
        }));
    }
}
