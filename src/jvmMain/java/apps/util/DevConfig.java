package apps.util;

import java.awt.*;

public class DevConfig {
    public static final int DWIDTH = 1280;
    public static final int DHEIGHT = 600;
    public static final int maxLogSize = 10000;
    public static final int maxPlotEntries = 1000; // should be >=defaultRange
    //region colors
    public static Color BACKGROUND = new Color(31, 0, 136, 255);
    public static Color borders = new Color(255, 166, 0, 255);
    public static Color text = new Color(255, 166, 0, 255);
    public static Color gridColor = new Color(200, 40, 70, 255);
    public static Color sliderColor = new Color(200, 40, 70, 255);
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
    public static int defaultRange = 1000;
    public static double optimalRatio = 16.0 / 9;
    //endregion
    //region menu
    public static int labelHorMargin = 7;
    public static int minSliderLength = 20;
    public static int maxSliderWidth = 20;
    public static int vertMargin = 4;
    public static int fontSize = 16;
    public static int maxNameLength = 30;
    public static int maxParticles = 100;
    public static int portButtonHeight = 20;
    public static long portListRefreshPeriod = 100; // millis
    public static long messageReceivePeriod = 10; // millis

    //endregion
}
