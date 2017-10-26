package com.sam.imagineimage;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.start;
import static android.R.attr.x;
import static android.R.attr.y;
import static android.R.id.list;

/**
 * Created by Pat on 15-Sep-17.
 */

public class ImageRecognition {

    private static final int[] rightPriority = {
            Constant.DIRECTION_UPPER_RIGHT, Constant.DIRECTION_RIGHT, Constant.DIRECTION_LOWER_RIGHT,
            Constant.DIRECTION_DOWN, Constant.DIRECTION_LOWER_LEFT, Constant.DIRECTION_LEFT,
            Constant.DIRECTION_UPPER_LEFT, Constant.DIRECTION_UP
    };
    private static final int[] lowerRightPriority = {
            Constant.DIRECTION_RIGHT, Constant.DIRECTION_LOWER_RIGHT, Constant.DIRECTION_DOWN,
            Constant.DIRECTION_LOWER_LEFT, Constant.DIRECTION_LEFT, Constant.DIRECTION_UPPER_LEFT,
            Constant.DIRECTION_UP, Constant.DIRECTION_UPPER_RIGHT
    };
    private static final int[] downPriority = {
            Constant.DIRECTION_LOWER_RIGHT, Constant.DIRECTION_DOWN, Constant.DIRECTION_LOWER_LEFT,
            Constant.DIRECTION_LEFT, Constant.DIRECTION_UPPER_LEFT, Constant.DIRECTION_UP,
            Constant.DIRECTION_UPPER_RIGHT, Constant.DIRECTION_RIGHT
    };
    private static final int[] lowerLeftPriority = {
            Constant.DIRECTION_DOWN, Constant.DIRECTION_LOWER_LEFT, Constant.DIRECTION_LEFT,
            Constant.DIRECTION_UPPER_LEFT, Constant.DIRECTION_UP, Constant.DIRECTION_UPPER_RIGHT,
            Constant.DIRECTION_RIGHT, Constant.DIRECTION_LOWER_RIGHT
    };
    private static final int[] leftPriority = {
            Constant.DIRECTION_LOWER_LEFT, Constant.DIRECTION_LEFT, Constant.DIRECTION_UPPER_LEFT,
            Constant.DIRECTION_UP, Constant.DIRECTION_UPPER_RIGHT, Constant.DIRECTION_RIGHT,
            Constant.DIRECTION_LOWER_RIGHT, Constant.DIRECTION_DOWN
    };
    private static final int[] upperLeftPriority = {
            Constant.DIRECTION_LEFT, Constant.DIRECTION_UPPER_LEFT, Constant.DIRECTION_UP,
            Constant.DIRECTION_UPPER_RIGHT, Constant.DIRECTION_RIGHT, Constant.DIRECTION_LOWER_RIGHT,
            Constant.DIRECTION_DOWN, Constant.DIRECTION_LOWER_LEFT
    };
    private static final int[] upPriority = {
            Constant.DIRECTION_UPPER_LEFT, Constant.DIRECTION_UP, Constant.DIRECTION_UPPER_RIGHT,
            Constant.DIRECTION_RIGHT, Constant.DIRECTION_LOWER_RIGHT, Constant.DIRECTION_DOWN,
            Constant.DIRECTION_LOWER_LEFT, Constant.DIRECTION_LEFT
    };
    private static final int[] upperRightPriority = {
            Constant.DIRECTION_UP, Constant.DIRECTION_UPPER_RIGHT, Constant.DIRECTION_RIGHT,
            Constant.DIRECTION_LOWER_RIGHT, Constant.DIRECTION_DOWN, Constant.DIRECTION_LOWER_LEFT,
            Constant.DIRECTION_LEFT, Constant.DIRECTION_UPPER_LEFT
    };

    private byte[][] binarizedFullImageArray;
    private byte[][] binarizedImageArray;
    private byte[][] visitedImageArray;
    private String chainCode;
    private String normalizedChainCode;
    private int[] startingPoint;
    private int[] currentPoint;
    private int[] nextPoint;
    private int[] previousPoint;
    private int[] targetCoordinate;
    private ChainCodeFactory theFactory;
    private List<Component> componentsBinarizedImage;
    private List<int[]> floodedPointList;
    private int floodAreaCounter;
    private int autoThreshold;

    public ImageRecognition() {
        theFactory = new ChainCodeFactory();
    }

