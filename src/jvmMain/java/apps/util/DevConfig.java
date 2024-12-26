package apps.util;

import java.awt.*;

public class DevConfig {
    public static final int DWIDTH = 1280;
    public static final int DHEIGHT = 1420;
    //region colors
    public static Color BACKGROUND = new Color(31, 0, 136, 255);
    public static Color CURSED = new Color(0, 30, 234, 255);
    public static Color HIGHLIGHT = new Color(196, 36, 0, 255);
    public static Color menuBackground = new Color(12, 11, 0, 61);
    public static Color shell = new Color(255, 166, 0, 255);
    public static Color web = new Color(255, 236, 177, 255);
    public static Color turtle = new Color(96, 183, 0, 255);
    //endregion
    //region webs
    public static int webLengthLimit = 35;
    public static double webRestNodeDistance = 14;
    public static double webFling = 24;
    public static int webTensileStrength = 10;
    public static double webDecayRate = 0.1;
    //endregion
    //region network
    public static long discoveryMilliTimeout = 1300;
    public static long multicastMilliPeriod = 1000;
    public static double doublePrecision = 100;
    //endregion
    //region turtles
    public static double turtleSize = 1.0;
    public static double turtleDeformThreshold = 3.7;
    public static double turtleMass = 4.0;
    public static int turtleNakedFrames = 30;
    public static double recoil = 10.0;
    //endregion
    //region shells
    public static double shellMergeThreshold = 8.0;
    public static double shellStrapExtensionLimit = 1.42;
    public static double shellMass = 4.0;
    //endregion
    //region player
    public static double playerSpawnSpread = 80;
    public static double approxPlayerSpawnVelocity = 6;
    public static double playerSpawnVelocitySpread = 1.5;
    public static int deathFrames = 120;
    //endregion
    //region physics
    public static double constraintTolerance = 0.0;
    public static double gravity = 50;
    public static double minGravityRadius = 80;
    public static double elasticity = 0.6;
    //endregion
    //region animations and effects
    public static double shakeDecay = 0.9;
    public static double shakeIntensity = 1.0;
    public static double burstIntensity = 0.0008;
    public static double particleLingerFrames = 20;
    public static int shellSnapFlashFrames = 50;
    //endregion
    //region menu
    public static int maxNameLength = 30;
    public static int maxParticles = 100;
    public static int portButtonHeight = 20;
    //endregion
}
