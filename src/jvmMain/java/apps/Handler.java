package apps;

//import apps.util.FlatInspector;
//import apps.util.FlatUIDefaultsInspector;
import apps.input.InputInfo;
import apps.output.AppWindow;
import apps.output.audio.Audio;
import apps.ui.Menu;
import apps.util.GFormatter;
import com.fazecast.jSerialComm.*;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
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
        if (handlers.length > 0) {
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
        }
        //endregion

    }

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static ProgramState state;
    static final InputInfo input = new InputInfo();
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    static boolean bullshitOn = false;
    private final static Thread onShutdown = new Thread(() -> {
// close ports?
    });

    public static void run() {
        Runtime.getRuntime().addShutdownHook(onShutdown);
        state = ProgramState.main;
        //region Set starting state
        Audio.start();
        //Menu.start();
        //endregion
        //region FlatLaf setup
        if( SystemInfo.isMacOS ) {
            // enable screen menu bar
            // (moves menu bar from JFrame window to top of screen)
            //System.setProperty( "apple.laf.useScreenMenuBar", "true" );

            // application name used in screen menu bar
            // (in first menu after the "apple" menu)
            System.setProperty( "apple.awt.application.name", "SerialPlotter" );

            // appearance of window title bars
            // possible values:
            //   - "system": use current macOS appearance (light or dark)
            //   - "NSAppearanceNameAqua": use light appearance
            //   - "NSAppearanceNameDarkAqua": use dark appearance
            // (must be set on main thread and before AWT/Swing is initialized;
            //  setting it on AWT thread does not work)
            System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
        }
        if( SystemInfo.isLinux ||SystemInfo.isWindows_10_orLater) {
            // enable custom window decorations
            JFrame.setDefaultLookAndFeelDecorated( true );
            JDialog.setDefaultLookAndFeelDecorated( true );
        }
        FlatDarculaLaf.setup();
        // smth ab screenshot mode, check FlatLaf/flatlaf-demo/src/main/java/com/formdev/flatlaf/demo
        //FlatInspector.install( "ctrl shift alt X" );
        //FlatUIDefaultsInspector.install( "ctrl shift alt Y" );
        // /FlatLafDemo.java if needed
        AppWindow frame = new AppWindow();
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );
        //endregion
    }

    public static void repaint(int x, int y, int width, int height) {
    }

    public static void repaint() {
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    public static int stringLength(String string) {
        return 0;
    }

    public static void setSounds(boolean on) {
        bullshitOn = on;
    }

    public static <V> V timeout(Callable<V> getter, int timeout) throws TimeoutException, ExecutionException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<V> handler = executor.submit(getter);
        try {
            return handler.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            handler.cancel(true);
            throw new TimeoutException();
        } catch (InterruptedException e) {
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

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public static ProgramState getState() {
        return state;
    }

    public static AppWindow getWindow() {
        return null;
    }
    //endregion
}
