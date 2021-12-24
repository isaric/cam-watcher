package com.path.variable.watcher.recorders;

import com.path.variable.watcher.config.CameraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class FFMpegRecorder extends Recorder {

    private static final Logger LOG = LoggerFactory.getLogger(FFMpegRecorder.class);

    private static final int ONE_DAY_MILLI = 1000 * 60 * 60 * 24;

    private final Timer timer;

    public FFMpegRecorder(CameraConfig config) {
        super(config);
        this.timer = new Timer();
    }

    @Override
    public void record() {
        // start initial recording instantly
        Process process = getAndStartProcess(config);
        // pass to timer to restart at start of each day
        timer.scheduleAtFixedRate(new RecordingTask(process), getStartOfNextDay(), ONE_DAY_MILLI);
        while (!stop) {
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                LOG.error("Sleep interrupted!", ex);
            }
        }
        timer.cancel();
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
        return List.of("ffmpeg", "-rtsp_transport", "tcp", "-i", config.getRtspUrl(), "-c", "copy", "-acodec", "aac", getRecordingPath(config));
    }

    private List<String> getV4l2Command(CameraConfig config) {
        //TODO - ffprobe to figure out FPS and size
        return List.of("ffmpeg", "-f", "v4l2", "-framerate", "30", "-video_size", "1280x720", "-i",
                "/dev/video%d".formatted(config.getDeviceId()));
    }

    private Date getStartOfNextDay() {
        return new Date(LocalDateTime.now().with(LocalTime.MIDNIGHT)
                                           .plusDays(1)
                                           .atZone(ZoneId.systemDefault())
                                           .toInstant()
                                           .toEpochMilli());
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
