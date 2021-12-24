package com.path.variable.watcher;

import com.path.variable.watcher.monitors.Monitor;
import com.path.variable.watcher.recorders.Recorder;

import java.util.Objects;

import static java.lang.System.currentTimeMillis;

public class Camera {

    private static final String THREAD_TEMPLATE = "thread-%s-%d";

    private final int id;

    private final Recorder recorder;

    private final Monitor monitor;

    private Thread recorderThread;

    private Thread monitorThread;

    public Camera(Recorder recorder, Monitor monitor, int id) {
        this.recorder = recorder;
        this.monitor = monitor;
        this.id = id;
        if (monitor != null) {
            this.monitorThread = new Thread(monitor::monitor);
            monitorThread.setName(THREAD_TEMPLATE.formatted(monitor.getName(), currentTimeMillis()));
        }
        if (recorder != null) {
            this.recorderThread = new Thread(recorder::record);
            recorderThread.setName(THREAD_TEMPLATE.formatted(recorder.getName(), currentTimeMillis()));
        }

    }

    public void init() {
        recorderThread.start();
        monitorThread.start();
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

    public boolean isAlive() {
        return (monitorThread != null && monitorThread.isAlive()) || (recorderThread != null && recorderThread.isAlive());
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
