package com.path.variable.watcher.recorders;

import com.path.variable.watcher.AbstractCameraComponent;
import com.path.variable.watcher.config.CameraConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.path.variable.commons.properties.Configuration.getConfiguration;

public abstract class Recorder extends AbstractCameraComponent {

    protected final CameraConfig config;

    protected Recorder(CameraConfig config) {
        this.config = config;
    }

    public abstract void record();

    protected static String getRecordingPath(CameraConfig config) {
        return "%s/%s/%s.mkv".formatted(getConfiguration().getString("recording.folder", "."),
                config.getLocation().replaceAll(" ","-"), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public String getName() {
        return "recorder-%s".formatted(config.getLocation());
    }

}
