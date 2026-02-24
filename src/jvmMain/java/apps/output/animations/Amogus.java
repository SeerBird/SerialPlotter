package apps.output.animations;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import apps.Handler;
import apps.Resources;
import apps.output.AppWindow;

import javax.imageio.ImageIO;

public class Amogus extends Canvas {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static ArrayList<BufferedImage> frames;
    static BufferedImage gif_consumer;
    int index;
    boolean painting = false;
    ScheduledFuture<?> animationFuture;

    public Amogus() {
        animationFuture = AppWindow.scheduler.scheduleAtFixedRate(this::repaint,
                0, 40, TimeUnit.MILLISECONDS);
    }

    static {
        frames = new ArrayList<>();
        for (File file : Resources.amogus) {
            try {
                frames.add(ImageIO.read(file));
            } catch (IOException e) {
                logger.info("Whatever.");
            }
        }
        try {
            gif_consumer = ImageIO.read(Resources.gif_consumer);
        } catch (IOException e) {
            logger.info("Whatever.");
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    public void stop() {
        animationFuture.cancel(true);
    }

    @Override
    public void paint(Graphics g) {
        if (!Handler.getBullshitOn()) {
            return;
        }
        if (painting) {
            repaint();
            return;
        }
        try {
            painting = true;
            if (getBufferStrategy() == null) {
                createBufferStrategy(2);
            }
            g = getBufferStrategy().getDrawGraphics();
            if (Math.random() < 3e-3) {
                g.drawImage(gif_consumer.getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST),
                        0, 0, null);
            } else {
                g.drawImage(frames.get(index).getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST),
                        0, 0, null);
                index = (index + 1) % Resources.amogus.size();
            }
            g.dispose();
            getBufferStrategy().show();
        } finally {
            painting = false;
        }
    }
}
