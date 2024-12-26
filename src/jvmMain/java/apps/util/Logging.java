package apps.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Logging {
    public static void setup() throws IOException, URISyntaxException {
        //region set up my logger
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        //FileHandler fileTxt = new FileHandler(Util.path + "TurtleLog%u.%g.txt", true);
        ConsoleHandler console = new ConsoleHandler();
        GFormatter formatter = new GFormatter();
        //fileTxt.setFormatter(formatter);
        console.setFormatter(formatter);
        console.setLevel(Level.INFO);
        //fileTxt.setLevel(Level.INFO);
        //logger.addHandler(fileTxt);
        logger.addHandler(console);
        //endregion
        //region silence default console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }
        //endregion
    }
}

class GFormatter extends Formatter {

    public String format(@NotNull LogRecord rec) {
        return "(" + calcDate(rec.getMillis()) + ") "
                + rec.getSourceClassName() + ": " +
                formatMessage(rec) +
                "\n";
    }

    @NotNull
    private String calcDate(long millis) {
        //SimpleDateFormat date_format = new SimpleDateFormat("MM.dd HH:mm ssSSS");
        SimpleDateFormat date_format = new SimpleDateFormat("ssSSS");
        Date date = new Date(millis);
        return date_format.format(date);
    }

    public String getHead(Handler h) {
        return "Logger 9000 activated";
    }

    public String getTail(Handler h) {
        return "Logger 9000 off\n\n";
    }
}
