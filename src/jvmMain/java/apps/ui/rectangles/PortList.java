package apps.ui.rectangles;

import apps.Handler;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;


import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PortList extends RectElement {
    public final ArrayList<Button> portButtons = new ArrayList<>();
    Button pressed;
    static SerialPort[] lastPorts = new SerialPort[0];

    public PortList(int x, int y, int width, int height) {
        super(x, y, width, height);

        Handler.getScheduler().scheduleAtFixedRate(() -> {
            SerialPort[] ports = Handler.getPorts();
            if (ports.length == 0) {
                Menu.log("No ports found!");
                return;
            }
            lastPorts = ports;
            updatePorts(ports);
            Menu.update();
            Handler.repaint(x,y,width,height);
        }, 8, DevConfig.portListRefreshPeriod, TimeUnit.MILLISECONDS);
    }

    public void updatePorts(SerialPort[] ports) {
        synchronized (portButtons) {
            if (hovered) {
                return; //don't move under the cursor.
            }
            this.portButtons.clear();
            int topY = y;
            for (SerialPort port : ports) {
                this.portButtons.add(new Button(x, topY, width, DevConfig.fontSize + DevConfig.vertMargin * 2,
                        () -> {
                            //updatePorts(); // commodification?
                            Menu.addPortPlotGroup(port);
                        }, port.getDescriptivePortName(), DevConfig.text));
                topY += DevConfig.fontSize + DevConfig.vertMargin * 2;
            }
        }
    }

    public void updateButtons() {
        synchronized (portButtons) {
            int topY = y;
            for (Button button : portButtons) {
                button.x = x;
                button.y = topY;
                button.width = width;
                button.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
                topY += DevConfig.fontSize + DevConfig.vertMargin * 2;
            }
            height = topY - y;
            Handler.repaint(x, y, width, height);
        }
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
