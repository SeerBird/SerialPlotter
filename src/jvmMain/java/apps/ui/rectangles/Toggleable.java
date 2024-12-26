package apps.ui.rectangles;

import java.awt.*;

public class Toggleable extends Label {
    protected boolean pressed;
    boolean state = false;

    public Toggleable(int x, int y, int width, int height, String text, Color color) {
        super(x, y, width, height, text, color);
    }

    @Override
    public boolean press(double x, double y) {
        pressed = super.press(x, y);
        return pressed;
    }

    @Override
    public void release() {
        if (pressed) {//unnecessary?
            toggle();
        }
        pressed = false;
    }

    public boolean getState() {
        return state;
    }

    public void toggle() {
        state^=true;
    }
}
