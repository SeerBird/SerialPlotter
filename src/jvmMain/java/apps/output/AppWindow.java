package apps.output;


import apps.Resources;
import apps.input.InputControl;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.ui.FlatTitlePane;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.logging.Logger;

public class AppWindow extends JFrame {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //region rename direction afterwards but I need this or my brain will break
    private static final String RIGHT = SpringLayout.EAST;
    private static final String LEFT = SpringLayout.WEST;
    private static final String TOP = SpringLayout.NORTH;
    private static final String BOTTOM = SpringLayout.SOUTH;
    //endregion
    //region default dimensions
    public static final int WIDTH = 1140;
    public static final int HEIGHT = 600;
    public static final int leftWidth = 300;
    public static final int commandLineHeight = 30;
    //endregion
    //region variables
    JTextPane log;
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
        JMenuBar menuBar = new JMenuBar();
        //region leftPanel
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(leftWidth,HEIGHT));
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
            log = new JTextPane();
            JScrollPane logScroll = new JScrollPane(log);
            leftPanel.add(logScroll);
            {
                logScroll.setMinimumSize(null);
                logScroll.setMaximumSize(null);
                addColoredText(log,"bababoi\n",Color.BLUE);

                //logScroll.setMinimumSize(new Dimension(100,200));
            }
            //logScroll.setBounds(0,0,leftWidth,HEIGHT);
            leftPanelLayout.putConstraint(LEFT, logScroll, 0, LEFT, leftPanel);
            leftPanelLayout.putConstraint(BOTTOM, logScroll, 0, TOP, commandLine);
            leftPanelLayout.putConstraint(TOP, logScroll, 0, TOP, leftPanel);
            leftPanelLayout.putConstraint(RIGHT, logScroll, 0, RIGHT, leftPanel);
            //endregion
            new ComponentResizer(new Insets(0, 0, 0, 5), leftPanel);
        }
        contentPaneLayout.putConstraint(LEFT, leftPanel, 0, LEFT, contentPane);
        contentPaneLayout.putConstraint(BOTTOM, leftPanel, 0, BOTTOM, contentPane);
        contentPaneLayout.putConstraint(TOP, leftPanel, 0, TOP, contentPane);
        //endregion
    }
    public void addColoredText(JTextPane pane, String text, Color color) {
        StyledDocument doc = pane.getStyledDocument();

        Style style = pane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        }
        catch (BadLocationException e) {
            logger.info(e.getMessage());
        }
    }

    public int stringLength(String string) {
        return this.getFontMetrics(this.getFont()).stringWidth(string);
    }
}