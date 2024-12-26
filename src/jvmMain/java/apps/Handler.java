package apps;

import apps.input.InputControl;
import apps.input.InputInfo;
import apps.output.AppWindow;
import apps.output.Renderer;
import apps.ui.Menu;
import apps.content.Content;
import com.fazecast.jSerialComm.*;

import java.util.*;
import java.util.logging.Logger;


public class Handler {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //region Jobs
    private static final HashMap<Job, Runnable> job = new HashMap<>();

    private enum Job {
        handleInput,
        updateMenu,
        updateContent,
        clearContent,
    }

    private static final ArrayList<Job> toRemove = new ArrayList<>();
    private static final ArrayList<Job> toAdd = new ArrayList<>();

    private static final ArrayList<Runnable> jobs = new ArrayList<>();
    //endregion
    static final AppWindow window = new AppWindow();
    private static ProgramState state;
    static final InputInfo input = new InputInfo();

    static {
        state = ProgramState.main;
        //region Define job dictionary
        job.clear();
        job.put(Job.updateContent, Content::update);
        job.put(Job.handleInput, InputControl::handleInput);
        job.put(Job.updateMenu, Menu::update);
        job.put(Job.clearContent, () -> {
            Content.clear();
            Content.update();
            removeJob(Job.clearContent);
        });
        //endregion
        //region Set starting state
        addJob(Job.handleInput);
        addJob(Job.updateMenu);
        //endregion
    }

    public static void out() {
        Renderer.drawImage(window.getCanvas());
        window.showCanvas();
    }

    public static void update() {
        //region remove and add jobs
        for (Job added : toAdd) {
            jobs.add(job.get(added));
        }
        toAdd.clear();
        for (Job removed : toRemove) {
            jobs.remove(job.get(removed));
        }
        toRemove.clear();
        //endregion
        //region get em done
        for (Runnable job : jobs) {
            job.run();
        }
        //endregion
    }

    public static SerialPort[] getPorts(){
        return SerialPort.getCommPorts();
    }

    //region Job Methods - merge some of them!
    private static void addJob(Job job) {
        toAdd.add(job);
    }

    private static void removeJob(Job job) {
        toRemove.add(job);
    }

    //endregion
    //region State traversal
    private static void setState(ProgramState state) {
        Handler.state = state;
        Menu.refreshMenuState();
    }

    public static void escape() {

    }

    //endregion
    //region Getters
    public static InputInfo getInput() {
        return input;
    }

    public static ProgramState getState() {
        return state;
    }

    public static AppWindow getWindow() {
        return window;
    }
    //endregion
}
