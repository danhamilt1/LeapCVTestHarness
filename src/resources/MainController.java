package resources;

import com.leapcv.LeapCVController;
import com.leapcv.LeapCVObjectDetector;
import com.leapcv.utils.LeapCVImageUtils;
import com.leapcv.utils.LeapCVMatcherType;
import com.leapcv.utils.LeapCVStereoMatcher;
import com.leapcv.utils.LeapCVStereoUtils;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class MainController {

    Mat leftMat;
    Mat rightMat;
    Mat outMat;
    private double fpsValue;
    @FXML
    private Canvas leftCanvas;
    @FXML
    private Canvas rightCanvas;
    @FXML
    private Canvas leftProc;
    @FXML
    private Canvas rightProc;
    @FXML
    private Canvas outCanvas;
    //private LeapCVStereoUtils leapStereo;
    @FXML
    private CheckBox gauss;
    @FXML
    private CheckBox med;
    @FXML
    private CheckBox undist;
    @FXML
    private CheckBox disparityCheck;
    @FXML
    private Button snap;

    @FXML
    private Label fps;

    private LeapCVController leapController;

    @SuppressWarnings("deprecation")
    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            this.leapController = new LeapCVController();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //this.leapStereo = new LeapCVStereoUtils();
//
//		Platform.runLater(()->{
//
//		});

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                long start = System.nanoTime();

                onSnapTaken();

                long stop = System.nanoTime();
                long elapsed = stop - start;
                double seconds = (double)elapsed/1000000000.0;
                fpsValue = 1/seconds;
            }
        };

        timer.start();


    }

    @FXML
    void onSnapTaken() {
//		Rect roi = new Rect(160,160,320,320);

        try {
            this.leapController.nextValidFrame();

            leftMat = this.leapController.getLeftImage();
            rightMat = this.leapController.getRightImage();
            outMat = Mat.eye(200,200, CvType.CV_8U);

            drawLeft(rightMat);
            drawRight(leftMat);

            if (undist.isSelected()) {
                leftMat = this.leapController.getLeftImageUndistorted();
                rightMat = this.leapController.getRightImageUndistorted();
            }

            if (med.isSelected()) {
                leftMat = this.medianExample(leftMat);
                rightMat = this.medianExample(rightMat);
            }

            if (gauss.isSelected()) {
                leftMat = this.gaussianExample(leftMat);
                rightMat = this.gaussianExample(rightMat);
            }

            if(disparityCheck.isSelected()){
                LeapCVStereoMatcher stereo = LeapCVStereoUtils.createMatcher(LeapCVMatcherType.STEREO_VAR);
                outMat = stereo.compute(leftMat, rightMat);
            }

            drawLeftProc(rightMat);
            drawRightProc(leftMat);

            drawOut(outMat);

            fps.setText(String.valueOf((int)this.fpsValue));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The leap motion is not connected");
        }
    }

    @FXML
    void onSnapButton(ActionEvent event) {
        Highgui.imwrite("/Volumes/macintosh_hdd/Users/daniel/Desktop/disp/dispSBGM/mugMatch3.png", outMat);

        //LeapCVStereoUtils utils = new LeapCVStereoUtils();

        //File file = new File("/Volumes/macintosh_hdd/Users/daniel/Desktop/outMat/dispSBGM/pc.obj");
        //utils.savePointCloud(utils.getPointCloud(outMat), file);
    }

    private void objectDetection() {
        leftMat = LeapCVImageUtils.gaussianBlur(leftMat);
        rightMat = LeapCVImageUtils.gaussianBlur(rightMat);

        //LeapCVObjectDetector objectDetector = new LeapCVObjectDetector();

        //Image image2 = LeapCVImageUtils.matToWritableImage(objectDetector.match(left, right));
        LeapCVObjectDetector obj = new LeapCVObjectDetector();
        outMat = obj.match(leftMat, rightMat);
    }

    void drawLeft(Mat image) {
        Mat out = new Mat();
        image.copyTo(out);
        Image outPut = LeapCVImageUtils.matToWritableImage(out, this.leftCanvas.getWidth(), this.leftCanvas.getHeight());
        this.leftCanvas.getGraphicsContext2D().drawImage(outPut, 0, 0);
    }

    void drawRight(Mat image) {
        Mat out = new Mat();
        image.copyTo(out);
        Image outPut = LeapCVImageUtils.matToWritableImage(out, this.rightCanvas.getWidth(), this.rightCanvas.getHeight());
        this.rightCanvas.getGraphicsContext2D().drawImage(outPut, 0, 0);
    }

    void drawLeftProc(Mat image) {
        Mat out = new Mat();
        image.copyTo(out);
        Image outPut = LeapCVImageUtils.matToWritableImage(out, this.leftProc.getWidth(), this.leftProc.getHeight());
        this.leftProc.getGraphicsContext2D().drawImage(outPut, 0, 0);
    }

    void drawRightProc(Mat image) {
        Mat out = new Mat();
        image.copyTo(out);
        Image outPut = LeapCVImageUtils.matToWritableImage(out, this.rightProc.getWidth(), this.rightProc.getHeight());
        this.rightProc.getGraphicsContext2D().drawImage(outPut, 0, 0);
    }

    void drawOut(Mat image) {
        Mat out = new Mat();
        image.copyTo(out);
        Image outPut = LeapCVImageUtils.matToWritableImage(out, this.outCanvas.getWidth(), this.outCanvas.getHeight());
        this.outCanvas.getGraphicsContext2D().drawImage(outPut, 0, 0);
    }

    Mat gaussianExample(Mat image) {
        LeapCVImageUtils.gaussianBlur(image);
        return image;
    }

    Mat medianExample(Mat image) {
        LeapCVImageUtils.medianBlur(image);
        return image;
    }

    Mat undistortExample() {
        return this.leapController.getLeftImageUndistorted();
    }

}
