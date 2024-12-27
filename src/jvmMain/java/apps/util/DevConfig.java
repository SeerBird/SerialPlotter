package apps.util;

import java.awt.*;

public class DevConfig {
    public static final int DWIDTH = 1280;
    public static final int DHEIGHT = 600;
    public static final int maxLogSize = 1000;
    public static final int maxPlotEntries = 1000; // should be >=defaultRange
    //region colors
    public static Color BACKGROUND = new Color(31, 0, 136, 255);
    public static Color CURSED = new Color(0, 30, 234, 255);
    public static Color HIGHLIGHT = new Color(196, 36, 0, 255);
    public static Color menuBackground = new Color(12, 11, 0, 61);
    public static Color shell = new Color(255, 166, 0, 255);
    public static Color web = new Color(255, 236, 177, 255);
    public static Color turtle = new Color(96, 183, 0, 255);
    //endregion
    //region animations and effects
    public static double shakeDecay = 0.9;
    public static double shakeIntensity = 1.0;
    public static double burstIntensity = 0.0008;
    public static double particleLingerFrames = 20;
    public static int shellSnapFlashFrames = 50;
    //endregion
    //region plot
    public static int defaultRange = 1000;
    public static double optimalRatio = 16.0/9;
    //endregion
    //region menu
    public static int labelHorMargin = 7;
    public static int vertMargin = 4;
    public static int fontSize = 16;
    public static int maxNameLength = 30;
    public static int maxParticles = 100;
    public static int portButtonHeight = 20;
    public static long portListRefreshPeriod = 100; // millis
    public static long messageReceivePeriod = 1000; // millis

    //endregion
}
