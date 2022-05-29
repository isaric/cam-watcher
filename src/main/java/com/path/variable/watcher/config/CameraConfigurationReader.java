package com.path.variable.watcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.path.variable.watcher.Camera;
import com.path.variable.watcher.monitors.OpenCvMonitor;
import com.path.variable.watcher.notifiers.Notifier;
import com.path.variable.watcher.notifiers.NotifierFactory;
import com.path.variable.watcher.recorders.FFMpegRecorder;
import com.path.variable.watcher.recorders.OpenCvRecorder;
import com.path.variable.watcher.recorders.Recorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.path.variable.watcher.config.CameraConstants.ENABLE_OPENCV;

public class CameraConfigurationReader {

    private static final Logger LOG = LoggerFactory.getLogger(CameraConfigurationReader.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final NotifierFactory notifierFactory = new NotifierFactory();

    public Set<Camera> loadRecordersFromFolder(File configFolder) {
        return mapFilesFromFolderToList(configFolder, this::loadCameraFromFile).collect(Collectors.toSet());
    }

    public Set<Integer> loadStopCommandsFromFolder(File stopFolder) {
        return mapFilesFromFolderToList(stopFolder, this::loadStopCommandFromFile).flatMap(List::stream)
                                                                                  .collect(Collectors.toSet());
    }

    private <T> Stream<T> mapFilesFromFolderToList(File folder, Function<File, T> mappingFunction) {
        File[] filesInFolder = folder.listFiles();
        if (filesInFolder == null) {
            return Stream.empty();
        }
        return Arrays.stream(filesInFolder)
                .filter(Objects::nonNull)
                .filter(File::isFile)
                .map(mappingFunction)
                .filter(Objects::nonNull);
    }

    private Camera loadCameraFromFile(File configFile) {
        try {
            int cameraId = resolveCameraIdFromFileName(configFile.getName());
            CameraConfig cameraConfig = getCameraConfig(configFile);

            var monitor = cameraConfig.getMonitorType() != null && ENABLE_OPENCV
                    ? new OpenCvMonitor(cameraConfig, cameraId, getNotifiers(cameraConfig.getNotifiers())) : null;
            LOG.info("Initialized monitor for location {} at number {}",
                    cameraConfig.getLocation(), cameraId);
            var recorder = getRecorder(cameraConfig);
            LOG.info("Initialized recorder for location {} at number {} with type {}", cameraConfig.getLocation(),
                    cameraId, cameraConfig.getRecorderType());
            configFile.delete();
            return new Camera(recorder, monitor, cameraId);
        } catch (IOException ex) {
            LOG.error("Could not read configuration file {}", configFile.getAbsolutePath(), ex);
            return null;
        }
    }

    private Recorder getRecorder(CameraConfig config) {
        if (config.getRecorderType() == null) {
            return null;
        }
        return switch (config.getRecorderType()) {
            case FFMPEG -> new FFMpegRecorder(config);
            case OPEN_CV -> new OpenCvRecorder(config);
        };
    }

    private int resolveCameraIdFromFileName(String name) {
        try {
            String[] underscoreSplit = name.split("_");
            String value = underscoreSplit[underscoreSplit.length - 1].split("\\.")[0];
            return Integer.parseInt(value);
        } catch (Exception ex) {
            // use at your own risk
            return  1 + (int)(Math.random() * 100);
        }

    }

    private List<Integer> loadStopCommandFromFile(File commandFile) {
        try {
            StopCommand stopCommand = getStopCommand(commandFile);
            commandFile.delete();
            LOG.info("Stopping cameras with id(s) {}", stopCommand.getCameraIds());
            return stopCommand.getCameraIds();
        } catch (IOException ex) {
            LOG.error("Could not read delete command file {}", commandFile.getAbsolutePath(), ex);
            return null;
        }
    }

    private List<Notifier> getNotifiers(List<Map<String, Object>> params) {
        return params.stream().map(notifierFactory::createNotifier).collect(Collectors.toList());
    }

    private CameraConfig getCameraConfig(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, CameraConfig.class);
    }

    private StopCommand getStopCommand(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, StopCommand.class);
    }

}
