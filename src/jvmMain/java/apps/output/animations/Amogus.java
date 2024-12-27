package apps.output.animations;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import apps.Resources;

import javax.imageio.ImageIO;

public class Amogus implements Animation {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static BufferedImage frame;
    static ArrayList<BufferedImage> frames;
    int x;
    int y;
    int width;
    int height;
    int index;
    static{
        frames = new ArrayList<>();
        for(File file:Resources.amogus){
            try {
                frames.add(ImageIO.read(file));
            } catch (IOException e) {
                logger.info("Whatever.");
            }
        }
    }

    public Amogus(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics g) {
        boolean res = g.drawImage(frames.get(index).getScaledInstance(width,height,Image.SCALE_FAST), x, y, null);
        if(res){
            //huh
        }
    }

    public void next() {
        index=(index+1)%Resources.amogus.size();
    }

    @Override
    public Rectangle rect() {
        return new Rectangle(x, y, width, height);
    }
}
