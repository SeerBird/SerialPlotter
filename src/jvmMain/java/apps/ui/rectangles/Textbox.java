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
    Consumer<String> action;
    public String currentDefaultText;
    public boolean cursor;
    ScheduledFuture cursorAnimation;

    public Textbox(int x, int y, int width, int height, String defaultText, Consumer<String> action, Color textColor) {
        super(x, y, width, height, defaultText, textColor);
        this.defaultText = defaultText;
        cursor = false;
        currentDefaultText = defaultText;
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
        text = currentDefaultText;
        if (cursorAnimation != null) {
            cursorAnimation.cancel(true);
        }
        cursor=false;
        Handler.repaint(x,y,width,height);
    }

    public void useValue() {
        currentDefaultText = text;
        if (Objects.equals(text, "")) {
            currentDefaultText = defaultText;
        }
        action.accept(String.valueOf(currentDefaultText));
    }
}
