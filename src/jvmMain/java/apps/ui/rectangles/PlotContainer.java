package apps.ui.rectangles;


import apps.Handler;
import apps.output.Renderer;
import apps.output.animations.Amogus;
import apps.ui.Menu;
import apps.util.DevConfig;
import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.Comparator;

public class PlotContainer extends RectElement {
    ArrayList<PortPlotGroup> portPlotGroups = new ArrayList<>();
    PortPlotGroup pressed;
    ArrayList<Amogus> amogi;

    public PlotContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
        amogi = new ArrayList<>();
    }

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

    public void addPortPlotGroup(SerialPort port) { // turns out there will only be one. who needs to change code, right?
        if (portPlotGroups.size() == 1) {
            portPlotGroups.get(0).getPort().closePort();
        }
        portPlotGroups.clear();
        portPlotGroups.add(new PortPlotGroup(x, y, width, height, port)); // make them stack later
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

    public void arrange() {
        //try to make the tiling match the width and height we have?
        if (portPlotGroups.isEmpty()) {
            return;
        }
        PortPlotGroup port = portPlotGroups.get(0);
        Button close = port.close;
        close.width = 40;
        close.height = 40;
        close.x = x + width - close.width;
        close.y = y;
        port.refresh();
        ArrayList<Plot> plots = new ArrayList<>(port.plots.values());
        plots.sort(Comparator.comparing(o -> o.title.text)); // make sure the order is consistent
        //region get best plot layout
        int xn = 1;
        int yn = 1;
        while (xn * yn < plots.size()) {
            double xratio = (double) (width / (xn + 1)) / ((double) height / yn);
            double yratio = (double) (width / xn) / ((double) height / (yn + 1));
            if (Math.abs(xratio - DevConfig.optimalRatio) < Math.abs(yratio - DevConfig.optimalRatio)) {
                xn++;
            } else {
                yn++;
            }
        }
        //endregion
        //region set the coords and heights of each plot
        int plotWidth = width / xn;
        int plotHeight = height / yn;
        for (int n = 0; n < plots.size(); n++) {
            int i = n % xn;
            int j = n / xn;
            Plot plot = plots.get(n);
            plot.x = x + i * plotWidth;
            plot.y = y + j * plotHeight;
            plot.width = plotWidth;
            plot.height = plotHeight;
            plot.arrange();
        }
        //endregion
        //region amogi
        removeAmogi();
        if (xn > 1 && yn > 1) {
            for (int n = plots.size(); n < xn * yn; n++) {
                int i = n % xn;
                int j = n / xn;
                Amogus amogus = new Amogus(x + i * plotWidth, y + j * plotHeight, plotWidth, plotHeight);
                amogi.add(amogus);
                Renderer.addAnimation(amogus);
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