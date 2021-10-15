package com.path.variable.picam.recorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.path.variable.picam.properties.RecorderConfig;
import com.path.variable.picam.properties.StopCommand;
import com.path.variable.picam.recorder.notifiers.Notifier;
import com.path.variable.picam.recorder.notifiers.NotifierFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecorderConfigurationReader {

    private static final Logger LOG = LoggerFactory.getLogger(RecorderConfigurationReader.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final NotifierFactory notifierFactory = new NotifierFactory();

    public List<Recorder> loadRecordersFromFolder(File configFolder) {
        return mapFilesFromFolderToList(configFolder, this::loadRecorderFromFile).collect(Collectors.toList());
    }

    public List<Integer> loadStopCommandsFromFolder(File stopFolder) {
        return mapFilesFromFolderToList(stopFolder, this::loadStopCommandFromFile).flatMap(List::stream)
                                                                                  .distinct()
                                                                                  .collect(Collectors.toList());
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

    private Recorder loadRecorderFromFile(File configFile) {
        try {
            int cameraId = resolveCameraIdFromFileName(configFile.getName());
            RecorderConfig recorderConfiguration = getRecorderConfig(configFile);

            var recorder = new Recorder(cameraId, recorderConfiguration.getDeviceId(), recorderConfiguration.getLocation(),
                    recorderConfiguration.getPath(), recorderConfiguration.getAreaMinimum(),
                    getNotifiers(recorderConfiguration.getNotifiers()));
            LOG.info("Initialized camera recorder for location {} at number {}",
                    recorder.getLocationName(), recorder.getCameraNumber());
            configFile.delete();
            return recorder;
        } catch (IOException ex) {
            LOG.error("Could not read configuration file {}", configFile.getAbsolutePath(), ex);
            return null;
        }
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
            return stopCommand.getCameraIds();
        } catch (IOException ex) {
            LOG.error("Could not read delete command file {}", commandFile.getAbsolutePath(), ex);
            return null;
        }
    }

    private List<Notifier> getNotifiers(List<Map<String, Object>> params) {
        return params.stream().map(notifierFactory::createNotifier).collect(Collectors.toList());
    }

    private RecorderConfig getRecorderConfig(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, RecorderConfig.class);
    }

    private StopCommand getStopCommand(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, StopCommand.class);
    }

}
