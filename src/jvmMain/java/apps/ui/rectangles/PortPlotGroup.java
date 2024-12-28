package apps.ui.rectangles;

import apps.Handler;
import apps.ui.IElement;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Logger;

public class PortPlotGroup extends RectElement implements SerialPortMessageListener {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    SerialPort port;
    public Button close;
    HashMap<String, Plot> plots;
    HashMap<String, Plot> newPlots;
    IElement pressed;
    String leftover;
    long lastReceivedTime;

    public PortPlotGroup(int x, int y, int width, int height, @NotNull SerialPort port) {
        super(x, y, width, height);
        plots = new HashMap<>();
        newPlots = new HashMap<>();
        lastReceivedTime = 0;
        leftover = "";
        this.port = port;
        boolean res = port.openPort();
        if (!res) {
            logger.info("plotter failed to open");
            Menu.log("Failed to open port");
        } else{
            logger.info("Opened port "+port.getDescriptivePortName());
            Menu.log("Opened port "+port.getDescriptivePortName());
        }
        res = port.addDataListener(this);
        if (!res) {
            logger.info("plotter failed to listen");
            Menu.log("Failed to attach listener to port");
        }else{
            logger.info("Listening to "+port.getDescriptivePortName());
            Menu.log("Listening to  "+port.getDescriptivePortName());
        }
        PortPlotGroup ref = this;
        close = new Button(x, y, width, height, () -> {
            port.closePort();
            Menu.removePortPlotGroup(ref);
        }, "X", DevConfig.borders);
        Menu.setCommandConsumer(this);
    }

    @Override
    public boolean press(double x, double y) {
        for (Plot plot : plots.values()) {
            if (plot.press(x, y)) {
                pressed = plot;
                return true;
            }
        }
        if (close.press(x, y)) {
            pressed = close;
            return true;
        }
        return false; //will change if there are more than 1
    }

    @Override
    public void release() {
        if (pressed == null) {
            return; // shouldn't happen
        }
        pressed.release();
        pressed = null;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE |
                SerialPort.LISTENING_EVENT_BREAK_INTERRUPT |
                SerialPort.LISTENING_EVENT_PORT_DISCONNECTED |
                SerialPort.LISTENING_EVENT_DATA_WRITTEN;
    }

    @Override
    public void serialEvent(@NotNull SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                if (System.nanoTime() - lastReceivedTime < DevConfig.messageReceivePeriod * 1000000) {
                    return;
                }
                lastReceivedTime = System.nanoTime();
                byte[] buf = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(buf, buf.length);
                String message = leftover+new String(buf, StandardCharsets.UTF_8);
                leftover="";
                //region read message
                while (message.contains(";")) {
                    String packet = message.substring(0, message.indexOf(";")); // a:n,b:m,...,z:k
                    Menu.log(packet);
                    packet+=",";
                    if (!packet.contains(",")) {
                        logger.info("Missing ',' before ';', what happened?");
                        message = message.substring(message.indexOf(";")+1);
                        continue; // discard this packet
                    }
                    //region read packet
                    while (packet.contains(",")) {
                        //region read pair
                        String pair = packet.substring(0, packet.indexOf(",")); //a:n
                        if (!pair.contains(":")) {
                            logger.info("Missing ':' before ',', what happened?");
                            packet = packet.substring(packet.indexOf(",")+1);
                            continue; //discard this pair
                        }
                        String key = pair.substring(0, pair.indexOf(":"));
                        String valueString = pair.substring(pair.indexOf(":") + 1);
                        float value;
                        try {
                            value = Float.parseFloat(valueString);
                        } catch (NumberFormatException e) {
                            logger.info("Number format exception: "+valueString);
                            packet = packet.substring(packet.indexOf(",")+1);
                            continue; //discard this pair
                        }
                        //region put the value in the plots
                        if (plots.get(key) == null) {
                            newPlots.put(key, new Plot(0, 0, width, height, key));
                            newPlots.get(key).addValue(value);
                            Menu.update();
                            Handler.repaint(x, y, width, height);
                        } else {
                            Plot plot = plots.get(key);
                            plot.addValue(value);
                            Handler.repaint(plot.x, plot.y, plot.width, plot.height);
                        }
                        //endregion
                        //endregion
                        packet = packet.substring(packet.indexOf(",")+1);
                    }
                    if(!packet.isEmpty()){
                        logger.info("Leftovers of packet: "+packet);
                        message = message.substring(message.indexOf(";")+1);
                        continue; //discard this packet
                    }
                    //endregion
                    message = message.substring(message.indexOf(";")+1);
                }
                if(!message.isEmpty()){
                    leftover = message;
                    logger.info("Leftovers of message: "+message);
                }
                //endregion
                Handler.repaint();
                break;
            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                port.closePort();
                Menu.removePortPlotGroup(this);
                logger.info("Port " + port.getDescriptivePortName() + " disconnected.");
                Menu.log("Port " + port.getDescriptivePortName() + " disconnected.");
                break;
        }
    }

    public SerialPort getPort() {
        return port;
    }

    public HashMap<String, Plot> getPlots() {
        return plots;
    }

    @Override
    public byte[] getMessageDelimiter() {
        return ";".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        return true;
    }

    public void refresh() {
        for (String key : newPlots.keySet()) {
            plots.put(key, newPlots.get(key));
        }
        newPlots.clear();
    }
}
