package com.path.variable.picam.properties;

import java.util.List;

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
