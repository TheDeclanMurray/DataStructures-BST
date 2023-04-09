package org.headroyce.declanm2022;

/**
 * Represent a 2D point in space
 * @author Brian Sea
 */
public class Point {
    public double x;
    public double y;

    public Point(double x, double y ){
        this.x = x;
        this.y = y;
    }

    public String toString(){
        String rtn = "(" + this.x +", "+this.y+")";
        return rtn;
    }
}
