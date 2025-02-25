package apps.ui;

import apps.Handler;
import apps.ProgramState;
import apps.output.audio.Audio;
import apps.output.audio.Sound;
import apps.ui.rectangles.*;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Menu {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final ArrayList<IElement> elements = new ArrayList<>();
    //region predefined elements
    private static final PortList portList = new PortList(0, 0, 100, 100);
    private static final PlotContainer plotContainer = new PlotContainer(100, 0, 300, 300);
    private static final TextArea log = new TextArea(0, 0, 100, 100);
    private static PortPlotGroup commandConsumer; // it's a plot in case I want to show stuff
    private static final Textbox commandLine = new Textbox(0, 0, 100,
            DevConfig.fontSize + DevConfig.vertMargin * 2, "", Menu::sendCommand,
            DevConfig.borders, false);
    //endregion
    private static final HashMap<ProgramState, ArrayList<IElement>> menuPresets = new HashMap<>();
    private static IElement pressed;
    private static IElement hovered;
    private static Focusable focused;
    private static Integer middleOrigin;
    private static final ArrayList<String> commandLog = new ArrayList<>();
    private static int commandID = 0;
    private static boolean waiting = false;
    private static boolean toUpdate = true;
    private static final Thread onShutdown = new Thread(() -> {
        for (PortPlotGroup port : plotContainer.getPortPlotGroups()) {
            port.close();
        }
    });

    public static void start() {
        Runtime.getRuntime().addShutdownHook(onShutdown);
        //region create the presets for all the apps states
        //region main
        savePreset(ProgramState.main, log, portList, commandLine, plotContainer);
        //endregion
        //endregion
        elements.clear();
        elements.addAll(menuPresets.get(ProgramState.main));
    }

    //region Update Contents
    public static void queueUpdate() {
        toUpdate = true;
    }

    public static void update() {
        if (!toUpdate) {
            return;
        }
        toUpdate = false;
        ScheduledFuture<?> future = Handler.getScheduler().schedule(() -> logger.info("We're fucked!"), 1500, TimeUnit.MILLISECONDS);
        try {
            //region resize and arrange everything
            int width = Handler.getWindow().getWidth();
            int height = Handler.getWindow().getHeight();
            if (width > 600) {
                portList.width = width / 3;
                commandLine.width = width / 3;
                log.width = width / 3;
                plotContainer.width = width - width / 3;
                plotContainer.x = width / 3;
            } else {
                portList.width = 0;
                commandLine.width = 0;
                log.width = 0;
                plotContainer.width = width;
                plotContainer.x = 0;
            }
            portList.y = 0;
            portList.x = 0;
            plotContainer.y = 0;
            portList.updateButtons(); // determines portList's height
            if (portList.height > height) {
                commandLine.text = "Stop contorting the window!";
            }
            log.y = portList.height;
            log.height = Math.max(0, height - commandLine.height - portList.height);
            commandLine.y = height - commandLine.height;
            plotContainer.height = height;
            //endregion
            plotContainer.update();
        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            future.cancel(true);
        }
    }

    private static void sendCommand(String command) {
        if (command.isEmpty()) {
            return;
        }
        //region add command to commandLog
        commandLog.add(command);
        if (commandLog.size() > DevConfig.maxCommandLogSize) {
            commandLog.remove(0);
        }
        commandID = commandLog.size();
        //endregion
        if (commandConsumer == null) {
            Menu.log("No port connected! I think...");
            return;
        }
        SerialPort port = commandConsumer.getPort();
        if (!port.isOpen()) {
            Menu.log("Port closed.");
            return;
        }
        byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
        Thread task = new Thread(() -> {
            int result = port.writeBytes(bytes, bytes.length);
            Menu.log("Sent \"" + command + "\" as " + result + " byte" + (result == 1 ? "" : "s"));
        });
        task.start();
        commandLine.text = "";
        Audio.playSound(Sound.pewPew);
        //commandLine.enter(); // I think this is redundant? each textBox has a leaveOnSubmit property now...
    }

    public static void refreshMenuState() {
        elements.clear();
        elements.addAll(menuPresets.get(Handler.getState()));
    }

    public static void addPortPlotGroup(SerialPort port) {
        plotContainer.addPortPlotGroup(port);
    }

    public static void log(String string) { //called concurrently?
        log.log(string);
    }

    public static void setCommandConsumer(PortPlotGroup port) {
        commandConsumer = port;
    }

    //endregion
    //region Input
    public static boolean press(ArrayRealVector pos) {
        for (IElement element : elements) {
            if (element.press(pos)) {
                pressed = element;
                return true;
            }
        }
        return false;
    }

    public static void hover(ArrayRealVector pos) {
        for (IElement element : new ArrayList<>(elements)) {
            if (element.hover(pos)) {
                hovered = element;
                break;
            }
        }
        if (middleOrigin != null) {
            int shift = (int) pos.getEntry(1) - middleOrigin;
            shift = (int) (Math.log(Math.abs(shift) + 1) * Math.signum(shift));
            logger.info(String.valueOf(shift));
            scroll(shift);
        }
    }

    public static void scroll(int steps) {
        if (hovered instanceof Scrollable) {
            ((Scrollable) hovered).scroll(steps);
        }
    }

    public static boolean release() {
        if (pressed != null) {
            if (waiting) {
                Menu.log("Still waiting!");
                return false;
            }
            pressed.release();
            pressed = null;
            return true;
        }
        return false;
    }

    public static void focus(Focusable element) {
        if (focused != null && focused != element) {
            focused.leave();
        }
        focused = element;
    }

    public static void unfocus() { //I can make this multilevel. no need though.
        if (focused == null) {
            return;
        }
        focused.leave();
        focused = null;
    }

    public static void commandShift(int n) {
        commandID = Math.max(Math.min(commandID + n, commandLog.size()), 0);
        if (commandID == commandLog.size()) {
            commandLine.setText("");
        } else {
            commandLine.setText(commandLog.get(commandID));
        }
        commandLine.displayIndex = 0;
        commandLine.shift(commandLine.text.length());
        Handler.repaint(commandLine.x, commandLine.y, commandLine.width, commandLine.height);
    }

    //endregion
    //region Rendering helpers

    public static ArrayList<IElement> getElements() {
        return elements;
    }

    public static boolean isCommandConsumer(PortPlotGroup plot) {
        return plot == commandConsumer;
    }

    //endregion
    public static Focusable getFocused() {
        return focused;
    }

    public static IElement getPressed() {
        return pressed;
    }

    public static void removePortPlotGroup(PortPlotGroup port) {
        plotContainer.removePortPlotGroup(port);
    }

    //region scroll with middle button
    public static void middleDown(int y) {
        middleOrigin = y;
    }

    public static void middleUp() {
        middleOrigin = null;
    }

    //endregion
    //region Private
    private static void savePreset(ProgramState state, IElement... presetElements) {
        menuPresets.put(state, new ArrayList<>(List.of(presetElements)));
        elements.clear();
    }

    public static void pause(int maxMillis) {
        waiting = true;
        Handler.getScheduler().schedule(() -> waiting = false, maxMillis, TimeUnit.MILLISECONDS);
    }

    public static void unpause() {
        waiting = false;
    }


    //endregion
}
