package apps;

import apps.input.InputInfo;
import apps.output.AppWindow;
import apps.output.Renderer;
import apps.ui.Menu;
import apps.util.GFormatter;
import com.fazecast.jSerialComm.*;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Handler {
    static{
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
    }
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static final AppWindow window = new AppWindow();
    private static ProgramState state;
    static final InputInfo input = new InputInfo();
    private final static Thread onShutdown = new Thread(()->{

    });

    public static void run() {
        Runtime.getRuntime().addShutdownHook(onShutdown);
        state = ProgramState.main;
        //region Set starting state
        Menu.start();
        Renderer.start();
        //endregion
    }

    public static void repaint(int x, int y, int width, int height) {
        window.repaintCanvas(x, y, width, height);
    }
    public static void repaint(){
        window.repaintCanvas();
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
