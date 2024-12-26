package apps.ui.rectangles;

import apps.output.Renderer;
import apps.output.animations.Animation;
import apps.output.animations.TextCursorAnimation;
import apps.ui.Focusable;
import apps.ui.Menu;

import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;


public class Textbox extends Label implements Focusable {
    final String defaultText;
    Consumer<String> action;
    String currentDefaultText;
    Animation cursorAnimation;

    public Textbox(int x, int y, int width, int height, String defaultText, Consumer<String> action, Color textColor) {
        super(x, y, width, height, defaultText, textColor);
        this.defaultText = defaultText;
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
        cursorAnimation = Renderer.addAnimation(new TextCursorAnimation(this));
    }

    @Override
    public void leave() {
        text = currentDefaultText;
        Renderer.removeAnimation(cursorAnimation);
    }

    public void useValue() {
        currentDefaultText = text;
        if (Objects.equals(text, "")) {
            currentDefaultText = defaultText;
        }
        action.accept(String.valueOf(currentDefaultText));
    }
}
