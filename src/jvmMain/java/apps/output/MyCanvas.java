package apps.output;

import java.awt.*;

public class MyCanvas extends Canvas {
    @Override
    public void paint(Graphics g) {
        Renderer.drawImage(g);
    }
}
