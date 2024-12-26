package apps.content;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;


public final class Content {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void update() { // make it all multiplied by dt?

    }


    public static ArrayRealVector getDistance(ArrayRealVector pos1, @NotNull ArrayRealVector pos2) {
        return pos2.combine(1, -1, pos1);
    }

    public static void set(@NotNull ContentData data) {

    }

    public static void clear() {

    }
}
