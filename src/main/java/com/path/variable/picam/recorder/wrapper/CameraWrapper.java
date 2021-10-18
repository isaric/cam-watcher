package com.path.variable.picam.recorder.wrapper;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

import static com.path.variable.commons.properties.Configuration.getConfiguration;
import static org.opencv.videoio.Videoio.CAP_PROP_AUTO_EXPOSURE;
import static org.opencv.videoio.Videoio.CAP_PROP_EXPOSURE;

public class CameraWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(CameraWrapper.class);

    private VideoCapture camera;

    private final Supplier<VideoCapture> cameraSupplier;

    private final Integer retryAttempts;


    public CameraWrapper(Supplier<VideoCapture> cameraSupplier, Integer retryAttempts) {
        this.cameraSupplier = cameraSupplier;
        this.camera = cameraSupplier.get();
        this.retryAttempts = retryAttempts;
        if (camera == null) {
            throw new IllegalStateException("Camera object is null! Terminating sequence!");
        }
        camera.set(CAP_PROP_AUTO_EXPOSURE, 1);
        camera.set(CAP_PROP_EXPOSURE, getConfiguration().getDouble("global.exposure"));
    }

    public Mat read() {
        int retry = retryAttempts != null ? retryAttempts + 1 : 1;
        while (true) {
            try {
                return readInternal();
            } catch (IllegalStateException ex) {
                retry = retry - 1;
                LOG.info("Error in camera. Retry attempts left: {}", retry);
                if (retry <= 0) {
                    throw ex;
                }
                this.camera.release();
                this.camera = null;
            }
        }
    }

    private Mat readInternal() {
        VideoCapture cam = getCamera();
        Mat target = new Mat();
        var success = true;
        try {
            success = cam.read(target);
        } catch (Exception ex) {
            throw new IllegalStateException("Exception while reading image! Throwing exception!");
        }
        if (!success) {
            throw new IllegalStateException("Could not read image. Camera object returned null!");
        }
        return target;
    }

    public VideoCapture getCamera() {
        if (this.camera == null) {
            this.camera = this.cameraSupplier.get();
        }
        return this.camera;
    }

    public void release() {
        camera.release();
    }

    public boolean isOpened() {
        return camera.isOpened();
    }
}
