package apps.output;

import apps.Resources;
import apps.util.DevConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Logger;

public class Plot extends Canvas {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    final HashMap<String, ArrayList<Float>> dataSets = new HashMap<>();
    final HashMap<String, ArrayList<Float>> newDataSets = new HashMap<>();
    final HashMap<String, JToggleButton> dataSetToggles = new HashMap<>();
    int rangeN = DevConfig.defaultRange;
    boolean painting = false;
    JLabel titleLabel;
    String plotName;
    JPanel legend;
    JScrollPane legendScroll;

    public Plot(JLabel titleLabel, String plotName, JPanel legend, JScrollPane legendScroll) {
        this.titleLabel = titleLabel;
        this.plotName = plotName;
        this.legend = legend;
        this.legendScroll = legendScroll;
    }

    public void addValue(String key, float value) {
        ArrayList<Float> dataSet;
        synchronized (dataSets) {
            dataSet = dataSets.get(key);
        }
        //region create data set if absent
        if (dataSet == null) {
            //TODO: there was a synch(newDataSets) here
            dataSet = newDataSets.get(key);
            if (dataSet == null) {
                dataSet = new ArrayList<>();
                newDataSets.put(key, dataSet);
                JToggleButton dataSetToggle = new JToggleButton(key, true);
                dataSetToggles.put(key, dataSetToggle);
                SwingUtilities.invokeLater(() -> {
                    legend.add(dataSetToggle);
                    //region update legend and plot colors
                    double maxwidth = 0;
                    double totheight = 0;
                    ArrayList<JToggleButton> toggleButtons = new ArrayList<>(dataSetToggles.values());
                    legend.removeAll();
                    toggleButtons.sort(Comparator.comparing(JToggleButton::getText));
                    for (int i = 0; i < toggleButtons.size(); i++) {
                        JToggleButton but = toggleButtons.get(i);
                        but.setIcon(Resources.disabledIcons.get(i % Resources.disabledIcons.size()));
                        but.setSelectedIcon(Resources.enabledIcons.get(i % Resources.enabledIcons.size()));
                        legend.add(but);
                        totheight += but.getPreferredSize().getHeight();
                        if (but.getPreferredSize().getWidth() + 16 > maxwidth) {
                            maxwidth = but.getPreferredSize().getWidth() + 16;
                        }//TODO: leave these hard-coded numbers be and enjoy life
                    }
                    totheight = Math.min(26 * DevConfig.legendEntryNum + 6, totheight + 6);
                    legendScroll.setPreferredSize(new Dimension((int) maxwidth, (int) totheight));
                    //legend.setPreferredSize(new Dimension((int) maxwidth,legend.getHeight()));
                    legendScroll.getParent().revalidate();
                    //endregion});
                });
            }
        }
        //endregion
        dataSet.add(value);
        while (dataSet.size() > rangeN) {
            dataSet.remove(0);
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(@NotNull Graphics g) {
        if (painting) {
            repaint();
            return;
        }
        painting = true;
        if (getBufferStrategy() == null) {
            createBufferStrategy(2);
        }
        g = getBufferStrategy().getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        //g.clearRect(0, 0, getWidth(), getHeight());
        dataSets.putAll(newDataSets);
        newDataSets.clear();
        int width = getWidth();
        int height = getHeight();
        int pwidth = width;
        int pheight = height - 2 * DevConfig.vertMargin;
        //region find minimum and maximum value
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        boolean atLeastOneValue = false;
        for (String dataSetName : new ArrayList<>(dataSets.keySet())) {
            if (!dataSetToggles.get(dataSetName).isSelected()) {
                continue;
            }
            ArrayList<Float> dataSet = dataSets.get(dataSetName);
            for (Float value : new ArrayList<>(dataSet)) {
                if (value == null) {
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
            g.drawLine(getX(), y,
                    getX() + getWidth(), y);
            g.drawString(String.valueOf(nDecPlaces((value / unit), 3)), getX(), y);
            lineID = goingUp ? lineID + 1 : lineID - 1;
        }
        //endregion
        //endregion
        //region title and range
        if (order == 0) {
            titleLabel.setText(plotName);
        } else {
            titleLabel.setText("E" + order + ":" + plotName);
        }

        //endregion
        //region draw plot line segments between data points
        for (int i = 0; i < dataSetNames.size(); i++) { // no colors left? no plot for you.
            if (!dataSetToggles.get(dataSetNames.get(i)).isSelected()) {
                continue;
            }
            g.setColor(DevConfig.plotColors.get(i % DevConfig.plotColors.size()));
            ArrayList<Float> dataSet = dataSets.get(dataSetNames.get(i));
            for (int n = 0; n < dataSet.size() - 1; n++) {
                g.drawLine(Math.round(getX() + ((float) (n * pwidth)) / (float) (dataSet.size() - 1)),
                        plotYFromValue(min, max, dataSet.get(n)),
                        Math.round(getX() + ((float) ((n + 1) * pwidth)) / (float) (dataSet.size() - 1)),
                        plotYFromValue(min, max, dataSet.get(n + 1)));
            }
        }
        //endregion
        g.dispose();
        getBufferStrategy().show();
        painting = false;
    }

    private static float nDecPlaces(float number, int n) {
        return (float) (Math.round(number * Math.pow(10, n - 1)) / Math.pow(10, n - 1));
    }

    private int plotYFromValue(float min, float max, float value) {
        int y = Math.round(getHeight() - DevConfig.vertMargin - (getHeight() - 2 * DevConfig.vertMargin) * (value - min) / (max - min));
        return y;
    }
}
