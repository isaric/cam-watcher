package com.path.variable.watcher.monitors;

import com.path.variable.watcher.config.CameraConfig;
import com.path.variable.watcher.detectors.AbstractDetector;
import com.path.variable.watcher.detectors.AreaMotionDetector;
import com.path.variable.watcher.notifiers.Notifier;
import com.path.variable.watcher.wrapper.CameraWrapper;
import com.path.variable.watcher.wrapper.WriterWrapper;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.path.variable.commons.properties.Configuration.getConfiguration;
import static com.path.variable.watcher.config.CameraConstants.*;
import static com.path.variable.watcher.util.Util.hasNotElapsed;
import static com.path.variable.watcher.util.Util.sleep;
import static java.lang.String.format;

public class OpenCvMonitor extends Monitor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenCvMonitor.class);

    private final int fps;

    private final Size frameSize;

    private final int cameraNumber;

    private final String locationName;

    private final CameraWrapper camera;

    private final WriterWrapper writer;

    private final AbstractDetector detector;

    private final Double recorderSpecificArea;

    private boolean stop;

    public OpenCvMonitor(CameraConfig config, Integer cameraNumber, List<Notifier> notifiers) {
        super(notifiers);
        String tempFilePath = format(TEMP_FILE_NAME_TEMPLATE, cameraNumber);
        this.cameraNumber = cameraNumber;
        this.locationName = config.getLocation();
        this.recorderSpecificArea = config.getAreaMinimum();
        this.camera = new CameraWrapper(getVideoSupplier(config.getDeviceId(), config.getPath()), config.getRetries());
        this.fps = (int) camera.getCamera().get(Videoio.CAP_PROP_FPS);
        this.frameSize = new Size((int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_WIDTH), (int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_HEIGHT));
        var save = new VideoWriter(tempFilePath, VideoWriter.fourcc('x', '2', '6', '4'), fps, frameSize, true);
        this.writer = new WriterWrapper(save, this.locationName, config.isPrintTimestamp());
        this.detector = setupDetector();
    }

    @Override
    public String getName() {
        return "monitor-%s-%d".formatted(locationName, cameraNumber);
    }

    public void monitor() {
        sleep(1000);
        try {
            if (camera.isOpened()) {
                int captureDuration = getConfiguration().getInteger("capture.duration", 1);
                while (!stop) {
                    if (detector.detect()) {
                        var msg = String.format(ALERT_MESSAGE_TEMPLATE, locationName);
                        notify(msg);
                        LOG.info(msg);
                        try {
                            takeSnapshot();
                            doRecordForMinutes(captureDuration);
                        } catch (Exception ex) {
                            var errMsg = "Recording of motion interrupted!";
                            notify(errMsg);
                            LOG.error(errMsg);
                            terminate(ex);
                        }
                    }
                }
            }
        } finally {
            camera.release();
            writer.release();
        }
    }

    @Override
    protected void stop() {
        this.stop = true;
    }

    private void terminate(Exception ex) {
        if (ex instanceof IllegalStateException) {
            var msg = "Camera offline! Terminating recorder!";
            LOG.error(msg);
            notify(msg);
            Thread.currentThread().interrupt();
        }
    }

    private void doRecordForMinutes(int minutes) {
        String filename = FILE_NAME_FORMAT.formatted(locationName, cameraNumber, FILE_DATE_FORMAT.format(new Date()));
        String fullPath = "%s/%s".formatted(SNAPSHOT_ROOT, filename);
        writer.open(fullPath, VideoWriter.fourcc('x', '2', '6', '4'), fps, frameSize, true);
        var start = ZonedDateTime.now();
        while (hasNotElapsed(start, minutes, ChronoUnit.MINUTES) && !this.stop) {
            var frame = camera.read();
            writer.writeFrame(frame);
        }
    }

    private void takeSnapshot() {
        Mat image = camera.read();
        Imgcodecs.imwrite("%s-%s,jpg".formatted(locationName, FILE_DATE_FORMAT.format(new Date())), image);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpenCvMonitor) {
            return ((OpenCvMonitor) obj).getCameraNumber() == this.cameraNumber;
        }
        return false;
    }
}
