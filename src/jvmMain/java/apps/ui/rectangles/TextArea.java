package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Scrollable;
import apps.util.DevConfig;

import java.util.ArrayList;

public class TextArea extends RectElement implements Scrollable {
    public ArrayList<String> entries;
    ArrayList<String> newEntries;
    int bottomEntry;

    public TextArea(int x, int y, int width, int height) {
        super(x, y, width, height);
        entries = new ArrayList<>();
        newEntries = new ArrayList<>();
    }

    public void refresh() {
        entries.addAll(newEntries);
        while (entries.size() > DevConfig.maxLogSize) {
            entries.remove(0);
        }
        newEntries.clear();
    }

    @Override
    public boolean press(double x, double y) {
        return false;
    }

    @Override
    public void scroll(int steps) {

    }

    public void log(String string) { //called concurrently?
        newEntries.add(string);
        Handler.repaint(x,y,width,height);
    }
}
