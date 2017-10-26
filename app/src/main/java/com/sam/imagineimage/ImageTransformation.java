package com.sam.imagineimage;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.bitmap;
import static android.R.attr.x;

/**
 * Created by Pat on 15-Sep-17.
 */

public class ImageTransformation {

    public static int[][] getImageBins(Bitmap bmp, int imageType) {
        int[][] theBins;

        int iLen = bmp.getWidth();
        int jLen = bmp.getHeight();

        if (imageType == Constant.RGB_IMAGE) {

            theBins = new int[Constant.ARGB_USED][Constant.SIZE];

            for (int i = 0; i < Constant.ARGB_USED; i++) {
                for (int j = 0; j < Constant.SIZE; j++) {
                    theBins[i][j] = 0;
                }
            }

            for (int i = 0; i < iLen; i++) {
                for (int j = 0; j < jLen; j++) {
                    theBins[Constant.RED][Color.red(bmp.getPixel(i, j))]++;
                    theBins[Constant.GREEN][Color.green(bmp.getPixel(i, j))]++;
                    theBins[Constant.BLUE][Color.blue(bmp.getPixel(i, j))]++;
                }
            }
        }
        else if (imageType == Constant.GRAYSCALE_IMAGE) {

            theBins = new int[1][Constant.SIZE];

            for (int j = 0; j < Constant.SIZE; j++) {
                theBins[0][j] = 0;
            }

            for (int i = 0; i < iLen; i++) {
                for (int j = 0; j < jLen; j++) {
                    int intensity = Color.red(bmp.getPixel(i, j)); // grayscale image has same value for R, G, and B
                    theBins[0][intensity]++;
                }
            }
        }
        else {
            theBins = new int[0][0];
        }

        return theBins;
    }

    public static Bitmap setGrayscale(Bitmap bmp) {
        int iLen = bmp.getWidth();
        int jLen = bmp.getHeight();

        Bitmap resultBmp = Bitmap.createBitmap(iLen, jLen, Bitmap.Config.ARGB_8888);

        Integer pixel;
        int A, R, G, B;

        for(int i=0; i < iLen; i++) {
            for (int j = 0; j < jLen; j++) {
                pixel = bmp.getPixel(i, j);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                R = G = B = (int)(0.299 * R + 0.587 * G + 0.114 * B); //rec601 luma for PAL and NTSC
                resultBmp.setPixel(i, j, Color.argb(A, R, G, B));
            }
        }

        return resultBmp;
    }

    public static Bitmap transformImage(Bitmap bmp, int transformationType) {
        return transformImage(bmp, transformationType, 0);
    }

    public static Bitmap transformImage(Bitmap bmp, int transformationType, int theThreshold) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Integer currentValue;
        int A, R, G, B;
        float[] lookupTable = new float[0];

        Bitmap transformedImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        if (transformationType == Constant.HISTOGRAM_EQUALIZATION)
            lookupTable = getALU(bmp, Constant.HISTOGRAM_EQUALIZATION);

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                currentValue = bmp.getPixel(i, j);

