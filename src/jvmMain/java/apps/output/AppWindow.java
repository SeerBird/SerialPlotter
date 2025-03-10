package apps.output;


import apps.Handler;
import apps.Resources;
import apps.input.InputControl;
import apps.ui.Menu;
import apps.util.DevConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class AppWindow extends JFrame {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private int width = DevConfig.DWIDTH;
    private int height = DevConfig.DHEIGHT;
    private int newwidth = width;
    private int newheight = height;
    private final Canvas canvas;

    public AppWindow() {
        setIgnoreRepaint(true);
        setResizable(true);
        setSize(width, height);
        this.setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //region Add canvas(for buffer strategy I think?? might be unnecessary)
        canvas = new MyCanvas();
        canvas.setBackground(DevConfig.BACKGROUND);
        //region set font
        try {
            canvas.setFont(Font.createFont(Font.TRUETYPE_FONT, Resources.comfortaa)
                    .deriveFont(Font.BOLD, DevConfig.fontSize));
        } catch (FontFormatException | IOException e) {
            canvas.setFont(canvas.getFont().deriveFont(Font.BOLD, DevConfig.fontSize));
        }catch (Exception e){
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
        //endregion
        canvas.setSize(width, height);
        getContentPane().add(canvas);
        pack();
        //endregion
        //region hook up apps.input
        InputControl input = new InputControl();
        addKeyListener(input);
        canvas.addKeyListener(input);
        addMouseListener(input);
        canvas.addMouseListener(input);
        addMouseMotionListener(input);
        canvas.addMouseMotionListener(input);
        addMouseWheelListener(input);
        canvas.addMouseWheelListener(input);
        addWindowListener(input);
        //endregion
        this.getRootPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                width = e.getComponent().getWidth();
                height = e.getComponent().getHeight(); //does this give the right size?
                Menu.queueUpdate();
                canvas.repaint();
            }
        });
        setVisible(true);
    }
    public static void start(){

    }
    public void repaintCanvas(int x,int y,int width, int height){
        canvas.repaint(x,y,width,height);
    }
    public void repaintCanvas(){
        canvas.repaint();
    }

    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public int stringLength(String string) {
        if(canvas!=null){
            return canvas.getFontMetrics(canvas.getFont()).stringWidth(string);
        }
        return 0;
    }
}