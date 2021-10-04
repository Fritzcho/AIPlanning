
package aiplanning;

import finitestatemachine.State;

import java.awt.*;
import java.util.*;

class validState implements State {
    private final Point point;
    Set<SokobanBox> boxes = new LinkedHashSet<>();

    public validState(Point p) {
        this.point = p;
    }

    public validState(Point p, Set<SokobanBox> list) {
        this.point = p;
        this.boxes = list;
    }

    public Point getPoint(){
        return point;
    }

    public HashSet<SokobanBox> getBoxes(){
            HashSet<SokobanBox> b = new HashSet<>();
            for(SokobanBox b2 : boxes){
                b.add(new SokobanBox(b2.getPoint()));
            }
            return b;
    }


    public SokobanBox getBox(Point p) {
        for(SokobanBox box : boxes){
            if (box.getPoint().equals(p)){
                return box;
            }
        }
        return null;
    }

    public Boolean hasBox(Point p){
        for(SokobanBox box : boxes){
            if (box.getPoint().equals(p)){
                return true;
            }
        }
        return false;
    }

    public Point getEast() {
        return new Point(this.point.x+1, this.point.y);
    }

    public Point getWest() {
        return new Point(this.point.x-1, this.point.y);
    }

    public Point getSouth() {
        return new Point(this.point.x, this.point.y+1);
    }

    public Point getNorth() {
        return new Point(this.point.x, this.point.y-1);
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
        return "State: ("+(point.x+1)+","+(point.y+1)+")";
    }

    public boolean equals(validState v){
        /*if(v.getPoint().equals(this.point) &&
                this.boxes.stream().allMatch(b -> boxes.stream().allMatch(b2 -> b2.getPoint().equals(b.getPoint())))){
            return true;
        } else{
            return false;
        }*/

        return v.boxes.equals(boxes);
    }
}