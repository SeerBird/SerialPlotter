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
        setSize(DevConfig.DWIDTH, DevConfig.DHEIGHT);
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
        SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
        JMenuBar menuBar = new JMenuBar();
        //region leftPanel
        JPanel leftPanel = new JPanel();
        contentPane.add(leftPanel);
        leftPanel.setBounds(0,0,leftWidth,HEIGHT);
        {
            //region commandLine
            JTextField commandLine = new JTextField();
            leftPanel.add(commandLine);
            {
                commandLine.setMinimumSize(new Dimension(0, 0));
                commandLine.setMaximumSize(null);
            }
            commandLine.setBounds(0,HEIGHT-commandLineHeight,leftWidth,commandLineHeight);
            layout.putConstraint(LEFT, commandLine, 0, LEFT, leftPanel);
            layout.putConstraint(RIGHT, commandLine, 0, RIGHT, leftPanel);
            layout.putConstraint(BOTTOM, commandLine, 0, BOTTOM, leftPanel);
            //endregion
            //region logScroll
            log = new JTextPane();
            JScrollPane logScroll = new JScrollPane(log);
            leftPanel.add(logScroll);
            {
                logScroll.setMinimumSize(null);
                logScroll.setMaximumSize(null);
                //addColoredText(log,"bababoi\n",Color.BLUE);

                //logScroll.setMinimumSize(new Dimension(100,200));
            }
            logScroll.setBounds(0,0,leftWidth,HEIGHT);
            layout.putConstraint(LEFT, logScroll, 0, LEFT, leftPanel);
            layout.putConstraint(BOTTOM, logScroll, 0, TOP, commandLine);
            layout.putConstraint(TOP, logScroll, 0, TOP, leftPanel);
            layout.putConstraint(RIGHT, logScroll, 0, RIGHT, leftPanel);
            //endregion
            new ComponentResizer(new Insets(0, 0, 0, 5), leftPanel);
        }
        layout.putConstraint(LEFT, leftPanel, 0, LEFT, contentPane);
        layout.putConstraint(BOTTOM, leftPanel, 0, BOTTOM, contentPane);
        layout.putConstraint(TOP, leftPanel, 0, TOP, contentPane);
        //endregion
        pack();

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