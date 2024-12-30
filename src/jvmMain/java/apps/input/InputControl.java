package apps.input;

import apps.Handler;
import apps.ProgramState;
import apps.ui.Menu;
import apps.ui.rectangles.Button;
import apps.ui.rectangles.Textbox;
import apps.util.DevConfig;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.NotNull;

import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import static apps.input.InputControl.Mousebutton.*;
import static java.awt.event.KeyEvent.*;

public class InputControl extends MouseAdapter implements KeyListener {
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
                        textbox.setText(textbox.text.substring(0, textbox.text.length() - 1));
                    }
                } else if (textbox.text.length() < DevConfig.maxNameLength) {
                    textbox.setText(textbox.text + getText(e));
                    if (textbox.text.length() > DevConfig.maxNameLength) {
                        textbox.setText(textbox.text.substring(0, DevConfig.maxNameLength - 1));
                    }
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
        if (key == VK_M) {
            if (Menu.getFocused() == null) {
                Handler.setSounds(!Handler.getSoundOn());
                Menu.log("Sound "+(Handler.getSoundOn()?"on":"off"));
            }
        }
    }

    //region (not used)
    @Override
    public void keyTyped(KeyEvent e) {

    }
    //endregion
    //endregion

    enum Mousebutton {
        Left,
        Right,
        Middle
    }

    public static final ArrayRealVector mousepos = new ArrayRealVector(new Double[]{0.0, 0.0});
    static boolean Shift = false;
    static String text = "";

    @NotNull
    private static String getText(@NotNull KeyEvent e) {
        StringBuilder textBuilder = new StringBuilder();
        int key = e.getKeyCode();
        if (key >= 0x2C && key <= 0x69) {
            textBuilder.append(e.getKeyChar());
        }
        String text = textBuilder.toString();
        if (Shift) {
            text = text.toLowerCase();
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
