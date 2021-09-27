package aiplanning;

import java.awt.*;

public class SokobanBox {
    private Point point;

    public SokobanBox(Point p) {
        this.point = p;
    }

    public Point getPoint(){
        return point;
    }

    public void moveEast() {
        this.point = new Point(point.x+1, point.y);
    }

    public void moveWest() {
        this.point = new Point(point.x-1, point.y);
    }

    public void moveNorth() {
        this.point = new Point(point.x, point.y-1);
    }

    public void moveSouth() {
        this.point = new Point(point.x, point.y+1);
    }

    public String toString() {
        return "State: ("+(point.x+1)+","+(point.y+1)+")";
    }
}
