package com.sam.imagineimage;

/**
 * Created by Pat on 18-Sep-17.
 */

public class ChainCode {
    private String objectName;
    private int up;
    private int upperRight;
    private int right;
    private int lowerRight;
    private int down;
    private int lowerLeft;
    private int left;
    private int upperLeft;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public ChainCode(String theObjectName,
                     int directionUp,
                     int directionUpperRight,
                     int directionRight,
                     int directionLowerRight,
                     int directionDown,
                     int directionLowerLeft,
                     int directionLeft,
                     int directionUpperLeft) {
        objectName = theObjectName;

        up = directionUp;
        upperRight = directionUpperRight;
        right = directionRight;
        lowerRight = directionLowerRight;
        down = directionDown;
        lowerLeft = directionLowerLeft;
        left = directionLeft;
        upperLeft = directionUpperLeft;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public int getUpperRight() {
        return upperRight;
    }

    public void setUpperRight(int upperRight) {
        this.upperRight = upperRight;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getLowerRight() {
        return lowerRight;
    }

    public void setLowerRight(int lowerRight) {
        this.lowerRight = lowerRight;
    }

    public int getDown() {
        return down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public int getLowerLeft() {
        return lowerLeft;
    }

    public void setLowerLeft(int lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getUpperLeft() {
        return upperLeft;
    }

    public void setUpperLeft(int upperLeft) {
        this.upperLeft = upperLeft;
    }

    public boolean isMatched(ChainCode theComparison) {
        if ((theComparison.getUp() == this.getUp()) &&
                (theComparison.getUpperRight() == this.getUpperRight()) &&
                (theComparison.getRight() == this.getRight()) &&
                (theComparison.getLowerRight() == this.getLowerRight()) &&
                (theComparison.getDown() == this.getDown()) &&
                (theComparison.getLowerLeft() == this.getLowerLeft()) &&
                (theComparison.getLeft() == this.getLeft()) &&
                (theComparison.getUpperLeft() == this.getUpperLeft()))
            return true;
        else
            return false;
    }
}
