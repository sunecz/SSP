package gui;

import javax.swing.Timer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sune.etc.recorder.RecorderPanel;
import sune.etc.recorder.ScreenRecorder;

public class RecordTest extends Application {
	
	private static final String APP_TITLE = "Recorder Test";
	
	private Stage stage;
	private Scene scene;
	private BorderPane pane;
	
	private ScreenRecorder recorder;
	private RecorderPanel panel;
	
	private Timer timer;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		pane  = new BorderPane();
		scene = new Scene(pane, 800, 600);
		
		recorder = new ScreenRecorder();
		panel 	 = new RecorderPanel(recorder);
		pane.setCenter(panel);
		
		timer = new Timer(100, (e) -> {
			refreshTitle(panel.getFPS());
		});
		timer.setRepeats(true);
		timer.start();
		
		stage.setScene(scene);
		stage.setTitle(APP_TITLE);
		stage.setOnCloseRequest((event) -> stop());
		stage.setResizable(true);
		stage.centerOnScreen();
		stage.show();
		panel.start();
	}
	
	void refreshTitle(int fps) {
		if(!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> refreshTitle(fps));
			return;
		}
		stage.setTitle(APP_TITLE + " (" + fps + " FPS)");
	}
	
	@Override
	public void stop() {
		panel.stop();
		timer.stop();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}