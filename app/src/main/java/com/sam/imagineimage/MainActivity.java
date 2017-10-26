package com.sam.imagineimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, OnChartGestureListener, OnChartValueSelectedListener {

    private Button btnSelectImage;
    private Button btnGrayscale;
    private Button btnTransform;
    private Button btnSmoothing;
    private Button btnBinarization;
    private Button btnAutoBinarization;
    private Button btnRecognize;
    private Button btnRecognizeNumber;
    private Button btnThresholdUp;
    private Button btnThresholdDown;
    private Button btnErotionDilation;

    private ImageView ivInputImage;
    private ImageView ivOutputImage;
    private LineChart lcHistogram;
    private EditText etThreshold;
    private TextView tvTextOut;

    private int[][] colorBins;
    private int[][] grayScaleBins;
    private int[] grayscaleHist; // histogram data for grayscaled or transformed image
    private Bitmap grayscaleBmp;
    private boolean isGrayscaled;
    private int totalPixelsIn;
    private int totalPixelsOut;
    private byte[][] binarizedImageArray;
    private int imageStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // All clickable buttons go here
        btnSelectImage = (Button) findViewById(R.id.btn_selectImage);
        btnGrayscale = (Button) findViewById(R.id.btn_grayscale);
        btnTransform = (Button) findViewById(R.id.btn_transform);
        btnSmoothing = (Button) findViewById(R.id.btn_smoothing);
        btnBinarization = (Button) findViewById(R.id.btn_binarization);
        btnAutoBinarization = (Button) findViewById(R.id.btn_autobinarization);
        btnRecognize = (Button) findViewById(R.id.btn_recognize);
        btnRecognizeNumber = (Button) findViewById(R.id.btn_recognizeNumber);
        btnThresholdUp = (Button) findViewById(R.id.btn_thresholdUp);
        btnThresholdDown = (Button) findViewById(R.id.btn_thresholdDown);
        btnErotionDilation = (Button) findViewById(R.id.btn_erotionDilation);

        ivInputImage = (ImageView) findViewById(R.id.iv_inputImage);
        ivOutputImage = (ImageView) findViewById(R.id.iv_outputImage);
        lcHistogram = (LineChart) findViewById(R.id.lc_histogram);

        etThreshold = (EditText) findViewById(R.id.et_threshold);
        tvTextOut = (TextView) findViewById(R.id.tv_outputText);

        btnSelectImage.setOnClickListener(this);
        btnGrayscale.setOnClickListener(this);
        btnTransform.setOnClickListener(this);
        btnSmoothing.setOnClickListener(this);
        btnBinarization.setOnClickListener(this);
        btnAutoBinarization.setOnClickListener(this);
        btnRecognize.setOnClickListener(this);
        btnRecognizeNumber.setOnClickListener(this);
        btnThresholdUp.setOnClickListener(this);
        btnThresholdDown.setOnClickListener(this);
        btnErotionDilation.setOnClickListener(this);

        lcHistogram.setOnChartGestureListener(this);
        lcHistogram.setOnChartValueSelectedListener(this);

        tvTextOut.setMovementMethod(new ScrollingMovementMethod());

        etThreshold.setText(Integer.toString(127), TextView.BufferType.EDITABLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_selectImage:
                selectImage();
                break;
            case R.id.btn_grayscale:
                setGrayscale();
                break;
            case R.id.btn_transform:
                setInputImage(grayscaleBmp);
                setOutputImage(ImageTransformation.transformImage(grayscaleBmp, Constant.HISTOGRAM_EQUALIZATION), true);
                imageStatus = Constant.IMAGE_TRANSFORMED;
                break;
            case R.id.btn_smoothing:
                performSmoothing();
                imageStatus = Constant.IMAGE_SMOOTH;
                break;
            case R.id.btn_binarization:
                performBinarization(getManualThreshold());
                break;
            case R.id.btn_thresholdUp:
                setManualThreshold(Constant.PLUS_ONE);
                break;
            case R.id.btn_thresholdDown:
                setManualThreshold(Constant.MINUS_ONE);
                break;
            case R.id.btn_erotionDilation:
                performErotionDilation();
                break;
            case R.id.btn_autobinarization:
                performAutoBinarization();
                break;
            case R.id.btn_recognize:
                performImageRecognition(false);
                //generateChainCode();
                break;
            case R.id.btn_recognizeNumber:
                performImageRecognition(true);
                //generateChainCode();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                setInputImage(selectedImage);
                imageStatus = Constant.IMAGE_INITIAL;
                ivOutputImage.setImageDrawable(null);
                totalPixelsOut = 0;
                colorBins = ImageTransformation.getImageBins(selectedImage, Constant.RGB_IMAGE);
                setHistogram(Constant.RGB_IMAGE, selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "You haven't picked an image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            // or highlightTouch(null) for callback to onNothingSelected(...)
            lcHistogram.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: "
                + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + lcHistogram.getLowestVisibleXIndex()
                + ", high: " + lcHistogram.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + lcHistogram.getXChartMin()
                + ", xmax: " + lcHistogram.getXChartMax()
                + ", ymin: " + lcHistogram.getYChartMin()
                + ", ymax: " + lcHistogram.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    public void selectImage() {
        ivInputImage.setImageDrawable(null);
        totalPixelsIn = 0;
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, Constant.RESULT_LOAD_IMG);
    }

    public void setInputImage(Bitmap bmp) {
        ivInputImage.setImageDrawable(null);
        totalPixelsIn = 0;

        ivInputImage.setImageBitmap(bmp);
        totalPixelsIn = bmp.getWidth() * bmp.getHeight();
    }

    public void setOutputImage(Bitmap bmp, boolean showHistogram) {
        ivOutputImage.setImageDrawable(null);
        totalPixelsOut = 0;

        ivOutputImage.setImageBitmap(bmp);
        totalPixelsOut = bmp.getWidth() * bmp.getHeight();

        if (showHistogram)
            setHistogram(Constant.GRAYSCALE_IMAGE, bmp);
        else
            lcHistogram.setVisibility(View.INVISIBLE);

    }

    public void setHistogram(int imageType, Bitmap bmp) {
        ArrayList<String> xVals = setXAxisValues();
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        if (imageType == Constant.RGB_IMAGE) {

            ArrayList<Entry> yValsRed = setYAxisValues(Constant.RED);
            ArrayList<Entry> yValsGreen = setYAxisValues(Constant.GREEN);
            ArrayList<Entry> yValsBlue = setYAxisValues(Constant.BLUE);

            LineDataSet redSet;
            redSet = new LineDataSet(yValsRed, "RED");
            redSet.setColor(Color.RED);
            redSet.setDrawCircles(false);
            redSet.setDrawValues(false);

            LineDataSet greenSet;
            greenSet = new LineDataSet(yValsGreen, "GREEN");
            greenSet.setColor(Color.GREEN);
            greenSet.setDrawCircles(false);
            greenSet.setDrawValues(false);

            LineDataSet blueSet;
            blueSet = new LineDataSet(yValsBlue, "BLUE");
            blueSet.setColor(Color.BLUE);
            blueSet.setDrawCircles(false);
            blueSet.setDrawValues(false);

            dataSets.add(redSet);
            dataSets.add(greenSet);
            dataSets.add(blueSet);

        } else if (imageType == Constant.GRAYSCALE_IMAGE) {

            grayScaleBins = ImageTransformation.getImageBins(bmp, Constant.GRAYSCALE_IMAGE);

            ArrayList<Entry> yValsGray = setTransformedImageYAxisValues(grayScaleBins);

            LineDataSet grayscaleSet;
            grayscaleSet = new LineDataSet(yValsGray, "GRAYSCALE");
            grayscaleSet.setColor(Color.BLACK);
            grayscaleSet.setDrawCircles(false);
            grayscaleSet.setDrawValues(false);

            dataSets.add(grayscaleSet);
        }

        LineData lineData = new LineData(xVals, dataSets);

        lcHistogram.setData(lineData);

        //disable description at bottom right corner
        lcHistogram.setDescription("");

        //disable Legend
        lcHistogram.getLegend().setEnabled(false);

        //set X axis position to bottom
        lcHistogram.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        //snap y=0 to bottom of histogram
        lcHistogram.getAxisLeft().setAxisMinValue(0f);
        lcHistogram.getAxisRight().setAxisMinValue(0f);

        //refresh the LineChart
        lcHistogram.invalidate();

        lcHistogram.setVisibility(View.VISIBLE);
    }

    public void setGrayscale() {
        BitmapDrawable drawable = (BitmapDrawable) ivInputImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Bitmap resultBmp = ImageTransformation.setGrayscale(bitmap);

        //ivOutputImage.setImageBitmap(resultBmp);
        //totalPixelsOut = resultBmp.getWidth() * resultBmp.getHeight();
        setOutputImage(resultBmp, true);
        //setHistogram(Constant.GRAYSCALE_IMAGE, resultBmp);
        grayscaleBmp = resultBmp;
        imageStatus = Constant.IMAGE_GRAYSCALED;
    }

    public void performSmoothing() {
        BitmapDrawable drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Bitmap resultBmp = ImageTransformation.transformImage(bitmap, Constant.SMOOTHING);

        setInputImage(bitmap);
        setOutputImage(resultBmp, true);
    }

    public int getManualThreshold() {
        String thr = etThreshold.getText().toString();
        int theThreshold = Integer.parseInt(thr);

        return theThreshold;
    }

    public void setManualThreshold(int operation) {
        int currentThreshold = getManualThreshold();
        int newThreshold = 0;
        if (currentThreshold == 0 || currentThreshold == 255)
            return;

        if (operation == Constant.PLUS_ONE)
            newThreshold = currentThreshold + 1;
        else if (operation == Constant.MINUS_ONE)
            newThreshold = currentThreshold - 1;

        etThreshold.setText(Integer.toString(newThreshold));
        performBinarization(newThreshold);
    }
    
    public void performBinarization(int thr) {
        BitmapDrawable drawable;
        Bitmap bitmap;
        int theThreshold = thr;

        if (imageStatus >= Constant.IMAGE_BINARIZED) {
            drawable = (BitmapDrawable) ivInputImage.getDrawable();
            bitmap = drawable.getBitmap();

            setOutputImage(ImageTransformation.transformImage(bitmap, Constant.BINARIZATION, theThreshold), false);
        }
        //maybe more than image_smooth
        else if (imageStatus >= Constant.IMAGE_TRANSFORMED) {
            drawable = (BitmapDrawable) ivOutputImage.getDrawable();
            bitmap = drawable.getBitmap();

            setInputImage(bitmap);
            setOutputImage(ImageTransformation.transformImage(bitmap, Constant.BINARIZATION, theThreshold), false);

            imageStatus = Constant.IMAGE_BINARIZED;
        }

        etThreshold.setText(Integer.toString(theThreshold));

        drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        bitmap = drawable.getBitmap();

        binarizedImageArray = ImageTransformation.getBinarizedArray(bitmap);
    }

    public void performErotionDilation() {
        BitmapDrawable drawable;
        Bitmap theBitmap;

        drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        theBitmap = drawable.getBitmap();

        if (imageStatus >= Constant.IMAGE_ERODED_DILATED)
            binarizedImageArray = ImageTransformation.getBinarizedArray(theBitmap);

        setInputImage(theBitmap);
        setOutputImage(ImageTransformation.erotionDilation(binarizedImageArray), false);

        drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        theBitmap = drawable.getBitmap();

        binarizedImageArray = ImageTransformation.getBinarizedArray(theBitmap);

        imageStatus = Constant.IMAGE_ERODED_DILATED;
    }

    public void performAutoBinarization() {
        BitmapDrawable drawable;
        Bitmap bitmap;

        grayscaleHist = new int[grayScaleBins[0].length];

        System.arraycopy(grayScaleBins[0], 0, grayscaleHist, 0, grayScaleBins[0].length);

        List<Integer> thresholdCandidates = new ArrayList<>();
        thresholdCandidates = ImageTransformation.getThresholdCandidates(grayscaleHist);

        ImageRecognition ir = new ImageRecognition();

        drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        bitmap = drawable.getBitmap();

        setInputImage(bitmap);
        setOutputImage(ir.autoBinarization(bitmap, thresholdCandidates), false);

        imageStatus = Constant.IMAGE_BINARIZED;

        etThreshold.setText(Integer.toString(ir.getAutoThreshold()));

        drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        bitmap = drawable.getBitmap();

        binarizedImageArray = ir.getBinarizedArray(bitmap);
    }

    public void performImageRecognition(boolean isNumber) {
        ImageRecognition ir = new ImageRecognition(binarizedImageArray);
        String theResult = ir.recognizeImage(isNumber);

        if (!isNumber) {
            Toast.makeText(MainActivity.this, "Number of components in this image: " + theResult, Toast.LENGTH_LONG).show();

            //String goldenRatio = ir.getGoldenRatioCalculation();
            String recognitionInformation = ir.getFaceSimilarity();
            tvTextOut.setText(recognitionInformation);

        } else {
            Toast.makeText(MainActivity.this, "This is an image of " + theResult, Toast.LENGTH_LONG).show();

            String chainCode = ir.getChainCode();

            StringBuilder displayResult = new StringBuilder();
            displayResult.append("This is an image of : ").append(theResult).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
            displayResult.append(chainCode);
            tvTextOut.setText(displayResult.toString());

            Log.i("Displaying chain code: ", chainCode);
        }

        BitmapDrawable drawable;
        Bitmap theBitmap;

        drawable = (BitmapDrawable) ivOutputImage.getDrawable();
        theBitmap = drawable.getBitmap();
        setInputImage(theBitmap);
        setOutputImage(ir.highlightComponent(), false);

        imageStatus = Constant.IMAGE_RECOGNIZED;
    }

    /*public void generateChainCode() {
        ImageRecognition ir = new ImageRecognition(binarizedImageArray);
        String imageChainCode = ir.generateChainCode();

        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("imageChainCode", imageChainCode);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(MainActivity.this, "Image chain codes has been copied to clipboard", Toast.LENGTH_LONG).show();
    }*/

    private ArrayList<String> setXAxisValues(){
        ArrayList<String> xVals = new ArrayList<>();

        for(int i=0; i < Constant.SIZE; i++) {
            xVals.add(Integer.toString(i));
        }

        return xVals;
    }

    private ArrayList<Entry> setYAxisValues(int binColor){
        ArrayList<Entry> yVals = new ArrayList<>();

        for(int i=0; i < Constant.SIZE; i++) {
            yVals.add(new Entry(colorBins[binColor][i], i));
        }

        return yVals;
    }

    private ArrayList<Entry> setTransformedImageYAxisValues(int[][] theBin){
        ArrayList<Entry> yVals = new ArrayList<>();

        for(int i=0; i < Constant.SIZE; i++) {
            yVals.add(new Entry(theBin[0][i], i));
        }

        return yVals;
    }
}

// TODO: Linear Transformation (Piecewise?) https://www.researchgate.net/publication/253893859_IMAGE_ENHANCEMENT_METHOD_USING_PIECEWISE_LINEAR_TRANSFORMS