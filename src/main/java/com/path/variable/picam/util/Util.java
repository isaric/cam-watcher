package com.path.variable.picam.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class Util {

    public static boolean hasNotElapsed(ZonedDateTime start, int diff, ChronoUnit unit) {
        var now = ZonedDateTime.now();
        var dur = Duration.between(start, now);
        if (unit == ChronoUnit.MINUTES) {
            return (dur.getSeconds() / 60) < diff;
        }
        return dur.get(unit) < diff;
    }

    public static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
