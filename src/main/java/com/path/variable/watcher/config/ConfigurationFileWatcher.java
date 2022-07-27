package com.path.variable.watcher.config;

import com.path.variable.watcher.Camera;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * ConfigurationFileWatcher - Watches the recorder configuration drop-in folder
 * for new camera files and stop files. Reacts only to creation events.
 * In case that a camera with an existing id is loaded it does nothing.
 * Therefore, make sure to delete that camera first using a stop command.
 */
public class ConfigurationFileWatcher {

    private final Path watcherPath;

    private final Path stopPath;

    private final Set<Camera> recorders;

    private final CameraConfigurationReader reader;

    private WatchKey mainKey;

    private WatchKey stopKey;


    public ConfigurationFileWatcher(String watcherPath, Set<Camera> recorders, CameraConfigurationReader reader) {
        this.watcherPath = Path.of(watcherPath);
        this.stopPath = Path.of(watcherPath, "/stop");
        this.recorders = recorders;
        this.reader = reader;
    }

    public void init() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchService stopWatcher = FileSystems.getDefault().newWatchService();
        mainKey = watcherPath.register(watcher, ENTRY_CREATE);
        stopKey = stopPath.register(stopWatcher, ENTRY_CREATE);
        recorders.forEach(Camera::init);
    }

    public void watch() {
        pollForEventAndSlurp(mainKey, reader::loadRecordersFromFolder, watcherPath.toFile(), this::mergeRecordersAndThreads);
        pollForEventAndSlurp(stopKey, reader::loadStopCommandsFromFolder, stopPath.toFile(), this::deleteRecordersAndStopThreads);
    }

    private <T> void pollForEventAndSlurp(WatchKey key, Function<File, Set<T>> slurpCommand, File folder, Consumer<Set<T>> merger) {
        for (WatchEvent<?> event : key.pollEvents()) {
            if (ENTRY_CREATE.equals(event.kind())) {
                merger.accept(slurpCommand.apply(folder));
            }
        }
    }

    private void mergeRecordersAndThreads(Set<Camera> newRecorders) {
        newRecorders.forEach(this::addAndRunThread);
    }

    private void addAndRunThread(Camera camera) {
        if (recorders.contains(camera)) {
            recorders.remove(camera);
            recorders.add(camera);
            camera.init();
        }
    }

    private void deleteRecordersAndStopThreads(Set<Integer> deviceIds) {
        recorders.stream().filter( r -> deviceIds.contains(r.getId())).forEach(Camera::stop);
    }
}
