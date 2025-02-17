package apps.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DevConfig {
    public static final int DWIDTH = 1280;
    public static final int DHEIGHT = 600;
    public static final int maxLogSize = 10000;
    public static final int maxPlotEntries = 10000; // should be >=defaultRange
    //region colors
    public static Color BACKGROUND = new Color(20, 21, 37, 255);
    public static Color borders = new Color(222, 158, 0, 255);
    public static Color text = new Color(222, 158, 0, 255);
    public static Color gridColor = new Color(255, 204, 0, 255);
    public static Color sliderColor = new Color(90, 188, 249, 255);
    private static final Color plotColor1 = new Color(0, 210, 218, 255);
    private static final Color plotColor2 = new Color(101, 194, 3, 255);
    private static final Color plotColor3 = new Color(255, 0, 140, 255);
    private static final Color plotColor4 = new Color(253, 210, 53, 255);
    public static ArrayList<Color> plotColors = new ArrayList<>(List.of(new Color[]{plotColor1, plotColor2, plotColor3, plotColor4}));
    //endregion
    //region animations and effects
    public static double shakeDecay = 0.9;
    public static double shakeIntensity = 1.0;
    public static double burstIntensity = 0.0008;
    public static double particleLingerFrames = 20;
    public static int shellSnapFlashFrames = 50;
    //endregion
    //region plot
    public static int optimalGridlineSpacing = 40;
    public static int defaultRange = 150;
    public static double optimalRatio = 16.0 / 9;
    //endregion
    //region menu
    public static int labelHorMargin = 7;
    public static int minSliderLength = 20;
    public static int maxSliderWidth = 20;
    public static int vertMargin = 4;
    public static int fontSize = 16;
    public static int maxTextboxLength = 3000;
    public static int maxParticles = 100;
    public static int portButtonHeight = 20;
    public static int maxCommandLogSize = 3000;
    public static int maxLogEntryLength = 300;

    //endregion
    public static long outOfPacketMessageTimeout = 1000;
    public static long portListRefreshPeriod = 100; // millis
    public static long messageReceivePeriod = 10; // millis
}
