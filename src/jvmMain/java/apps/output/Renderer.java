package apps.output;


import apps.output.animations.Animation;
import apps.ui.IElement;
import apps.ui.Menu;
import apps.ui.rectangles.*;
import apps.ui.rectangles.Button;
import apps.ui.rectangles.Label;
import apps.ui.rectangles.TextArea;
import apps.util.DevConfig;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Renderer {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static Graphics g;
    static final ArrayList<Animation> animations = new ArrayList<>();
    static int x = 0;
    static int y = 0;
    static int width = 0;
    static int height = 0;

    public static void update() { //get new info and progress animations
        for (Animation animation : new ArrayList<>(animations)) {
            if (!animation.drawNext(g)) {
                removeAnimation(animation);
            }
        }
    }

    public static void drawImage(@NotNull Graphics g, int width, int height) {
        Renderer.width = width;
        Renderer.height = height;
        Renderer.g = g;
        g.translate(x, y);
        fill(DevConfig.BACKGROUND);
        update();
        drawContent();
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
            g.fillRect(port.close.x,port.close.y,port.close.width,port.close.height);
            drawButton(port.close);
        }
    }

    private static void drawPlot(@NotNull Plot plot) {
        drawLabel(plot.title);
        drawTextbox(plot.range);
        drawRect(plot, DevConfig.shell);
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
        ArrayList<String> entries = area.entries;
        if (area.width < 10) {
            return;
        }
        int topy = area.y + area.height;
        for (int i = entries.size() - 1; i > -1; i--) {
            g.setColor(DevConfig.shell);
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
        animations.remove(animation);
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

