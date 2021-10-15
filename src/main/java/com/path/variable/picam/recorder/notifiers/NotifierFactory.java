package com.path.variable.picam.recorder.notifiers;

import com.path.variable.commons.slack.SlackHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NotifierFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NotifierFactory.class);

    public Notifier createNotifier(Map<String, Object> params) {
        String typeValue = (String) params.get("type");
        return switch (NotifierType.getTypeByValue(typeValue)) {
            case SLACK -> getSlackNotifier(params);
            case NO_OP -> getNoOpNotifier();
        };
    }

    private Notifier getSlackNotifier(Map<String, Object> params) {
        return new Notifier() {

            private final SlackHook hook = new SlackHook((String) params.get("url"));

            @Override
            public void notify(String message) {
                hook.sendPlainText(message);
            }
        };
    }

    private  Notifier getNoOpNotifier() {
        return LOG::info;
    }

}
