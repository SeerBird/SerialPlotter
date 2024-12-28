package apps.util;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class GFormatter extends Formatter {

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
        return "Logger 9000 activated\n";
    }

    public String getTail(Handler h) {
        return "Logger 9000 off\n\n";
    }
}
