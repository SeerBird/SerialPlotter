package apps.output;

import apps.util.DevConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;

public class Plot extends Canvas {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    final HashMap<String, ArrayList<Float>> dataSets = new HashMap<>();
    final HashMap<String, ArrayList<Float>> newDataSets = new HashMap<>();
    int rangeN = DevConfig.defaultRange;
    JLabel titleLabel;
    String plotName;
    JPanel legend;
    public Plot(JLabel titleLabel, String plotName,JPanel legend){
        this.titleLabel = titleLabel;
        this.plotName = plotName;
        this.legend = legend;
    }
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
    @Override
    public void paint(@NotNull Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int pwidth = width;
        int pheight = height;
        //region find minimum and maximum value
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        boolean atLeastOneValue = false;
        for (ArrayList<Float> dataSet : new ArrayList<>(dataSets.values())) {
            for (Float value : new ArrayList<>(dataSet)) {
                if(value==null){
                    continue; //VERY BAD FIX. CHECK WHERE THE NULL COMES FROM!
                }
                atLeastOneValue = true;
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }
        //endregion
        //region don't draw the values if there are no values, duh
        if (max == min || !atLeastOneValue) {
            return; // nah you can figure out what the value is if it's only one. get out.
        }
        //endregion
        ArrayList<String> dataSetNames = new ArrayList<>(dataSets.keySet());
        Collections.sort(dataSetNames);
        //region grid
        int order = (int) Math.round((Math.log(max - min)) / Math.log(10)) - 1;
        if (order > -4 && order < 4) {
            order = 0;
        }
        float unit = (float) Math.pow(10, order);
        if (unit == 0) {
            return;
        }
        float unitRange = (max - min) / unit;
        g.setColor(DevConfig.text);
        //region get valueGridSpacing
        int prevGridSpacing = Integer.MIN_VALUE;

        float step = 0.5F;
        float unitGridSpacing = step;
        int gridSpacing = (int) Math.ceil(pheight * unitRange / unitGridSpacing);
        while (Math.abs(prevGridSpacing - DevConfig.optimalGridlineSpacing) >=
                Math.abs(gridSpacing - DevConfig.optimalGridlineSpacing)) {
            unitGridSpacing += step;
            prevGridSpacing = gridSpacing;
            gridSpacing = (int) (pheight / (unitRange / (unitGridSpacing)));
        }
        unitGridSpacing -= step;
        if (gridSpacing < DevConfig.optimalGridlineSpacing / 2) {
            logger.info("Ass spacing");
        }
        float valueGridSpacing = (unitGridSpacing * unit);
        //endregion
        //region find main line
        float mainLine;
        if (min < 0 && max > 0) {
            mainLine = 0;
        } else {
            mainLine = (max + min) / 2;
        }
        //endregion
        //region draw grid and labels
        boolean goingUp = true;
        for (int lineID = 0; ; ) {
            float value = mainLine + valueGridSpacing * lineID;
            if (value > max) {
                goingUp = false;
                lineID = -1;
                continue;
            }
            if (value < min) {
                break;
            }
            int y = plotYFromValue(min, max, value);
            g.drawLine(getX(), y + DevConfig.vertMargin,
                    getX() + getWidth(), y + DevConfig.vertMargin);
            g.drawString(String.valueOf(nDecPlaces((value / unit), 3)), getX(), y + DevConfig.vertMargin);
            lineID = goingUp ? lineID + 1 : lineID - 1;
        }
        //endregion
        //endregion
        //region title and range
        if(order==0){
            titleLabel.setText(plotName);
        }else{
            titleLabel.setText("E" + order + ":" + plotName);
        }

        //endregion
        //region draw plot line segments between data points
        for (int i = 0; i < DevConfig.plotColors.size() && i < dataSetNames.size(); i++) { // no colors left? no plot for you.
            g.setColor(DevConfig.plotColors.get(i));
            ArrayList<Float> dataSet = dataSets.get(dataSetNames.get(i));
            for (int n = 0; n < dataSet.size() - 1; n++) {
                g.drawLine(Math.round(getX() + ((float) (n * pwidth)) / (float) (dataSet.size() - 1)),
                        plotYFromValue(min, max, dataSet.get(n)),
                        Math.round(getX() + ((float) ((n + 1) * pwidth)) / (float) (dataSet.size() - 1)),
                        plotYFromValue(min, max, dataSet.get(n + 1)));
            }
        }
        //endregion
        //region legend. is this really enough?
        /*
        int topy = plot.y + DevConfig.fontSize;
        int x = plot.x + plot.title.width + plot.range.width + DevConfig.labelHorMargin;
        int legendWidth = plot.width - plot.title.width - plot.range.width;
        for (int i = 0; i < DevConfig.plotColors.size() && i < dataSetNames.size(); i++) {
            g.setColor(DevConfig.plotColors.get(i));
            g.drawString(truncateStringEnd(dataSetNames.get(i), legendWidth), x, topy);
            topy += DevConfig.fontSize;
            if (topy > plot.y + plot.height) {
                break;
            }
        }
         */
        //endregion
    }
    private static float nDecPlaces(float number, int n) {
        return (float) (Math.round(number * Math.pow(10, n - 1)) / Math.pow(10, n - 1));
    }
    private int plotYFromValue(float min, float max, float value) {
        return Math.round(getY() + getHeight() - DevConfig.vertMargin - (getHeight() - 2 * DevConfig.vertMargin) * (value - min) / (max - min));
    }
}
