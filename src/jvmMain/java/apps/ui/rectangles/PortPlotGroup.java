package apps.ui.rectangles;

import apps.Handler;
import apps.output.audio.Audio;
import apps.output.audio.Sound;
import apps.ui.IElement;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class PortPlotGroup extends RectElement implements SerialPortMessageListener {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    SerialPort port;
    public Button closeButton;
    public Label title;
    public Textbox baudrate;
    HashMap<String, Plot> plots;
    HashMap<String, Plot> newPlots;
    IElement pressed;
    String leftover;
    long lastReceivedTime;
    final HashMap<ScheduledExecutorService, Boolean> timeoutFlags = new HashMap<>();

    public PortPlotGroup(int x, int y, int width, int height, @NotNull SerialPort port) throws TimeoutException {
        super(x, y, width, height);
        plots = new HashMap<>();
        newPlots = new HashMap<>();
        lastReceivedTime = 0;
        leftover = "";
        this.port = port;
        title = new Label(x, y, width, height, port.getDescriptivePortName(), DevConfig.text);
        baudrate = new Textbox(x, y, width, height, String.valueOf(port.getBaudRate()), (String rate) -> {
            Thread task = new Thread(() -> {
                boolean res = false;
                try {
                    res = port.setBaudRate(Integer.parseInt(rate));
                } catch (NumberFormatException e) {
                    Audio.playSound(Sound.stopPls);
                    Menu.log("Couldn't change baudrate to rate");
                    return;
                }
                if (res) {
                    Menu.log("Changed baudrate to " + rate);
                } else {
                    Menu.log("Baudrate " + rate + " not allowed on this system");
                }
            });
            task.start();
        }, DevConfig.text, true);
        //region connect to port
        boolean res = false;
        try {
            res = Handler.timeout(port::openPort, 1000);
        } catch (TimeoutException e) {
            throw new TimeoutException("Timed out opening port");
        } catch (ExecutionException e) {
            logger.info("Exception opening port: " + e.getCause());
            Menu.log("Exception opening port: " + e.getCause());
        }
        if (!res) {
            logger.info("plotter failed to open");
            Menu.log("Failed to open port");
        } else {
            logger.info("Opened " + port.getDescriptivePortName());
            Menu.log("Opened " + port.getDescriptivePortName());
        }
        try {
            res = Handler.timeout(() -> port.addDataListener(this), 1000);
        } catch (TimeoutException e) {
            throw new TimeoutException("Timed out attaching listener to port");
        } catch (ExecutionException e) {
            logger.info("Exception attaching listener: " + e.getCause());
            Menu.log("Exception attaching listener: " + e.getCause());
        }
        if (!res) {
            logger.info("plotter failed to listen");
            Menu.log("Failed to attach listener");
        } else {
            logger.info("Listening to " + port.getDescriptivePortName());
            Menu.log("Listening to  " + port.getDescriptivePortName());
        }
        //endregion
        //region close button
        PortPlotGroup ref = this;
        closeButton = new Button(x, y, width, height, () -> {
            Thread task = new Thread(this::close);
            task.start();
            Menu.removePortPlotGroup(ref);
        }, "X", DevConfig.text);
        //endregion
        Menu.setCommandConsumer(this);
    }

    //region Serial
    @Override
    public void serialEvent(@NotNull SerialPortEvent event) {
        switch (event.getEventType()) {
            // region data available
            case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                if (System.nanoTime() - lastReceivedTime < DevConfig.messageReceivePeriod * 1000000) {
                    return;
                }
                lastReceivedTime = System.nanoTime();
                byte[] buf = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(buf, buf.length);
                boolean plotAdded = false;
                String message = leftover + new String(buf, StandardCharsets.UTF_8);
                leftover = "";
                //region read message
                while (true) {
                    timeoutFlags.replaceAll((s, v) -> true);
                    if ((!message.contains("{"))) {
                        leftover = message;
                        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                        timeoutFlags.put(scheduler, false);
                        scheduler.schedule(() -> {
                            synchronized (timeoutFlags) {
                                if (!timeoutFlags.get(scheduler)) {
                                    Menu.log(leftover);
                                    leftover = "";
                                }
                                timeoutFlags.remove(scheduler);
                            }
                        }, 100, TimeUnit.MILLISECONDS);
                        break; //nothing left to read
                    }
                    //region log text that is between packets(is this sane?)
                    String outsideOfPacket = message.substring(0, message.indexOf("{"));
                    if (!outsideOfPacket.isEmpty()) {
                        Menu.log('"' + outsideOfPacket + '"');
                        message = message.substring(message.indexOf("{"));
                    }
                    //endregion
                    if (!message.contains("}")) {
                        leftover = message;
                        break; //nothing left to read
                    }
                    String packet = message.substring(1, message.indexOf("}")); // plotName(key:value,key:value)plotName(key:value,key:value)
                    message = message.substring(message.indexOf("}") + 1); // will this break if the message ends with "}"?
                    //region read packet
                    while (true) {
                        if (!packet.contains("(")) {
                            if (!packet.isEmpty()) {
                                logger.info("Leftovers in packet: " + packet);
                                Menu.log("Leftovers in packet: " + packet);
                            }
                            break;
                        }
                        String plotName = packet.substring(0, packet.indexOf("(")); // is "" an ok plot name? prolly.
                        Plot plot = plots.get(plotName);
                        //region create plot if absent
                        if (plot == null) {
                            plot = newPlots.get(plotName);
                            if (plot == null) {
                                plot = new Plot(0, 0, width, height, plotName);
                                newPlots.put(plotName, plot);
                                plotAdded = true;
                            }
                        }
                        //endregion
                        packet = packet.substring(packet.indexOf("("));
                        if (!packet.contains(")")) {
                            logger.info("Unclosed (: " + plotName + packet);
                            Menu.log("Unclosed (: " + plotName + packet);
                            break;
                        }
                        String plotData = packet.substring(1, packet.indexOf(")")); // key:value,key:value
                        packet = packet.substring(packet.indexOf(")") + 1);
                        Menu.log(plotName + "(" + plotData + ")");
                        plotData += ",";
                        //region read plot data
                        while (plotData.contains(",")) {
                            //region read pair
                            String pair = plotData.substring(0, plotData.indexOf(",")); //a:n
                            plotData = plotData.substring(plotData.indexOf(",") + 1);
                            if (!pair.contains(":")) {
                                logger.info("Malformed pair: " + pair);
                                continue; //discard this pair
                            }
                            String key = pair.substring(0, pair.indexOf(":"));
                            String valueString = pair.substring(pair.indexOf(":") + 1);
                            float value;
                            try {
                                value = Float.parseFloat(valueString);
                            } catch (NumberFormatException e) {
                                logger.info("Number format exception: " + valueString);
                                continue; //discard this pair
                            }
                            //endregion
                            plot.addValue(key, value);
                        }
                        if (!plotData.isEmpty()) {
                            Menu.log("Leftovers in plot data: " + plotData);
                        }
                        //endregion
                    }
                    //endregion
                }
                //endregion
                if (plotAdded) {
                    Menu.update();
                }
                Handler.repaint();
                break;
            // endregion
            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                port.closePort();
                Menu.removePortPlotGroup(this);
                logger.info("Port " + port.getDescriptivePortName() + " disconnected.");
                Menu.log("Port " + port.getDescriptivePortName() + " disconnected.");
                break;
            default:
                logger.info("Unexpected event type: " + event.getEventType());
                Menu.log("Unexpected event type: " + event.getEventType());
        }
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE |
                SerialPort.LISTENING_EVENT_BREAK_INTERRUPT |
                SerialPort.LISTENING_EVENT_PORT_DISCONNECTED |
                SerialPort.LISTENING_EVENT_DATA_WRITTEN;
    }

    @Override
    public byte[] getMessageDelimiter() {
        return ";".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        return true;
    }

    //endregion
    //region Input
    @Override
    public boolean press(double x, double y) {
        for (Plot plot : plots.values()) {
            if (plot.press(x, y)) {
                pressed = plot;
                return true;
            }
        }
        if (closeButton.press(x, y)) {
            pressed = closeButton;
            return true;
        }
        if (baudrate.press(x, y)) {
            pressed = baudrate;
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

    //endregion
    public SerialPort getPort() {
        return port;
    }

    public HashMap<String, Plot> getPlots() {
        return plots;
    }

    public void refresh() {
        for (String key : newPlots.keySet()) {
            plots.put(key, newPlots.get(key));
        }
        newPlots.clear();
    }

    public void close() {
        port.removeDataListener();
        Menu.log("Stopped listening to " + port.getDescriptivePortName());
        port.closePort();
        Menu.log("Closed " + port.getDescriptivePortName());
    }
}
