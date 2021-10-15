package com.path.variable.picam.recorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class ConfigurationFileWatcher {

    private final Path watcherPath;

    private final Path stopPath;

    private final List<Thread> threads;

    private final List<Recorder> recorders;

    private final RecorderConfigurationReader slurper;

    private WatchKey mainKey;

    private WatchKey stopKey;


    public ConfigurationFileWatcher(String watcherPath, List<Thread> threads, List<Recorder> recorders) {
        this.watcherPath = Path.of(watcherPath);
        this.stopPath = Path.of(watcherPath, "/stop");
        this.threads = threads;
        this.recorders = recorders;
        this.slurper = new RecorderConfigurationReader();
    }

    public void init() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        mainKey = watcherPath.register(watcher, ENTRY_CREATE);
        stopKey = stopPath.register(watcher, ENTRY_CREATE);
        recorders.forEach(this::startRecording);
    }

    public void watch() {
        pollForEventAndSlurp(mainKey, slurper::loadRecordersFromFolder, watcherPath.toFile(), this::mergeRecordersAndThreads);
        pollForEventAndSlurp(stopKey, slurper::loadStopCommandsFromFolder, stopPath.toFile(), this::deleteRecordersAndStopThreads);
    }

    private <T> void pollForEventAndSlurp(WatchKey key, Function<File, List<T>> slurpCommand, File folder, Consumer<List<T>> merger) {
        for (WatchEvent<?> event : key.pollEvents()) {
            if (ENTRY_CREATE.equals(event.kind())) {
                merger.accept(slurpCommand.apply(folder));
            }
        }
    }

    private void mergeRecordersAndThreads(List<Recorder> newRecorders) {
        newRecorders.forEach(this::addToListsAndRunThread);
    }

    private void addToListsAndRunThread(Recorder recorder) {
        recorders.add(recorder);
        startRecording(recorder);
    }

    private void startRecording(Recorder recorder) {
        Runnable run = recorder::record;
        var t = new Thread(run);
        t.setName(format("Recorder-%d", recorder.getCameraNumber()));
        threads.add(t);
        t.start();
    }

    private void deleteRecordersAndStopThreads(List<Integer> deviceIds) {
        recorders.stream().filter( r -> deviceIds.contains(r.getCameraNumber())).forEach(Recorder::stop);
    }
}
