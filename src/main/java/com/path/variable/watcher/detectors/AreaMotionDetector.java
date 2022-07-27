package com.path.variable.watcher.detectors;

import com.path.variable.watcher.wrapper.CameraWrapper;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.opencv.imgproc.Imgproc.*;

public class AreaMotionDetector extends AbstractDetector {

    private final CameraWrapper camera;

    public AreaMotionDetector(Map<String, Object> parameters) {
        super(parameters);
        camera = get("camera");
    }

    @Override
    public boolean detect() {
        var frame1 = doConversions(camera.read());
        var frame2 = doConversions(camera.read());

        var delta = getDelta(frame1, frame2);

        var threshold = getThreshold(delta);

        var dilate = getDilated(threshold);

        var contours = getContours(dilate);

        return contours.stream().anyMatch(this::isContourLargeEnough);
    }

    private Mat doConversions(Mat initial) {
        var resize = new Mat();
        var gray = new Mat();
        var blur = new Mat();
        Imgproc.resize(initial, resize, new Size(500.00, 370.0));
        Imgproc.cvtColor(resize, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(gray, blur, new Size(21, 21), new Point(0, 0));
        initial.release();
        gray.release();
        resize.release();
        return blur;
    }

    private Mat getDelta(Mat frame1, Mat frame2) {
        var delta = new Mat();
        Core.absdiff(frame1, frame2, delta);
        frame1.release();
        frame2.release();
        return delta;
    }

    private Mat getThreshold(Mat delta) {
        var threshold = new Mat();
        threshold(delta, threshold, 25, 255, Imgproc.THRESH_BINARY);
        delta.release();
        return threshold;
    }

    private Mat getDilated(Mat threshold) {
        var dilate = new Mat();
        dilate(threshold, dilate, new Mat(), new Point(0, 0), 3);
        threshold.release();
        return dilate;
    }

    private boolean isContourLargeEnough(MatOfPoint contour) {
        Mat clone = contour.clone();
        boolean result = contourArea(clone) > (double) get("motion.contour.area");
        clone.release();
        contour.release();
        return result;
    }

    private List<MatOfPoint> getContours(Mat dilate) {
        var contours = new ArrayList<MatOfPoint>();
        findContours(dilate, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        dilate.release();
        return contours;
    }
}
