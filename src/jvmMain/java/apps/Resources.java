package apps;

import apps.util.FlatSVGIcon;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class Resources {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static final URL goodnight;
    public static URL vine;
    public static URL pew;
    public static URL pipe;
    public static URL textBoxFail;
    public static URL stoppls;
    public static File comfortaa;
    public static ArrayList<File> amogus;
    public static ArrayList<FlatSVGIcon> enabledIcons;
    public static ArrayList<FlatSVGIcon> disabledIcons;
    public static FlatSVGIcon crossIcon;

    static {
        goodnight = Resources.class.getResource("goodnight.wav");
        vine = Resources.class.getResource("vine.wav");
        pew = Resources.class.getResource("pew.wav");
        pipe = Resources.class.getResource("pipe.wav");
        textBoxFail = Resources.class.getResource("textBoxFail.wav");
        stoppls = Resources.class.getResource("stoppls.wav");
        crossIcon = new FlatSVGIcon(Resources.class.getResource("icons/close.svg"));
        enabledIcons = new ArrayList<>();
        disabledIcons = new ArrayList<>();
        for(int i=1;i<5;i++){
            enabledIcons.add(new FlatSVGIcon(Resources.class.getResource("icons/enabled"+ i + ".svg")).derive(0.25F));
            disabledIcons.add(new FlatSVGIcon(Resources.class.getResource("icons/disabled"+ i+ ".svg")).derive(0.25F));
        }
        amogus = new ArrayList<>();
        try {
            comfortaa = getFile("Comfortaa.ttf");
            for (int i = 0; i < 22; i++) {
                amogus.add(getFile("amogus/ezgif-frame-0" + (i + 1) + ".jpg"));
            }
        } catch (Exception e) {
            logger.info("File: " + e.getMessage());
            logger.info((Objects.requireNonNull(Resources.class.getResource("Comfortaa.ttf"))).toExternalForm());
            throw new RuntimeException(e);
        }
        assert (comfortaa != null); //TODO: assert everything is loaded well
    }

    private static File getFile(String path) {
        File file;
        URL res = Resources.class.getResource(path);
        if(res==null){
            logger.info("URL not found!");
            return null;
        }
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = Resources.class.getResourceAsStream(path);
                if(input==null){
                    logger.info("Couldn't get the InputStream");
                    return null;
                }
                file = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file.deleteOnExit();
            } catch (IOException ex) {
                logger.info(ex.getMessage());
                return null;
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(res.getFile());
        }

        if (!file.exists()) {
            logger.info("File not found");
        }
        return file;
    }
}
