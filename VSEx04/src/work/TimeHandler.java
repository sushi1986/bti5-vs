package work;

/**
 * VS Lab4
 * @author Phillip Gesien, Raphael Hiesgen
 */

public class TimeHandler {
    private static long offset = 0;

    public static void adjustTime(long offset) {
        TimeHandler.offset += offset;
    }

    public static long generateTimeStamp() {
        return System.currentTimeMillis() + offset;
    }

}
