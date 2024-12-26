package apps.input;

import apps.Handler;
import apps.ProgramState;
import apps.ui.Menu;
import apps.ui.rectangles.Textbox;
import apps.util.DevConfig;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.NotNull;

import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import static apps.input.InputControl.Mousebutton.Left;
import static apps.input.InputControl.Mousebutton.Right;
import static java.awt.event.KeyEvent.*;

public class InputControl extends MouseAdapter implements KeyListener {
    //region Events
    private static final Map<Integer, KeyEvent> keyPressEvents = new HashMap<>();
    private static final Map<Integer, KeyEvent> keyReleaseEvents = new HashMap<>();
    private static final Map<Mousebutton, MouseEvent> mousePressEvents = new HashMap<>();
    private static final Map<Mousebutton, MouseEvent> mouseReleaseEvents = new HashMap<>();
    private static int scroll = 0;

    //endregion
    //region MouseListener methods
    @Override
    public void mousePressed(MouseEvent e) {
        mousePressEvents.put(getButton(e.getButton()), e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseReleaseEvents.put(getButton(e.getButton()), e);
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent e) {
        mousepos.setEntry(0, e.getPoint().x);
        mousepos.setEntry(1, e.getPoint().y);
    }

    @Override
    public void mouseDragged(@NotNull MouseEvent e) {
        mousepos.setEntry(0, e.getPoint().x);
        mousepos.setEntry(1, e.getPoint().y);
    }

    @Override
    public void mouseWheelMoved(@NotNull MouseWheelEvent e) {
        scroll += e.getScrollAmount();
    }

    //endregion
    //region KeyListener methods
    @Override
    public void keyPressed(@NotNull KeyEvent e) {
        keyPressEvents.put(e.getKeyCode(), e);
    }

    @Override
    public void keyReleased(@NotNull KeyEvent e) {
        keyReleaseEvents.put(e.getKeyCode(), e);
    }

    //region (not used)
    @Override
    public void keyTyped(KeyEvent e) {

    }
    //endregion
    //endregion

    enum Mousebutton {
        Left,
        Right
    }

    public static final ArrayRealVector mousepos = new ArrayRealVector(new Double[]{0.0, 0.0});

    static {
        for (int i = 0; i <= 0xE3; i++) {
            keyPressEvents.put(i, null);
            keyReleaseEvents.put(i, null);
        }
    }

    static String text = "";

    public static void handleInput() {
        InputInfo input = Handler.getInput();
        ProgramState state = Handler.getState();
        //region Always
        if (Menu.getFocused() != null) {
            if (Menu.getFocused() instanceof Textbox textbox) {
                //region unfocus if pressed escape or clicked LMB outside of the textbox
                if (pressed(VK_ESCAPE)) {
                    Menu.unfocus();
                    dispatchText();
                    dispatch(Left);
                    dispatch(VK_ESCAPE);
                } else if (released(Left) && Menu.getPressed() != textbox) {
                    Menu.unfocus();
                    dispatchText();
                    dispatch(Left);
                }
                //endregion
                //region use value and unfocus if pressed enter
                else if (pressed(VK_ENTER)) {
                    textbox.useValue();
                    Menu.unfocus();
                    dispatchText();
                }
                //endregion
                //region otherwise append and remove text according to the pressed keys
                else if (pressed(VK_BACK_SPACE)) {
                    if (!textbox.text.isEmpty()) {
                        textbox.setText(textbox.text.substring(0, textbox.text.length() - 1));
                    }
                    unpress(VK_BACK_SPACE);
                } else if (textbox.text.length() < DevConfig.maxNameLength) {
                    textbox.setText(textbox.text + getText());
                    if (textbox.text.length() > DevConfig.maxNameLength) {
                        textbox.setText(textbox.text.substring(0, DevConfig.maxNameLength - 1));
                    }
                }
                //endregion
            }
        }
        if (released(VK_ESCAPE)) {
            input.escape = true;
            dispatch(VK_ESCAPE);
        }
        /*
        if (pressed(VK_SPACE)) {
            Handler.debug = true;
            unpress(KeyEvent.VK_SPACE);
        }
        if (released(VK_SPACE)) {
            Handler.debug = false;
            unrelease(KeyEvent.VK_SPACE);
        }*/
        //endregion
        //region Menu
        if (pressed(Left)) {
            if (Menu.press(mousepos)) {
                unpress(Left);
            }
        }
        if (released(Left)) {
            if (Menu.release()) {
                dispatch(Left);
            }
        }
        //endregion
        //region Play
        /*
        if (state == playClient || state == playServer) {
            if (pressed(VK_SPACE)) {
                input.drag();
            }
            if (released(VK_SPACE)) {
                dispatch(VK_SPACE);
            }
            if (pressed(Left)) {
                input.webFling();
                dispatch(Left);
            }
            if (pressed(Right)) {
                input.detachWeb();
                dispatch(Right);
            }
            if (pressed(VK_C)) {
                input.create();
                dispatch(KeyEvent.VK_C);
            }
            if (pressed(VK_S)) {
                Menu.showScores();
                Menu.refreshScores();
            } else {
                Menu.hideScores();
            }
            if (released(VK_S)) {
                dispatch(VK_S);
            }
        } else {
            dispatch(Left);
        }
        */
        //endregion
    }


    private static void dispatchText() {
        for (int key = 0x2C; key < 0x69 + 1; key++) {
            dispatch(key);
        }
        dispatch(VK_ENTER);
        dispatch(VK_SHIFT);
        text = "";
    }

    @NotNull
    private static String getText() {
        StringBuilder textBuilder = new StringBuilder();
        for (int key = 0x2C; key < 0x69 + 1; key++) {
            if (pressed(key)) {
                textBuilder.append(keyPressEvents.get(key).getKeyChar());
                unpress(key);
            }
        }
        if (released(VK_SHIFT)) {
            dispatch(VK_SHIFT);
        }
        String text = textBuilder.toString();
        if (!pressed(VK_SHIFT)) {
            text = text.toLowerCase();
        }
        text = text.replaceAll("\\p{C}", "");
        return text;
    }

    private static Mousebutton getButton(int button) {
        if (button == MouseEvent.BUTTON1) {
            return Left;
        } else {
            return Right;
        }
    }

    //region Private key/mousebutton getters and setters
    private static boolean pressed(int key) {
        return keyPressEvents.get(key) != null;
    }

    private static boolean released(int key) {
        return keyReleaseEvents.get(key) != null;
    }

    private static void unrelease(int key) {
        keyReleaseEvents.put(key, null);
    }

    private static void unpress(int key) {
        keyPressEvents.put(key, null);
    }

    private static void dispatch(int key) {
        keyReleaseEvents.put(key, null);
        keyPressEvents.put(key, null);
    }

    private static boolean pressed(Mousebutton button) {
        return mousePressEvents.get(button) != null;
    }

    private static boolean released(Mousebutton button) {
        return mouseReleaseEvents.get(button) != null;
    }

    private static void unrelease(Mousebutton button) {
        mouseReleaseEvents.put(button, null);
    }

    private static void unpress(Mousebutton button) {
        mousePressEvents.put(button, null);
    }

    private static void dispatch(Mousebutton button) {
        mouseReleaseEvents.put(button, null);
        mousePressEvents.put(button, null);
    }
    //endregion
}
