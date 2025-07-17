package com.netit;



public class Point {
    private int x;
    private int y;
    private final int type;

    public Point(int x, int y, int type){
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getType() { return this.type; }
    public void setX(int x) { this.x = x; }
    public void setY(int y){ this.y = y; }

    @Override
    public String toString(){
        return "Point{x=" + x + ", y=" + y + "}";
    }


}

