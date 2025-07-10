package apps.output;


import apps.Handler;
import apps.Resources;
import apps.input.InputControl;
import apps.output.audio.Audio;
import apps.output.audio.Sound;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.ui.FlatTitlePane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class AppWindow extends JFrame implements SerialPortMessageListener {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //region rename direction afterwards but I need this or my brain will break
    private static final String RIGHT = SpringLayout.EAST;
    private static final String LEFT = SpringLayout.WEST;
    private static final String TOP = SpringLayout.NORTH;
    private static final String BOTTOM = SpringLayout.SOUTH;
    //endregion
    //region defaults
    public static final int WIDTH = 1140;
    public static final int HEIGHT = 600;
    public static final int leftWidth = 300;
    public static final int commandLineHeight = 30;
    private static final int DEFAULT_BAUDRATE = 9600;
    //endregion
    //region variables
    JTextPane log;
    JScrollPane logScroll;
    JLabel portTitle;
    JTextField baudrate;
    SerialPort[] lastPorts = new SerialPort[0];
    JCheckBoxMenuItem logPackets;
    SerialPort connected;
    JPanel plotGroup;
    String leftover;
    long lastReceivedTime;
    final HashMap<Integer, Boolean> timeoutFlags = new HashMap<>();
    HashMap<String, Plot> plots = new HashMap<>();
    final HashMap<String, Plot> newPlots = new HashMap<>();
    JPanel rightPanel;
    //endregion

    public AppWindow() {
        setResizable(true);
        setMinimumSize(new Dimension(400, 400));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //region set font
        try {
            setFont(Font.createFont(Font.TRUETYPE_FONT, Resources.comfortaa)
                    .deriveFont(Font.BOLD, DevConfig.fontSize));
        } catch (FontFormatException | IOException e) {
            setFont(getFont().deriveFont(Font.BOLD, DevConfig.fontSize));
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
        //endregion
        //region hook up apps.input
        //InputControl input = new InputControl();
        //addKeyListener(input);
        //addMouseListener(input);
        //addMouseMotionListener(input);
        //addMouseWheelListener(input);
        //addWindowListener(input);
        //endregion
        initComponents();
    }

    private void initComponents() {
        setTitle("SerialPlotter");
        Container contentPane = getContentPane();
        SpringLayout contentPaneLayout = new SpringLayout();
        contentPane.setLayout(contentPaneLayout);
        //region menuBar
        JMenuBar menuBar = new JMenuBar();
        //region portConnect
        JMenu portConnect = new JMenu("Connect");
        Handler.getScheduler().scheduleAtFixedRate(() -> {
            SerialPort[] ports = Handler.getPorts();
            if (ports.length == 0) {
                log("No ports found!");
                return;
            }
            lastPorts = ports;
            if (portConnect.isPopupMenuVisible()) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                portConnect.removeAll();
                for (SerialPort port : ports) {
                    JMenuItem portButton = new JMenuItem(new AbstractAction(port.getDescriptivePortName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            logger.info("Connecting to port " + port.getDescriptivePortName() + " , trust");
                            connectToPort(port);
                        }
                    });
                    portConnect.add(portButton);
                }
            });
        }, 8, DevConfig.portListRefreshPeriod, TimeUnit.MILLISECONDS);
        menuBar.add(portConnect);
        //endregion
        //region disconnect
        JButton disconnect = new JButton("Disconnect");
        disconnect.addActionListener(e -> closePort(connected));
        menuBar.add(disconnect);
        //endregion
        //region logPackets
        logPackets = new JCheckBoxMenuItem("Log packets");


        menuBar.add(logPackets);
        //endregion logPackets
        setJMenuBar(menuBar);
        //endregion
        //region leftPanel
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(leftWidth, HEIGHT));
        contentPane.add(leftPanel);
        //leftPanel.setBounds(0,0,leftWidth,HEIGHT);
        {
            SpringLayout leftPanelLayout = new SpringLayout();
            leftPanel.setLayout(leftPanelLayout);
            //region commandLine
            JTextField commandLine = new JTextField();
            leftPanel.add(commandLine);
            {
                commandLine.setMinimumSize(null);
                commandLine.setMaximumSize(null);
            }
            //commandLine.setBounds(0,HEIGHT-commandLineHeight,leftWidth,commandLineHeight);
            leftPanelLayout.putConstraint(LEFT, commandLine, 0, LEFT, leftPanel);
            leftPanelLayout.putConstraint(RIGHT, commandLine, 0, RIGHT, leftPanel);
            leftPanelLayout.putConstraint(BOTTOM, commandLine, 0, BOTTOM, leftPanel);
            //endregion
            //region logScroll
            log = new JTextPane() {
                public boolean getScrollableTracksViewportWidth() {
                    return getSize().width < getParent().getSize().width;
                }

                public void setSize(@NotNull Dimension d) {
                    if (d.width < getParent().getSize().width) {
                        d.width = getParent().getSize().width;
                    }
                    super.setSize(d);
                }
            };
            log.setEditable(false);
            logScroll = new JScrollPane(log);
            new SmartScroller(logScroll);
            leftPanel.add(logScroll);
            {
                logScroll.setMinimumSize(null);
                logScroll.setMaximumSize(null);
            }
            leftPanelLayout.putConstraint(LEFT, logScroll, 0, LEFT, leftPanel);
            leftPanelLayout.putConstraint(BOTTOM, logScroll, 0, TOP, commandLine);
            leftPanelLayout.putConstraint(TOP, logScroll, 0, TOP, leftPanel);
            leftPanelLayout.putConstraint(RIGHT, logScroll, 0, RIGHT, leftPanel);
            //endregion
        }
        contentPaneLayout.putConstraint(LEFT, leftPanel, 0, LEFT, contentPane);
        contentPaneLayout.putConstraint(BOTTOM, leftPanel, 0, BOTTOM, contentPane);
        contentPaneLayout.putConstraint(TOP, leftPanel, 0, TOP, contentPane);
        //endregion
        //region rightPanel
        rightPanel = new JPanel();
        {
            SpringLayout rightPanelLayout = new SpringLayout();
            rightPanel.setLayout(rightPanelLayout);
            //region baudrate
            baudrate = new JTextField();
            rightPanelLayout.putConstraint(RIGHT, baudrate, 0, RIGHT, rightPanel);
            rightPanelLayout.putConstraint(TOP, baudrate, 0, TOP, rightPanel);
            rightPanel.add(baudrate);
            //endregion
            //region baudrateLabel
            JLabel baudrateLabel = new JLabel("Baudrate:");
            rightPanelLayout.putConstraint(TOP, baudrateLabel, 0, TOP, rightPanel);
            rightPanelLayout.putConstraint(RIGHT, baudrateLabel, 0, LEFT, baudrate);
            rightPanelLayout.putConstraint(BOTTOM, baudrateLabel, 0, BOTTOM, baudrate);
            rightPanel.add(baudrateLabel);
            //endregion
            //region portTitle
            portTitle = new JLabel();
            rightPanelLayout.putConstraint(TOP, portTitle, 0, TOP, rightPanel);
            rightPanelLayout.putConstraint(RIGHT, portTitle, 0, LEFT, baudrateLabel);
            rightPanelLayout.putConstraint(LEFT, portTitle, 0, LEFT, rightPanel);
            rightPanelLayout.putConstraint(BOTTOM, portTitle, 0, BOTTOM, baudrate);
            rightPanel.add(portTitle);
            //endregion
            //region plotGroup
            plotGroup = new JPanel();
            rightPanelLayout.putConstraint(TOP, plotGroup, 0, BOTTOM, baudrate);
            rightPanelLayout.putConstraint(BOTTOM, plotGroup, 0, BOTTOM, rightPanel);
            rightPanelLayout.putConstraint(RIGHT, plotGroup, 0, RIGHT, rightPanel);
            rightPanelLayout.putConstraint(LEFT, plotGroup, 0, LEFT, rightPanel);
            rightPanel.add(plotGroup);
            //endregion
            rightPanel.setVisible(false);
        }
        contentPane.add(rightPanel);
        contentPaneLayout.putConstraint(RIGHT, rightPanel, 0, RIGHT, contentPane);
        contentPaneLayout.putConstraint(TOP, rightPanel, 0, TOP, contentPane);
        contentPaneLayout.putConstraint(BOTTOM, rightPanel, 0, BOTTOM, contentPane);
        contentPaneLayout.putConstraint(LEFT, rightPanel, 0, RIGHT, leftPanel);
        //endregion
        new ComponentResizer(new Insets(0, 0, 0, 5), leftPanel, rightPanel);
    }

    public void log(String message) { //TODO: colours in log here would be good
        StyledDocument doc = log.getStyledDocument();

        Style style = log.addStyle("Color Style", null);
        StyleConstants.setForeground(style, Color.white);
        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            logger.info(e.getMessage());
        }
    }

    private void connectToPort(SerialPort port) {
        if (connected != null) {
            try {
                Handler.timeout(() -> closePort(connected), 1000);
            } catch (ExecutionException e) {
                logger.info("Exception closing " + connected.getDescriptivePortName());
                Menu.log("Exception closing " + connected.getDescriptivePortName());
            } catch (TimeoutException e) {
                logger.info("Timed out closing " + connected.getDescriptivePortName());
                Menu.log("Timed out closing " + connected.getDescriptivePortName());
            }
        }
        connected = null;
        SwingUtilities.invokeLater(this::clearRightPanel);
        //region try to open and attach this to port
        boolean opening = false;
        boolean listening = false;
        try {
            opening = Handler.timeout(port::openPort, 1000);
        } catch (TimeoutException e) {
            logger.info("Timed out opening port " + port.getDescriptivePortName());
            log("Timed out opening port " + port.getDescriptivePortName());
        } catch (ExecutionException e) {
            logger.info("Exception opening port " + port.getDescriptivePortName() + ": " + e.getMessage());
            log("Exception opening port " + port.getDescriptivePortName() + ": " + e.getMessage());
        }
        if (!opening) {
            logger.info("Failed to open port " + port.getDescriptivePortName());
            log("Failed to open port " + port.getDescriptivePortName());
        } else {
            logger.info("Opened " + port.getDescriptivePortName());
            log("Opened " + port.getDescriptivePortName());
        }
        try {
            listening = Handler.timeout(() -> {
                port.flushDataListener();
                port.flushIOBuffers(); //TODO: this line and the above one, can they be removed?
                port.removeDataListener();
                return port.addDataListener(this);
            }, 1000);
        } catch (TimeoutException e) {
            logger.info("Timed out attaching listener to port " + port.getDescriptivePortName());
            log("Timed out attaching listener to port " + port.getDescriptivePortName());
        } catch (ExecutionException e) {
            logger.info("Exception attaching listener to port " + port.getDescriptivePortName() + ": " + e.getMessage());
            log("Exception attaching listener to port " + port.getDescriptivePortName() + ": " + e.getMessage());
        }
        if (!listening) {
            logger.info("Failed to attach listener to port " + port.getDescriptivePortName());
            log("Failed to attach listener to port " + port.getDescriptivePortName());
        } else {
            logger.info("Listening to  " + port.getDescriptivePortName());
            log("Listening to  " + port.getDescriptivePortName());
        }
        //endregion
        if (!(listening && opening)) {
            return;
        }
        //region set up right panel
        connected = port;
        SwingUtilities.invokeLater(() -> {
            portTitle.setText(port.getDescriptivePortName());
            rightPanel.setVisible(true);
        });
        //endregion
    }

    private boolean closePort(SerialPort port) {
        if (port == null) {
            log("No port connected!");
            return false;
        }
        port.removeDataListener();
        Menu.log("Stopped listening to " + port.getDescriptivePortName());
        boolean res = port.closePort();
        if (res) {
            Menu.log("Closed " + port.getDescriptivePortName());
        } else {
            Menu.log("Failed to close " + port.getDescriptivePortName());
        }
        return res;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (connected != event.getSerialPort()) {
            return; // this prolly can't happen
        }
        try {
            switch (event.getEventType()) {
                // region data available
                case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                    if (System.nanoTime() - lastReceivedTime < DevConfig.messageReceivePeriod * 1000000) {
                        return; // let's not check our inbox too often
                    }
                    lastReceivedTime = System.nanoTime();
                    byte[] buf = new byte[connected.bytesAvailable()];
                    int numRead = connected.readBytes(buf, buf.length);
                    boolean plotAdded = false;
                    String message = leftover + new String(buf, StandardCharsets.UTF_8);
                    leftover = "";
                    //region read message
                    while (true) {
                        //region log text that is outside a packet after a timeout(100ms)
                        synchronized (timeoutFlags) {
                            timeoutFlags.replaceAll((s, v) -> true);
                        }
                        if ((!message.contains("{"))) {
                            if(message.length()>DevConfig.maxBetweenMessageLength){
                                log('"'+message+'"');
                                break;
                            }
                            leftover = message;
                            int uniqueIndex = 0;
                            synchronized (timeoutFlags) {
                                while (timeoutFlags.containsKey(uniqueIndex)) {
                                    uniqueIndex++;
                                }
                                timeoutFlags.put(uniqueIndex, false);
                            }

                            int finalUniqueIndex = uniqueIndex;
                            Handler.getScheduler().schedule(() -> {
                                synchronized (timeoutFlags) {
                                    if (!timeoutFlags.get(finalUniqueIndex)) {
                                        log('"'+leftover+'"');
                                        leftover = "";
                                    }
                                    timeoutFlags.remove(finalUniqueIndex);
                                }
                            }, 100, TimeUnit.MILLISECONDS);
                            break; //nothing left to read
                        }
                        //endregion
                        //region log text that is between packets
                        String outsideOfPacket = message.substring(0, message.indexOf("{"));
                        if (!outsideOfPacket.isEmpty()) {
                            log('"' + outsideOfPacket + '"');
                            message = message.substring(message.indexOf("{"));
                        }
                        //endregion
                        if (!message.contains("}")) {
                            leftover = message;
                            break; //nothing left to read
                        }

                        String packet = message.substring(1, message.indexOf("}")); // plotName(key:value,key:value)plotName(key:value,key:value)
                        message = message.substring(message.indexOf("}") + 1); // will this break if the message ends with "}"?
                        //region read packet
                        while (true) {
                            if (!packet.contains("(")) {
                                if (!packet.isEmpty()) {
                                    logger.info("Leftovers in packet: " + packet);
                                    log("Leftovers in packet: " + packet);
                                }
                                break;
                            }
                            String plotName = packet.substring(0, packet.indexOf("(")); // is "" an ok plot name? prolly.
                            Plot plot = plots.get(plotName);
                            //region create plot if absent
                            if (plot == null) {
                                synchronized (newPlots) {
                                    plot = newPlots.get(plotName);
                                    if (plot == null) {
                                        plot = createNewPlot(plotName);
                                        newPlots.put(plotName, plot);
                                    }
                                }
                            }
                            //endregion
                            packet = packet.substring(packet.indexOf("("));
                            if (!packet.contains(")")) {
                                logger.info("Unclosed (: " + plotName + packet);
                                Menu.log("Unclosed (: " + plotName + packet);
                                break;
                            }
                            String plotData = packet.substring(1, packet.indexOf(")")); // key:value,key:value
                            packet = packet.substring(packet.indexOf(")") + 1);
                            if (logPackets.isSelected()) {
                                log(plotName + "(" + plotData + ")");
                            }
                            plotData += ",";
                            //region read plot data
                            while (plotData.contains(",")) {
                                //region read pair
                                String pair = plotData.substring(0, plotData.indexOf(",")); //a:n
                                plotData = plotData.substring(plotData.indexOf(",") + 1);
                                if (!pair.contains(":")) {
                                    logger.info("Malformed pair: " + pair);
                                    continue; //discard this pair
                                }
                                String key = pair.substring(0, pair.indexOf(":"));
                                String valueString = pair.substring(pair.indexOf(":") + 1);
                                float value;
                                try {
                                    value = Float.parseFloat(valueString);
                                } catch (NumberFormatException e) {
                                    logger.info("Number format exception: " + valueString);
                                    continue; //discard this pair
                                }
                                //endregion
                                plot.addValue(key, value);
                            }
                            if (!plotData.isEmpty()) {
                                log("Leftovers in plot data: " + plotData);
                            }
                            //endregion
                        }
                        //endregion
                    }
                    //endregion
                    if (plotAdded) {
                        Menu.queueUpdate();
                        Handler.repaint();
                    }
                    break;
                // endregion
                case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                    clearRightPanel();
                    closePort(connected); //TODO: really not sure ab this
                    logger.info("Port " + connected.getDescriptivePortName() + " disconnected.");
                    log("Port " + connected.getDescriptivePortName() + " disconnected.");
                    break;
                default:
                    logger.info("Unexpected event type: " + event.getEventType());
                    log("Unexpected event type: " + event.getEventType());
            }
        } catch (Exception e) {
            logger.info(e.getClass() + e.getMessage());
        }
    }

    @NotNull
    private Plot createNewPlot(String plotName) {
        JLabel plotTitle = new JLabel(plotName);
        JTextField range = new JTextField(String.valueOf(DevConfig.defaultRange));
        JPanel legend = new JPanel();
        Plot plot = new Plot(plotTitle, plotName, legend);
        range.addActionListener(e -> {
            try {
                int value = Integer.parseInt(e.getActionCommand());
                if (value < 1) {
                    throw new NumberFormatException("Nope");
                }
                plot.rangeN = value;
            } catch (NumberFormatException ex) {
                Audio.playSound(Sound.stopPls);
            }
        });
        JPanel plotPanel = new JPanel(); //TODO: layout of everything and update legend inside Plot
        return plot;
    }

    private void clearRightPanel() {
        portTitle.setText("");
        plotGroup.removeAll();
        baudrate.setText(String.valueOf(DEFAULT_BAUDRATE));
        rightPanel.setVisible(false);
    }

    @Override
    public byte[] getMessageDelimiter() {
        return ";".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        return true;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE |
                SerialPort.LISTENING_EVENT_BREAK_INTERRUPT |
                SerialPort.LISTENING_EVENT_PORT_DISCONNECTED |
                SerialPort.LISTENING_EVENT_DATA_WRITTEN;
    }
}