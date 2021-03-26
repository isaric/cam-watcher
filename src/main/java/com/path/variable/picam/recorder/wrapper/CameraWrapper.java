package com.path.variable.picam.recorder.wrapper;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import static com.path.variable.commons.properties.Configuration.getConfiguration;
import static org.opencv.videoio.Videoio.CAP_PROP_AUTO_EXPOSURE;
import static org.opencv.videoio.Videoio.CAP_PROP_EXPOSURE;

public class CameraWrapper implements Runnable{

    protected final VideoCapture camera;

    public CameraWrapper(VideoCapture camera) {
        this.camera = camera;
        if (camera == null) {
            throw new IllegalStateException("Camera object is null! Terminating sequence!");
        }
        camera.set(CAP_PROP_AUTO_EXPOSURE, 1);
        camera.set(CAP_PROP_EXPOSURE, getConfiguration().getDouble("global.exposure"));
    }

    public Mat read() {
        Mat target = new Mat();
        var success = true;
        try {
            success = camera.read(target);
        } catch (Exception ex) {
            throw new IllegalStateException("Exception while reading image! Throwing exception!");
        }
        if (!success) {
            throw new IllegalStateException("Could not read image. Camera object returned null!");
        }
        return target;
    }

    public void release() {
        camera.release();
    }

    public boolean isOpened() {
        return camera.isOpened();
    }


    @Override
    public void run() {
        //empty method for subclass
    }
}
