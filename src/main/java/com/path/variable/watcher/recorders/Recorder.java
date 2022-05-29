package com.path.variable.watcher.recorders;

import com.path.variable.watcher.AbstractCameraComponent;
import com.path.variable.watcher.config.CameraConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;

import static com.path.variable.watcher.config.CameraConstants.RECORDING_ROOT;

public abstract class Recorder extends AbstractCameraComponent {

    protected static final int ONE_MINUTE_MILLI = 1000 * 60;

    protected final CameraConfig config;

    protected final Timer timer;

    protected Recorder(CameraConfig config) {
        this.config = config;
        this.timer = new Timer();
    }

    public abstract void record();

    public void stop() {
        timer.cancel();
    }

    @Override
    public String getName() {
        return "recorder-%s".formatted(config.getLocation());
    }

    protected static String getRecordingPath(CameraConfig config) {
        return "%s/%s/%s.mkv".formatted(RECORDING_ROOT.getAbsolutePath(),
                config.getLocation().replaceAll(" ","-"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")));
    }

}
