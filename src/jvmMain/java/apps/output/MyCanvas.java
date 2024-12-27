package apps.output;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class MyCanvas extends Canvas {
    private BufferedImage bufferImage;
    private Graphics bufferGraphics;
    private BufferStrategy strategy;

    public void paint(Graphics g) {
        if(strategy==null){
            createBufferStrategy(2);
            strategy = getBufferStrategy();
        }
        Graphics bg = strategy.getDrawGraphics();
        Renderer.drawImage(bg, getWidth(), getHeight());
        strategy.show();
    }
    public void update(Graphics g) {
        paint(g);
    }
}
