package apps.input;

import apps.Handler;
import apps.ui.Focusable;
import apps.ui.Menu;
import apps.ui.rectangles.Textbox;
import apps.util.DevConfig;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;

import static apps.input.InputControl.Mousebutton.*;
import static java.awt.event.KeyEvent.*;

public class InputControl extends MouseAdapter implements KeyListener, WindowListener {
    //region Events
    private static int scroll = 0;

    //endregion
    //region MouseListener methods
    @Override
    public void mousePressed(MouseEvent e) {
        Mousebutton button = getButton(e.getButton());
        if (button == Left) {
            Menu.press(mousepos);
        } else if (button == Middle) {
            Menu.middleDown((int) mousepos.getEntry(1));
        }
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        Mousebutton button = getButton(e.getButton());
        if (Menu.getFocused() != null) {
            if (Menu.getFocused() instanceof Textbox textbox) {
                if (button == Left) {
                    if (Menu.getPressed() == textbox) {
                        return;
                    }
                    Menu.unfocus();
                }
            }
        }
        if (button == Left) {
            Menu.release();
        } else if (button == Middle) {
            Menu.middleUp();
        }
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent e) {
        mousepos.setEntry(0, e.getPoint().x);
        mousepos.setEntry(1, e.getPoint().y);
        Menu.hover(mousepos);
    }

    @Override
    public void mouseDragged(@NotNull MouseEvent e) {
        mousepos.setEntry(0, e.getPoint().x);
        mousepos.setEntry(1, e.getPoint().y);
        Menu.hover(mousepos);
    }

    @Override
    public void mouseWheelMoved(@NotNull MouseWheelEvent e) {
        Menu.scroll(e.getWheelRotation());
    }

    //endregion
    //region KeyListener methods
    @Override
    public void keyPressed(@NotNull KeyEvent e) {
        int key = e.getKeyCode();
        if (key == VK_SHIFT) {
            Shift = true;
            return;
        }
        if (key == VK_CONTROL) {
            Control = true;
            return;
        }
        if (Menu.getFocused() != null) {
            //region textbox
            if (Menu.getFocused() instanceof Textbox textbox) {
                if (key == VK_ESCAPE) {
                    Menu.unfocus();
                } else if (key == VK_ENTER) {
                    textbox.useValue();
                    if (textbox.leaveOnSubmit) {
                        Menu.unfocus();
                    } else {
                        textbox.resetText();
                    }
                    text = "";
                } else if (key == VK_BACK_SPACE) {
                    if (!textbox.text.isEmpty()) {
                        textbox.backspace();
                    }
                } else if (Control && key == VK_V) {
                    try {
                        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                        if (clip.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                            textbox.insertText((String) clip.getData(DataFlavor.stringFlavor));
                        }
                    } catch (UnsupportedFlavorException | IOException ignore) {
                    } // maybe think ab IOException?
                } else if (key == VK_LEFT) {
                    textbox.shift(-1);
                } else if (key == VK_RIGHT) {
                    textbox.shift(1);
                } else if (textbox.text.length() < DevConfig.maxTextboxLength) {
                    textbox.insertText(getText(e));
                }
                Handler.repaint(textbox.x, textbox.y, textbox.width, textbox.height);
            }
            //endregion
        }
    }

    @Override
    public void keyReleased(@NotNull KeyEvent e) {
        int key = e.getKeyCode();
        if (key == VK_SHIFT) {
            Shift = false;
            return;
        }
        if (key == VK_CONTROL) {
            Control = false;
            return;
        }
        if (key == VK_M) {
            if (Menu.getFocused() == null) {
                Handler.setSounds(!Handler.getBullshitOn());
                Menu.log("Sound " + (Handler.getBullshitOn() ? "on" : "off"));
            }
        }
    }


    //endregion
    //region Window events
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {
        if (lastFocused != null) {
            lastFocused.enter();
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        lastFocused = Menu.getFocused();
        Menu.unfocus();
    }
    //endregion

    enum Mousebutton {
        Left,
        Right,
        Middle
    }

    public static final ArrayRealVector mousepos = new ArrayRealVector(new Double[]{0.0, 0.0});
    static boolean Shift = false;
    static boolean Control = false;
    static Focusable lastFocused;
    static String text = "";
    static Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();

    @NotNull
    private static String getText(@NotNull KeyEvent e) {
        StringBuilder textBuilder = new StringBuilder();
        int key = e.getKeyCode();
        if ((key >= 0x2C && key <= 0x69) || key == VK_SPACE) {
            textBuilder.append(e.getKeyChar());
        }
        String text = textBuilder.toString();
        if (Shift) {
            text = text.toUpperCase();
        }
        text = text.replaceAll("\\p{C}", "");
        return text;
    }

    private static Mousebutton getButton(int button) {
        if (button == MouseEvent.BUTTON1) {
            return Left;
        } else if (button == MouseEvent.BUTTON2) {
            return Middle;
        } else if (button == MouseEvent.BUTTON3) {
            return Right;
        } else {
            return Right;
        }
    }

    public static boolean getShift() {
        return Shift;
    }
}
