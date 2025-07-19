package apps.output;


import apps.Handler;
import apps.Resources;
import apps.output.audio.Audio;
import apps.output.audio.Sound;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static apps.output.LogEntry.Type.*;

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
    //endregion
    //region variables
    JTextField commandLine;
    private static final ArrayList<String> commandLog = new ArrayList<>();
    private static int commandID = 0;
    final ArrayList<LogEntry> newLogEntries = new ArrayList<>();
    final ArrayList<LogEntry> logEntries = new ArrayList<>();
    HashMap<LogEntry.Type, Style> messageStyles = new HashMap<>();
    boolean lastLoggingPackets = DevConfig.defaultLoggingPackets;
    int baudrateDefault = 9600;
    JTextPane log;
    JScrollPane logScroll;
    JLabel portTitle;
    JTextField baudrate;
    SerialPort[] lastPorts = new SerialPort[0];
    JCheckBoxMenuItem logPackets;
    SerialPort connected;
    boolean connecting = false;
    JPanel plotGroup;
    String leftover = "";
    long lastReceivedTime;
    final HashMap<Integer, Boolean> timeoutFlags = new HashMap<>();
    final HashMap<String, Plot> plots = new HashMap<>();
    final HashMap<String, Plot> newPlots = new HashMap<>();
    final AppWindow window = this; //TODO: this is ugly plsssss
    final MouseListener refocuser = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent e) {
            window.requestFocus();
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    };
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
        AppWindow window = this;
        this.addMouseListener(refocuser);
        initComponents();
    }

    private void initComponents() {
        setTitle("SerialPlotter");
        Container contentPane = getContentPane();
        SpringLayout contentPaneLayout = new SpringLayout();
        contentPane.setLayout(contentPaneLayout);
        //region menuBar
        JMenuBar menuBar = new JMenuBar();
        menuBar.addMouseListener(refocuser);
        //region portConnect
        JMenu portConnect = new JMenu("Connect");
        Handler.getScheduler().scheduleAtFixedRate(() -> {
            SerialPort[] ports = Handler.getPorts();
            if (ports.length == 0) {
                log("No ports found!", Warning);
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
        //region clearPackets
        JButton clearPackets = new JButton("Clear log");
        clearPackets.addActionListener(e -> {
            synchronized (logEntries) {
                logEntries.clear();
            }
            SwingUtilities.invokeLater(() -> {
                StyledDocument doc = log.getStyledDocument();
                try {
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            });
        });
        menuBar.add(clearPackets);
        //endregion
        //region logPackets
        logPackets = new JCheckBoxMenuItem("Log packets");
        logPackets.setSelected(DevConfig.defaultLoggingPackets);
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
            commandLine = new JTextField();
            commandLine.addActionListener(e -> sendCommand(commandLine.getText()));
            commandLine.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {

                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        commandShift(-1);
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        commandShift(1);
                    }
                }
            });

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
            messageStyles.put(Info, log.addStyle("Info", null));
            messageStyles.put(Warning, log.addStyle("Warning", null));
            messageStyles.put(Packet, log.addStyle("Packet", null));
            messageStyles.put(ErrorMessage, log.addStyle("ErrorMessage", null));
            messageStyles.put(OutMessage, log.addStyle("OutMessage", null));
            StyleConstants.setForeground(messageStyles.get(Info), DevConfig.infoColor);
            StyleConstants.setForeground(messageStyles.get(Warning), Color.orange);
            StyleConstants.setForeground(messageStyles.get(Packet), Color.white);
            StyleConstants.setForeground(messageStyles.get(ErrorMessage), Color.red);
            StyleConstants.setForeground(messageStyles.get(OutMessage), Color.white);
            Handler.getScheduler().scheduleAtFixedRate(this::refreshLog, 8, 16, TimeUnit.MILLISECONDS);
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
            baudrate.addActionListener(event -> {
                String rate = baudrate.getText();
                Thread task = new Thread(() -> {
                    boolean res;
                    try {
                        res = Handler.timeout(() -> connected.setBaudRate(Integer.parseInt(rate)), 1000);
                    } catch (NumberFormatException e) {
                        Audio.playSound(Sound.stopPls);
                        log("Couldn't change baudrate: is this a number?", Warning);
                        return;
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof NumberFormatException) {
                            Audio.playSound(Sound.stopPls);
                            log("Couldn't change baudrate: is this a number?", Warning);
                        } else {
                            log("Couldn't change baudrate", ErrorMessage);
                        }

                        return;
                    } catch (TimeoutException e) {
                        log("Timed out changing baudrate", ErrorMessage);
                        return;
                    }
                    if (res) {
                        log("Changed baudrate to " + rate, Info);
                        baudrateDefault = Integer.parseInt(rate);
                    } else {
                        log("Baudrate " + rate + " not allowed on this system", Warning);
                    }
                });
                task.start();
                baudrate.transferFocusBackward();
            });
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
            plotGroup.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    rearrangePlotGroup();
                }
            });
            rightPanelLayout.putConstraint(TOP, plotGroup, 0, BOTTOM, baudrate);
            rightPanelLayout.putConstraint(BOTTOM, plotGroup, 0, BOTTOM, rightPanel);
            rightPanelLayout.putConstraint(RIGHT, plotGroup, 0, RIGHT, rightPanel);
            rightPanelLayout.putConstraint(LEFT, plotGroup, 0, LEFT, rightPanel);
            rightPanel.add(plotGroup);
            //endregion
            rightPanel.setVisible(false);
        }
        rightPanel.setOpaque(false);
        plotGroup.setOpaque(false);
        contentPane.add(rightPanel);
        contentPaneLayout.putConstraint(RIGHT, rightPanel, 0, RIGHT, contentPane);
        contentPaneLayout.putConstraint(TOP, rightPanel, 0, TOP, contentPane);
        contentPaneLayout.putConstraint(BOTTOM, rightPanel, 0, BOTTOM, contentPane);
        contentPaneLayout.putConstraint(LEFT, rightPanel, 0, RIGHT, leftPanel);
        //endregion
        new ComponentResizer(new Insets(0, 0, 0, 5), leftPanel, rightPanel);
    }

    public void log(String message, LogEntry.Type type) { //TODO: colours in log here would be good
        logger.info(message);
        message += "\n";
        synchronized (newLogEntries) {
            newLogEntries.add(new LogEntry(message, type));
        }
    }

    private void refreshLog() {
        boolean loggingPackets = logPackets.isSelected();
        ArrayList<LogEntry> newMessages;
        synchronized (newLogEntries) {
            newMessages = new ArrayList<>(newLogEntries);
            newLogEntries.clear();
        }
        synchronized (logEntries) {
            logEntries.addAll(newMessages);
            while (logEntries.size() > DevConfig.maxLogSize) {
                logEntries.remove(0);
            }
        }
        if (loggingPackets == lastLoggingPackets) {
            SwingUtilities.invokeLater(() -> {
                StyledDocument doc = log.getStyledDocument();
                for (LogEntry message : newMessages) {
                    if ((!loggingPackets) && (message.type == Packet)) {
                        continue;
                    }
                    try {
                        doc.insertString(doc.getLength(), message.string, messageStyles.get(message.type));
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e); // ?
                    }
                }
            });
        } else {
            ArrayList<LogEntry> messages;
            synchronized (logEntries) {
                messages = new ArrayList<>(logEntries);
            }
            SwingUtilities.invokeLater(() -> {
                StyledDocument doc = log.getStyledDocument();
                try {
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
                for (LogEntry message : messages) {
                    if ((!loggingPackets) && (message.type == Packet)) {
                        continue;
                    }
                    try {
                        doc.insertString(doc.getLength(), message.string, messageStyles.get(message.type));
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e); // ?
                    }
                }
            });
        }
        lastLoggingPackets = loggingPackets;
    }

    private void sendCommand(@NotNull String command) {
        if (command.isEmpty()) {
            return;
        }
        //region add command to commandLog
        commandLog.add(command);
        if (commandLog.size() > DevConfig.maxCommandLogSize) {
            commandLog.remove(0);
        }
        commandID = commandLog.size();
        //endregion
        if (connected == null) {
            log("No port connected! I think...", Warning);
            return;
        }
        if (!connected.isOpen()) {
            log("Port closed.", Warning);
            return;
        }
        byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
        Thread task = new Thread(() -> {
            int result = connected.writeBytes(bytes, bytes.length);
            log("Sent \"" + command + "\" as " + result + " byte" + (result == 1 ? "" : "s"), Info);
        });
        task.start();
        commandLine.setText("");
        Audio.playSound(Sound.pewPew);
        //commandLine.enter(); // I think this is redundant? each textBox has a leaveOnSubmit property now...
    }

    private void commandShift(int n) {
        commandID = Math.max(Math.min(commandID + n, commandLog.size()), 0);
        if (commandID == commandLog.size()) {
            commandLine.setText("");
        } else {
            commandLine.setText(commandLog.get(commandID));
        }
        commandLine.setCaretPosition(commandLine.getText().length());
    }

    private void connectToPort(SerialPort port) {
        if (connecting) {
            log("Already connecting!", Warning);
            return;
        }
        //region try to open and attach this to port
        connecting = true;
        Thread connectTask = new Thread(() -> {
            if (connected != null) {
                try {
                    Handler.timeout(() -> closePort(connected), 1000);
                } catch (ExecutionException e) {
                    log("Exception closing " + connected.getDescriptivePortName(), ErrorMessage);
                } catch (TimeoutException e) {
                    log("Timed out closing " + connected.getDescriptivePortName(), ErrorMessage);
                }
            }
            connected = null;
            try {
                port.flushIOBuffers();
                boolean opening = false;
                boolean listening = false;
                try {
                    opening = Handler.timeout(port::openPort, 1000);
                } catch (TimeoutException e) {
                    log("Timed out opening port " + port.getDescriptivePortName(), ErrorMessage);
                } catch (ExecutionException e) {
                    log("Exception opening port " + port.getDescriptivePortName() + ": " + e.getMessage(), ErrorMessage);
                }
                if (!opening) {
                    log("Failed to open port " + port.getDescriptivePortName(), ErrorMessage);
                    return;
                } else {
                    log("Opened " + port.getDescriptivePortName(), Info);
                }
                try {
                    listening = Handler.timeout(() -> {
                        port.flushDataListener();
                        port.flushIOBuffers();
                        port.removeDataListener();
                        return port.addDataListener(this);
                    }, 1000);
                } catch (TimeoutException e) {
                    log("Timed out attaching listener to port " + port.getDescriptivePortName(), ErrorMessage);
                } catch (ExecutionException e) {
                    log("Exception attaching listener to port " + port.getDescriptivePortName() + ": "
                            + e.getMessage(), ErrorMessage);
                }
                if (!listening) {
                    log("Failed to attach listener to port " + port.getDescriptivePortName(), ErrorMessage);
                    return;
                } else {
                    log("Listening to  " + port.getDescriptivePortName(), Info);
                }
                connected = port;
            } finally {
                connecting = false;
            }
            boolean res;
            try {
                res = Handler.timeout(() -> connected.setBaudRate(baudrateDefault), 1000);
            } catch (ExecutionException e) {
                log("Couldn't change baudrate", ErrorMessage);
                return;
            } catch (TimeoutException e) {
                log("Timed out changing baudrate", ErrorMessage);
                return;
            }
            if (res) {
                log("Set baudrate to the default " + baudrateDefault, Info);
            } else {
                log("Baudrate " + baudrateDefault + " not allowed on this system", Warning);
            }
        });
        connectTask.start();
        //endregion
        //region set up right panel
        SwingUtilities.invokeLater(() -> {
            baudrate.setText(String.valueOf(baudrateDefault));
            portTitle.setText(port.getDescriptivePortName());
            rightPanel.setVisible(true);
        });
        //endregion
    }

    private boolean closePort(SerialPort port) {
        plots.clear();
        if (port == null) {
            log("No port connected!", Warning);
            return false;
        }
        port.removeDataListener();
        log("Stopped listening to " + port.getDescriptivePortName(), Info);
        boolean res = port.closePort();
        if (res) {
            log("Closed " + port.getDescriptivePortName(), Info);
        } else {
            log("Failed to close " + port.getDescriptivePortName(), ErrorMessage);
        }
        SwingUtilities.invokeLater(this::clearRightPanel);
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
                            if (message.length() > DevConfig.maxBetweenMessageLength) {
                                log(message, OutMessage);
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
                                        if (!leftover.isEmpty()) {
                                            log(leftover, OutMessage);
                                            leftover = "";
                                        }
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
                            log(outsideOfPacket, OutMessage);
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
                                    log("Leftovers in packet: " + packet, Warning);
                                }
                                break;
                            }
                            String plotName = packet.substring(0, packet.indexOf("(")); // is "" an ok plot name? prolly.
                            Plot plot;
                            synchronized (plots){
                            plot = plots.get(plotName);}
                            //region create plot if absent
                            if (plot == null) {
                                synchronized (newPlots) { //TODO: pretty sure this synch is unnecessary
                                    plot = newPlots.get(plotName);
                                    if (plot == null) {
                                        plot = createNewPlot(plotName);
                                        newPlots.put(plotName, plot);
                                        plotAdded = true;
                                    }
                                }
                            }
                            //endregion
                            packet = packet.substring(packet.indexOf("("));
                            if (!packet.contains(")")) {
                                log("Unclosed (: " + plotName + packet, Warning);
                                break;
                            }
                            String plotData = packet.substring(1, packet.indexOf(")")); // key:value,key:value
                            packet = packet.substring(packet.indexOf(")") + 1);
                            if (logPackets.isSelected()) {
                                log(plotName + "(" + plotData + ")", Packet);
                            }
                            plotData += ",";
                            //region read plot data
                            while (plotData.contains(",")) {
                                //region read pair
                                String pair = plotData.substring(0, plotData.indexOf(",")); //a:n
                                plotData = plotData.substring(plotData.indexOf(",") + 1);
                                if (!pair.contains(":")) {
                                    log("Malformed key-value pair: " + pair, Warning);
                                    continue; //discard this pair
                                }
                                String key = pair.substring(0, pair.indexOf(":"));
                                String valueString = pair.substring(pair.indexOf(":") + 1);
                                float value;
                                try {
                                    value = Float.parseFloat(valueString);
                                } catch (NumberFormatException e) {
                                    log("Number format exception in packet: " + valueString, Warning);
                                    continue; //discard this pair
                                }
                                //endregion
                                plot.addValue(key, value);
                            }
                            if (!plotData.isEmpty()) {
                                log("Leftovers in plot data: " + plotData, Warning);
                            }
                            //endregion
                        }
                        //endregion
                    }
                    //endregion
                    if (plotAdded) {
                        plots.putAll(newPlots);
                        newPlots.clear();
                    }
                    for (Plot p : plots.values()) {
                        p.repaint();
                    }
                    break;
                // endregion
                case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                    clearRightPanel();
                    log("Port " + connected.getDescriptivePortName() + " disconnected.", Info);
                    closePort(connected); //TODO: really not sure ab this
                    break;
                default:
                    log("Unexpected event type: " + event.getEventType(), ErrorMessage);
            }
        } catch (Exception e) {
            logger.info(e.getClass() + e.getMessage());
        }
    }

    @NotNull
    private Plot createNewPlot(String plotName) {
        JLabel plotTitle = new JLabel(plotName);
        JButton removeButton = new JButton(Resources.crossIcon);
        JTextField range = new JTextField(String.valueOf(DevConfig.defaultRange));
        JPanel legend = new JPanel();
        JScrollPane legendScroll = new JScrollPane(legend);
        legend.setLayout(new GridLayout(0, 1));
        Plot plot = new Plot(plotTitle, plotName, legend, legendScroll);
        plot.setBackground(new Color(60, 63, 65));
        range.addActionListener(e -> {
            try {
                int value = Integer.parseInt(e.getActionCommand());
                if (value < 1) {
                    throw new NumberFormatException("Nope");
                }
                plot.rangeN = value;
                log("Changed range of plot \"" + plot.plotName + "\" to " + value, Info);
            } catch (NumberFormatException ex) {
                log("Please enter a number for the plot range", Warning);
                Audio.playSound(Sound.stopPls);
            }

        });
        JPanel plotPanel = new JPanel();
        removeButton.addActionListener(e->{
            synchronized (plots){
                plots.remove(plot.plotName);
            }
            SwingUtilities.invokeLater(()->{
                this.requestFocus();
                plotGroup.remove(plotPanel);
                rearrangePlotGroup();
            });
        });
        SpringLayout plotLayout = new SpringLayout();
        plotPanel.setLayout(plotLayout);
        plotPanel.add(removeButton);
        plotPanel.add(plotTitle);
        plotPanel.add(range);
        plotPanel.add(legendScroll);
        plotPanel.add(plot);
        plotLayout.putConstraint(TOP, plotTitle, 0, TOP, plotPanel);
        plotLayout.putConstraint(TOP, range, 0, TOP, plotPanel);
        plotLayout.putConstraint(TOP, legendScroll, 0, TOP, plotPanel);
        plotLayout.putConstraint(TOP, removeButton, 0, TOP, plotPanel);
        plotLayout.putConstraint(LEFT, removeButton, 0, LEFT, plotPanel);
        plotLayout.putConstraint(LEFT, plotTitle, 0, RIGHT, removeButton);
        plotLayout.putConstraint(RIGHT, plotTitle, 0, LEFT, range);
        plotLayout.putConstraint(RIGHT, range, 0, LEFT, legendScroll);
        plotLayout.putConstraint(RIGHT, legendScroll, 0, RIGHT, plotPanel);
        plotLayout.putConstraint(BOTTOM, plotTitle, 0, BOTTOM, range);
        plotLayout.putConstraint(TOP, plot, 0, BOTTOM, plotTitle);
        plotLayout.putConstraint(RIGHT, plot, 0, RIGHT, plotPanel);
        plotLayout.putConstraint(LEFT, plot, 0, LEFT, plotPanel);
        plotLayout.putConstraint(BOTTOM, plot, 0, BOTTOM, plotPanel);
        SwingUtilities.invokeLater(() -> {
            plotGroup.add(plotPanel);
            rearrangePlotGroup();
            removeButton.setIcon(Resources.crossIcon.derive(plotTitle.getHeight(),plotTitle.getHeight()));
        });
        return plot;
    }

    private void rearrangePlotGroup() {
        int xn = 1;
        int yn = 1;
        int width = plotGroup.getWidth();
        int height = plotGroup.getHeight();
        while (xn * yn < plots.size() + newPlots.size()) {
            double xratio = ((double) width / (xn + 1)) / ((double) height / yn);
            double yratio = ((double) width / xn) / ((double) height / (yn + 1));
            if (Math.abs(Math.log(xratio / DevConfig.optimalRatio)) < Math.abs(Math.log(yratio / DevConfig.optimalRatio))) {
                xn++;
            } else {
                yn++;
            }
        }
        GridLayout plotGroupLayout = new GridLayout(yn, xn);
        plotGroup.setLayout(plotGroupLayout);
        plotGroup.revalidate();
    }

    private void clearRightPanel() {
        portTitle.setText("");
        plotGroup.removeAll();
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
                SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }
}