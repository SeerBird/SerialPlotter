package apps;

import com.fazecast.jSerialComm.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PortTester {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        SerialPort[] ports = SerialPort.getCommPorts();
        scheduler.scheduleAtFixedRate(() -> {
            SerialPort port = ports[1];
            boolean res = port.openPort();
            if (!res) {
                logger.info("Tester failed to open");
            }
            byte[] buf = "goo:100;gaa:50;".getBytes(StandardCharsets.UTF_8);
            port.writeBytes(buf, buf.length);

            res = port.closePort();
            if (!res) {
                logger.info("Tester failed to close");
            }
        }, 8, 20, TimeUnit.MILLISECONDS);
    }
}
