package apps;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class Resources {
    public static final URL goodnight;
    public static URL vine;
    public static URL pew;
    public static URL pipe;
    public static File comfortaa;
    public static ArrayList<File> amogus;

    static {
        goodnight = Resources.class.getResource("goodnight.wav");
        vine = Resources.class.getResource("vine.wav");
        pew = Resources.class.getResource("pew.wav");
        pipe = Resources.class.getResource("pipe.wav");
        amogus = new ArrayList<>();
        try {
            comfortaa = new File(Objects.requireNonNull(Resources.class.getResource("Comfortaa.ttf")).toURI());
            for (int i = 0; i < 11; i++) {
                amogus.add(new File(Objects.requireNonNull(Resources.class
                        .getResource("amogus/ezgif-frame-00" + (i+1)+".jpg")).toURI()));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
