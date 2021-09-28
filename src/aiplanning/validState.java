
package aiplanning;

import finitestatemachine.State;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class validState implements State {
    private final Point point;
    Set<SokobanBox> boxes = new HashSet<>();

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


    public void addBox(SokobanBox b){
        boxes.add(b);
    }

    public Set<SokobanBox> getBoxes(){
        return boxes;
    }

    public Boolean hasBox(Point p){
        for(SokobanBox box : boxes){
            if (box.getPoint().equals(p)){
                return true;
            }
        }
        return false;
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
}