package apps.ui.rectangles;


import apps.Handler;
import apps.output.audio.Audio;
import apps.output.audio.Sound;

import java.awt.*;

public class Button extends Label {
    private Runnable action;
    private boolean pressed;

    public Button(int x, int y, int width, int height, Runnable action, String text, Color textColor) {
        super(x, y, width, height, text, textColor);
        this.action = action;
        pressed = false;
    }

    @Override
    public boolean press(double x, double y) {
        pressed = super.press(x, y);
        Handler.repaint(this.x,this.y,width,height);
        return pressed;
    }

    @Override
    public void release() {
        if (pressed) {//unnecessary?
            action.run();
            //Audio.playSound(Sound.button);
        }
        pressed = false;
        Handler.repaint(x,y,width,height);
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    public boolean isPressed() {
        return pressed;
    }
}