                A = Color.alpha(currentValue);
                switch (transformationType) {
                    case Constant.HISTOGRAM_EQUALIZATION:
                        R = G = B = (int) lookupTable[Color.red(currentValue)]; // grayscale image has same value for R, G, and B
                        break;
                    case Constant.SMOOTHING:
                        // set the 1-pixel-frame to white
                        if (i == 0 || j == 0 || i == w - 1 || j == h - 1) {
                            R = G = B = 255;
                        } else {
                            R = G = B = getSmoothValue(currentValue, bmp.getPixel(i, j-1), bmp.getPixel(i+1, j-1), bmp.getPixel(i+1, j), bmp.getPixel(i+1, j+1),
                                    bmp.getPixel(i, j+1), bmp.getPixel(i-1, j+1), bmp.getPixel(i-1, j), bmp.getPixel(i-1, j-1));
                        }
                        break;
                    case Constant.BINARIZATION:
                        R = G = B = (Color.red(currentValue) >= theThreshold) ? 255 : 0;
                        break;
                    default:
                        R = G = B = 0;
                        break;
                }
                transformedImage.setPixel(i, j, Color.argb(A, R, G, B));
            }
        }

        return transformedImage;
    }

    public static Bitmap erotionDilation(byte[][] binarizedImage) {
        int R, G, B;
        Bitmap transformedImage = Bitmap.createBitmap(binarizedImage.length, binarizedImage[0].length, Bitmap.Config.ARGB_8888);

        //Erotion
        for (int i=0; i<binarizedImage.length; i++){
            for (int j=0; j<binarizedImage[i].length; j++){
                if (binarizedImage[i][j] == 0){
                    if (i>0 && binarizedImage[i-1][j]==1) binarizedImage[i-1][j] = 2;
                    if (j>0 && binarizedImage[i][j-1]==1) binarizedImage[i][j-1] = 2;
                    if (i+1<binarizedImage.length && binarizedImage[i+1][j]==1) binarizedImage[i+1][j] = 2;
                    if (j+1<binarizedImage[i].length && binarizedImage[i][j+1]==1) binarizedImage[i][j+1] = 2;
                }
            }
        }
        for (int i=0; i<binarizedImage.length; i++){
            for (int j=0; j<binarizedImage[i].length; j++){
                if (binarizedImage[i][j] == 2){
                    binarizedImage[i][j] = 0;
                }
            }
        }

        //Dilation
        for (int i=0; i<binarizedImage.length; i++){
            for (int j=0; j<binarizedImage[i].length; j++){
                if (binarizedImage[i][j] == 1){
                    if (i>0 && binarizedImage[i-1][j]==0) binarizedImage[i-1][j] = 2;
                    if (j>0 && binarizedImage[i][j-1]==0) binarizedImage[i][j-1] = 2;
                    if (i+1<binarizedImage.length && binarizedImage[i+1][j]==0) binarizedImage[i+1][j] = 2;
                    if (j+1<binarizedImage[i].length && binarizedImage[i][j+1]==0) binarizedImage[i][j+1] = 2;
                }
            }
        }
        for (int i=0; i<binarizedImage.length; i++){
            for (int j=0; j<binarizedImage[i].length; j++){
                if (binarizedImage[i][j] == 2){
                    binarizedImage[i][j] = 1;
                }
                R = G = B = (binarizedImage[i][j] == 1) ? 0 : 255;
                transformedImage.setPixel(i, j, Color.rgb(R, G, B));
            }
        }

        return transformedImage;
    }

    public static Bitmap highlightComponent(byte[][] binarizedImage, List<Component> componentList) {
        int R, G, B;
        Bitmap highlightedImage = Bitmap.createBitmap(binarizedImage.length, binarizedImage[0].length, Bitmap.Config.ARGB_8888);

        for (int i=0; i<binarizedImage.length; i++){
            for (int j=0; j<binarizedImage[i].length; j++){
                R = G = B = (binarizedImage[i][j] == 1) ? 0 : 255;
                highlightedImage.setPixel(i, j, Color.rgb(R, G, B));
            }
        }

        //change to green for highlighter
        R = 0;
        G = 255;
        B = 0;

        for(Component c : componentList) {
            for(int x = c.getxMin(); x <= c.getxMax(); x++) {
                highlightedImage.setPixel(x, c.getyMin(), Color.rgb(R, G, B));
                highlightedImage.setPixel(x, c.getyMax(), Color.rgb(R, G, B));
            }
            for(int y = c.getyMin(); y <= c.getyMax(); y++) {
                highlightedImage.setPixel(c.getxMin(), y, Color.rgb(R, G, B));
                highlightedImage.setPixel(c.getxMax(), y, Color.rgb(R, G, B));
            }
        }

        return highlightedImage;
    }

    public static byte[][] getBinarizedArray(Bitmap bmp) {
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

    public static float[] getALU(Bitmap bmp, int transformationType) {
        float[] lookupTable = new float[Constant.SIZE];
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int totalPixel = w * h;

        if (transformationType == Constant.HISTOGRAM_EQUALIZATION) {
            int[][] grayscaleBin = ImageTransformation.getImageBins(bmp, Constant.GRAYSCALE_IMAGE);

            int sum = 0;
            for (int j = 0; j < Constant.SIZE; j++) {
                sum += grayscaleBin[0][j];
                lookupTable[j] = sum * 255 / totalPixel;
            }

        }

        return lookupTable;
    }

    public static int getSmoothValue(int center, int top, int upperRight, int right, int lowerRight, int bottom, int lowerLeft, int left, int upperLeft) {
        int[] arr = new int[9];
        int smoothValue;

        arr[0] = center;
        arr[1] = top;
        arr[2] = upperRight;
        arr[3] = right;
        arr[4] = lowerRight;
        arr[5] = bottom;
        arr[6] = lowerLeft;
        arr[7] = left;
        arr[8] = upperLeft;

        //Get the median
        Arrays.sort(arr);
        smoothValue = arr[4];

        arr = null;

        return smoothValue;
    }

    public static List<Integer> getThresholdCandidates(int[] histogramData) {
        List<Integer> thresholdCandidates = new ArrayList<>();
        int flat = 0;
        boolean hasAscended = false;

        for (int i = 1; i < histogramData.length - 1; i++) {
            if (histogramData[i] > 0)
                hasAscended = true;
            if (!hasAscended) //start picking candidate after the first data that is not zero
                continue;

            if ((histogramData[i] < histogramData[i-1]) && (histogramData[i] < histogramData[i+1])) {
                thresholdCandidates.add(i);
                flat = 0;
            } else if ((histogramData[i] == histogramData[i-1]) && (histogramData[i] == histogramData[i+1])) {
                flat++;
            } else if ((histogramData[i] == histogramData[i-1]) && (histogramData[i] < histogramData[i+1])) {
                for (int j = flat; j <= 0; j--)
                    thresholdCandidates.add(i - j);
                flat = 0;
            } else if ((histogramData[i] == histogramData[i-1]) && (histogramData[i] > histogramData[i+1])) {
                flat = 0;
            } else if ((histogramData[i] < histogramData[i-1]) && (histogramData[i] == histogramData[i+1])) {
                flat++;
            } else if ((histogramData[i] > histogramData[i-1]) && (histogramData[i] == histogramData[i+1])) {
                flat++;
            } else if ((histogramData[i] < histogramData[i-1]) && (histogramData[i] > histogramData[i+1])) {
                //going downhill
                continue;
            } else if ((histogramData[i] > histogramData[i-1]) && (histogramData[i] < histogramData[i+1])) {
                //climbing
                continue;
            } else if ((histogramData[i] > histogramData[i-1]) && (histogramData[i] > histogramData[i+1])) {
                //reach peak
                continue;
            }
        }

        return thresholdCandidates;
    }

    // http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html
    public static int getImageThreshold(int[] histogramData, int total) {
        int threshold = 0;

        float sum = 0;
        for (int i=0 ; i<256 ; i++)
            sum += i * histogramData[i];

        float sumB = 0;
        int wB = 0;
        int wF;

        float varMax = 0;

        for (int i=0 ; i<256 ; i++) {
            wB += histogramData[i]; // Weight Background
            if (wB == 0) continue;

            wF = total - wB; // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (i * histogramData[i]);

            float mB = sumB / wB; // Mean Background
            float mF = (sum - sumB) / wF; // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

}
