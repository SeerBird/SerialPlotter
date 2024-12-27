package apps.util;


import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static String path;

    static {
        try {
            String prefix = "/TurtleSmash/";//remove TurtleSmash for windows?
            //region get log path
            String os = System.getProperty("os.name");
            URI uri;
            if (os.contains("Windows")) { //check separate versions?
                path = (System.getenv("LOCALAPPDATA") + prefix).replaceAll("\\\\", "/");
                uri = URI.create("file:/" + path.substring(0, path.length() - 1));
                Files.createDirectories(Paths.get(uri));
            } else if (os.contains("mac")) { //figure this out
                path = "~/Library/Application /Support";
                uri = URI.create("file:/" + path);
                Files.createDirectories(Paths.get(uri));
            } else {
                throw new RuntimeException("Can't run on your machine, sorry");
            }
            //endregion
        } catch (Exception death) {

        }
    }
}
