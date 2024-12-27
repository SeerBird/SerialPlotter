package apps.ui.rectangles;

import apps.util.DevConfig;

import java.util.ArrayList;

public class Plot extends RectElement {
    RectElement pressed;
    public Textbox range;
    public Label title;
    ArrayList<Float> values;
    int rangeN = DevConfig.defaultRange;

    public Plot(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        range = new Textbox(x, y, width, height, String.valueOf(DevConfig.defaultRange), (String string) -> {
            try {
                rangeN = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                // moan I think.
            }
        }, DevConfig.borders);
        values = new ArrayList<>();
        this.title = new Label(x,y,width/2,height,"E0;"+title,DevConfig.borders);
    }


    @Override
    public boolean press(double x, double y) {
        if (range.press(x, y)) {
            pressed = range;
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        if (pressed == null) {
            return;
        }
        pressed.release();
        pressed = null;
    }

    public void addValue(float value) {
        values.add(value);
        while (values.size() > rangeN) {
            values.remove(0); // this feels bad computationally. I'm sure it's fine though.
        }
    }

    public ArrayList<Float> getValues() {
        return values;
    }

    public void arrange() {
        title.width = width / 2;
        range.width = width / 2;
        title.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        range.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        title.x = x;
        title.y = y;
        range.x = x + width / 2;
        range.y = y;
    }
}
