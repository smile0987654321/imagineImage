package com.sam.imagineimage;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.x;

/**
 * Created by Pat on 25-Sep-17.
 */

public class Component {
    private ChainCode chainCode;
    private byte[][] binarizedComponent;
    private String chainCodeString;
    private int width;
    private int height;
    private int xMax; //coordinate of X in the big picture
    private int yMax; //coordinate of Y in the big picture
    private int xMin; //coordinate of X in the big picture
    private int yMin; //coordinate of Y in the big picture
    private boolean onTopFrame;
    private boolean onBottomFrame;
    private boolean onLeftFrame;
    private boolean onRightFrame;
    private int holeNumber;
    private byte[][] visitedImageArray;
    private int[] startingPoint;
    private List<int[]> pixelCoordinate;

    public int getHoleNumber() {
        return holeNumber;
    }

    private Component(byte[][] theBinarizedComponent) {
        binarizedComponent = new byte[theBinarizedComponent.length][theBinarizedComponent[0].length];
        for (int i=0; i < theBinarizedComponent.length; i++)
            System.arraycopy(theBinarizedComponent[i], 0, binarizedComponent[i], 0, theBinarizedComponent[i].length);
    }

    private Component(List<int[]> theList, int imageWidth, int imageHeight) {
        int x = 0, y = 1;

        this.pixelCoordinate = new ArrayList<>(theList);

        int xmin = theList.get(0)[x],
                xmax = 0,
                ymin = theList.get(0)[y],
                ymax = 0;

        this.onTopFrame = this.onBottomFrame = this.onLeftFrame = this.onRightFrame = false;

        for(int[] point : theList) {
            xmin = (point[x] < xmin) ? point[x] : xmin;
            xmax = (point[x] > xmax) ? point[x] : xmax;
            ymin = (point[y] < ymin) ? point[y] : ymin;
            ymax = (point[y] > ymax) ? point[y] : ymax;

            //Attempt to remove components sticking to the frame
            if (point[x] == 0)
                this.onLeftFrame = true;

            if (point[x] == imageWidth - 1)
                this.onRightFrame = true;

            if (point[y] == 0)
                this.onTopFrame = true;

            if (point[y] == imageHeight - 1)
                this.onBottomFrame = true;
        }

        int w = xmax - xmin + 1;
        int h = ymax - ymin + 1;

        this.xMax = (xmax == w) ? xmax - 1 : xmax;
        this.yMax = (ymax == h) ? ymax - 1 : ymax;
        this.xMin = xmin;
        this.yMin = ymin;
        this.width = w;
        this.height = h;

        binarizedComponent = new byte[w][h];

        for (int theY = 0; theY < h; theY++)
            for (int theX = 0; theX < w; theX++) {
                binarizedComponent[theX][theY] = 0;
            }

        for(int[] point : theList) {
            Log.i("Point X: ", Integer.toString(point[x]));
            Log.i("xmin: ", Integer.toString(xmin));
            Log.i("Point Y: ", Integer.toString(point[y]));
            Log.i("ymin: ", Integer.toString(ymin));
            binarizedComponent[point[x] - xmin][point[y] - ymin] = 1;
        }

        countOpening();
    }

    public static Component create(byte[][] theBinarizedComponent) {
        return new Component(theBinarizedComponent);
    }

    public static Component create(List<int[]> theList, int imageWidth, int imageHeight) {
        return new Component(theList, imageWidth, imageHeight);
    }

    public byte[][] getBinarizedComponent() {
        return binarizedComponent;
    }

    private void countOpening() {
        //TODO: need to figure out how to ignore background
        this.holeNumber = 0;
        boolean isFinished = false;
        startingPoint = new int[2];
        visitedImageArray = new byte[binarizedComponent.length][binarizedComponent[0].length];
        for (int i=0; i < binarizedComponent.length; i++) {
            System.arraycopy(binarizedComponent[i], 0, visitedImageArray[i], 0, binarizedComponent[i].length);
        }

        while (!isFinished) {
            startingPoint = getStartingPoint(visitedImageArray);

            if (floodFill(startingPoint[0], startingPoint[1], (byte)1, (byte)0))
                this.holeNumber++;

            if (startingPoint[0] == visitedImageArray.length && startingPoint[1] == visitedImageArray[0].length)
                isFinished = true;
        }
    }

    private int[] getStartingPoint(byte[][] theBinarizedImageArray) {
        int w = theBinarizedImageArray.length;
        int h = theBinarizedImageArray[0].length;

        // pixels scanning row by row
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (theBinarizedImageArray[x][y] == 0) {
                    startingPoint[0] = x;
                    startingPoint[1] = y;

                    return startingPoint;
                }

        startingPoint[0] = w;
        startingPoint[1] = h;

        return startingPoint;
    }

    private boolean floodFill(int x, int y, byte newColor, byte oldColor) {
        // flood filling the visited image array

        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= visitedImageArray.length) return false;
        if (y >= visitedImageArray[0].length) return false;

        if (newColor == visitedImageArray[x][y]) return false;
        if (oldColor != visitedImageArray[x][y]) return false;

        visitedImageArray[x][y] = newColor;

        floodFill(x - 1, y, newColor, oldColor);
        floodFill(x + 1, y, newColor, oldColor);
        floodFill(x, y - 1, newColor, oldColor);
        floodFill(x, y + 1, newColor, oldColor);

        return true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getxMax() {
        return xMax;
    }

    public int getyMax() {
        return yMax;
    }

    public int getxMin() {
        return xMin;
    }

    public int getyMin() {
        return yMin;
    }

    public boolean isOnTopFrame() {
        return onTopFrame;
    }

    public boolean isOnBottomFrame() {
        return onBottomFrame;
    }

    public boolean isOnLeftFrame() {
        return onLeftFrame;
    }

    public boolean isOnRightFrame() {
        return onRightFrame;
    }

    public ChainCode getChainCode() {
        return chainCode;
    }

    public void setChainCode(ChainCode chainCode) {
        this.chainCode = chainCode;
    }

    public String getChainCodeString() {
        return chainCodeString;
    }

    public void setChainCodeString(String chainCodeString) {
        this.chainCodeString = chainCodeString;
    }

    public int getFirstYCoordinate(int x) {
        int y = 0;

        for (int[] point : pixelCoordinate) {
            if (point[0] == x) {
                y = point[1];
                break;
            }
        }

        return y;
    }

    public int getLastYCoordinate(int x) {
        int y = 0;

        for (int[] point : pixelCoordinate) {
            if (point[0] == x) {
                y = point[1];
            }
        }

        return y;
    }

    public int getFirstXCoordinate(int y) {
        int x = 0;

        for (int[] point : pixelCoordinate) {
            if (point[1] == y) {
                x = point[0];
                break;
            }
        }

        return x;
    }

    public int getLastXCoordinate(int y) {
        int x = 0;

        for (int[] point : pixelCoordinate) {
            if (point[1] == y) {
                x = point[0];
            }
        }

        return x;
    }

    @Override
    public String toString() {
        return "Component{" +
                "binarizedComponent=" + Arrays.toString(binarizedComponent) +
                '}';
    }
}
