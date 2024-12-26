package apps.input;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.io.Serial;
import java.io.Serializable;

public class InputInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 4550;
    public ArrayRealVector mousepos = new ArrayRealVector(new Double[]{0.0, 0.0});
    public int scroll = 0;
    public boolean escape = false;

    public InputInfo() {
    }

    public void reset() {

    }
}
