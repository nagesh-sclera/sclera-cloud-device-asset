package io.sclera.utils.ips;

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
