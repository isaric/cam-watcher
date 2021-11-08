package com.path.variable.picam.properties;

import java.util.List;

/**
 * StopCommand - a pojo for loading a list of ids from the stop drop-in folder
 * Just a simple list of integers
 */
public class StopCommand {

    private List<Integer> cameraIds;

    public List<Integer> getCameraIds() {
        return cameraIds;
    }

    public StopCommand setCameraIds(List<Integer> cameraIds) {
        this.cameraIds = cameraIds;
        return this;
    }
}
