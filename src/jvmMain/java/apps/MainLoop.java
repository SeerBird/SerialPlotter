package apps;

import apps.util.Logging;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class MainLoop {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static boolean run;
    private final static Thread onShutdown = new Thread(Config::savePrefs);

    public static void run() {
        Runtime.getRuntime().addShutdownHook(onShutdown);
        run = true;
        try {
            Logging.setup();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failure creating the log files: " + e.getMessage());
        }
        int minFrameTime = 1000000000 / 60; // nanoseconds
        long last = 0;
        long now;
        while (run) {
            now = System.nanoTime();
            long timeLeft = minFrameTime - now + last;
            if (timeLeft <= 0) { // if a time of a frame has passed, tick
                Handler.update();
                Handler.out();
                last = now;
            } else { // otherwise, sleep for the time left
                Handler.out();
            }
        }
    }

    public static void terminate() {
        run = false;
    }
}
