package gui;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sune.ssp.Server;
import sune.ssp.data.FileInfoData;
import sune.ssp.data.Message;
import sune.ssp.event.ServerEvent;
import sune.ssp.file.FileSender;
import sune.ssp.file.TransferType;
import sune.ssp.logger.Logger;
import sune.ssp.logger.ThreadLogger;
import sune.ssp.secure.SecureServer;
import sune.ssp.secure.SimpleSession;
import sune.ssp.secure.SimpleSession.SessionKey;
import sune.ssp.util.AntiSpamProtection;
import sune.ssp.util.Formatter;
import sune.ssp.util.PortUtils;
import sune.ssp.util.Resource;
import sune.ssp.util.Utils;
import sune.test.matrix.Matrix;
import sune.util.crypt.Crypt;

public class WindowServer {
	
	private static final String WINDOW_TITLE 	= "SSP Server (Sune's Simple Protocol)";
	private static final Image 	WINDOW_ICON  	= Resource.image("/gui/icon.png");
	private static final String DEFAULT_ADDR 	= PortUtils.getLocalIpAddress();
	private static final int 	DEFAULT_PORT 	= 2400;
	private static final int 	DEFAULT_TIMEOUT = 8000;
	private Server server;
	
	private Stage stage;
	private Scene scene;
	private BorderPane pane;
	private VBox mainBox;
	private TextArea txtOutput;
	private TextField txtInput;
	private Button btnSend;
	
	private MenuBar menuBar;
	private Menu menuServer;
	private Menu menuFiles;
	private MenuItem menuItemStart;
	private MenuItem menuItemStop;
	private MenuItem menuItemSendFiles;
	
	private TableView<ClientTableInfo> tableClients;
	private Map<String, ClientTableInfo> clientInfos = new LinkedHashMap<>();
	
	private TableView<FileTableInfo> tableTransfers;
	private Map<String, FileSender> fileSenders  = new LinkedHashMap<>();
	private Map<String, FileTableInfo> fileInfos = new LinkedHashMap<>();
	private FileChooser fileChooser;
	
	private ContextMenu menuTransfers;
	private ContextMenu menuClients;
	private MenuItem menuItemTerminate;
	private MenuItem menuItemDisconnect;
	
	private Matrix matrix;
	
	private Logger logger = new ThreadLogger() {
		
		@Override
		public void printLog(String text) {
			Platform.runLater(() -> {
				txtOutput.appendText(text);
			});
		}
		
		@Override
		public void log(String string) {
			addLog(string + "\n");
		}
		
		@Override
		public void logf(String string, Object... args) {
			addLog("[INFO] " + String.format(string, args) + "\n");
		}
		
		@Override
		public void logerrf(String string, Object... args) {
			addLog("[ERROR] " + String.format(string, args) + "\n");
		}
	};
	
	private boolean correctlyPressed;
	private String selectedIPAddress = DEFAULT_ADDR;
	private int selectedPort 		 = DEFAULT_PORT;
	private int selectedTimeout		 = DEFAULT_TIMEOUT;
	
