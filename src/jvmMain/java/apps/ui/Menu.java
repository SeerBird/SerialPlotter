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
            DevConfig.fontSize+DevConfig.vertMargin*2, "",
            (String command) -> {
                if (commandConsumer == null) {
                    return; // maybe say smth?
                }
                SerialPort port = commandConsumer.getPort();
                if (!port.isOpen()) {
                    return; // again, maybe say smth?
                }
                byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
                Thread task = new Thread(() -> {
                    int result = port.writeBytes(bytes, bytes.length);
                    // maybe SAY SMTH..??
                });
                task.start();
            }, DevConfig.shell);
    //endregion
    private static final HashMap<ProgramState, ArrayList<IElement>> menuPresets = new HashMap<>();
    private static IElement pressed;
    private static IElement hovered;
    private static Focusable focused;

    static {
        //region create the presets for all the apps states
        //region main
        savePreset(ProgramState.main, log, portList, commandLine, plotContainer);
        //endregion
        /*
        //region host
        savePreset(ProgramState.host,
                new Button(DevConfig.WIDTH / 2 - 75, 200, 150, 150, Handler::hostToPlayServer, "Play", DevConfig.shell),
                new Textbox(DevConfig.WIDTH / 2 - 75, 370, 150, 40, Config.getServerName(), (serverName) -> {
                    Config.setServerName(serverName);
                    Broadcaster.setMessage(Config.getServerName());
                }, DevConfig.turtle));
        //endregion
        //region connect
        savePreset(ProgramState.discover,
                serverList);
        //endregion
        //region lobby
        savePreset(ProgramState.lobby,
                playerList,
                lobbyWaiting);
        //endregion
        //region playServer
        savePreset(ProgramState.playServer, scoreBoard);
        //endregion
        //region playClient
        savePreset(ProgramState.playClient, scoreBoard);
        //endregion

         */
        //endregion
        refreshMenuState();
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
            plotContainer.y = 0;
        } else {
            portList.width = 0;
            commandLine.width = 0;
            log.width = 0;
            plotContainer.width = width;
            plotContainer.x = 0;
            plotContainer.y = 0;
        }
        portList.updatePorts(); // determines portList's height
        if(portList.height>height){
            commandLine.text = "Stop contorting the window!";
        }
        log.height = Math.max(0,height- commandLine.height);
        commandLine.y = height- commandLine.height-DevConfig.vertMargin;
        plotContainer.height = height;
        //endregion
        plotContainer.arrange();
        log.refresh();
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
        focused = element;
    }

    public static void unfocus() { //I can make this multilevel. no need though.
        focused.leave();
        focused = null;
    }

    public static void resize() {

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

    public static void removePortPlotGroup(PortPlotGroup plot) {
        plotContainer.removePlot(plot);
    }
    //endregion
}
