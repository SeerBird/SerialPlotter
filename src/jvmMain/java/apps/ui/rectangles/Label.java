package apps.ui.rectangles;


import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Label extends RectElement {
    public String text;
    public boolean textChanged = false;
    public Color textColor;

    public Label(int x, int y, int maxwidth, int maxheight, @NotNull String text, Color textColor) {
        super(x, y, maxwidth, maxheight);
        this.text = text;
        this.textColor=textColor;
    }

    @Override
    public void release() {

    }

    public void setText(String text) {
        this.text = text;
    }
}
