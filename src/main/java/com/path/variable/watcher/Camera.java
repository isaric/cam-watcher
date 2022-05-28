package com.path.variable.watcher;

import com.path.variable.watcher.monitors.Monitor;
import com.path.variable.watcher.recorders.Recorder;

import java.util.Objects;

public class Camera {

    private final int id;

    private final Recorder recorder;

    private final Monitor monitor;

    public Camera(Recorder recorder, Monitor monitor, int id) {
        this.recorder = recorder;
        this.monitor = monitor;
        this.id = id;

    }

    public void init() {
        recorder.record();
        monitor.monitor();
    }

    public void stop() {
        if (recorder != null) {
            recorder.stop();
        }
        if (monitor != null) {
            monitor.stop();
        }
    }

    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Camera camera = (Camera) o;
        return id == camera.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
