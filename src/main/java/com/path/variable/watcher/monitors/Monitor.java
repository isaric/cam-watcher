package com.path.variable.watcher.monitors;

import com.path.variable.watcher.AbstractCameraComponent;
import com.path.variable.watcher.notifiers.Notifier;

import java.util.List;

public abstract class Monitor extends AbstractCameraComponent {

    protected final List<Notifier> notifiers;

    protected Monitor(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    public abstract void monitor();

    protected void notify(String message) {
        notifiers.forEach(n -> n.notify(message));
    }
}
