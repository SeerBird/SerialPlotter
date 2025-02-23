package apps;

import apps.ui.Menu;
import apps.ui.rectangles.Plot;
import apps.util.DevConfig;
import apps.util.GFormatter;
import com.fazecast.jSerialComm.*;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
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
        //region set up my logger
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        ConsoleHandler console = new ConsoleHandler();
        GFormatter formatter = new GFormatter();
        //region file logger
        /*
        FileHandler fileTxt;
        try {
            fileTxt = new FileHandler(Util.path + "SerialLog%u.%g.txt", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileTxt.setFormatter(formatter);
        fileTxt.setLevel(Level.INFO);
        logger.addHandler(fileTxt);
         */
        //endregion
        console.setFormatter(formatter);
        console.setLevel(Level.INFO);


        logger.addHandler(console);
        //endregion
        //region silence default console

        Logger rootLogger = Logger.getLogger("");
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }
        //endregion
        state.set(0, 200);
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
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
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
            int length = port.writeBytes(buf, buf.length);
            logger.info("Tester sending shit: "+length+" bytes");
        }, 8, 10, TimeUnit.MILLISECONDS);
        /*
        scheduler.scheduleAtFixedRate(() -> {
            byte[] buf = ("A 8 0 1 3 BABABOI live and prosper").getBytes(StandardCharsets.UTF_8);
            port.writeBytes(buf, buf.length);}, 10, 1501, TimeUnit.MILLISECONDS);
         */
    }
    @NotNull
    private static String truncate(double num){
        String str = String.valueOf(num);
        return  str.substring(0, str.indexOf(".") + 3);
    }
}
