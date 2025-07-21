package apps.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DevConfig {
    public static final int maxLogSize = 10000;
    public static final int maxPlotEntries = 10000; // should be >=defaultRange
    //region colors
    public static final Color infoColor = new Color(42, 175, 31);
    public static Color borders = new Color(43, 43, 43, 255);
    public static Color text = new Color(222, 158, 0, 255);
    private static final Color plotColor1 = new Color(0, 210, 218, 255);
    private static final Color plotColor2 = new Color(101, 194, 3, 255);
    private static final Color plotColor3 = new Color(255, 0, 140, 255);
    private static final Color plotColor4 = new Color(253, 210, 53, 255);
    public static ArrayList<Color> plotColors = new ArrayList<>(List.of(new Color[]{plotColor1, plotColor2, plotColor3, plotColor4}));
    //endregion
    //region plot
    public static int optimalGridlineSpacing = 40;
    public static int defaultRange = 150;
    public static double optimalRatio = 16.0 / 9;
    public static int vertMargin = 16;

    //endregion
    public static int fontSize = 16;
    public static int maxTextboxLength = 3000; //TODO: do all these max limits for completeness' sake
    public static int maxCommandLogSize = 3000;
    public static int maxLogEntryLength = 3000;
    public static int maxBetweenMessageLength = 1000;

    public static long outOfPacketMessageTimeout = 100;
    public static long portListRefreshPeriod = 100; // millis
    public static long messageReceivePeriod = 15; // millis
    public static final boolean defaultLoggingPackets = false;
    public static final int legendEntryNum = 3;

}
