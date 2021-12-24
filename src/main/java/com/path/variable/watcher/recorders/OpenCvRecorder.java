package com.path.variable.watcher.recorders;

import com.path.variable.watcher.config.CameraConfig;
import com.path.variable.watcher.wrapper.CameraWrapper;
import com.path.variable.watcher.wrapper.WriterWrapper;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static com.path.variable.watcher.util.Util.sleep;

public class OpenCvRecorder extends Recorder {

    private static final Logger LOG = LoggerFactory.getLogger(Recorder.class);

    private final CameraWrapper camera;

    private final WriterWrapper writer;

    private final int fps;

    private final Size frameSize;

    public OpenCvRecorder(CameraConfig config) {
        super(config);
        camera = new CameraWrapper(getVideoSupplier(config.getDeviceId(), config.getPath()), config.getRetries());
        fps = (int) camera.getCamera().get(Videoio.CAP_PROP_FPS);
        frameSize = new Size((int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_WIDTH),
                (int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_HEIGHT));
        writer = new WriterWrapper(new VideoWriter(getRecordingPath(config), VideoWriter.fourcc('x', '2', '6', '4'), fps,
                frameSize, true), config.getLocation(), config.isPrintTimestamp());
    }

    @Override
    public void record() {
        sleep(1000);
        LocalDateTime start = LocalDateTime.now();
        while (camera.isOpened() && !this.stop) {
            var frame = camera.read();
            writer.writeFrame(frame);
            if (LocalDateTime.now().getDayOfMonth() > start.getDayOfMonth()) {
                writer.open(getRecordingPath(config), VideoWriter.fourcc('x', '2', '6', '4'), fps, frameSize, true);
            }
        }
        LOG.info("Camera {} has closed", getName());
    }
}