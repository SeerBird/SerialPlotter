package apps.ui.rectangles;

import apps.Handler;
import apps.input.InputControl;
import apps.ui.Scrollable;
import apps.util.DevConfig;

import java.util.ArrayList;

public class TextArea extends RectElement implements Scrollable {
    public ArrayList<String> entries;
    ArrayList<String> newEntries;
    int deltaVertShift;
    public int shiftUp;
    int deltaHorShift;
    public int shiftLeft;

    public TextArea(int x, int y, int width, int height) {
        super(x, y, width, height);
        entries = new ArrayList<>();
        newEntries = new ArrayList<>();
        shiftUp =0;
    }

    public void refresh() {
        entries.addAll(newEntries);
        while (entries.size() > DevConfig.maxLogSize) {
            entries.remove(0);
        }
        newEntries.clear();
        //region vertical shift
        shiftUp += deltaVertShift;
        deltaVertShift =0;
        if(shiftUp >entries.size()-1){
            shiftUp =entries.size()-1;
        }
        if(shiftUp <0){
            shiftUp =0;
        }
        //endregion
        //region horizontal shift
        shiftLeft+=deltaHorShift;
        deltaHorShift=0;
        if(shiftLeft<0){
            shiftLeft=0;
        }
        //endregion
    }
    public void limitHorShift(int maxShift){ // called from the render thread
        shiftLeft = Math.min(shiftLeft,maxShift);
    }

    @Override
    public boolean press(double x, double y) {
        return false;
    }

    @Override
    public void scroll(int steps) {
        if(InputControl.getShift()){
            deltaHorShift+=steps*4;
        }else{
        deltaVertShift -=steps;}
        Handler.repaint(x,y,width,height);
    }

    public void log(String string) { //called concurrently?
        if(shiftUp !=0){
            shiftUp++;
        }
        newEntries.add(string);
        Handler.repaint(x,y,width,height);
    }
}