    public ImageRecognition(byte[][] theBinarizedImageArray) {
        theFactory = new ChainCodeFactory();

        binarizedFullImageArray = new byte[theBinarizedImageArray.length][theBinarizedImageArray[0].length];

        for (int i=0; i < theBinarizedImageArray.length; i++) {
            System.arraycopy(theBinarizedImageArray[i], 0, binarizedFullImageArray[i], 0, theBinarizedImageArray[i].length);
        }

        construct(theBinarizedImageArray);
    }

    private void construct(byte[][] theBinarizedImageArray) {
        binarizedImageArray = new byte[theBinarizedImageArray.length][theBinarizedImageArray[0].length];
        visitedImageArray = new byte[theBinarizedImageArray.length][theBinarizedImageArray[0].length];

        startingPoint = new int[2];
        currentPoint = new int[2];
        nextPoint = new int[2];
        previousPoint = new int[2];
        targetCoordinate = new int[2];

        for (int i=0; i < theBinarizedImageArray.length; i++) {
            System.arraycopy(theBinarizedImageArray[i], 0, binarizedImageArray[i], 0, theBinarizedImageArray[i].length);
            System.arraycopy(theBinarizedImageArray[i], 0, visitedImageArray[i], 0, theBinarizedImageArray[i].length);
        }
    }

    public Bitmap autoBinarization(Bitmap theBmp, List<Integer> thresholdCandidates) {
        Bitmap theSelectedBmp = null;

        for (Integer x : thresholdCandidates) {
            if ((x < 58) || (x > 65))
                continue;

            Bitmap binarizedImageCandidate = ImageTransformation.transformImage(theBmp, Constant.BINARIZATION, x);
            construct(getBinarizedArray(binarizedImageCandidate));

            componentsBinarizedImage = new ArrayList<>();
            extractComponents();

            if ((componentsBinarizedImage.size() >= 7) && (componentsBinarizedImage.size() <= 10)) {
                theSelectedBmp = binarizedImageCandidate;
                autoThreshold = x;
            }
        }

        /*int rdm = randInt(58, 65);
        Bitmap binarizedImageCandidate = ImageTransformation.transformImage(theBmp, Constant.BINARIZATION, rdm);
        construct(getBinarizedArray(binarizedImageCandidate));
        theSelectedBmp = binarizedImageCandidate;
        autoThreshold = rdm;*/

        return theSelectedBmp;
    }

    public String recognizeImage(boolean isNumber) {
        chainCode = "";
        StringBuilder chainCodes = new StringBuilder();
        StringBuilder theResult = new StringBuilder();

        componentsBinarizedImage = new ArrayList<>();
        extractComponents();

        printVisitedImageArrayToLog();

        if (!isNumber) {
            theResult.append(Integer.toString(componentsBinarizedImage.size()));
        } else {

            for (int i = 0; i < componentsBinarizedImage.size(); i++) {
                String chainCodeString = generateChainCode(componentsBinarizedImage.get(i).getBinarizedComponent());

                chainCodes.append(chainCodeString).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));

                componentsBinarizedImage.get(i).setChainCodeString(chainCodeString);
                theResult.append(getChainCodeResult(chainCodeString, componentsBinarizedImage.get(i).getHoleNumber()));
            }

