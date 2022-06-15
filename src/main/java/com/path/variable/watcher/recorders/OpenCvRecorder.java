package com.path.variable.watcher.recorders;

import com.path.variable.watcher.config.CameraConfig;
import com.path.variable.watcher.wrapper.CameraWrapper;
import com.path.variable.watcher.wrapper.WriterWrapper;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;

import static com.path.variable.watcher.util.Util.hasNotElapsed;

public class OpenCvRecorder extends Recorder {

    private static final Logger LOG = LoggerFactory.getLogger(Recorder.class);

    public OpenCvRecorder(CameraConfig config) {
        super(config);


    }

    @Override
    public void record() {
        timer.scheduleAtFixedRate(getTask(), 1000L, ONE_MINUTE_MILLI);
    }
    private TimerTask getTask() {
        return new TimerTask() {

            private final int fps;

            private final Size frameSize;

            private final CameraWrapper camera;

            private final WriterWrapper writer;

            {
                this.camera = new CameraWrapper(getVideoSupplier(config.getDeviceId(), config.getRtspUrl()), config.getRetries());
                this.fps = (int) camera.getCamera().get(Videoio.CAP_PROP_FPS);
                this.frameSize = new Size((int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_WIDTH),
                        (int) camera.getCamera().get(Videoio.CAP_PROP_FRAME_HEIGHT));
                this.writer = new WriterWrapper(new VideoWriter(getRecordingPath(config), VideoWriter.fourcc('x', '2', '6', '4'), fps,
                        frameSize, true), config.getLocation(), config.isPrintTimestamp());
            }
            @Override
            public void run() {
                writer.open(getRecordingPath(config), VideoWriter.fourcc('x', '2', '6', '4'), fps, frameSize, true);
                var start = ZonedDateTime.now();
                while (camera.isOpened() && hasNotElapsed(start, 1, ChronoUnit.MINUTES)) {
                    var frame = camera.read();
                    writer.writeFrame(frame);
                }
                LOG.info("Camera {} has closed", getName());
                camera.release();
                writer.release();
            }

            @Override
            public boolean cancel() {
                writer.release();
                camera.release();
                return super.cancel();
            }
        };
    }
}