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
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
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
        for (IElement e : Menu.getElements()) {
            if (e instanceof PortList) {
                g.setColor(DevConfig.BACKGROUND);
                g.fillRect(e.x, e.y, ((PortList) e).width, ((PortList) e).height);
                for (Button port : ((PortList) e).portButtons) {
                    drawButton(port);
                }
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

    private static void drawPlotContainer(PlotContainer e) {
        if (!e.getPortPlotGroups().isEmpty()) {
            PortPlotGroup port = e.getPortPlotGroups().get(0);
            for (Plot plot : port.getPlots().values()) {
                drawPlot(plot);
            }
            g.setColor(DevConfig.BACKGROUND);
            g.fillRect(port.close.x, port.close.y, port.close.width, port.close.height);
            drawButton(port.close);
        }
    }

    private static void drawPlot(@NotNull Plot plot) {
        //logger.info("Started a plot");
        drawRect(plot, DevConfig.borders);
        //region draw plot line segments between data points
        int pwidth = plot.width;
        int pheight = plot.height - plot.title.height;
        ArrayList<Float> values = plot.getValues();
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for (Float value : values) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        if (max == min) {
            //logger.info("Only one value?");
            return; // nah you can figure out what the value is if it's only one. get out.
        }
        for (int i = 0; i < values.size() - 1; i++) {
            g.drawLine(Math.round(plot.x + ((float) (i * pwidth)) / (float) values.size()),
                    plotYFromValue(plot, min, max, values.get(i)),
                    Math.round(plot.x + ((float) ((i + 1) * pwidth)) / (float) values.size()),
                    plotYFromValue(plot, min, max, values.get(i + 1)));
        }
        //endregion
        //region grid
        int order = (int) Math.round((Math.log(max - min)) / Math.log(10)) - 1;
        float unit = (float) Math.pow(10, order);
        if (unit == 0) {
            return;
        }
        float unitRange = (max - min) / unit;
        g.setColor(DevConfig.gridColor);
        //region get valueGridSpacing
        int prevGridSpacing = Integer.MIN_VALUE;

        float step = 0.5F;
        float unitGridSpacing = step;
        int gridSpacing = (int) Math.ceil(pheight * unitRange / unitGridSpacing);
        while (Math.abs(prevGridSpacing - DevConfig.optimalGridlineSpacing) >
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
            g.drawLine(plot.x, y,
                    plot.x + plot.width, y);
            g.drawString(String.valueOf(nDecPlaces((value / unit), 3)), plot.x, y);
            lineID = goingUp ? lineID + 1 : lineID - 1;
        }
        //endregion
        //endregion
        String title = plot.title.text;
        title = "E" + order + ";" + title.substring(title.indexOf(";") + 1); // is this thread-safe? prolly not.
        plot.title.text = title;
        drawLabel(plot.title);
        drawTextbox(plot.range);
        //logger.info("Finished a plot");
    }

    private static float nDecPlaces(float number, int n) {
        return (float) (Math.round(number * Math.pow(10, n - 1)) / Math.pow(10, n - 1));
    }

    @Contract(pure = true)
    private static int plotYFromValue(@NotNull Plot plot, float min, float max, float value) {
        return Math.round(plot.y + plot.height - (plot.height - plot.title.height) * (value - min) / (max - min));
    }

    //region draw elements
    private static void drawRect(@NotNull RectElement e, Color color) {
        g.setColor(color);
        g.drawRect(e.x, e.y, e.width, e.height);
    }


    private static void drawButton(@NotNull Button button) {
        if (button.isPressed()) {
            g.setColor(button.textColor.darker());
        } else {
            g.setColor(button.textColor);
        }
        g.drawRect(button.x, button.y, button.width, button.height);

        //g.drawRect(button.x + 4, button.y + 4, button.width - 8, button.height - 8);
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
        drawLabelText(textbox, textbox.textColor);
        drawRect(textbox, textbox.textColor);
        if (textbox.cursor) {
            int x = textbox.x + textbox.width / 2 + g.getFontMetrics().stringWidth(textbox.text) / 2 + 2;
            int y = textbox.y + textbox.height / 2 + DevConfig.fontSize / 3;
            g.drawLine(x, y, x, y - DevConfig.fontSize * 2 / 3);
        }
    }

    private static void drawTextArea(@NotNull TextArea area) {
        area.refresh();
        ArrayList<String> entries = area.entries;
        if (area.width < 10) {
            return;
        }
        int topy = area.y + area.height;
        for (int i = entries.size() - 1; i > -1; i--) {
            g.setColor(DevConfig.borders);
            topy -= g.getFontMetrics().getHeight();
            if (topy < area.y) {
                break;
            }
            String entry = entries.get(i);
            //region shorten entry to make it fit, adding ... if shortening was needed
            if (g.getFontMetrics().stringWidth(entry) > area.width) {
                entry = truncateString(entry, area.width);
            }
            g.drawString(entry, area.x, topy);
            //endregion
        }
    }

    private static void drawLabelText(@NotNull Label label, Color color) {
        g.setColor(color);
        int y = label.y + label.height / 2 + DevConfig.fontSize / 3;
        if (getStringWidth(label.text) < label.width - DevConfig.labelHorMargin * 2) {
            g.drawString(label.text, label.x + label.width / 2 - g.getFontMetrics().stringWidth(label.text) / 2, y);
        } else {
            g.drawString(truncateString(label.text, label.width - DevConfig.labelHorMargin * 2),
                    label.x + DevConfig.labelHorMargin, y);
        }

    }

    //endregion
    //endregion

    //region Animations
    public static Animation addAnimation(Animation animation) {
        animations.add(animation);
        return animation;
    }

    public static void removeAnimation(Animation animation) {
        synchronized (animations){
        animations.remove(animation);}
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
    private static String truncateString(String string, int length) {
        String entry = string;
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