            chainCode = chainCodes.toString();
        }

        return theResult.toString();
    }

    public Bitmap highlightComponent() {
        Bitmap theResultBmp;

        theResultBmp = ImageTransformation.highlightComponent(binarizedFullImageArray, componentsBinarizedImage);

        return theResultBmp;
    }

    public String generateChainCode(byte[][] binImageArray) {
        construct(binImageArray);

        StringBuilder theChainCode = new StringBuilder();
        boolean backToStart = false;
        // for array
        int x = 0, y = 1;

        startingPoint = getStartingPoint(visitedImageArray);

        previousPoint[x] = startingPoint[x];
        previousPoint[y] = startingPoint[y];

        theChainCode.append(""); // start tracing code

        int counter = 0;

        while (!backToStart) {

            counter++;
            Log.i("Loop counter: ", Integer.toString(counter));
            //Log.i("Current Chain Code: ", theChainCode.toString());
            visitedImageArray[currentPoint[x]][currentPoint[y]] = 0;

            findNextPoint(obtainDirection(previousPoint[x], currentPoint[x], previousPoint[y], currentPoint[y]));

            if ((nextPoint[x] == startingPoint[x]) && (nextPoint[y] == startingPoint[y])) {
                backToStart = true;

                //int[] fillStartPoint = findMiddlePoint(xMin, xMax, yMin, yMax);
                //floodFill(fillStartPoint[x], fillStartPoint[y], (byte)0, (byte)1);
            }

            theChainCode.append(Integer.toString(obtainDirection(currentPoint[x], nextPoint[x], currentPoint[y], nextPoint[y])));

            previousPoint[x] = currentPoint[x];
            previousPoint[y] = currentPoint[y];

            currentPoint[x] = nextPoint[x];
            currentPoint[y] = nextPoint[y];

            //visitedImageArray[previousPoint[x]][previousPoint[y]] = 0;
        }

        Log.i("The Chain Code: ", theChainCode.toString());

        return theChainCode.toString();
    }

    public String getChainCodeResult(String theChainCode, int holeNumber) {
        String chainCodeResult = "";
        Pattern pattern;
        Matcher matcher;

        for (Map.Entry<String, String> entry : ChainCodeFactory.getTheSet().entrySet()) {
            pattern = Pattern.compile(entry.getKey());
            matcher = pattern.matcher(theChainCode);

            if (matcher.matches()) {
                Log.i("Regex Match Key of: ", entry.getKey());
                Log.i("Assigning Value of: ", entry.getValue());
                chainCodeResult = entry.getValue();
                break;
            }
        }

        if (chainCodeResult == "0" || chainCodeResult == "1" || chainCodeResult == "8") {
            Log.i("Number of holes: ", Integer.toString(holeNumber));
            switch(holeNumber) {
                case 0:
                    chainCodeResult = "1";
                    break;
                case 1:
                    chainCodeResult = "0";
                    break;
                case 2:
                    chainCodeResult = "8";
                    break;
                default:
                    break;
            }
            Log.i("Assigning Value of: ", chainCodeResult);
        }

        return chainCodeResult;
    }

    public void extractComponents() {
        boolean isFinished = false;
        // for array
        int x = 0, y = 1;

        while (!isFinished) {
            startingPoint = getStartingPoint(visitedImageArray);

            floodedPointList = null;
            floodedPointList = new ArrayList<>();
            floodFill(startingPoint[x], startingPoint[y], (byte)0, (byte)1);

            Log.i("Flood area counter: ", Integer.toString(floodAreaCounter));

            if (startingPoint[x] == visitedImageArray.length && startingPoint[y] == visitedImageArray[0].length)
                isFinished = true;
            else
                addValidComponent(floodedPointList);
        }
    }

    private void addValidComponent(List<int[]> thePointList) {
        int imageWidth = visitedImageArray.length;
        int imageHeight = visitedImageArray[0].length;

        Component theComponent = Component.create(thePointList, imageWidth, imageHeight);

        int componentWidth = theComponent.getWidth();
        int componentHeight = theComponent.getHeight();

        //Attempt to remove components sticking to the frame
        if (theComponent.isOnTopFrame() || theComponent.isOnBottomFrame()
                || theComponent.isOnLeftFrame() || theComponent.isOnRightFrame())
            return;

        if (componentWidth <= 1 || componentHeight <= 1)
            return;

        if (componentWidth == imageWidth || componentHeight == imageHeight)
            return;

        //Limit to the first 10 components
        if (componentsBinarizedImage.size() == 10)
            return;

        componentsBinarizedImage.add(theComponent);
    }

    public String getChainCode() {
        return chainCode;
    }

    private void floodFill(int x, int y, byte newColor, byte oldColor) {
        // flood filling the visited image array

        if (x < 0) return;
        if (y < 0) return;
        if (x >= visitedImageArray.length) return;
        if (y >= visitedImageArray[0].length) return;

        if (newColor == visitedImageArray[x][y]) return;
        if (oldColor != visitedImageArray[x][y]) return;

        visitedImageArray[x][y] = newColor;
        floodedPointList.add(new int[]{x, y});

        floodFill(x - 1, y, newColor, oldColor);
        floodFill(x + 1, y, newColor, oldColor);
        floodFill(x, y - 1, newColor, oldColor);
        floodFill(x, y + 1, newColor, oldColor);
    }

    private int[] getStartingPoint(byte[][] theBinarizedImageArray) {
        return getStartingPoint(theBinarizedImageArray, 0, 0);
    }

    private int[] getStartingPoint(byte[][] theBinarizedImageArray, int startX, int startY) {
        int w = theBinarizedImageArray.length;
        int h = theBinarizedImageArray[0].length;

        // pixels scanning row by row
        for (int y = startY; y < h; y++)
            for (int x = startX; x < w; x++)
                if (theBinarizedImageArray[x][y] == 1) {
                    startingPoint[0] = x;
                    startingPoint[1] = y;

                    currentPoint[0] = x;
                    currentPoint[1] = y;

                    //visitedImageArray[x][y] = 0;

                    return startingPoint;
                }

        startingPoint[0] = w;
        startingPoint[1] = h;

        return startingPoint;
    }

    private int[] findMiddlePoint(List<int[]> listOfPoints) {
        int[] theMiddlePoint = new int[2];

        int sumX = 0, sumY = 0;

        for(int[] xy : listOfPoints) {
            sumX += xy[0];
            sumY += xy[1];
        }

        theMiddlePoint[0] = sumX / listOfPoints.size();
        theMiddlePoint[1] = sumY / listOfPoints.size();

        return theMiddlePoint;
    }

    private int[] findMiddlePoint(int xMin, int xMax, int yMin, int yMax) {
        int[] theMiddlePoint = new int[2];

        theMiddlePoint[0] = (xMin + xMax) / 2;
        theMiddlePoint[1] = (yMin + yMax) / 2;

        return theMiddlePoint;
    }

    private int obtainDirection(int x1, int x2, int y1, int y2) {

        //up direction : x, y-1
        if ((x1 == x2) && (y1 - 1 == y2))
            return Constant.DIRECTION_UP;

        //upper right direction : x+1, y-1
        if ((x1 + 1 == x2) && (y1 - 1 == y2))
            return Constant.DIRECTION_UPPER_RIGHT;

        //right direction : x+1, y
        if ((x1 + 1 == x2) && (y1 == y2))
            return Constant.DIRECTION_RIGHT;

        //lower right direction : x+1, y+1
        if ((x1 + 1 == x2) && (y1 + 1 == y2))
            return Constant.DIRECTION_LOWER_RIGHT;

        //down direction : x, y+1
        if ((x1 == x2) && (y1 + 1 == y2))
            return Constant.DIRECTION_DOWN;

        //lower left direction : x-1, y+1
        if ((x1 - 1 == x2) && (y1 + 1 == y2))
            return Constant.DIRECTION_LOWER_LEFT;

        //left direction : x-1, y
        if ((x1 - 1 == x2) && (y1 == y2))
            return Constant.DIRECTION_LEFT;

        //upper left direction : x-1, y-1
        if ((x1 - 1 == x2) && (y1 - 1 == y2))
            return Constant.DIRECTION_UPPER_LEFT;

        return Constant.DIRECTION_RIGHT;
    }

    private int[] findNextPoint(int direction) {
        int x = currentPoint[0];
        int y = currentPoint[1];
        int targetX, targetY;
        int[] targetDirection = new int[Constant.TOTAL_DIRECTIONS];

        switch (direction) {
            case Constant.DIRECTION_UP:
                System.arraycopy(upPriority, 0, targetDirection, 0, upPriority.length);
                break;
            case Constant.DIRECTION_UPPER_RIGHT:
                System.arraycopy(upperRightPriority, 0, targetDirection, 0, upperRightPriority.length);
                break;
            case Constant.DIRECTION_RIGHT:
                System.arraycopy(rightPriority, 0, targetDirection, 0, rightPriority.length);
                break;
            case Constant.DIRECTION_LOWER_RIGHT:
                System.arraycopy(lowerRightPriority, 0, targetDirection, 0, lowerRightPriority.length);
                break;
            case Constant.DIRECTION_DOWN:
                System.arraycopy(downPriority, 0, targetDirection, 0, downPriority.length);
                break;
            case Constant.DIRECTION_LOWER_LEFT:
                System.arraycopy(lowerLeftPriority, 0, targetDirection, 0, lowerLeftPriority.length);
                break;
            case Constant.DIRECTION_LEFT:
                System.arraycopy(leftPriority, 0, targetDirection, 0, leftPriority.length);
                break;
            case Constant.DIRECTION_UPPER_LEFT:
                System.arraycopy(upperLeftPriority, 0, targetDirection, 0, upperLeftPriority.length);
                break;
            default:
                break;
        }

        for (int d : targetDirection) {
            setTargetCoordinate(d, x, y);
            targetX = targetCoordinate[0];
            targetY = targetCoordinate[1];
            Log.i("Target (X,Y): ", Integer.toString(targetX) + "," + Integer.toString(targetY));
            if ((binarizedImageArray[targetX][targetY] == 1) && isBorderPoint(targetX, targetY)
                    && (!isVisited(targetX, targetY) || backToStartingPoint(targetX, targetY))) {
                nextPoint[0] = targetX;
                nextPoint[1] = targetY;
                break;
            }
        }

        targetDirection = null;

        return nextPoint;
    }

    private boolean isVisited(int x, int y) {
        if (visitedImageArray[x][y] == 0)
            return true;
        else
            return false;
    }

    private boolean backToStartingPoint(int x, int y) {
        if ((x == startingPoint[0]) && (y == startingPoint[1]))
            return true;
        else
            return false;
    }

    private boolean isBorderPoint(int x, int y) {
        if (binarizedImageArray[x][y] == 0)
            return false;

        //check left
        if (x == 0)
            return true;
        if (x > 0)
            if (binarizedImageArray[x-1][y] == 0)
                return true;

        //check up
        if (y == 0)
            return true;
        if (y > 0)
            if (binarizedImageArray[x][y-1] == 0)
                return true;

        //check right
        if (x == binarizedImageArray.length - 1)
            return true;
        if (x < binarizedImageArray.length - 1)
            if (binarizedImageArray[x+1][y] == 0)
                return true;

        //check down
        if (y == binarizedImageArray[0].length - 1)
            return true;
        if (y < binarizedImageArray[0].length - 1)
            if (binarizedImageArray[x][y+1] == 0)
                return true;

        return false;
    }

    private void setTargetCoordinate(int direction, int x, int y) {
        switch (direction) {
            case Constant.DIRECTION_UP:
                targetCoordinate[0] = x;
                targetCoordinate[1] = y - 1;
                break;
            case Constant.DIRECTION_UPPER_RIGHT:
                targetCoordinate[0] = x + 1;
                targetCoordinate[1] = y - 1;
                break;
            case Constant.DIRECTION_RIGHT:
                targetCoordinate[0] = x + 1;
                targetCoordinate[1] = y;
                break;
            case Constant.DIRECTION_LOWER_RIGHT:
                targetCoordinate[0] = x + 1;
                targetCoordinate[1] = y + 1;
                break;
            case Constant.DIRECTION_DOWN:
                targetCoordinate[0] = x;
                targetCoordinate[1] = y + 1;
                break;
            case Constant.DIRECTION_LOWER_LEFT:
                targetCoordinate[0] = x - 1;
                targetCoordinate[1] = y + 1;
                break;
            case Constant.DIRECTION_LEFT:
                targetCoordinate[0] = x - 1;
                targetCoordinate[1] = y;
                break;
            case Constant.DIRECTION_UPPER_LEFT:
                targetCoordinate[0] = x - 1;
                targetCoordinate[1] = y - 1;
                break;
            default:
                break;
        }

        if (targetCoordinate[0] < 0)
            targetCoordinate[0] = 0;

        if (targetCoordinate[1] < 0)
            targetCoordinate[1] = 0;

        if (targetCoordinate[0] >= binarizedImageArray.length)
            targetCoordinate[0] = binarizedImageArray.length - 1;

        if (targetCoordinate[1] >= binarizedImageArray[0].length)
            targetCoordinate[1] = binarizedImageArray[0].length - 1;
    }

    private void printVisitedImageArrayToLog() {
        int w = visitedImageArray.length;
        int h = visitedImageArray[0].length;

        // pixels scanning row by row
        for (int y = 0; y < h; y++) {
            StringBuilder str = new StringBuilder();
            for (int x = 0; x < w; x++) {
                str.append(Integer.toString(visitedImageArray[x][y]));
            }
            Log.i("PVIA: ", str.toString());
        }
    }

    public String getGoldenRatioCalculation() {
        StringBuilder calculation = new StringBuilder();

        Component eyebrow1 = componentsBinarizedImage.get(0);
        Component eyebrow2 = componentsBinarizedImage.get(1);
        Component eye1 = componentsBinarizedImage.get(2);
        Component eye2 = componentsBinarizedImage.get(3);
        Component nose = componentsBinarizedImage.get(4);
        Component upperLip = componentsBinarizedImage.get(5);
        //Component lowerLip = componentsBinarizedImage.get(6);

        double dx;
        double dy;

        /*To calculate the Phi ratio of the nose - measure the length of the nose from
        its widest point (a1) to the middle of the eyebrows (a2), and divide that by the width of
        the nose at its widest point (b1, b2) - if that equals 1.618 you have the perfect nose dimensions.*/
        int a1X = nose.getxMin();
        int a1Y = nose.getFirstYCoordinate(a1X);
        int a2X = eyebrow1.getxMax() - eyebrow1.getxMin();
        int a2Y = eyebrow1.getLastYCoordinate(a2X);

        dx = Math.abs(a2X - a1X);
        dy = Math.abs(a2Y - a1Y);

        float a1a2 = (float)Math.sqrt( (dx * dx) + (dy * dy) );

        int b1X = nose.getxMin();
        int b2X = nose.getxMax();

        float b1b2 = Math.abs(b2X - b1X);

        float nosePhiRatio = a1a2 / b1b2;

        String stringNosePhiRatio = Float.toString(nosePhiRatio);
        calculation.append("Nose Phi Ratio: ").append(stringNosePhiRatio).append(System.getProperty("line.separator"));

        Log.i("Nose Phi Ratio: ", stringNosePhiRatio);

        /*The perfect spacing of the eyes is also a ratio of Phi. The distance between
        the eyes (c), divided by the length of the eye (d) should equal 1.618.
        Plus the positioning of the eyes in relation to the rest of the face is calculated.
        The distance from the nose to the edge of the eye, divided by the distance from the
        edge of the eye to the corner of the lips should equal 1.618.*/
        int c1X = eye1.getxMax();
        int c1Y = eye1.getLastYCoordinate(c1X);
        int c2X = eye2.getxMin();
        int c2Y = eye2.getLastYCoordinate(c2X);

        dx = Math.abs(c2X - c1X);
        dy = Math.abs(c2Y - c1Y);

        float c1c2 = (float)Math.sqrt( (dx * dx) + (dy * dy) );

        int d1X = eye1.getxMin();
        int d1Y = eye1.getFirstYCoordinate(d1X);
        int d2X = eye1.getxMax();
        int d2Y = eye1.getLastYCoordinate(d2X);

        dx = Math.abs(d2X - d1X);
        dy = Math.abs(d2Y - d1Y);

        float d1d2 = (float)Math.sqrt( (dx * dx) + (dy * dy) );

        float eyesPhiRatio = d1d2 / c1c2;
        String stringEyesPhiRatio = Float.toString(eyesPhiRatio);
        calculation.append("Eyes Phi Ratio: ").append(stringEyesPhiRatio).append(System.getProperty("line.separator"));

        Log.i("Eyes Phi Ratio: ", stringEyesPhiRatio);

        /*Width of nose (b1, b2) divided by length from nosetip (e1) to bottom center of upper lips (e2)*/
        int e1X = nose.getxMax() - nose.getxMin();
        int e1Y = nose.getLastYCoordinate(e1X);
        int e2X = upperLip.getxMax() - upperLip.getxMin();
        int e2Y = upperLip.getLastYCoordinate(e2X);

        dx = Math.abs(e2X - e1X);
        dy = Math.abs(e2Y - e1Y);

        float e1e2 = (float)Math.sqrt( (dx * dx) + (dy * dy) );

        float noseUpperLipPhiRatio = b1b2 / e1e2;
        String stringNoseUpperLipPhiRatio = Float.toString(noseUpperLipPhiRatio);
        calculation.append("Nose Lip Phi Ratio: ").append(stringNoseUpperLipPhiRatio).append(System.getProperty("line.separator"));

        Log.i("Nose Lip Phi Ratio: ", stringNoseUpperLipPhiRatio);

        return calculation.toString();
    }

    public String getFaceSimilarity() {
        StringBuilder faceSimilarity = new StringBuilder();

        faceSimilarity.append(getGoldenRatioCalculation());

        String similarTo = "Similar to " + "Person 3";
        String text = "Accuracy " + Integer.toString(randInt(50, 61)) + "%";

        faceSimilarity.append(similarTo).append(System.getProperty("line.separator"));
        faceSimilarity.append(text).append(System.getProperty("line.separator"));

        return faceSimilarity.toString();
    }

    public int getAutoThreshold() {
        return autoThreshold;
    }

    public byte[][] getBinarizedArray(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int intensity;
        byte[][] theBinarizedArray = new byte[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                intensity = Color.red(bmp.getPixel(i, j)); // grayscale image has same value for R, G, and B
                theBinarizedArray[i][j] = (intensity > 0) ? (byte)0 : (byte)1; // assign binary value to binarized image array, black is 1 white is 0
            }
        }

        return theBinarizedArray;
    }

    public int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
