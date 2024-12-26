package apps;

import apps.util.Util;

import java.io.*;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class Config {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String playerNameKey = "playerName";
    private static final String serverNameKey = "serverName";
    private static final String fileName = "Config.xml";
    private static final Preferences prefs = Preferences.userRoot().node(Config.class.getName());

    static {
        try {
            Preferences.importPreferences(new FileInputStream(Util.path + fileName));
        } catch (IOException | InvalidPreferencesFormatException e) {
            logger.warning("Failed to import config, creating default");
            prefs.put(playerNameKey, "creature");
            prefs.put(serverNameKey, "Shenanigans");
        }
    }

    public static String getPlayerName() {
        return prefs.get(playerNameKey, "creature");
    }

    public static String getServerName() {
        return prefs.get(serverNameKey, "Shenanigans");
    }

    public static void savePrefs() {
        try {
            OutputStream osTree = new BufferedOutputStream(new FileOutputStream(Util.path + fileName));
            prefs.exportSubtree(osTree);
            osTree.close();
            logger.info("Config saved");
        } catch (IOException | BackingStoreException e) {
            logger.severe("Failed to save the config file");
        }
    }

    public static void setName(String text) { //make this a proper method if there are more than 2 settings
        prefs.put(playerNameKey, text);
    }

    public static void setServerName(String text) {
        prefs.put(serverNameKey, text);
    }
}
