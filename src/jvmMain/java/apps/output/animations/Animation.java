package apps.output.animations;

import java.awt.*;

public interface Animation {
    /**
     * @param g the Graphics object to draw the animation frame on
     * @return true to keep the animation running, false to make the renderer dispose of the animation
     */
    boolean drawNext(Graphics g);
}
