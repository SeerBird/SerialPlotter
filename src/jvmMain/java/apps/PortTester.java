package apps;

import apps.ui.Menu;
import apps.ui.rectangles.Plot;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.*;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

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
    private static final Float dt = 0.1F;

    public static void start() {
        state.set(0, 200);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort port = ports[1];
        port.setBaudRate(115200);
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
                    buf = ("I am a " + System.nanoTime() % 100 + " sentient \n octopus \n\n").getBytes(StandardCharsets.UTF_8);
                    port.writeBytes(buf, buf.length);
                }
            }
        });
        scheduler.scheduleAtFixedRate(() -> {
            SimpleMatrix delta = generator.mult(state).divide(1000);
            state = state.plus(delta);
            time = (time + dt) % 200;
            double sin1 = 160 * Math.sin(time)+20;
            double sin2 = 300 * Math.sin(0.5 * time + 3)-100;
            double sin3 = 130 * Math.sin(2 * time + 3);
            double exp = Math.min(Math.exp(time/10),Double.MAX_VALUE);
            byte[] buf = (
                    "{" +
                            "someSines(sin1:" + truncate(sin1) + ",sin2:" + truncate(sin2) + ")" +
                            "otherStuff(a:"+truncate(sin1+sin2)+",b:"+truncate(sin2+sin3)+",silly:"+truncate(sin1+sin3)+")"+
                            "exp(tooMuch?:"+exp+")"+
                            "}"
            ).getBytes(StandardCharsets.UTF_8);
            //byte[] buf = ("sin1:"+ sin1+";").getBytes(StandardCharsets.UTF_8);

            port.writeBytes(buf, buf.length);
        }, 8, 15000, TimeUnit.MILLISECONDS);
    }
    @NotNull
    private static String truncate(double num){
        String str = String.valueOf(num);
        return  str.substring(0, str.indexOf(".") + 3);
    }
}
