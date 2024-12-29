package apps.ui.rectangles;

import apps.output.audio.Audio;
import apps.output.audio.Sound;
import apps.util.DevConfig;

import java.util.ArrayList;
import java.util.HashMap;

public class Plot extends RectElement {
    RectElement pressed;
    public Textbox range;
    public Label title;
    HashMap<String, ArrayList<Float>> dataSets;
    HashMap<String, ArrayList<Float>> newDataSets;
    int rangeN = DevConfig.defaultRange;

    public Plot(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        range = new Textbox(x, y, width, height, String.valueOf(DevConfig.defaultRange), (String string) -> {
            try {
                rangeN = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                Audio.playSound(Sound.stopPls);
            }
        }, DevConfig.borders);
        dataSets = new HashMap<>();
        newDataSets = new HashMap<>();
        this.title = new Label(x, y, width / 2, height, "E0;" + title, DevConfig.borders);
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

    public void addValue(String key, float value) {
        ArrayList<Float> dataSet = dataSets.get(key);
        //region create data set if absent
        if (dataSet == null) {
            dataSet = newDataSets.get(key);
            if (dataSet == null) {
                dataSet = new ArrayList<>();
                newDataSets.put(key, dataSet);
            }
        }
        //endregion
        dataSet.add(value);
    }

    public HashMap<String, ArrayList<Float>> getDataSets() {
        return dataSets;
    }

    public void update() {
        for(String dataSetName:newDataSets.keySet()){
            dataSets.put(dataSetName,newDataSets.get(dataSetName));
        }
        newDataSets.clear();
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
