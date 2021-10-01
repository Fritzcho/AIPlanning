
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

    public validState(Point p,Set<SokobanBox> list) {
        this.point = p;
        this.boxes = list;
    }

    public validState(Set<SokobanBox> list) {
        this.point = new Point(1,1);
        this.boxes = list;
    }

    public Point getPoint(){
        return point;
    }

    public void addBox(SokobanBox b){
        boxes.add(b);
    }

    public void setBoxes(Set<SokobanBox> b) {
        this.boxes = b;
    }

    public Set<SokobanBox> getBoxes(){
        Set<SokobanBox> b = new HashSet<>();
        for(SokobanBox b2 : boxes){
            b.add(new SokobanBox(b2.getPoint()));
        }
        return b;
    }

    public SokobanBox getBox(Point p) {
        return boxes.stream().filter(n ->
                        n.getPoint().equals(p))
                        .findFirst().orElseThrow();
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
        return new Point(point.x+1, point.y);
    }

    public Point getWest() {
        return new Point(point.x-1, point.y);
    }

    public Point getSouth() {
        return new Point(point.x, point.y+1);
    }

    public Point getNorth() {
        return new Point(point.x, point.y-1);
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
        if(v.getBoxes().equals(boxes))
            return true;
        else
            return false;
    }
}