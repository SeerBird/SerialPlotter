package apps.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Maths {
    public static boolean areClockwise(@NotNull ArrayRealVector p1, @NotNull ArrayRealVector p2, @NotNull ArrayRealVector p3) {
        return (p3.getEntry(1) - p1.getEntry(1)) * (p2.getEntry(0) - p1.getEntry(0)) <
                (p2.getEntry(1) - p1.getEntry(1)) * (p3.getEntry(0) - p1.getEntry(0));
    }

    public static boolean intersect(@NotNull ArrayRealVector p1, @NotNull ArrayRealVector p2,
                                    @NotNull ArrayRealVector p3, @NotNull ArrayRealVector p4) {
        return (areClockwise(p1, p3, p4) ^ areClockwise(p2, p3, p4)) &&
                (areClockwise(p1, p2, p3) ^ areClockwise(p1, p2, p4));
    }

    public static ArrayRealVector reflect(ArrayRealVector point, ArrayRealVector axisPoint, @NotNull ArrayRealVector normalizedAxis) {
        if (normalizedAxis.getNorm() != 1.0) {
            normalizedAxis.mapMultiplyToSelf(1 / normalizedAxis.getNorm());
        }
        ArrayRealVector midpoint = axisPoint.combine(1, 1, normalizedAxis.mapMultiply(point.dotProduct(normalizedAxis) - axisPoint.dotProduct(normalizedAxis)));
        return midpoint.combine(2, -1, point);
    }

    public static ArrayRealVector normalize(@NotNull ArrayRealVector v) {
        return (ArrayRealVector) v.mapMultiply(1 / v.getNorm());
    }

    @NotNull
    public static ArrayRealVector randomUnitVector() {
        double theta = Math.random() * Math.PI * 2;
        double x = Math.cos(theta);
        double y = Math.sin(theta);
        return getVector(x, y);
    }

    @NotNull
    public static ArrayRealVector randomUnitVector(double circleFraction) {
        double theta = circleFraction * Math.PI * 2;
        double x = Math.cos(theta);
        double y = Math.sin(theta);
        return getVector(x, y);
    }

    public static ArrayRealVector i = new ArrayRealVector(new Double[]{1.0, 0.0});
    public static ArrayRealVector j = new ArrayRealVector(new Double[]{0.0, 1.0});

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ArrayRealVector getVector(double... coords) {
        return new ArrayRealVector(coords);
    }

    public static double round(double number, int decimals) {
        return ((double) (int) (number * Math.pow(10, decimals))) / Math.pow(10, decimals);
    }
}
