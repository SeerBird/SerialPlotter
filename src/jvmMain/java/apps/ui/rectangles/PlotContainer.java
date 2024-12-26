package apps.ui.rectangles;


import apps.ui.IElement;
import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;

public class PlotContainer extends RectElement {
    ArrayList<Plot> plots = new ArrayList<>();
    Plot pressed;
    public PlotContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void release() {
        if(pressed == null){
            return;
        }
        pressed.release();
        pressed = null;
    }

    @Override
    public boolean press(double x, double y) {
        for(Plot plot:plots){
            if(plot.press(x,y)){
                pressed = plot;
                return true;
            }
        }
        return false;
    }

    public void addPlot(SerialPort port) {
        plots.add(new Plot(x,y,width,height, port)); // make them stack
    }
}
