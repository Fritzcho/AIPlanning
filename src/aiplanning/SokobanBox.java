package aiplanning;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SokobanBox {
    private Point point;


    public SokobanBox(Point p) {
        this.point = p;
    }

    public Point getPoint(){
        return point;
    }

    public void setPoint(Point p){
        this.point = p;
    }

    public void moveEast() { this.point = new Point(point.x+1, point.y); }

    public void moveWest() { this.point = new Point(point.x-1, point.y); }

    public void moveNorth() { this.point = new Point(point.x, point.y-1); }

    public void moveSouth() { this.point = new Point(point.x, point.y+1); }

    public Point getEast() {
        return new Point(point.x+1, point.y);
    }

    public Point getWest() {
        return new Point(point.x-1, point.y);
    }

    public Point getNorth() {
        return new Point(point.x, point.y-1);
    }

    public Point getSouth() {
        return new Point(point.x, point.y+1);
    }


    public Set<Point> getAdjacent(){
        Set<Point> s = new HashSet<>();

        s.add(new Point((int)point.getX()+1, (int)point.getY()));
        s.add(new Point((int)point.getX()-1, (int)point.getY()));
        s.add(new Point((int)point.getX(), (int)point.getY()+1));
        s.add(new Point((int)point.getX(), (int)point.getY()-1));

        return s;
    }


    public String toString() {
        return "State: ("+(point.x)+","+(point.y)+")";
    }

    public boolean equals(SokobanBox b){
        if(b.getPoint().equals(this.point)){
            return true;
        }else{
            return false;
        }
    }
}