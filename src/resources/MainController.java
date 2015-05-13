package resources;

import com.leapcv.LeapCVController;
import com.leapcv.LeapCVObjectDetector;
import com.leapcv.utils.LeapCVImageUtils;
import com.leapcv.utils.LeapCVMatcherType;
import com.leapcv.utils.LeapCVStereoMatcher;
import com.leapcv.utils.LeapCVStereoUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

import java.io.File;

public class MainController {

    Mat leftMat;
    Mat rightMat;
    Mat outMat;
    private double fpsValue;

    private final String STEREO_VAR = "Stereo Variation";
    private final String STEREO_SGBM = "StereoSGBM";
    private final String STEREO_BM = "StereoBM";

    @FXML
    private Canvas leftCanvas;
    @FXML
    private Canvas objectDetectCanvas;
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
    private CheckBox detectionCheck;

    @FXML
    private ChoiceBox disparityType;
    @FXML
    private Button snap;

    @FXML
    private Label fps;
    @FXML
    private Button pointBtn;

    private LeapCVController leapController;

    @SuppressWarnings("deprecation")
    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            this.leapController = new LeapCVController();

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.disparityType.getItems().addAll(STEREO_VAR, STEREO_SGBM, STEREO_BM);
        this.disparityType.getSelectionModel().selectFirst();

		Platform.runLater(() -> {
            pointBtn.setVisible(false);
            disparityType.setVisible(false);
            outCanvas.setVisible(false);
            objectDetectCanvas.setVisible(false);
        });

        //UI Timer for image refresh
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

            //Run based on selected UI checkboxes
            if (undist.isSelected()) {
                leftMat = this.leapController.getLeftImageUndistorted();
                rightMat = this.leapController.getRightImageUndistorted();
            }

            //Median blur
            if (med.isSelected()) {
                leftMat = this.medianExample(leftMat);
                rightMat = this.medianExample(rightMat);
            }

            //Gaussian blur
            if (gauss.isSelected()) {
                leftMat = this.gaussianExample(leftMat);
                rightMat = this.gaussianExample(rightMat);
            }

            //Disparity map
            if(disparityCheck.isSelected()){
                LeapCVStereoMatcher stereo = null;
                //Select stereo algorithm type from factory
                switch((String)this.disparityType.getValue()){
                    case STEREO_VAR:
                        stereo = LeapCVStereoUtils.createMatcher(LeapCVMatcherType.STEREO_VAR);
                        break;
                    case STEREO_SGBM:
                        stereo = LeapCVStereoUtils.createMatcher(LeapCVMatcherType.STEREO_SGBM);
                        break;
                    case STEREO_BM:
                        stereo = LeapCVStereoUtils.createMatcher(LeapCVMatcherType.STEREO_BM);
                        break;
                }

                outMat = stereo.compute(rightMat, leftMat);

                //  Set visible items for disparity Map but only do it once
                if (!pointBtn.isVisible()){
                    pointBtn.setVisible(true);
                    disparityType.setVisible(true);
                    outCanvas.setVisible(true);
                }

            } else {
                if (pointBtn.isVisible()) {
                    pointBtn.setVisible(false);
                    disparityType.setVisible(false);
                    outCanvas.setVisible(false);
                }
            }

            drawLeftProc(rightMat);
            drawRightProc(leftMat);

            drawOut(outMat);

            //objectDetection
            if(detectionCheck.isSelected()) {

                LeapCVObjectDetector det = new LeapCVObjectDetector();
                drawObj(det.match(leftMat, Highgui.imread("/Volumes/macintosh_hdd/Users/daniel/Desktop/leapcv_op/toMatch.png")));
                if(!objectDetectCanvas.isVisible()){
                    objectDetectCanvas.setVisible(true);
                }

            } else {
                if(objectDetectCanvas.isVisible()){
                    objectDetectCanvas.setVisible(false);
                }
            }





            fps.setText(String.valueOf((int) this.fpsValue));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The leap motion is not connected");
        }
    }

    @FXML
    void onSnapButton(ActionEvent event) {

        Highgui.imwrite("/Volumes/macintosh_hdd/Users/daniel/Desktop/leapcv_op/toMatch.png", LeapCVImageUtils.crop(leftMat, 0.2));

        //LeapCVStereoUtils utils = new LeapCVStereoUtils();

        //File file = new File("/Volumes/macintosh_hdd/Users/daniel/Desktop/outMat/dispSBGM/pc.obj");
        //utils.savePointCloud(utils.getPointCloud(outMat), file);
    }

    @FXML
    void onPointButton(ActionEvent event) {
        File toSave = new File("/Volumes/macintosh_hdd/Users/daniel/Desktop/leapcv_op/pointCloud.obj");
        Mat pointCloud = new Mat();

        Highgui.imwrite("/Volumes/macintosh_hdd/Users/daniel/Desktop/leapcv_op/pointcloud.bmp", leftMat);

        System.out.println("Width: " + leftMat.width() + " Height: " + leftMat.height());
        pointCloud = LeapCVStereoUtils.getPointCloud(outMat);
        LeapCVStereoUtils.savePointCloud(pointCloud, toSave);
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

    void drawObj(Mat image) {
        Mat out = new Mat();
        image.copyTo(out);
        Image outPut = LeapCVImageUtils.matToWritableImage(out, this.objectDetectCanvas.getWidth(), this.objectDetectCanvas.getHeight());
        this.objectDetectCanvas.getGraphicsContext2D().drawImage(outPut, 0, 0);
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
