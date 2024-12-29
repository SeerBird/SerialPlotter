package apps;

import apps.ui.Menu;
import apps.ui.rectangles.Plot;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.*;
import org.ejml.simple.SimpleMatrix;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PortTester {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final SimpleMatrix generator = new SimpleMatrix(new double[][]{{-1, 2, 0, 1},
            {-5, -8, 3, 2},
            {-9, -1, -6, -6},
            {5, 2, -1, 6}});
    private static SimpleMatrix state = new SimpleMatrix(4, 1);
    private static Float time = 0F;
    private static final Float dt = 0.01F;

    public static void start() {
        state.set(0, 200);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort port = ports[1];
        port.setBaudRate(20000);
        port.openPort();
        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE |
                        SerialPort.LISTENING_EVENT_BREAK_INTERRUPT |
                        SerialPort.LISTENING_EVENT_PORT_DISCONNECTED |
                        SerialPort.LISTENING_EVENT_DATA_WRITTEN;
            }
            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    byte[] buf = new byte[port.bytesAvailable()];
                    int numRead = port.readBytes(buf, buf.length);
                    String message = new String(buf, StandardCharsets.UTF_8);
                    logger.info("Tester got '" + message + "'");
                    buf = ("(gaoo:"+ System.nanoTime() % 3 + ")").getBytes(StandardCharsets.UTF_8);
                    port.writeBytes(buf, buf.length);
                }
            }
        });
        scheduler.scheduleAtFixedRate(() -> {
            SimpleMatrix delta = generator.mult(state).divide(1000);
            state = state.plus(delta);
            String goo = String.valueOf(state.get(0, 0));
            String gaa = String.valueOf(state.get(1, 0));
            time=(time+dt)%200;
            goo = String.valueOf(160*Math.sin(time));
            gaa = String.valueOf(200*Math.sin(0.5*time+3));
            goo = goo.substring(0, goo.indexOf(".") + 3);
            gaa = gaa.substring(0, gaa.indexOf(".") + 3);
            byte[] buf = ("{kill(goo:" + goo + ",gaa:" + gaa +")}").getBytes(StandardCharsets.UTF_8);
            //byte[] buf = ("goo:"+ goo+";").getBytes(StandardCharsets.UTF_8);

            port.writeBytes(buf, buf.length);
        }, 8, 10, TimeUnit.MILLISECONDS);
    }
}
