package apps;

import com.fazecast.jSerialComm.*;
import org.ejml.simple.SimpleMatrix;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PortTester {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final SimpleMatrix generator = new SimpleMatrix(new double[][]{{-1,2,0,1},
            {-5,-8,3,2},
            {-9,-1,-6,-6},
            {5,2,-1,6}});
    private static SimpleMatrix state = new SimpleMatrix(4,1);

    public static void start() {
        state.set(0,200);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        SerialPort[] ports = SerialPort.getCommPorts();
        scheduler.scheduleAtFixedRate(() -> {
            SerialPort port = ports[1];
            boolean res = port.openPort();
            if (!res) {
                //logger.info("Tester failed to open");
            }
            SimpleMatrix delta = generator.mult(state).divide(1000);
            state = state.plus(delta);
            String goo = String.valueOf(state.get(0,0));
            String gaa = String.valueOf(state.get(1,0));
            goo=goo.substring(0,goo.indexOf(".")+2);
            gaa=gaa.substring(0,gaa.indexOf(".")+2);
            byte[] buf = ("goo:"+ goo +",gaa:"+ gaa + ",gee:"+System.nanoTime()%3+",gao:"+ goo +",goa:"+ gaa + ",gea:"+System.nanoTime()%3+";").getBytes(StandardCharsets.UTF_8);
            //byte[] buf = ("goo:"+ goo+";").getBytes(StandardCharsets.UTF_8);

            port.writeBytes(buf, buf.length);

            res = port.closePort();
            if (!res) {
                //logger.info("Tester failed to close");
            }
        }, 8, 20, TimeUnit.MILLISECONDS);
    }
}
