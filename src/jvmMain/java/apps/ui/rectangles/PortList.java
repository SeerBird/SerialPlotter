package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;


import java.util.ArrayList;

public class PortList extends RectElement{
    public ArrayList<Button> ports;
    Button pressed;
    public PortList(int x, int y, int width, int height) {
        super(x, y, width, height);
        ports = new ArrayList<>();
    }
    public void updatePorts(){
        if(hovered){
            return; //don't move under the cursor.
        }
        this.ports.clear();
        int topY = y;
        for(SerialPort port: Handler.getPorts()){
            this.ports.add(new Button(x,topY,width, DevConfig.fontSize+DevConfig.vertMargin*2,
                    ()-> Menu.addPortPlotGroup(port),port.getDescriptivePortName(), DevConfig.shell));
            topY+=DevConfig.fontSize+20;
        }
        height = topY-y;
    }

    @Override
    public void release() {
        if(pressed == null){
            return; //can this happen? don't care
        }
        pressed.release();
    }

    @Override
    public boolean press(double x, double y) {
        if (super.press(x,y)){
            for(Button port:ports){
                if(port.press(x,y)){
                    pressed = port;
                    return true;
                }
            }
        }
        return false;
    }
}
