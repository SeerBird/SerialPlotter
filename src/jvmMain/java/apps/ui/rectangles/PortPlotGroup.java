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
import com.sun.tools.attach.AttachOperationFailedException;
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
    final HashMap<Integer, Boolean> timeoutFlags = new HashMap<>();

    public PortPlotGroup(int x, int y, int width, int height, @NotNull SerialPort port) {
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
                    res = Handler.timeout(() -> port.setBaudRate(Integer.parseInt(rate)), 1000);
                } catch (NumberFormatException e) {
                    Audio.playSound(Sound.stopPls);
                    logger.info("Couldn't change baudrate: is this a number?");
                    Menu.log("Couldn't change baudrate: is this a number?");
                    return;
                } catch (ExecutionException e) {
                    logger.info("Couldn't change baudrate");
                    Menu.log("Couldn't change baudrate");
                    return;
                } catch (TimeoutException e) {
                    logger.info("Timed out changing baudrate");
                    Menu.log("Timed out changing baudrate");
                    return;
                }
                if (res) {
                    logger.info("Changed baudrate to " + rate);
                    Menu.log("Changed baudrate to " + rate);
                } else {
                    logger.info("Baudrate " + rate + " not allowed on this system");
                    Menu.log("Baudrate " + rate + " not allowed on this system");
                }
            });
            task.start();
        }, DevConfig.text, true);
        //region connect to port
        Thread task = new Thread(() -> {
            Menu.pause(3000);
            boolean opening = false;
            boolean listening = false;
            try {
                opening = Handler.timeout(port::openPort, 1000);
            } catch (TimeoutException e) {
                logger.info("Timed out opening port");
                Menu.log("Timed out opening port");
            } catch (ExecutionException e) {
                logger.info("Exception opening port: " + e.getMessage());
                Menu.log("Exception opening port: " + e.getMessage());
            }
            if (!opening) {
                logger.info("Failed to open port");
                Menu.log("Failed to open port");
            } else {
                logger.info("Opened " + port.getDescriptivePortName());
                Menu.log("Opened " + port.getDescriptivePortName());
            }
            try {
                listening = Handler.timeout(() -> {
                    port.flushDataListener();
                    port.flushIOBuffers();
                    port.removeDataListener();
                    return port.addDataListener(this);
                }, 1000);
            } catch (TimeoutException e) {
                logger.info("Timed out attaching listener to port");
                Menu.log("Timed out attaching listener to port");
            } catch (ExecutionException e) {
                logger.info("Exception attaching listener: " + e.getMessage());
                Menu.log("Exception attaching listener: " + e.getMessage());
            }
            if (!listening) {
                logger.info("Failed to attach listener");
                Menu.log("Failed to attach listener");
            } else {
                logger.info("Listening to  " + port.getDescriptivePortName());
                Menu.log("Listening to  " + port.getDescriptivePortName());
            }
            if (!listening) {
                Menu.removePortPlotGroup(this);
            }
            Menu.unpause();
        });
        task.start();
        //endregion
        //region close button
        PortPlotGroup ref = this;
        closeButton = new Button(x, y, width, height, () -> {
            Thread closeTask = new Thread(() -> {
                try {
                    Handler.timeout(this::close, 1000);
                } catch (ExecutionException e) {
                    logger.info("Exception closing "+port.getDescriptivePortName());
                    Menu.log("Exception closing "+port.getDescriptivePortName());
                } catch (TimeoutException e) {
                    logger.info("Timed out closing "+port.getDescriptivePortName());
                    Menu.log("Timed out closing "+port.getDescriptivePortName());
                }
            });
            closeTask.start();
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
                        int uniqueIndex = 0;
                        while(timeoutFlags.containsKey(uniqueIndex)){
                            uniqueIndex++;
                        }
                        timeoutFlags.put(uniqueIndex, false);
                        int finalUniqueIndex = uniqueIndex;
                        Handler.getScheduler().schedule(() -> {
                            synchronized (timeoutFlags) {
                                if (!timeoutFlags.get(finalUniqueIndex)) {
                                    Menu.log(leftover);
                                    leftover = "";
                                }
                                timeoutFlags.remove(finalUniqueIndex);
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
                boolean plotAddedFinal = plotAdded;
                Thread task = new Thread(()->{
                    if (plotAddedFinal) {
                    Menu.update();
                    Handler.repaint();
                }});
                task.start();
                break;
            // endregion
            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                Thread disconnectTask = new Thread(()->{
                    port.closePort();
                    Menu.removePortPlotGroup(this);
                    logger.info("Port " + port.getDescriptivePortName() + " disconnected.");
                    Menu.log("Port " + port.getDescriptivePortName() + " disconnected.");
                });
                disconnectTask.start();
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

    public boolean close() {
        port.removeDataListener();
        Menu.log("Stopped listening to " + port.getDescriptivePortName());
        boolean res = port.closePort();
        if (res) {
            Menu.log("Closed " + port.getDescriptivePortName());
        } else {
            Menu.log("Failed to close " + port.getDescriptivePortName());
        }
        return res;
    }

    public static class DataListenerAttachException extends Exception {

        public DataListenerAttachException(String message) {
            super(message);
        }
    }
}
