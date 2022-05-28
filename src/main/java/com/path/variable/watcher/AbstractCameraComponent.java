package com.path.variable.watcher;

import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Base class extended by both monitor and recorder hierarchies.
 * Provides some convenience methods and the name identifier to them.
 */
public abstract class AbstractCameraComponent {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCameraComponent.class);

    public abstract String getName();

    protected abstract void stop();

    protected Supplier<VideoCapture> getVideoSupplier(Integer deviceId, String path) {
        LOG.debug("Recreating Video Capture deviceId = {}, path = {}", deviceId, path);
        return () -> {
            if (path == null) {
                if (deviceId != null) return new VideoCapture(deviceId);
                throw new IllegalArgumentException("Neither device id nor camera path have been defined!");
            }
            return new VideoCapture(path);
        };
    }

}
