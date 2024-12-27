package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;


import java.io.Serial;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PortList extends RectElement {
    public final ArrayList<Button> portButtons = new ArrayList<>();
    Button pressed;
    static SerialPort[] lastPorts = new SerialPort[0];

    public PortList(int x, int y, int width, int height) {
        super(x, y, width, height);

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(()->{
            SerialPort[] ports = Handler.getPorts();
            if(ports.length!= lastPorts.length){
                lastPorts = ports;
                updatePorts();
                Menu.update();
            }
        },8,DevConfig.portListRefreshPeriod, TimeUnit.MILLISECONDS);
    }

    public void updatePorts() {
        if (hovered) {
            return; //don't move under the cursor.
        }
        this.portButtons.clear();
        int topY = y;
        for (SerialPort port : Handler.getPorts()) {
            this.portButtons.add(new Button(x, topY, width, DevConfig.fontSize + DevConfig.vertMargin * 2,
                    () -> Menu.addPortPlotGroup(port), port.getDescriptivePortName(), DevConfig.shell));
            topY += DevConfig.fontSize + 20;
        }
    }
    public void updateButtons(){
        int topY = y;
        for (Button button: portButtons) {
            button.x=x;
            button.y=topY;
            button.width=width;
            button.height=DevConfig.fontSize + DevConfig.vertMargin * 2;
            topY += DevConfig.fontSize + DevConfig.vertMargin * 2;
        }
        height = topY - y;
        Handler.repaint(x,y,width,height);
    }

    @Override
    public void release() {
        if (pressed == null) {
            return; //can this happen? don't care
        }
        pressed.release();
    }

    @Override
    public boolean press(double x, double y) {
        if (super.press(x, y)) {
            for (Button port : portButtons) {
                if (port.press(x, y)) {
                    pressed = port;
                    return true;
                }
            }
        }
        return false;
    }
}
