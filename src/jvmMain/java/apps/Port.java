package apps;

import com.fazecast.jSerialComm.*;
public class Port implements SerialPortDataListener{
    SerialPort port;
    public Port(SerialPort port){
        this.port = port;
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
}