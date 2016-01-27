// SSP - Sune's Simple Protocol
package gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	static boolean CLIENT;
	static boolean SERVER;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		//if(SERVER) new WindowServer();
		if(CLIENT) new WindowClient();
	}
	
	public static void main(String[] args) {
		CLIENT = !(args.length > 0 && !args[0].equalsIgnoreCase("client"));
		SERVER = !(args.length > 0 && !args[0].equalsIgnoreCase("server"));
		launch(args);
	}
}