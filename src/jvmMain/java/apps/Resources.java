package apps;

import java.net.URL;

public class Resources {
    public static final URL goodnight;
    public static URL vine;
    public static URL pew;
    public static URL pipe;

    static {
        goodnight = Resources.class.getResource("goodnight.wav");
        vine = Resources.class.getResource("vine.wav");
        pew = Resources.class.getResource("pew.wav");
        pipe = Resources.class.getResource("pipe.wav");
    }
}
