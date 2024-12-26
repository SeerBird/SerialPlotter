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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Menu {
    private static final ArrayList<IElement> elements = new ArrayList<>();
    private static final PortList portList = new PortList(0, 0, 100, 100);
    private static final PlotContainer plotContainer = new PlotContainer(100, 0, 300, 300);
    private static final TextArea log = new TextArea(0, 0, 100, 100);
    private static SerialPort commandConsumer;
    private static final Textbox commandLine = new Textbox(0, 200, 100, 100, "om nom nom", (String command) -> {
        if (commandConsumer == null) {
            return; // maybe say smth?
        }
        if (!commandConsumer.isOpen()) {
            return; // again, maybe say smth?
        }
        byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
        Thread task = new Thread(()->{
            int result = commandConsumer.writeBytes(bytes, bytes.length);
        });
    }, DevConfig.shell);
    private static final HashMap<ProgramState, ArrayList<IElement>> menuPresets = new HashMap<>();
    private static IElement pressed;
    private static IElement hovered;
    private static Focusable focused;

    static {
        //region create the presets for all the apps states
        //region main
        savePreset(ProgramState.main, portList, log, commandLine, plotContainer);
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
        //region resize everything
        //endregion
        portList.updatePorts();
    }

    public static void refreshMenuState() {
        elements.clear();
        elements.addAll(menuPresets.get(Handler.getState()));
    }

    public static void addPlot(SerialPort port) {
        plotContainer.addPlot(port);
    }
    public static void log(String string){ //called concurrently?
        log.log(string);
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
    public static ArrayList<IElement> getElements() {
        return elements;
    }

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
    //endregion
}
