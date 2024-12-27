package apps.output;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class MyCanvas extends Canvas {
    private BufferedImage bufferImage;
    private Graphics bufferGraphics;

    public void paint(Graphics g) {
        BufferStrategy strategy = getBufferStrategy();
        if(strategy==null){
            createBufferStrategy(2);
            strategy = getBufferStrategy();
        }
        Graphics bg = strategy.getDrawGraphics();
        Renderer.drawImage(bg, getWidth(), getHeight());
        strategy.show();
    }
}