	@SuppressWarnings("unchecked")
	public WindowServer() {
		stage 	= new Stage();
		pane  	= new BorderPane();
		mainBox = new VBox(5);
		scene 	= new Scene(pane, 1000, 400);
		scene.getStylesheets().add(
			Resource.stylesheet("/gui/server.css"));
		
		txtOutput 	   = new TextArea();
		txtInput  	   = new TextField();
		btnSend   	   = new Button("Send");
		tableClients   = new TableView<>();
		tableTransfers = new TableView<>();
		
		menuBar 		  = new MenuBar();
		menuServer 		  = new Menu("Server");
		menuFiles 		  = new Menu("Files");
		menuItemStart     = new MenuItem("Start");
		menuItemStop	  = new MenuItem("Stop");
		menuItemSendFiles = new MenuItem("Send files");
		
		menuItemStart.setOnAction((event) -> server.start());
		menuItemStop.setOnAction((event)  -> server.stop());
		menuItemSendFiles.setOnAction((event) -> {
			List<File> files;
			if((files = fileChooser.showOpenMultipleDialog(stage)) != null) {
				if(!files.isEmpty()) {
					server.sendFiles(files.toArray(
						new File[files.size()]));
				}
			}
		});
		
		menuServer.getItems().addAll(menuItemStart, menuItemStop);
		menuFiles.getItems().addAll(menuItemSendFiles);
		menuBar.getMenus().addAll(menuServer, menuFiles);
		
		menuItemStop.setDisable(true);
		menuFiles.setDisable(true);
		
		TableColumn<ClientTableInfo, String> tc_colAddr = new TableColumn<>("IP address");
		TableColumn<ClientTableInfo, String> tc_colName = new TableColumn<>("Username");
		tc_colAddr.setPrefWidth(120.0);
		tc_colName.setPrefWidth(120.0);
		tc_colAddr.setCellValueFactory(new PropertyValueFactory<ClientTableInfo, String>("IP"));
		tc_colName.setCellValueFactory(new PropertyValueFactory<ClientTableInfo, String>("Username"));
		tableClients.getColumns().addAll(tc_colAddr, tc_colName);
		tableClients.setSortPolicy((param) -> { return false; });
		tableClients.setEditable(false);
		BorderPane.setMargin(tableClients, new Insets(10, 0, 10, 10));
		tableClients.setPlaceholder(new Label("No connected client."));
		
		TableColumn<FileTableInfo, String> tt_colName = new TableColumn<>("File");
		TableColumn<FileTableInfo, String> tt_colType = new TableColumn<>("Type");
		TableColumn<FileTableInfo, String> tt_colDesc = new TableColumn<>("Status");
		tt_colName.setPrefWidth(100.0);
		tt_colType.setPrefWidth(70.0);
		tt_colDesc.setPrefWidth(120.0);
		tt_colName.setCellValueFactory(new PropertyValueFactory<FileTableInfo, String>("Name"));
		tt_colType.setCellValueFactory(new PropertyValueFactory<FileTableInfo, String>("Type"));
		tt_colDesc.setCellValueFactory(new PropertyValueFactory<FileTableInfo, String>("Status"));
		tableTransfers.getColumns().addAll(tt_colName, tt_colType, tt_colDesc);
		tableTransfers.setSortPolicy((param) -> { return false; });
		tableTransfers.setEditable(false);
		BorderPane.setMargin(tableTransfers, new Insets(10, 10, 10, 0));
		tableTransfers.setPlaceholder(new Label("No transfer in progress."));
		
		fileChooser = new FileChooser();
		fileChooser.setTitle("Select a file");
		ExtensionFilter filterAll = new ExtensionFilter("All files (*.*)", "*.*");
		fileChooser.getExtensionFilters().add(filterAll);
		fileChooser.setSelectedExtensionFilter(filterAll);
		
		menuClients 	   = new ContextMenu();
		menuItemDisconnect = new MenuItem("Disconnect client");
		menuItemDisconnect.setOnAction((event) -> {
			SelectionModel<ClientTableInfo> sm = tableClients.getSelectionModel();
			ClientTableInfo clientInfo		   = sm.getSelectedItem();
			if(clientInfo != null) server.disconnect(clientInfo.getIP());
		});
		
		menuClients.getItems().addAll(menuItemDisconnect);
		menuClients.setAutoFix(true);
		menuClients.setAutoHide(true);
		
		tableClients.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
			if(event.getButton() == MouseButton.SECONDARY &&
			   !tableClients.getItems().isEmpty()) {
				SelectionModel<ClientTableInfo> sm = tableClients.getSelectionModel();
				if(!menuClients.isShowing() && sm.getSelectedItem() != null) {
					menuClients.show(
						stage,
						event.getScreenX(),
						event.getScreenY());
				}
			}
		});
		
		menuTransfers 	  = new ContextMenu();
		menuItemTerminate = new MenuItem("Terminate transfer");
		menuItemTerminate.setOnAction((event) -> {
			SelectionModel<FileTableInfo> sm = tableTransfers.getSelectionModel();
			FileTableInfo fileInfo			 = sm.getSelectedItem();
			
			if(fileInfo != null) {
				String hash = fileInfo.getHash();
				TransferType type = fileInfo.getTransferType();
				server.terminate(fileInfo.getSender(), hash, type);
				fileInfos.remove(hash);
				Platform.runLater(() -> {
					tableTransfers.getItems().remove(fileInfo);
				});
				logger.logf("File %s %s has been terminated!",
					Utils.fancyEnumName(type), fileInfo.getName());
			}
		});
		
		menuTransfers.getItems().addAll(menuItemTerminate);
		menuTransfers.setAutoFix(true);
		menuTransfers.setAutoHide(true);
		
		tableTransfers.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
			if(event.getButton() == MouseButton.SECONDARY &&
			   !tableTransfers.getItems().isEmpty()) {
				SelectionModel<FileTableInfo> sm = tableTransfers.getSelectionModel();
				if(!menuTransfers.isShowing() && sm.getSelectedItem() != null) {
					menuTransfers.show(
						stage,
						event.getScreenX(),
						event.getScreenY());
				}
			}
		});
		
		HBox box = new HBox(5);
		box.getChildren().addAll(txtInput, btnSend);
		menuBar.setPrefHeight(25);
		mainBox.getChildren().addAll(txtOutput, box);
		HBox hbox = new HBox(0);
		hbox.setPrefHeight(50);
		hbox.setMinHeight(Region.USE_PREF_SIZE);
		hbox.setMaxHeight(Region.USE_PREF_SIZE);
		matrix = new Matrix(0, 0, 10);
		matrix.setAutoResize(hbox);
		matrix.setOptimized(true);
		matrix.setHighlight(true);
		matrix.setMaxPath(2);
		matrix.start();
		hbox.getChildren().add(matrix);
		GridPane grid = new GridPane();
		grid.getChildren().addAll(hbox, tableClients, mainBox, tableTransfers);
		GridPane.setConstraints(hbox, 0, 0, 3, 1);
		GridPane.setConstraints(tableClients, 0, 1);
		GridPane.setConstraints(mainBox, 1, 1);
		GridPane.setConstraints(tableTransfers, 2, 1);
		GridPane.setHgrow(mainBox, Priority.ALWAYS);
		GridPane.setVgrow(mainBox, Priority.ALWAYS);
		GridPane.setVgrow(tableClients, Priority.ALWAYS);
		GridPane.setVgrow(tableTransfers, Priority.ALWAYS);
		GridPane.setMargin(tableClients, new Insets(10, 0, 10, 10));
		GridPane.setMargin(tableTransfers, new Insets(10, 10, 10, 0));
		pane.setTop(menuBar);
		pane.setCenter(grid);
		
		btnSend.setOnAction((event) -> sendMessage());
		txtInput.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				sendMessage();
			}
		});
		
		txtOutput.setEditable(false);
		txtOutput.setWrapText(true);
		Font f = new Font("Consolas", 12);
		txtOutput.setFont(f);
		txtInput.setFont(f);
		VBox.setVgrow(txtOutput, Priority.ALWAYS);
		HBox.setHgrow(txtInput, Priority.ALWAYS);
		
		mainBox.setPadding(new Insets(10));
		stage.setScene(scene);
		stage.setTitle(WINDOW_TITLE);
		stage.getIcons().add(WINDOW_ICON);
		stage.setOnCloseRequest((event) -> stop());
		stage.setResizable(false);
		stage.centerOnScreen();
		stage.show();
		
		txtInput.setPrefHeight(box.getHeight());
		txtInput.setMaxHeight(Region.USE_PREF_SIZE);
		txtInput.setMinHeight(Region.USE_PREF_SIZE);
		logger.init();
		
		if(showStartupDialog()) start(selectedPort);
		else 					stop();
	}
	
	public boolean showStartupDialog() {
		correctlyPressed = false;
		Stage stage   	= new Stage();
		GridPane pane 	= new GridPane();
		Scene scene   	= new Scene(pane, 300, 120);
		HBox boxBottom 	= new HBox(5);
		
		Label lblIPAddr 	= new Label("IP Address");
		Label lblPort 		= new Label("Port");
		Label lblTimeout	= new Label("Timeout");
		TextField txtIPAddr = new TextField();
		TextField txtPort 	= new TextField();
		MouseSlider timeout = new MouseSlider(0, 32000);
		Button btnStart		= new Button("Start");
		
		txtIPAddr.setText(selectedIPAddress);
		txtPort.setText(Integer.toString(selectedPort));
		txtIPAddr.setDisable(true);
		timeout.setValue(selectedTimeout);
		timeout.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
		timeout.setValueFormat("%.0f");
		timeout.setPadding(new Insets(-1));
		timeout.setBorder(new Border(new BorderStroke(
			Color.GRAY, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1))));
		timeout.setForegroundMargin(new Insets(1));
		
		class ActionStart {
			
			public void handle() {
				String textIP	= txtIPAddr.getText();
				String textPort = txtPort.getText();
				if(textIP 	!= null && !textIP.isEmpty() &&
				   textPort != null && !textPort.isEmpty()) {
					selectedIPAddress = textIP;
					selectedPort 	  = Integer.parseInt(textPort);
					selectedTimeout	  = (int) timeout.getValue();
					correctlyPressed  = true;
					stage.close();
				}
			}
		}
		ActionStart action = new ActionStart();
		btnStart.setOnAction((event) -> action.handle());
		btnStart.setOnKeyPressed((event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				action.handle();
			}
		});
		txtIPAddr.setOnKeyPressed((event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				action.handle();
			}
		});
		txtPort.setOnKeyPressed((event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				action.handle();
			}
		});
		
		boxBottom.getChildren().addAll(btnStart);
		boxBottom.setAlignment(Pos.BOTTOM_RIGHT);
		pane.setPadding(new Insets(10));
		pane.getChildren().addAll(
			lblIPAddr, txtIPAddr, lblPort, txtPort,
			lblTimeout, timeout, boxBottom);
		GridPane.setConstraints(lblIPAddr, 0, 0);
		GridPane.setConstraints(txtIPAddr, 1, 0);
		GridPane.setConstraints(lblPort, 0, 1);
		GridPane.setConstraints(txtPort, 1, 1);
		GridPane.setConstraints(lblTimeout, 0, 2);
		GridPane.setConstraints(timeout, 1, 2);
		GridPane.setConstraints(boxBottom, 0, 3, 2, 1);
		GridPane.setHgrow(txtIPAddr, Priority.ALWAYS);
		GridPane.setHgrow(txtPort, Priority.ALWAYS);
		GridPane.setHgrow(boxBottom, Priority.ALWAYS);
		GridPane.setVgrow(boxBottom, Priority.ALWAYS);
		GridPane.setMargin(lblIPAddr, new Insets(0, 15, 5, 0));
		GridPane.setMargin(txtIPAddr, new Insets(0, 0, 5, 0));
		GridPane.setMargin(lblPort, new Insets(0, 15, 5, 0));
		GridPane.setMargin(txtPort, new Insets(0, 0, 5, 0));
		btnStart.requestFocus();
		
		stage.setScene(scene);
		stage.setTitle("Start server");
		stage.getIcons().add(WINDOW_ICON);
		stage.setResizable(false);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(this.stage);
		stage.centerOnScreen();
		stage.showAndWait();
		return correctlyPressed;
	}
	
	public void start(int port) {
		server = SecureServer.create(port, "somerandompassword",
			Crypt.getAES(), SimpleSession.createSession(SessionKey.RSA_2048));
		server.setAntiSpamProtection(new AntiSpamProtection(400, 8));
		server.addListener(ServerEvent.CLIENT_CONNECTED, (client) -> {
			logger.logf("%s is trying to connect to the server!",
				client.getIP());
		});
		server.addListener(ServerEvent.CLIENT_INFO_RECEIVED, (info) -> {
			String clientIP			   = info.getSenderIP();
			String uuid			   	   = info.getUUID();
			ClientTableInfo clientInfo = new ClientTableInfo(
				clientIP, uuid, info.getUsername());
			clientInfos.put(clientIP, clientInfo);
			Platform.runLater(() -> {
				tableClients.getItems().add(clientInfo);
			});
			logger.logf("%s has been connected to the server!",
				info.getUsername());
		});
		server.addListener(ServerEvent.CLIENT_ALREADY_CONNECTED, (client) -> {
			logger.logf("%s has tried to connect to the server, but it is already connected!",
				client.getUsername());
		});
		server.addListener(ServerEvent.STARTED, (value) -> {
			Platform.runLater(() -> {
				menuItemStart.setDisable(true);
				menuItemStop.setDisable(false);
				menuFiles.setDisable(false);
			});
			logger.logf("Server on port %d has been started!",
				server.getPort());
		});
		server.addListener(ServerEvent.CANNOT_START, (value) -> {
			logger.logerrf("Cannot start server on port %d!",
				server.getPort());
		});
		server.addListener(ServerEvent.STOPPED, (value) -> {
			Platform.runLater(() -> {
				menuItemStart.setDisable(false);
				menuItemStop.setDisable(true);
				menuFiles.setDisable(true);
			});
			logger.logf("Server on port %d has been stopped!",
				server.getPort());
		});
		server.addListener(ServerEvent.CANNOT_STOP, (value) -> {
			logger.logerrf("Cannot stop server on port %d!",
				server.getPort());
		});
		server.addListener(ServerEvent.DATA_RECEIVED, (data) -> {
			if(data instanceof Message) {
				logger.log(Formatter.formatMessage((Message) data));
			} else if(data instanceof FileInfoData) {
				FileInfoData fi = (FileInfoData) data;
				String hash 	= fi.getHash();
				String name 	= fi.getName();
				long size 		= fi.getSize();
				String sender 	= fi.getUUID();
				FileTableInfo fInfo = new FileTableInfo(
					sender, hash, name, size, TransferType.RECEIVE);
				fileInfos.put(hash, fInfo);
				Platform.runLater(() -> {
					tableTransfers.getItems().add(fInfo);
				});
				logger.logf("File %s will be sent!", name);
			}
		});
		server.addListener(ServerEvent.FILE_DATA_RECEIVED, (data) -> {
			String hash = data.getHash();
			int length  = data.getLength();
			
			FileTableInfo fileInfo;
			if((fileInfo = fileInfos.get(hash)) != null) {
				fileInfo.update(
					fileInfo.getCurrentSize() + length);
				Platform.runLater(() -> {
					tableTransfers.refresh();
				});
			}
		});
		server.addListener(ServerEvent.FILE_SEND_BEGIN, (sender) -> {
			String fileName = sender.getFile().getName();
			String fileHash = sender.getHash();
			fileSenders.put(fileHash, sender);
			FileTableInfo fileInfo = new FileTableInfo(
				server.getIP(),
				fileHash,
				fileName,
				sender.getTotalSize(),
				TransferType.SEND);
			fileInfos.put(fileHash, fileInfo);
			Platform.runLater(() -> {
				tableTransfers.getItems().add(fileInfo);
			});
			logger.logf("File %s is being sent!", fileName);
		});
		server.addListener(ServerEvent.FILE_SEND_END, (sender) -> {
			String fileName = sender.getFile().getName();
			String fileHash = sender.getHash();
			logger.logf("File %s has been sent!", fileName);
			server.getAcceptedFiles().removeValue(fileHash);
			fileSenders.remove(fileHash);
			FileTableInfo fileInfo = fileInfos.get(fileHash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			fileInfos.remove(fileHash);
		});
		server.addListener(ServerEvent.FILE_DATA_SENT, (sender) -> {
			FileTableInfo fileInfo = fileInfos.get(sender.getHash());
			fileInfo.update(sender.getCurrentSize());
			Platform.runLater(() -> {
				tableTransfers.refresh();
			});
		});
		server.addListener(ServerEvent.FILE_RECEIVED, (value) -> {
			String hash 	  	   = value.getHash();
			FileTableInfo fileInfo = fileInfos.get(hash);
			fileInfos.remove(hash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			logger.logf("File %s has been received!", value.getName());
		});
		server.addListener(ServerEvent.FILE_RECEIVE_TERMINATED, (value) -> {
			String hash 	  	   = value.getHash();
			FileTableInfo fileInfo = fileInfos.get(hash);
			fileInfos.remove(hash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			logger.logf("File receiving %s has been terminated by user (%s)!",
				value.getName(), value.getSender());
		});
		server.addListener(ServerEvent.FILE_SEND_TERMINATED, (value) -> {
			logger.logf("File sending %s has been terminated for %s!",
				value.getFile().getName(), value.getSender().getSender());
			if(server.getTerminatedFiles().countForValue(value.getHash()) 
					== server.getClients().size()) {
				String hash 	  	   = value.getHash();
				FileTableInfo fileInfo = fileInfos.get(hash);
				server.getTerminatedFiles().removeValue(hash);
				fileInfos.remove(hash);
				Platform.runLater(() -> {
					tableTransfers.getItems().remove(fileInfo);
				});
			}
		});
		server.addListener(ServerEvent.CLIENT_DISCONNECTED, (value) -> {
			String clientIP 		   = value.getIP();
			String username			   = value.getUsername();
			ClientTableInfo clientInfo = clientInfos.get(clientIP);
			clientInfos.remove(clientIP);
			Platform.runLater(() -> {
				tableClients.getItems().remove(clientInfo);
			});
			
			for(int i = 0; i < tableTransfers.getItems().size(); i++) {
				try {
					FileTableInfo info = tableTransfers.getItems().get(i);
					if(info.getSender().equals(clientIP)) {
						final int index = i;
						Platform.runLater(() -> {
							tableTransfers.getItems().remove(index);
						});
						fileInfos.remove(info.getHash());
					}
				} catch(Exception ex) {
				}
			}
			
			logger.logf("%s has been disconnected!", username);
		});
		server.setTimeout(selectedTimeout);
		server.setServerName("Test Server");
		new Thread(() -> server.start()).start();
	}
	
	public void sendMessage() {
		if(!server.isRunning())
			return;
		
		String text;
		if((text = txtInput.getText()) != null && !text.isEmpty()) {
			Message message = new Message(text, server.getServerName());
			logger.log(Formatter.formatMessage(message));
			server.send(message);
			txtInput.setText("");
		}
	}
	
	public void stop() {
		if(server != null) server.stop();
		if(logger != null) logger.dispose();
		if(matrix != null) matrix.stop();
		if(stage  != null) stage.close();
	}
}