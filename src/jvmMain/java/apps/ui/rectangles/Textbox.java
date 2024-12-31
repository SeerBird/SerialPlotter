package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Focusable;
import apps.ui.Menu;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class Textbox extends Label implements Focusable {
    final String defaultText;
    public boolean leaveOnSubmit;
    Consumer<String> action;
    public boolean cursor;
    ScheduledFuture cursorAnimation;

    public Textbox(int x, int y, int width, int height, String defaultText, Consumer<String> action, Color textColor, boolean leaveOnSubmit) {
        super(x, y, width, height, defaultText, textColor);
        this.defaultText = defaultText;
        this.leaveOnSubmit = leaveOnSubmit;
        cursor = false;
        this.action = action;
    }

    @Override
    public void release() {
        enter();
    }

    @Override
    public void enter() {
        Menu.focus(this);
        resetAnimation();
    }
    public void resetAnimation(){
        if (cursorAnimation != null) {
            cursorAnimation.cancel(true);
        }
        cursorAnimation = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(()->{
            cursor^=true;
            Handler.repaint(x,y,width,height);
        },0,400, TimeUnit.MILLISECONDS);
    }

    @Override
    public void leave() {
        if (cursorAnimation != null) {
            cursorAnimation.cancel(true);
        }
        cursor=false;
        Handler.repaint(x,y,width,height);
    }
    public void resetText(){
        text="";
        Handler.repaint(x,y,width,height);
    }

    public void useValue() {
        action.accept(String.valueOf(text));
    }
}
