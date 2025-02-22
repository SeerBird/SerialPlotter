package apps.ui.rectangles;


import apps.Handler;
import apps.output.Renderer;
import apps.output.animations.Amogus;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class PlotContainer extends RectElement {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    ArrayList<PortPlotGroup> portPlotGroups = new ArrayList<>();
    PortPlotGroup pressed;
    ArrayList<Amogus> amogi;

    public PlotContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
        amogi = new ArrayList<>();
    }

    //region Input
    @Override
    public void release() {
        if (pressed == null) {
            return;
        }
        pressed.release();
        pressed = null;
    }

    @Override
    public boolean press(double x, double y) {
        for (PortPlotGroup portPlotGroup : portPlotGroups) {
            if (portPlotGroup.press(x, y)) {
                pressed = portPlotGroup;
                return true;
            }
        }
        return false;
    }
    //endregion

    public void addPortPlotGroup(SerialPort port) { // turns out there will only be one. who needs to change code, right?
        try {
            if (portPlotGroups.size() == 1) {
                portPlotGroups.get(0).close();
            }
            portPlotGroups.clear();
            try {
                portPlotGroups.add(new PortPlotGroup(x, y, width, height, port));
            } catch (TimeoutException e) {
                Menu.log(e.getMessage());
            } catch(ExecutionException e){
                Menu.log(e.getMessage());
            } catch(PortPlotGroup.DataListenerAttachException e){
                Menu.log(e.getMessage());
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            Menu.log(e.getMessage());
        }
        Menu.update();
        Handler.repaint(x, y, width, height);
    }

    public void removePortPlotGroup(PortPlotGroup plot) {
        portPlotGroups.remove(plot);
        removeAmogi();
        Handler.repaint(x, y, width, height);
    }

    public ArrayList<PortPlotGroup> getPortPlotGroups() {
        return portPlotGroups;
    }

    public PortPlotGroup getPressed() {
        return pressed;
    }

    public void update() {
        //try to make the tiling match the width and height we have?
        if (portPlotGroups.isEmpty()) {
            return;
        }
        PortPlotGroup port = portPlotGroups.get(0);
        Button close = port.closeButton;
        close.width = DevConfig.fontSize + DevConfig.vertMargin * 2;
        close.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        close.x = x;
        close.y = y;
        Label title = port.title;
        title.x = x + close.width;
        title.y = y;
        title.width = Math.min(width * 2 / 3, Handler.stringLength(title.text) + 2 * DevConfig.labelHorMargin);
        title.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        Textbox baudrate = port.baudrate;
        baudrate.x = x + close.width + title.width;
        baudrate.y = y;
        baudrate.width = width - close.width - title.width;
        baudrate.height = DevConfig.fontSize + DevConfig.vertMargin * 2;
        port.refresh();
        ArrayList<Plot> plots = new ArrayList<>(port.plots.values());
        plots.sort(Comparator.comparing(o -> o.title.text)); // make sure the order is consistent
        //region get best plot layout
        int remainingHeight = height - title.height;
        int xn = 1;
        int yn = 1;
        while (xn * yn < plots.size()) {
            double xratio = (double) (width / (xn + 1)) / ((double) remainingHeight / yn);
            double yratio = (double) (width / xn) / ((double) remainingHeight / (yn + 1));
            if (Math.abs(Math.log(xratio / DevConfig.optimalRatio)) < Math.abs(Math.log(yratio / DevConfig.optimalRatio))) {
                xn++;
            } else {
                yn++;
            }
        }
        //endregion
        //region set the coords and heights of each plot
        int plotWidth = width / xn;
        int plotHeight = remainingHeight / yn;
        for (int n = 0; n < plots.size(); n++) {
            int i = n % xn;
            int j = n / xn;
            Plot plot = plots.get(n);
            plot.x = x + i * plotWidth;
            plot.y = y + title.height + j * plotHeight;
            plot.width = plotWidth;
            plot.height = plotHeight;
            plot.update();
        }
        //endregion
        //region amogi
        removeAmogi();
        if (Handler.getBullshitOn()) {
            if (xn > 1 && yn > 1) {
                for (int n = plots.size(); n < xn * yn; n++) {
                    int i = n % xn;
                    int j = n / xn;
                    Amogus amogus = new Amogus(x + i * plotWidth, y + title.height + j * plotHeight, plotWidth, plotHeight);
                    amogi.add(amogus);
                    Renderer.addAnimation(amogus);
                }
            }
        }
        //endregion
        Handler.repaint(x, y, width, height);
    }

    private void removeAmogi() {
        for (Amogus amogus : amogi) {
            Renderer.removeAnimation(amogus);
        }
        amogi.clear();
    }
}
