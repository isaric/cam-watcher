package com.path.variable.picam.recorder;

import com.path.variable.picam.properties.RecorderConfig;
import com.path.variable.picam.recorder.detectors.AbstractDetector;
import com.path.variable.picam.recorder.detectors.AreaMotionDetector;
import com.path.variable.picam.recorder.notifiers.Notifier;
import com.path.variable.picam.recorder.wrapper.CameraWrapper;
import com.path.variable.picam.recorder.wrapper.WriterWrapper;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.path.variable.commons.properties.Configuration.getConfiguration;
import static com.path.variable.picam.properties.RecorderConstants.*;
import static com.path.variable.picam.util.Util.hasNotElapsed;
import static java.lang.String.format;
import static java.nio.file.Files.move;

public class Recorder {

    private static final Logger LOG = LoggerFactory.getLogger(Recorder.class);

    private final int fps;

    private final Size frameSize;

    private final int cameraNumber;

    private final String locationName;

    private final CameraWrapper camera;

    private final WriterWrapper writer;

    private final String tempFilePath;

    private final AbstractDetector detector;

    private final List<Notifier> notifiers;

    private final Double recorderSpecificArea;

    private boolean stop;

    public Recorder(RecorderConfig config, Integer cameraNumber, List<Notifier> notifiers) {
        this.notifiers = notifiers;
        this.tempFilePath = format(TEMP_FILE_NAME_TEMPLATE, cameraNumber);
        this.cameraNumber = cameraNumber;
        this.locationName = config.getLocation();
        this.recorderSpecificArea = config.getAreaMinimum();
        this.camera = new CameraWrapper(getVideoSupplier(config.getDeviceId(), config.getPath()), config.getRetries());
        this.fps = (int) camera.getCamera().get(Videoio.CAP_PROP_FPS);
        this.frameSize = new Size((int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_WIDTH), (int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_HEIGHT));
        var save = new VideoWriter(tempFilePath, VideoWriter.fourcc('x', '2', '6', '4'), fps, frameSize, true);
        this.writer = new WriterWrapper(save, this.locationName, config.getPrintTimestamp());
        this.detector = setupDetector();
    }

    public void record() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (camera.isOpened()) {
                int captureDuration = getConfiguration().getInteger("capture.duration", 1);
                while (!stop) {
                    if (detector.detect()) {
                        var msg = String.format(ALERT_MESSAGE_TEMPLATE, locationName, STANDARD_DATE_FORMAT.format(new Date()));
                        notify(msg);
                        LOG.info(msg);
                        try {
                            doRecordForMinutes(captureDuration);
                        } catch (Exception ex) {
                            var errMsg = "Recording of motion interrupted. Attempting to salvage file.";
                            notify(errMsg);
                            LOG.error(errMsg);
                            attemptSalvage();
                            terminate(ex);
                        }
                        moveFileToDumpLocation();
                        deleteFile();
                    }
                }
            }
        } finally {
            camera.release();
            writer.release();
        }
    }

    public void stop() {
        this.stop = true;
    }

    private Supplier<VideoCapture> getVideoSupplier(Integer deviceId, String path) {
        return () -> {
            if (path == null) {
                if (deviceId != null) return new VideoCapture(deviceId);
                throw new IllegalArgumentException("Neither device id nor camera path have been defined!");
            }
            return new VideoCapture(path);
        };
    }

    private void attemptSalvage() {
        var tFile = new File(String.format(TEMP_FILE_NAME_TEMPLATE, cameraNumber));
        if (tFile.exists() && tFile.isFile() && tFile.length() > 0) {
            moveFileToDumpLocation();
            deleteFile();
        }
    }

    private void notify(String message) {
        notifiers.forEach(n -> n.notify(message));
    }

    private void terminate(Exception ex) {
        if (ex instanceof IllegalStateException) {
            var msg = "Camera offline! Terminating recorder!";
            LOG.error(msg);
            notify(msg);
            Thread.currentThread().interrupt();
        }
    }

    private void deleteFile() {
        var deleted = new File(tempFilePath).delete();
        LOG.debug("Temp file deleted: {}", deleted);
        writer.open(tempFilePath, VideoWriter.fourcc('x', '2', '6', '4'), fps, frameSize, true);
    }

    private void moveFileToDumpLocation() {
        var destination = new File(DUMP_DIR, String.format(FILE_NAME_FORMAT, cameraNumber, FILE_DATE_FORMAT.format(new Date())));
        try {
            writer.release();
            move(new File(tempFilePath).toPath(), destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            var errMsg = "Could not copy file for upload!";
            notify(errMsg);
            LOG.error(errMsg);
        }
    }

    private void doRecordForMinutes(int minutes) {
        var start = ZonedDateTime.now();
        while (hasNotElapsed(start, minutes, ChronoUnit.MINUTES) && !this.stop) {
            var frame = camera.read();
            writer.writeFrame(frame);
        }
    }

    private AbstractDetector setupDetector() {
        var detectorParameters = new HashMap<String, Object>();
        detectorParameters.put("camera", camera);
        Double area = recorderSpecificArea == null ? getConfiguration().getDouble("area.minimum") : recorderSpecificArea;
        detectorParameters.put("motion.contour.area", area);
        return new AreaMotionDetector(detectorParameters);
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    public String getLocationName() {
        return locationName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Recorder) {
            return ((Recorder) obj).getCameraNumber() == this.cameraNumber;
        }
        return false;
    }
}
