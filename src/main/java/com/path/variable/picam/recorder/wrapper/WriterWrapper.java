package com.path.variable.picam.recorder.wrapper;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import java.util.Date;

import static com.path.variable.picam.properties.RecorderConstants.STANDARD_DATE_FORMAT;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_COMPLEX;

public class WriterWrapper {

    private final VideoWriter writer;

    private final String cameraName;

    private final boolean addTimestamp;

    public WriterWrapper(VideoWriter writer, String cameraName, boolean addTimestamp) {
        this.writer = writer;
        if (writer == null) {
            throw new IllegalStateException("Writer object is null! Terminating sequence!");
        }
        this.cameraName = cameraName;
        this.addTimestamp = addTimestamp;
    }

    public void open(String filename, int fourcc, double fps, Size frameSize, boolean isColor) {
        writer.open(filename, fourcc, fps, frameSize, isColor);
    }

    public void writeFrame(Mat frame) {
        if (addTimestamp) {
            Imgproc.putText(frame, String.format("CAM %s %s",cameraName , STANDARD_DATE_FORMAT.format(new Date())),
                    new Point(50, 50), FONT_HERSHEY_COMPLEX, 0.50, new Scalar(255, 255, 255));
        }
        writer.write(frame);
        frame.release();
    }

    public void release() {
        writer.release();
    }
}
