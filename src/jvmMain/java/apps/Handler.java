package apps;

import apps.input.InputInfo;
import apps.output.AppWindow;
import apps.output.Renderer;
import apps.output.audio.Audio;
import apps.ui.Menu;
import apps.util.GFormatter;
import com.fazecast.jSerialComm.*;

import java.util.concurrent.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Handler {
    static {
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
    private static ProgramState state;
    static final AppWindow window = new AppWindow();
    static final InputInfo input = new InputInfo();
    static boolean bullshitOn = true;
    private final static Thread onShutdown = new Thread(() -> {

    });

    public static void run() {
        Runtime.getRuntime().addShutdownHook(onShutdown);
        state = ProgramState.main;
        //region Set starting state
        Audio.start();
        Menu.start();
        Renderer.start();
        //endregion
    }

    public static void repaint(int x, int y, int width, int height) {
        window.repaintCanvas(x, y, width, height);
    }

    public static void repaint() {
        window.repaintCanvas();
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    public static int stringLength(String string) {
        return window.stringLength(string);
    }

    public static void setSounds(boolean on) {
        bullshitOn = on;
    }

    public static <V> V timeout(Callable<V> getter, int timeout) throws TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<V> handler = executor.submit(getter);
        try {
            return handler.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            handler.cancel(true);
            throw new TimeoutException();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    public static boolean getBullshitOn() {
        return bullshitOn;
    }

    public static ProgramState getState() {
        return state;
    }

    public static AppWindow getWindow() {
        return window;
    }
    //endregion
}
