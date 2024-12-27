package apps.ui.rectangles;

import apps.ui.IElement;

public abstract class RectElement extends IElement {
    public int width;
    public int height;
    public boolean hovered;

    public RectElement(int x, int y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }


    @Override
    public boolean press(double x, double y) {
        return inBounds(x,y);
    }

    @Override
    public void release() {

    }

    @Override
    public boolean hover(double x, double y) {
        hovered = inBounds(x,y);
        return hovered;
    }

    boolean inBounds(double x, double y){return x>this.x&&x<this.x+width&&y>this.y&&y<this.y+height;}

}
