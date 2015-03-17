package resources;

import java.awt.image.BufferedImage;
import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

import com.leapcv.*;

public class MainController {

	 	@FXML
	    private Canvas image;
	 	@FXML
	    private Canvas image2;

	    @FXML
	    private TextField lambda;

	    @FXML
	    private TextField fi;

	    @FXML
	    private TextField minDisp;

	    @FXML
	    private TextField polySigma;

	    @FXML
	    private TextField polyN;

	    @FXML
	    private TextField levels;

	    @FXML
	    private TextField maxDisp;

	    @FXML
	    private TextField cycle;
	    
	    @FXML
	    private Button snap;

	    @FXML
	    private TextField pyrScale;

	private LeapCVController leapController;
	private LeapCVStereoUtils leapStereo;

	@SuppressWarnings("deprecation")
	public void initialize() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		try {
			this.leapController = new LeapCVController();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.leapStereo = new LeapCVStereoUtils();
		
		Platform.runLater(()->{
			this.fi.setText(String.valueOf(this.leapStereo.getFi()));
			this.lambda.setText(String.valueOf(this.leapStereo.getLambda()));
			this.levels.setText(String.valueOf(this.leapStereo.getLevels()));
			this.maxDisp.setText(String.valueOf(this.leapStereo.getMaxDisp()));
			this.minDisp.setText(String.valueOf(this.leapStereo.getMinDisp()));
			this.polyN.setText(String.valueOf(this.leapStereo.getPolyN()));
			this.polySigma.setText(String.valueOf(this.leapStereo.getPolySigma()));
			this.pyrScale.setText(String.valueOf(this.leapStereo.getPyrScale()));
			this.cycle.setText(String.valueOf(this.leapStereo.getCycle()));
			
		});

		AnimationTimer timer = new AnimationTimer() {
			
			@Override
			public void handle(long now) {
				// TODO Auto-generated method stub
				onSnapTaken(new ActionEvent());
			}
		};
		
		timer.start();
		
		
	}

	@FXML
	void onSnapTaken(ActionEvent event) {
		Rect roi = new Rect(160,160,320,320);
		
		this.leapStereo.setFi(Double.valueOf(this.fi.getText()));
		this.leapStereo.setLambda(Double.valueOf(this.lambda.getText()));
		this.leapStereo.setLevels(Double.valueOf(this.levels.getText()));
		this.leapStereo.setMaxDisp(Double.valueOf(this.maxDisp.getText()));
		this.leapStereo.setMinDisp(Double.valueOf(this.minDisp.getText()));
		this.leapStereo.setPolyN(Double.valueOf(this.polyN.getText()));
		this.leapStereo.setPolySigma(Double.valueOf(this.polySigma.getText()));
		this.leapStereo.setPyrScale(Double.valueOf(this.pyrScale.getText()));
		this.leapStereo.setCycle(Double.valueOf(this.cycle.getText()));
		
		//this.fi.setText(String.valueOf(this.leapStereo.getFi()));
//		this.lambda.setText(String.valueOf(this.leapStereo.getLambda()));
//		this.levels.setText(String.valueOf(this.leapStereo.getLevels()));
//		this.maxDisp.setText(String.valueOf(this.leapStereo.getMaxDisp()));
//		this.minDisp.setText(String.valueOf(this.leapStereo.getMinDisp()));
//		this.polyN.setText(String.valueOf(this.leapStereo.getPolyN()));
//		this.polySigma.setText(String.valueOf(this.leapStereo.getPolySigma()));
//		this.pyrScale.setText(String.valueOf(this.leapStereo.getPyrScale()));
		try{
			this.leapController.nextValidFrame();
			
			Mat left = this.leapController.getLeftImageUndistorted();//Undistorted();
			
			Mat right = Highgui.imread("match.png");//= this.leapController.getRightImageUndistorted();//Undistorted();
			//LeapCVObjectDetector.sift(left);
			
			//LeapCVObjectDetector.sift(right);
			//Mat image = this.leapStereo.getDisparityMap(left.submat(roi), right.submat(roi));
			
			
			//Highgui.imwrite("outl.jpg", this.leapController.getLeftImageUndistorted().submat(roi));
			//Highgui.imwrite("outr.jpg", this.leapController.getRightImageUndistorted().submat(roi));
			
			//this.leapStereo.savePointCloud(this.leapStereo.getPointCloud(image), new File("Pointcloud.obj"));
			
			//Highgui.imwrite("match.png", this.leapController.getRightImageUndistorted());
			//System.exit(0);
			
			Image image2 = LeapCVImageUtils.matToWritableImage(LeapCVObjectDetector.match(left.submat(roi), right));//.submat(roi)));
			this.image.getGraphicsContext2D().drawImage(image2, 0, 0);
//			image2 = LeapCVImageUtils.matToWritableImage(right.submat(roi));
//			this.image2.getGraphicsContext2D().drawImage(image2, 0, 0);
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("The leap motion is not connected");
		}
	}

}
