package apps.output;


import apps.Handler;
import apps.output.animations.Animation;
import apps.ui.IElement;
import apps.ui.Menu;
import apps.ui.rectangles.*;
import apps.ui.rectangles.Button;
import apps.ui.rectangles.Label;
import apps.ui.rectangles.TextArea;
import apps.util.DevConfig;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Renderer {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static Graphics g;
    static final ArrayList<Animation> animations = new ArrayList<>();
    static int x = 0;
    static int y = 0;
    static int width = 0;
    static int height = 0;

    public static void start() {
        Handler.getScheduler().scheduleAtFixedRate(() -> {
            synchronized (animations) {
                for (Animation ani : animations) {
                    ani.next();
                    Rectangle rect = ani.rect();
                    Handler.repaint(rect.x, rect.y, rect.width, rect.height);
                }
            }
        }, 8, 1000 / 60, TimeUnit.MILLISECONDS);
    }

    public static void drawAnimations() { //get new info and progress animations
        for (Animation animation : new ArrayList<>(animations)) {
            animation.draw(g);
        }
    }

    public static void drawImage(@NotNull Graphics g, int width, int height) {
        Renderer.width = width;
        Renderer.height = height;
        Renderer.g = g;
        g.translate(x, y);
        fill(DevConfig.BACKGROUND);
        drawContent();
        drawAnimations();
        drawMenu();
        g.dispose();
    }

    //region Content
    private static void drawContent() { // get all the visible objects, effects, and particles on an image
    }
    //endregion

    //region Menu
    private static void drawMenu() {
        Menu.update();
        for (IElement e : Menu.getElements()) {
            if (e instanceof PortList) {
                drawPortList((PortList) e);
            } else if (e instanceof TextArea) {
                drawTextArea((TextArea) e);
            } else if (e instanceof Textbox) {
                drawTextbox((Textbox) e);
            } else if (e instanceof PlotContainer) {
                drawPlotContainer((PlotContainer) e);
            } else {
                logger.info("Encountered an IElement I did not anticipate. How?");
            }
        }
    }

    //region draw elements
    private static void drawPlot(@NotNull Plot plot) {
        drawRect(plot, DevConfig.borders);
        if (plot.height - plot.title.height < 5 || plot.width < 5) {
            return;
        }
        int vertMargin = plot.height / 100;
        int pwidth = plot.width;
        int pheight = plot.height - plot.title.height - 2 * vertMargin;
        HashMap<String, ArrayList<Float>> dataSets = plot.getDataSets();
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
            //logger.info("Only one value?");
            drawLabel(plot.title);
            drawTextbox(plot.range);
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
        float valueGridSpacing = ((float) unitGridSpacing * unit);
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
            int y = plotYFromValue(plot, min, max, value);
            g.drawLine(plot.x, y + vertMargin,
                    plot.x + plot.width, y + vertMargin);
            g.drawString(String.valueOf(nDecPlaces((value / unit), 3)), plot.x, y + vertMargin);
            lineID = goingUp ? lineID + 1 : lineID - 1;
        }
        //endregion
        //endregion
        //region title and range
        plot.title.text = "E" + order + ":" + plot.titleText; // is this thread-safe? prolly not.
        drawLabel(plot.title);
        drawRect(plot.title, DevConfig.borders);
        drawTextbox(plot.range);
        //endregion
        //region draw plot line segments between data points
        for (int i = 0; i < DevConfig.plotColors.size() && i < dataSetNames.size(); i++) { // no colors left? no plot for you.
            g.setColor(DevConfig.plotColors.get(i));
            ArrayList<Float> dataSet = dataSets.get(dataSetNames.get(i));
            for (int n = 0; n < dataSet.size() - 1; n++) {
                g.drawLine(Math.round(plot.x + ((float) (n * pwidth)) / (float) (dataSet.size() - 1)),
                        plotYFromValue(plot, min, max, dataSet.get(n)),
                        Math.round(plot.x + ((float) ((n + 1) * pwidth)) / (float) (dataSet.size() - 1)),
                        plotYFromValue(plot, min, max, dataSet.get(n + 1)));
            }
        }
        //endregion
        //region legend. is this really enough?
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
        //endregion
    }

    private static void drawPortList(@NotNull PortList portList) {
        g.setColor(DevConfig.BACKGROUND);
        g.fillRect(portList.x, portList.y, (portList).width, portList.height);
        synchronized (portList.portButtons) {
            for (Button port : new ArrayList<>(portList.portButtons)) {
                drawButton(port);
            }
        }
    }

    private static void drawPlotContainer(@NotNull PlotContainer e) {
        synchronized (e.portPlotGroups) {
            if (!e.portPlotGroups.isEmpty()) {
                PortPlotGroup port = e.portPlotGroups.get(0);
                drawTextbox(port.baudrate);
                drawLabel(port.title);
                Button button = port.closeButton;
                if (button.isPressed()) {
                    g.setColor(button.textColor.darker());
                } else {
                    g.setColor(button.textColor);
                }
                g.drawRect(button.x, button.y, button.width, button.height);
                g.drawLine(button.x, button.y, button.x + button.width, button.y + button.height);
                g.drawLine(button.x + button.width, button.y, button.x, button.y + button.height);
                for (Plot plot : port.getPlots().values()) {
                    drawPlot(plot);
                }
                g.setColor(DevConfig.BACKGROUND);
            }
        }
    }

    private static void drawRect(@NotNull RectElement e, Color color) {
        g.setColor(color);
        g.drawRect(e.x, e.y, e.width, e.height);
    }


    private static void drawButton(@NotNull Button button) {
        if (button.isPressed()) {
            g.setColor(DevConfig.borders.darker());
        } else {
            g.setColor(DevConfig.borders);
        }
        g.drawRect(button.x, button.y, button.width, button.height);

        //g.drawRect(button.x + 4, button.y + 4, button.width - 8, button.height - 8);
        if (button.isPressed()) {
            g.setColor(button.textColor.darker());
        } else {
            g.setColor(button.textColor);
        }
        drawLabelText(button, button.textColor);
    }

    private static void drawToggleable(@NotNull Toggleable toggle) {
        if (toggle.getState()) {
            g.setColor(toggle.textColor.darker());
        } else {
            g.setColor(toggle.textColor);
        }
        g.drawRect(toggle.x, toggle.y, toggle.width, toggle.height);
        g.drawRect(toggle.x + 4, toggle.y + 4, toggle.width - 8, toggle.height - 8);
        drawLabelText(toggle, toggle.textColor);
    }

    private static void drawLabel(@NotNull Label label) {
        drawLabelText(label, label.textColor);
    }

    private static void drawTextbox(@NotNull Textbox textbox) {
        g.setColor(textbox.textColor);
        int texty = textbox.y + textbox.height / 2 + DevConfig.fontSize / 3;
        String visibleString;
        int cursorSubstring;
        //region draw whole text if start to end fits
        if (getStringWidth(textbox.text) < textbox.width - DevConfig.labelHorMargin * 2) {
            visibleString = textbox.text;
            textbox.displayIndex = 0;
            cursorSubstring = textbox.cursorIndex + 1;
        }
        //endregion
        else {
            cursorSubstring = 0;
            visibleString = "";
            textbox.displayIndex = Math.min(textbox.displayIndex, textbox.cursorIndex + 1);
            //region decrement display until display to end doesn't fit
            if (textbox.displayIndex != 0) {
                visibleString = "...";
            }
            do {
                textbox.displayIndex--;
                if (textbox.displayIndex <= 0) {
                    break;
                }
            } while ((getStringWidth(visibleString + textbox.text.substring(textbox.displayIndex))
                    + 2 * DevConfig.labelHorMargin < textbox.width));
            textbox.displayIndex++;
            //endregion
            if (textbox.displayIndex != 0) {
                visibleString = "...";
                cursorSubstring = 3;
            } else {
                visibleString = "";
            }
            //region increment display until display to cursor fits
            while (true) {
                if (getStringWidth(visibleString + textbox.text.substring(textbox.displayIndex, textbox.cursorIndex + 1) + "...")
                        + 2 * DevConfig.labelHorMargin < textbox.width) {
                    break;
                }
                if (getStringWidth(visibleString + textbox.text.substring(textbox.displayIndex))
                        + 2 * DevConfig.labelHorMargin < textbox.width) {
                    break;
                }
                textbox.displayIndex++;
            }
            //endregion
            cursorSubstring += textbox.cursorIndex + 1 - textbox.displayIndex;
            visibleString += textbox.text.substring(textbox.displayIndex);
            visibleString = truncateStringEnd(visibleString, textbox.width - 2 * DevConfig.labelHorMargin);
        }
        g.drawString(visibleString, textbox.x + textbox.width / 2 - g.getFontMetrics().stringWidth(visibleString) / 2, texty);
        //region cursor
        if (textbox.cursor) {
            int x = textbox.x + textbox.width / 2 - g.getFontMetrics().stringWidth(visibleString) / 2
                    + g.getFontMetrics().stringWidth(visibleString.substring(0, cursorSubstring));
            int y = textbox.y + textbox.height / 2 + DevConfig.fontSize / 3;
            g.drawLine(x, y, x, y - DevConfig.fontSize * 2 / 3);
        }
        //endregion
        drawRect(textbox, textbox.textColor);
    }

    private static void drawTextArea(@NotNull TextArea area) {
        area.refresh();
        ArrayList<String> entries = area.entries;
        if (area.width < 10) {
            return;
        }
        int topy = area.y + area.height;
        int maxHorShift = area.shiftLeft;
        String longestVisibleEntry = "";
        for (int i = entries.size() - 1 - area.shiftUp; i > -1; i--) {
            topy -= g.getFontMetrics().getHeight();
            if (topy < area.y) {
                break;
            }
            if (entries.get(i).length() > longestVisibleEntry.length()) {
                longestVisibleEntry = entries.get(i);
            }
        }
        if (longestVisibleEntry.isEmpty()) {
            maxHorShift = 0;
        } else {
            maxHorShift = Math.min(maxHorShift, maxHorShift +
                    g.getFontMetrics().stringWidth(longestVisibleEntry.substring(0, longestVisibleEntry.length() - 1))
                    - area.shiftLeft);
        }
        area.limitHorShift(maxHorShift);
        g.setColor(DevConfig.text);
        topy = area.y + area.height; // reset topy
        for (int i = entries.size() - 1 - area.shiftUp; i > -1; i--) {
            topy -= g.getFontMetrics().getHeight();
            if (topy < area.y) {
                break;
            }
            String entry = entries.get(i);
            //region shorten entry to make it fit, adding ... if shortening was needed
            if (g.getFontMetrics().stringWidth(entry) > area.width + maxHorShift) {
                entry = truncateStringEnd(entry, area.width + maxHorShift);
            }
            g.drawString(entry, area.x - maxHorShift, topy);
            //endregion
        }
        //region slider
        float proportion = ((float) ((entries.size()) - area.shiftUp)) / (entries.size() + 1);
        int size = area.height;
        if (!entries.isEmpty()) {
            size = Math.min(area.height, Math.max(DevConfig.minSliderLength,
                    (area.height / g.getFontMetrics().getHeight() * area.height) / (entries.size())));
        }
        int middle = (int) (area.y + (area.height - size) * proportion) + size / 2;
        int top = middle - (size + 1) / 2;
        int bot = middle + (size + 1) / 2;
        int shift = 0; //down
        if (top < area.y) {
            shift = area.y - top;
        }
        if (bot > area.y + area.height) {
            shift = area.y + area.height - bot;
        }
        top += shift;
        bot += shift;
        g.setColor(DevConfig.borders);
        int width = Math.min(DevConfig.maxSliderWidth, area.width / 8);
        g.fillRect(area.x + area.width - width, top, width, (bot - top));
        //endregion
    }

    private static void drawLabelText(@NotNull Label label, Color color) {
        g.setColor(color);
        int y = label.y + label.height / 2 + DevConfig.fontSize / 3;
        if (getStringWidth(label.text) <= label.width - DevConfig.labelHorMargin * 2) {
            g.drawString(label.text, label.x + label.width / 2 - g.getFontMetrics().stringWidth(label.text) / 2, y);
        } else {
            g.drawString(truncateStringEnd(label.text, label.width - DevConfig.labelHorMargin * 2),
                    label.x + DevConfig.labelHorMargin, y);
        }
    }

    //endregion
    private static float nDecPlaces(float number, int n) {
        return (float) (Math.round(number * Math.pow(10, n - 1)) / Math.pow(10, n - 1));
    }

    @Contract(pure = true)
    private static int plotYFromValue(@NotNull Plot plot, float min, float max, float value) {
        return Math.round(plot.y + plot.height - DevConfig.vertMargin - (plot.height - plot.title.height - 2 * DevConfig.vertMargin) * (value - min) / (max - min));
    }
    //endregion

    //region Animations
    public static Animation addAnimation(Animation animation) {
        animations.add(animation);
        return animation;
    }

    public static void removeAnimation(Animation animation) {
        synchronized (animations) {
            animations.remove(animation);
        }
    }
    //endregion


    private static void fill(Color c) {
        g.setColor(c);
        g.fillRect(-200, -200, width + 400, height + 400);
    }

    private static int getStringWidth(String string) {
        if (g != null) {
            return g.getFontMetrics().stringWidth(string);
        } else {
            return -1;
        }
    }

    @NotNull
    private static String truncateStringEnd(String string, int length) {
        String entry = string;
        if (getStringWidth(entry) < length) {
            return entry;
        }
        do {
            if (entry.length() <= 1) {
                return "";
            }
            entry = entry.substring(0, entry.length() - 1);
        } while (g.getFontMetrics().stringWidth(entry + "...") > length);
        return entry + "...";
    }

    public static void setPos(@NotNull ArrayRealVector p) {
        x = (int) p.getEntry(0);
        y = (int) p.getEntry(1);
    }
}

