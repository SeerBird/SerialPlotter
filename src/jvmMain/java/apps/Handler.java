package apps;

import apps.input.InputControl;
import apps.input.InputInfo;
import apps.output.AppWindow;
import apps.ui.Menu;
import apps.content.Content;
import apps.util.Logging;
import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;


public class Handler {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static final AppWindow window = new AppWindow();
    private static ProgramState state;
    static final InputInfo input = new InputInfo();
    private final static Thread onShutdown = new Thread(()->{

    });

    public static void run() {
        Runtime.getRuntime().addShutdownHook(onShutdown);
        try {
            Logging.setup();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failure creating the log files: " + e.getMessage());
        }
        PortTester.start();
        state = ProgramState.main;
        //region Define job dictionary
        //endregion
        //region Set starting state
        Menu.start();
        //endregion
    }

    public static void repaint(int x, int y, int width, int height) {
        window.repaintCanvas(x, y, width, height);
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    //region State traversal
    private static void setState(ProgramState state) {
        Handler.state = state;
        Menu.refreshMenuState();
    }

    public static void escape() {

    }

    //endregion
    //region Getters
    public static InputInfo getInput() {
        return input;
    }

    public static ProgramState getState() {
        return state;
    }

    public static AppWindow getWindow() {
        return window;
    }
    //endregion
}
