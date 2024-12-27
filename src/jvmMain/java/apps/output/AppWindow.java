package apps.output;


import apps.Handler;
import apps.Resources;
import apps.input.InputControl;
import apps.util.DevConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;

public class AppWindow extends JFrame {
    BufferStrategy strategy;
    private int width = DevConfig.DWIDTH;
    private int height = DevConfig.DHEIGHT;
    private int newwidth = width;
    private int newheight = height;

    public AppWindow() {
        setIgnoreRepaint(true);
        System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
        setResizable(true);
        setSize(width, height);
        this.setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //region Add canvas(for buffer strategy I think?? might be unnecessary)
        Canvas canvas = new Canvas();
        //region set font
        try {
            canvas.setFont(Font.createFont(Font.TRUETYPE_FONT, Resources.comfortaa)
                    .deriveFont(Font.BOLD, DevConfig.fontSize));
        } catch (FontFormatException | IOException e) {
            canvas.setFont(canvas.getFont().deriveFont(Font.BOLD, DevConfig.fontSize));
        }
        //endregion
        canvas.setIgnoreRepaint(true);
        canvas.setSize(width, height);
        getContentPane().add(canvas);
        pack();
        //endregion
        //region buffer strategy
        canvas.createBufferStrategy(2);
        strategy = canvas.getBufferStrategy();
        //endregion
        //region hook up apps.input
        InputControl input = new InputControl();
        addKeyListener(input);
        canvas.addKeyListener(input);
        addMouseListener(input);
        canvas.addMouseListener(input);
        addMouseMotionListener(input);
        canvas.addMouseMotionListener(input);
        //endregion
        this.getRootPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                newwidth = e.getComponent().getWidth();
                newheight = e.getComponent().getHeight(); //does this give the right size?
            }
        });
        setVisible(true);
    }

    public void showCanvas() {
        if (!strategy.contentsLost()) {
            strategy.show();
        }
    }

    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public BufferStrategy getCanvas() {
        width = newwidth;
        height = newheight;

        return strategy;
    }
}