package com.path.variable.watcher.config;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.path.variable.commons.properties.Configuration.getConfiguration;

public class CameraConstants {

    public static final String ALERT_MESSAGE_TEMPLATE = "Motion detected in %s";

    public static final DateFormat STANDARD_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSS dd/MM/yyyy");

    public static final DateFormat FILE_DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

    public static final String FILE_NAME_FORMAT = "video_%s_%d_%s.mp4";

    public static final String TEMP_FILE_NAME_TEMPLATE = "video_%d.mp4";

    public static final File SNAPSHOT_ROOT = new File(getConfiguration().getString("snapshot.path"));

    public static final File RECORDING_ROOT = new File(getConfiguration().getString("recording.path", "."));

    public static final Double DEFAULT_AREA_MINIMUM = getConfiguration().getDouble("area.minimum");

    public static final Integer DEFAULT_CAPTURE_DURATION = getConfiguration().getInteger("capture.duration", 1);

    public static final boolean ENABLE_OPENCV = getConfiguration().getBoolean("enable.opencv", false);

    public static final String RECORDER_CONFIG_FOLDER = getConfiguration().getString("recorder.config.folder");

    private CameraConstants() {}
}
