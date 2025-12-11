package io.github.xivqn.utils;

public class TimeUtils {

    /**
     * Formats the given time in milliseconds to a string.
     *
     * @param totalMillis the time in milliseconds
     * @return the formatted time
     */
    public static String formatTime(long totalMillis) {
        long millis = totalMillis % 1000;
        long second = (totalMillis / 1000) % 60;
        long minute = (totalMillis / 1000 / 60) % 60;
        long hour = (totalMillis / 1000 / 60 / 60) % 24;
        long day = (totalMillis / 1000 / 60 / 60 / 24);

        return String.format("%dd %02d:%02d:%02d.%03d", day, hour, minute, second, millis);
    }

}
