// SSP - Sune's Simple Protocol
package gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	// TODO: Check and occasionally improve the security logic
	// TODO: If possible, improve the security logic speed
	// TODO: Add support for Datagram Sockets
	// TODO: Add support for P2P clients
	// TODO: Check and occasionally improve the event system
	// TODO: Implement a way for custom compression, hashing, crypting
	// TODO: Comment all the (public/protected) methods and variables
	// TODO: Instead of IP Address as an identificator of client, use UUID
	// TODO: Make Reliable Protocol implementation
	// 			- Sender receives response about the sent data
	//			- Aka: Was the data received, or not?
	// TODO: Fix bug with sending files from server to a client
	
	static boolean CLIENT;
	static boolean SERVER;
	
	WindowServer server;
	WindowClient client;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		if(SERVER) server = new WindowServer();
		if(CLIENT) client = new WindowClient();
	}
	
	@Override
	public void stop() throws Exception {
		if(client != null) client.stop();
		if(server != null) server.stop();
	}
	
	public static void main(String[] args) {
		CLIENT = !(args.length > 0 && !args[0].equalsIgnoreCase("client"));
		SERVER = !(args.length > 0 && !args[0].equalsIgnoreCase("server"));
		launch(args);
	}
}