package apps.ui;

import apps.Handler;
import apps.ProgramState;
import apps.ui.rectangles.*;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Menu {
    private static final ArrayList<IElement> elements = new ArrayList<>();
    //region predefined elements
    private static final PortList portList = new PortList(0, 0, 100, 100);
    private static final PlotContainer plotContainer = new PlotContainer(100, 0, 300, 300);
    private static final TextArea log = new TextArea(0, 0, 100, 100);
    private static PortPlotGroup commandConsumer; // it's a plot in case I want to show stuff
    private static final Textbox commandLine = new Textbox(0, 0, 100,
            DevConfig.fontSize + DevConfig.vertMargin * 2, "", Menu::sendCommand,
            DevConfig.borders);
    //endregion
    private static final HashMap<ProgramState, ArrayList<IElement>> menuPresets = new HashMap<>();
    private static IElement pressed;
    private static IElement hovered;
    private static Focusable focused;

    public static void start() {
        //region create the presets for all the apps states
        //region main
        savePreset(ProgramState.main, log, portList, commandLine, plotContainer);
        //endregion
        //endregion
        elements.clear();
        elements.addAll(menuPresets.get(ProgramState.main));
    }

    //region Update Contents
    public static void update() {
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
        log.height = Math.max(0, height - commandLine.height);
        commandLine.y = height - commandLine.height - DevConfig.vertMargin;
        plotContainer.height = height;
        //endregion
        plotContainer.arrange();
    }

    private static void sendCommand(String command) {
        if (commandConsumer == null) {
            Menu.log("No port connected! I think.");
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
            Menu.log("Sent " + result + " bytes");
        });
        task.start();
        commandLine.currentDefaultText = "";
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
        for (IElement element : elements) {
            if (element.hover(pos)) {
                hovered = element;
                return;
            }
        }
    }

    public static void scroll(int steps) {
        if (hovered instanceof Scrollable) {
            ((Scrollable) hovered).scroll(steps);
        }
    }

    public static boolean release() {
        if (pressed != null) {
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
        focused.leave();
        focused = null;
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

    //region Private
    private static void savePreset(ProgramState state, IElement... presetElements) {
        menuPresets.put(state, new ArrayList<>(List.of(presetElements)));
        elements.clear();
    }

    public static void removePortPlotGroup(PortPlotGroup port) {
        plotContainer.removePortPlotGroup(port);
    }
    //endregion
}
