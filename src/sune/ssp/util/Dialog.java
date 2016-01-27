package sune.ssp.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Dialog {
	
	private static long TIMEOUT = -1;
	public static void setTimeout(long timeout) {
		TIMEOUT = timeout;
	}
	
	private final static class DialogWindow {
		
		private final Alert alert;
		
		private volatile boolean running;
		private Thread thread = new Thread(() -> {
			long current = 0;
			while(running) {
				if(++current >= TIMEOUT) {
					hide();
					break;
				}
				Utils.sleep(1);
			}
		});
		
		public DialogWindow(AlertType type, String text, String title) {
			alert = new Alert(type);
			alert.setHeaderText(null);
			alert.setContentText(text);
			alert.setTitle(title);
		}
		
		public void setOwner(Stage stage) {
			alert.initModality(Modality.APPLICATION_MODAL);
			alert.initOwner(stage);
		}
		
		public void setButtons(ButtonType... buttons) {
			alert.getButtonTypes().clear();
			alert.getButtonTypes().addAll(buttons);
		}
		
		private void hide() {
			Platform.runLater(() -> {
				alert.setResult(ButtonType.CLOSE);
				alert.close();
			});
		}
		
		public void show() {
			if(TIMEOUT > -1) {
				running = true;
				thread.start();
			}
			alert.showAndWait();
			running = false;
		}
		
		public ButtonType getResult() {
			return alert.getResult();
		}
	}
	
	public static void showInfoDialog(Stage stage, String text, String title) {
		DialogWindow window = new DialogWindow(
			AlertType.INFORMATION, text, title);
		window.setOwner(stage);
		window.show();
	}
	
	public static ButtonType showQuestionDialog(Stage stage, String text,
			String title, ButtonType... buttons) {
		DialogWindow window = new DialogWindow(
			AlertType.CONFIRMATION, text, title);
		window.setButtons(buttons);
		window.setOwner(stage);
		window.show();
		return window.getResult();
	}
}