package io.sclera.utils.ips;

/**
 * Simple value object holding a two-dimensional (X, Y) coordinate used by indoor positioning
 * calculations.
 */
public class Coordinate {
    private double X;
    private double Y;

    public double getX() {
        return X;
    }
    public void setX(double x) {
        X = x;
    }
    public double getY() {
        return Y;
    }
    public void setY(double y) {
        Y = y;
    }
    @Override
    public String toString() {
        return "Coordinate [X=" + X + ", Y=" + Y + "]";
    }
}
