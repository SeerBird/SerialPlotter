package apps.ui.rectangles;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class Plot extends RectElement implements SerialPortDataListener {
    RectElement pressed;
    SerialPort port;
    Button close;
    Textbox range;
    public Plot(int x, int y, int width, int height, SerialPort port) {
        super(x, y, width, height);

    }
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE|
                SerialPort.LISTENING_EVENT_BREAK_INTERRUPT|
                SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch(event.getEventType()){
            case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:

            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                port.closePort();
        }
    }

    @Override
    public boolean press(double x, double y) {
        if(close.press(x,y)){
            pressed = close;
            return true;
        }
        if(range.press(x,y)){
            pressed = range;
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        if(pressed==null){
            return;
        }
        pressed.release();
        pressed = null;
    }
}
