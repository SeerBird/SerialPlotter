package apps;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class Resources {
    public static final URL goodnight;
    public static URL vine;
    public static URL pew;
    public static URL pipe;
    public static File comfortaa;

    static {
        goodnight = Resources.class.getResource("goodnight.wav");
        vine = Resources.class.getResource("vine.wav");
        pew = Resources.class.getResource("pew.wav");
        pipe = Resources.class.getResource("pipe.wav");
        try {
            comfortaa = new File(Objects.requireNonNull(Resources.class.getResource("Comfortaa.ttf")).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
