package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Focusable;
import apps.ui.Menu;
import apps.util.DevConfig;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class Textbox extends Label implements Focusable {
    final String defaultText;
    public boolean leaveOnSubmit;
    Consumer<String> action;
    public boolean cursor;
    public int cursorIndex; // the index of the letter after which text will be inserted
    public int displayIndex; // the index of the first letter displayed
    ScheduledFuture cursorAnimation;

    public Textbox(int x, int y, int width, int height, String defaultText, Consumer<String> action, Color textColor, boolean leaveOnSubmit) {
        super(x, y, width, height, defaultText, textColor);
        this.defaultText = defaultText;
        this.leaveOnSubmit = leaveOnSubmit;
        cursor = false;
        this.action = action;
        cursorIndex = defaultText.length() - 1;
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

    public void resetAnimation() {
        if (cursorAnimation != null) {
            cursorAnimation.cancel(true);
        }
        cursor = false;
        cursorAnimation = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            cursor ^= true;
            Handler.repaint(x, y, width, height);
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public void leave() {
        if (cursorAnimation != null) {
            cursorAnimation.cancel(true);
        }
        cursor = false;
        Handler.repaint(x, y, width, height);
    }

    public void resetText() {
        text = "";
        Handler.repaint(x, y, width, height);
    }

    public void useValue() {
        action.accept(String.valueOf(text));
    }

    public void insertText(String string) {
        text = text.substring(0, cursorIndex + 1) + string + text.substring(cursorIndex + 1);
        if (text.length() > DevConfig.maxTextboxLength) {
            text = text.substring(0, DevConfig.maxTextboxLength - 1);
        }
        shift(string.length());
        resetAnimation();
    }

    public void shift(int n) {
        cursorIndex = Math.max(Math.min(cursorIndex + n, text.length() - 1), -1);
        resetAnimation();
    }

    public void backspace() {
        if (!text.isEmpty()) {
            text = text.substring(0, cursorIndex) + text.substring(cursorIndex + 1);
            shift(-1);
        }
    }
}
