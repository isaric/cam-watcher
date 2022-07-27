package com.path.variable.watcher.recorders;

import com.path.variable.watcher.config.CameraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class FFMpegRecorder extends Recorder {

    private static final Logger LOG = LoggerFactory.getLogger(FFMpegRecorder.class);

    public FFMpegRecorder(CameraConfig config) {
        super(config);
    }

    @Override
    public void record() {
        // start initial recording instantly
        Process process = getAndStartProcess(config);
        // pass to timer to restart each minute
        timer.scheduleAtFixedRate(new RecordingTask(process), ONE_MINUTE_MILLI, ONE_MINUTE_MILLI);
    }

    private Process getAndStartProcess(CameraConfig config) {
        ProcessBuilder processBuilder = new ProcessBuilder(getFfmpegCommand(config));
        try {
            return processBuilder.start();
        } catch (IOException ex) {
            LOG.error("Could not start process for recorder {}",getName(), ex);
            return null;
        }
    }

    private List<String> getFfmpegCommand(CameraConfig config) {
        if (config.getRtspUrl() != null) {
            return getRtspCommand(config);
        }
        return getV4l2Command(config);
    }

    private List<String> getRtspCommand(CameraConfig config) {
        // any required auth should be included in the rtsp url
        return List.of("ffmpeg", "-rtsp_transport", "tcp", "-i", config.getRtspUrl(), "-r", "1", "-c",
                "copy", "-acodec", "aac", getRecordingPath(config));
    }

    private List<String> getV4l2Command(CameraConfig config) {
        //TODO - ffprobe to figure out FPS and size
        return List.of("ffmpeg", "-f", "v4l2", "-r", "1", "-i", "/dev/video%d".formatted(config.getDeviceId()), getRecordingPath(config), "-c", "copy");
    }

    class RecordingTask extends TimerTask {

        private Process current;

        public RecordingTask(Process initial) {
            this.current = initial;
        }

        @Override
        public void run() {
            current.destroy();
            current = getAndStartProcess(config);
        }
    }
}
