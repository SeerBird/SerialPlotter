package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Scrollable;
import apps.util.DevConfig;

import java.util.ArrayList;

public class TextArea extends RectElement implements Scrollable {
    public ArrayList<String> entries;
    ArrayList<String> newEntries;
    int deltaShift;
    public int shiftFromBottom;

    public TextArea(int x, int y, int width, int height) {
        super(x, y, width, height);
        entries = new ArrayList<>();
        newEntries = new ArrayList<>();
        shiftFromBottom=0;
    }

    public void refresh() {
        entries.addAll(newEntries);
        while (entries.size() > DevConfig.maxLogSize) {
            entries.remove(0);
        }
        newEntries.clear();
        shiftFromBottom+=deltaShift;
        deltaShift=0;
        if(shiftFromBottom>entries.size()-1){
            shiftFromBottom=entries.size()-1;
        }
        if(shiftFromBottom<0){
            shiftFromBottom=0;
        }
    }

    @Override
    public boolean press(double x, double y) {
        return false;
    }

    @Override
    public void scroll(int steps) {
        deltaShift -=steps;
        Handler.repaint(x,y,width,height);
    }

    public void log(String string) { //called concurrently?
        if(shiftFromBottom!=0){
            shiftFromBottom++;
        }
        newEntries.add(string);
        Handler.repaint(x,y,width,height);
    }
}
