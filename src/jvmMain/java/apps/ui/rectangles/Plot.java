package apps.ui.rectangles;

import apps.Handler;
import apps.output.audio.Audio;
import apps.output.audio.Sound;
import apps.util.DevConfig;

import java.util.ArrayList;
import java.util.HashMap;

public class Plot extends RectElement {
    RectElement pressed;
    public Textbox range;
    public String titleText;
    public Label title;
    final HashMap<String, ArrayList<Float>> dataSets = new HashMap<>();
    ;
    final HashMap<String, ArrayList<Float>> newDataSets = new HashMap<>();
    ;
    int rangeN = DevConfig.defaultRange;

    public Plot(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        this.titleText = title;
        range = new Textbox(x, y, width, height, String.valueOf(DevConfig.defaultRange), (String string) -> {
            try {
                rangeN = Integer.parseInt(string);
                if (rangeN > DevConfig.maxPlotEntries) {
                    rangeN = DevConfig.maxPlotEntries;
                }
            } catch (NumberFormatException e) {
                Audio.playSound(Sound.stopPls);
            }
        }, DevConfig.borders, true);
        this.title = new Label(x, y, width / 2, height, "E0:" + title, DevConfig.borders);
    }

    //region Input
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

    //endregion
    public void addValue(String key, float value) {
        ArrayList<Float> dataSet;
        synchronized (dataSets) {
            dataSet = dataSets.get(key);
        }
        //region create data set if absent
        if (dataSet == null) {
            synchronized (newDataSets) {
                dataSet = newDataSets.get(key);
                if (dataSet == null) {
                    dataSet = new ArrayList<>();
                    newDataSets.put(key, dataSet);
                }
            }
        }
        //endregion
        dataSet.add(value);
        while (dataSet.size() > rangeN) {
            dataSet.remove(0);
        }
    }

    public HashMap<String, ArrayList<Float>> getDataSets() {
        return dataSets;
    }

    public void update() {
        for (String dataSetName : newDataSets.keySet()) {
            dataSets.put(dataSetName, newDataSets.get(dataSetName));
        }
        newDataSets.clear();

        int legendWidth = 0;
        for (String dataSetName : dataSets.keySet()) {
            legendWidth = Math.max(legendWidth, Handler.stringLength(dataSetName) + 2 * DevConfig.labelHorMargin);
        }
        legendWidth = Math.min(legendWidth, width / 3);
        int remainingWidth = width - legendWidth;
        title.width = remainingWidth * 2 / 3;
        range.width = remainingWidth / 3;
        title.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        range.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        title.x = x;
        title.y = y;
        range.x = x + title.width;
        range.y = y;
    }
}
