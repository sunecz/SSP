package gui;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sune.ssp.Client;
import sune.ssp.data.Data;
import sune.ssp.data.FileInfoData;
import sune.ssp.data.Message;
import sune.ssp.etc.DataList;
import sune.ssp.etc.IPAddress;
import sune.ssp.etc.ServerClientInfo;
import sune.ssp.event.ClientEvent;
import sune.ssp.file.FileSender;
import sune.ssp.file.FileWriter;
import sune.ssp.file.TransferType;
import sune.ssp.logger.Logger;
import sune.ssp.logger.ThreadLogger;
import sune.ssp.secure.SecureClient;
import sune.ssp.util.Dialog;
import sune.ssp.util.Formatter;
import sune.ssp.util.PortUtils;
import sune.ssp.util.Randomizer;
import sune.ssp.util.Resource;
import sune.ssp.util.UnitHelper;
import sune.ssp.util.Utils;
import sune.ssp.util.Waiter;
import sune.test.matrix.Matrix;

public class WindowClient {
	
	private static final String WINDOW_TITLE 	 = "SSP Client (Sune's Simple Protocol)";
	private static final Image 	WINDOW_ICON  	 = Resource.image("/gui/icon.png");
	private static final String DEFAULT_ADDR 	 = PortUtils.getLocalIpAddress();
	private static final int 	DEFAULT_PORT 	 = 2400;
	private static final int 	DEFAULT_TIMEOUT  = 8000;
	private static final String DEFAULT_USERNAME = "username" + Randomizer.nextPositiveLong();
	private Client client;
	
	private Stage stage;
	private Scene scene;
	private BorderPane pane;
	private VBox mainBox;
	private TextArea txtOutput;
	private TextField txtInput;
	private Button btnSend;
	
	private MenuBar menuBar;
	private Menu menuClient;
	private Menu menuFiles;
	private MenuItem menuItemConnect;
	private MenuItem menuItemDisconnect;
	private MenuItem menuItemSendFiles;
	
	private TableView<FileTableInfo> tableTransfers;
	private Map<String, FileSender> fileSenders  = new LinkedHashMap<>();
	private Map<String, FileTableInfo> fileInfos = new LinkedHashMap<>();
	private Map<String, FileWriter> fileSavers 	 = new LinkedHashMap<>();
	private FileChooser fileChooserSend;
	private FileChooser fileChooserSave;
	
	private ContextMenu menuTransfers;
	private MenuItem menuItemTerminate;
	
	private TableView<ClientTableInfo> tableClients;
	private List<ClientTableInfo> clientsInfo = new ArrayList<>();
	
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
	private String selectedUsername	 = DEFAULT_USERNAME;
	
	private List<Runnable> runOnClose = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	public WindowClient() {
		stage 	= new Stage();
		pane  	= new BorderPane();
		mainBox = new VBox(5);
		scene 	= new Scene(pane, 1000, 400);
		scene.getStylesheets().add(
			Resource.stylesheet("/gui/client.css"));
		
		txtOutput 	   = new TextArea();
		txtInput  	   = new TextField();
		btnSend	  	   = new Button("Send");
		tableTransfers = new TableView<>();
		tableClients   = new TableView<>();
		
		menuBar 		   = new MenuBar();
		menuClient 		   = new Menu("Client");
		menuFiles 		   = new Menu("Files");
		menuItemConnect    = new MenuItem("Connect");
		menuItemDisconnect = new MenuItem("Disconnect");
		menuItemSendFiles  = new MenuItem("Send files");
		
		menuItemConnect.setOnAction((event) -> {
			new Thread(() -> {
				Platform.runLater(() -> {
					if(showStartupDialog()) {
						start(selectedIPAddress, selectedPort);
					}
				});
			}).start();
		});
		menuItemDisconnect.setOnAction((event) -> client.disconnect());
		menuItemSendFiles.setOnAction((event)  -> {
			List<File> files;
			if((files = fileChooserSend.showOpenMultipleDialog(stage)) != null) {
				if(!files.isEmpty()) {
					client.sendFiles(files.toArray(
						new File[files.size()]));
				}
			}
		});
		
		menuClient.getItems().addAll(menuItemConnect, menuItemDisconnect);
		menuFiles.getItems().addAll(menuItemSendFiles);
		menuBar.getMenus().addAll(menuClient, menuFiles);
		
		menuItemDisconnect.setDisable(true);
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
		
		fileChooserSend = new FileChooser();
		fileChooserSend.setTitle("Select a file");
		ExtensionFilter filterAll0 = new ExtensionFilter("All files (*.*)", "*.*");
		fileChooserSend.getExtensionFilters().add(filterAll0);
		fileChooserSend.setSelectedExtensionFilter(filterAll0);
		
		fileChooserSave = new FileChooser();
		fileChooserSave.setTitle("Save a file");
		ExtensionFilter filterAll1 = new ExtensionFilter("All files (*.*)", "*.*");
		fileChooserSave.getExtensionFilters().add(filterAll1);
		fileChooserSave.setSelectedExtensionFilter(filterAll1);
		
		menuTransfers 	  = new ContextMenu();
		menuItemTerminate = new MenuItem("Terminate transfer");
		menuItemTerminate.setOnAction((event) -> {
			SelectionModel<FileTableInfo> sm = tableTransfers.getSelectionModel();
			FileTableInfo fileInfo			= sm.getSelectedItem();
			
			if(fileInfo != null) {
				String hash = fileInfo.getHash();
				client.terminate(hash, fileInfo.getTransferType());
				fileInfos.remove(hash);
				Platform.runLater(() -> {
					tableTransfers.getItems().remove(fileInfo);
				});
				logger.logf("File %s %s has been terminated!",
					Utils.fancyEnumName(fileInfo.getTransferType()),
					fileInfo.getName());
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
		mainBox.getChildren().addAll(txtOutput, box);
		pane.setCenter(mainBox);
		pane.setTop(menuBar);
		pane.setLeft(tableClients);
		pane.setRight(tableTransfers);
		
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
		
		if(showStartupDialog()) start(selectedIPAddress, selectedPort);
		else 					stop();
	}
	
	enum ClientType {
		SECURED, NORMAL;
		
		@Override
		public String toString() {
			return Utils.fancyEnumName(this);
		}
	}
	
	public boolean showStartupDialog() {
		correctlyPressed = false;		
		Stage stage   	= new Stage();
		StackPane box 	= new StackPane();
		GridPane pane 	= new GridPane();
		Scene scene   	= new Scene(box, 300, 210);
		HBox boxBottom 	= new HBox(5);
		HBox boxTop 	= new HBox(5);
		
		Label lblType		  = new Label("Type");
		Label lblIPAddr 	  = new Label("IP Address");
		Label lblPort 		  = new Label("Port");
		Label lblTimeout	  = new Label("Timeout");
		Label lblUsername	  = new Label("Username");
		TextField txtIPAddr   = new TextField();
		TextField txtPort 	  = new NumberTextField();
		TextField txtUsername = new TextField();
		MouseSlider timeout   = new MouseSlider(0, 32000);
		Button btnStart		  = new Button("Connect");
		ComboBox<ClientType> cmbType = new ScrollComboBox<>();
		cmbType.getItems().addAll(ClientType.values());
		cmbType.setMaxWidth(Double.MAX_VALUE);
		
		Matrix matrix = new Matrix(250, 25, 10);
		matrix.setAutoResize(boxTop);
		
		txtIPAddr.textProperty().addListener((o) -> {
			String text = txtIPAddr.getText();
			if(text.length() > 15) {
				txtIPAddr.setText(
					text = text.substring(0, 15));
			}
			txtIPAddr.setBackground(new Background(
				new BackgroundFill(
					Color.web(
						IPAddress.isValidIPv4(text) ?
							"#9CFF91" : "#FF9393"),
					CornerRadii.EMPTY,
					new Insets(0)
				)
			));
		});
		
		txtPort.textProperty().addListener((o) -> {
			String text = txtPort.getText();
			if(text.length() > 5) {
				txtPort.setText(
					text = text.substring(0, 5));
			}
			try {
				txtPort.setBackground(new Background(
					new BackgroundFill(
						Color.web(
							IPAddress.isValidCustomPort(
								Integer.parseInt(text)) ?
								"#9CFF91" : "#FF9393"),
						CornerRadii.EMPTY,
						new Insets(0)
					)
				));
			} catch(Exception ex) {
			}
		});
		
		cmbType.getSelectionModel().selectedItemProperty().addListener((o) -> {
			if(cmbType.getSelectionModel().getSelectedItem() == ClientType.SECURED) {
				boxTop.setVisible(true);
				matrix.start();
			} else {
				boxTop.setVisible(false);
				matrix.stop();
			}
		});
		
		txtIPAddr.setText(selectedIPAddress);
		txtPort.setText(Integer.toString(selectedPort));
		txtUsername.setText(selectedUsername);
		cmbType.getSelectionModel().select(ClientType.SECURED);
		cmbType.setDisable(true);
		timeout.setValue(selectedTimeout);
		timeout.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
		timeout.setValueFormat("%.0fms");
		timeout.setPadding(new Insets(-1));
		timeout.setBorder(new Border(new BorderStroke(
			Color.GRAY, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1))));
		timeout.setForegroundMargin(new Insets(1));
		
		class ActionStart {
			
			public void handle() {
				String textIP		= txtIPAddr.getText();
				String textPort 	= txtPort.getText();
				String textUsername = txtUsername.getText();
				if(textIP 		!= null && !textIP.isEmpty()   &&
				   textPort 	!= null && !textPort.isEmpty() &&
				   textUsername != null && !textUsername.isEmpty()) {
					selectedIPAddress = textIP;
					selectedPort 	  = Integer.parseInt(textPort);
					selectedTimeout	  = (int) timeout.getValue();
					selectedUsername  = textUsername;
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
				event.consume();
			}
		});
		txtIPAddr.setOnKeyPressed((event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				action.handle();
				event.consume();
			}
		});
		txtPort.setOnKeyPressed((event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				action.handle();
				event.consume();
			}
		});
		txtUsername.setOnKeyPressed((event) -> {
			if(event.getCode() == KeyCode.ENTER) {
				action.handle();
				event.consume();
			}
		});
		
		pane.setBackground(new Background(
			new BackgroundFill(
				Color.WHITE.deriveColor(0.0, 1.0, 1.0, 0.5),
				CornerRadii.EMPTY, new Insets(0)
			)
		));
		box.getChildren().addAll(boxTop, pane);
		boxTop.getChildren().add(matrix);
		
		boxBottom.getChildren().addAll(btnStart);
		boxBottom.setAlignment(Pos.BOTTOM_RIGHT);
		pane.setPadding(new Insets(20));
		pane.getChildren().addAll(
			lblType, cmbType, lblIPAddr, txtIPAddr,
			lblPort, txtPort, lblTimeout, timeout,
			lblUsername, txtUsername, boxBottom);
		GridPane.setConstraints(lblType, 0, 0);
		GridPane.setConstraints(cmbType, 1, 0);
		GridPane.setConstraints(lblIPAddr, 0, 1);
		GridPane.setConstraints(txtIPAddr, 1, 1);
		GridPane.setConstraints(lblPort, 0, 2);
		GridPane.setConstraints(txtPort, 1, 2);
		GridPane.setConstraints(lblTimeout, 0, 3);
		GridPane.setConstraints(timeout, 1, 3);
		GridPane.setConstraints(lblUsername, 0, 4);
		GridPane.setConstraints(txtUsername, 1, 4);
		GridPane.setConstraints(boxBottom, 0, 5, 2, 1);
		GridPane.setHgrow(cmbType, Priority.ALWAYS);
		GridPane.setHgrow(txtIPAddr, Priority.ALWAYS);
		GridPane.setHgrow(txtPort, Priority.ALWAYS);
		GridPane.setHgrow(boxBottom, Priority.ALWAYS);
		GridPane.setVgrow(boxBottom, Priority.ALWAYS);
		GridPane.setMargin(lblType, new Insets(0, 15, 5, 0));
		GridPane.setMargin(cmbType, new Insets(0, 0, 5, 0));
		GridPane.setMargin(lblIPAddr, new Insets(0, 15, 5, 0));
		GridPane.setMargin(txtIPAddr, new Insets(0, 0, 5, 0));
		GridPane.setMargin(lblPort, new Insets(0, 15, 5, 0));
		GridPane.setMargin(txtPort, new Insets(0, 0, 5, 0));
		GridPane.setMargin(lblTimeout, new Insets(0, 15, 5, 0));
		GridPane.setMargin(timeout, new Insets(0, 0, 5, 0));
		GridPane.setMargin(lblUsername, new Insets(0, 15, 5, 0));
		GridPane.setMargin(txtUsername, new Insets(0, 0, 5, 0));
		btnStart.requestFocus();
		
		stage.setScene(scene);
		stage.setTitle("Connect to a server");
		stage.getIcons().add(WINDOW_ICON);
		stage.setResizable(false);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(this.stage);
		runOnClose.add(() -> {
			if(matrix != null) {
				matrix.stop();
			}
		});
		stage.centerOnScreen();
		stage.showAndWait();
		return correctlyPressed;
	}
	
	@SuppressWarnings("unchecked")
	public void start(String ipAddress, int port) {
		client = SecureClient.create(ipAddress, port);
		client.setUsername(selectedUsername);
		client.setPromptToReceiveFile(true);
		client.addListener(ClientEvent.CONNECTED, (value) -> {
			Platform.runLater(() -> {
				menuItemConnect.setDisable(true);
				menuItemDisconnect.setDisable(false);
				menuFiles.setDisable(false);
			});
			logger.logf("Client has been connected to %s:%d!",
				ipAddress, port);
		});
		client.addListener(ClientEvent.ALREADY_CONNECTED, (value) -> {
			Platform.runLater(() -> {
				menuItemConnect.setDisable(false);
				menuItemDisconnect.setDisable(true);
				menuFiles.setDisable(true);
			});
			logger.logerrf("Client is already connected!");
		});
		client.addListener(ClientEvent.CONNECTION_TIMEOUT, (value) -> {
			Platform.runLater(() -> {
				menuItemConnect.setDisable(false);
				menuItemDisconnect.setDisable(true);
				menuFiles.setDisable(true);
			});
			logger.logerrf("Timed out while connecting to server %s:%d!",
				ipAddress, port);
		});
		client.addListener(ClientEvent.CANNOT_CONNECT, (value) -> {
			logger.logerrf("Cannot connect to server %s:%d!",
				ipAddress, port);
		});
		client.addListener(ClientEvent.DISCONNECTED, (value) -> {
			Platform.runLater(() -> {
				menuItemConnect.setDisable(false);
				menuItemDisconnect.setDisable(true);
				menuFiles.setDisable(true);
			});
			fileInfos.clear();
			Platform.runLater(() -> {
				tableTransfers.getItems().clear();
				tableTransfers.refresh();
				tableClients.getItems().clear();
				tableClients.refresh();
			});
			logger.logf("Client has been disconnected from %s:%d!", 
				ipAddress, port);
		});
		client.addListener(ClientEvent.CANNOT_DISCONNECT, (value) -> {
			logger.logerrf("Cannot disconnect from %s:%d!", 
				ipAddress, port);
		});
		client.addListener(ClientEvent.PROMPT_RECEIVE_FILE, (dataWaiter) -> {
			Platform.runLater(() -> {
				Data data = dataWaiter.getData().cast();
				if(data instanceof FileInfoData) {
					FileInfoData fi = (FileInfoData) data;
					String hash 	= fi.getHash();
					String name 	= fi.getName();
					String sender	= fi.getUUID();
					long totalSize	= fi.getSize();
					long waitTime 	= fi.getWaitTime();
					Waiter waiter 	= dataWaiter.getWaiter();
					Dialog.setTimeout(waitTime);
					String username =
						sender.equals(client.getServerIdentificator()
											.getUUID()
											.toString()) ?
							fi.getSenderIP() : null;
					for(ClientTableInfo info : clientsInfo) {
						if(info.getUUID().equals(sender)) {
							username = info.getUsername();
							break;
						}
					}
					ButtonType result = Dialog.showQuestionDialog(
						stage,
						String.format(
							"Would you like to receive file %s (%s) from %s?",
							name, UnitHelper.formatSize(totalSize, 2), username),
						"Receive file",
						ButtonType.YES,
						ButtonType.NO);
					Dialog.setTimeout(-1);
					if(result == ButtonType.YES) {
						waiter.setWaitingState(true);
						fileChooserSave.setInitialFileName(name);
						
						File file;
						if((file = fileChooserSave.showSaveDialog(stage)) != null) {
							fileSavers.put(hash, FileWriter.create(file, fi.getSize()));
							waiter.setWaitingState(false);
							waiter.accept();
						} else {
							waiter.setWaitingState(false);
							waiter.refuse();
							FileTableInfo ftInfo = fileInfos.get(hash);
							fileInfos.remove(hash);
							tableTransfers.getItems().remove(ftInfo);
						}
					} else {
						waiter.refuse();
						FileTableInfo ftInfo = fileInfos.get(hash);
						fileInfos.remove(hash);
						tableTransfers.getItems().remove(ftInfo);
					}
				}
			});
		});
		client.addListener(ClientEvent.DATA_RECEIVED, (data) -> {
			if(data instanceof DataList) {
				DataList<?> list = (DataList<?>) data;
				if(list.isTypeOf(ServerClientInfo.class)) {
					updateClients((DataList<ServerClientInfo>) list);
				}
			} else if(data instanceof Message) {
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
		client.addListener(ClientEvent.FILE_DATA_RECEIVED, (data) -> {
			String hash 	 = data.getHash();
			int length  	 = data.getLength();
			byte[] rawData   = data.getRawData();
			FileWriter saver = fileSavers.get(hash);
			saver.write(rawData);
			
			FileTableInfo fileInfo;
			if((fileInfo = fileInfos.get(hash)) != null) {
				fileInfo.update(
					fileInfo.getCurrentSize() + length);
				Platform.runLater(() -> {
					tableTransfers.refresh();
				});
			}
		});
		client.addListener(ClientEvent.FILE_SEND_BEGIN, (sender) -> {
			String fileName = sender.getFile().getName();
			String fileHash = sender.getHash();
			fileSenders.put(fileHash, sender);
			FileTableInfo fileInfo = new FileTableInfo(
				client.getIP(),
				fileHash,
				fileName,
				sender.getTotalSize(),
				TransferType.SEND);
			fileInfos.put(fileHash, fileInfo);
			Platform.runLater(() -> {
				tableTransfers.getItems().add(fileInfo);
			});
			logger.logf("Sending file %s...", fileName);
		});
		client.addListener(ClientEvent.FILE_SEND_END, (sender) -> {
			String fileName = sender.getFile().getName();
			String fileHash = sender.getHash();
			logger.logf("File %s has been sent!", fileName);
			fileSenders.remove(fileHash);
			FileTableInfo fileInfo = fileInfos.get(fileHash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			fileInfos.remove(fileHash);
		});
		client.addListener(ClientEvent.FILE_DATA_SENT, (sender) -> {
			FileTableInfo fileInfo = fileInfos.get(sender.getHash());
			fileInfo.update(sender.getCurrentSize());
			Platform.runLater(() -> {
				tableTransfers.refresh();
			});
		});
		client.addListener(ClientEvent.FILE_RECEIVED, (value) -> {
			String hash 	  	   = value.getHash();
			FileTableInfo fileInfo = fileInfos.get(hash);
			fileSavers.get(hash).close();
			fileSavers.remove(hash);
			fileInfos.remove(hash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			logger.logf("File %s has been received!", value.getName());
		});
		client.addListener(ClientEvent.FILE_RECEIVE_TERMINATED, (value) -> {
			String hash 	  = value.getHash();
			FileTableInfo fileInfo = fileInfos.get(hash);
			fileInfos.remove(hash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			logger.logf("File receiving %s has been terminated!",
				value.getName());
		});
		client.addListener(ClientEvent.FILE_SEND_TERMINATED, (value) -> {
			String hash 	  = value.getHash();
			FileTableInfo fileInfo = fileInfos.get(hash);
			fileInfos.remove(hash);
			Platform.runLater(() -> {
				tableTransfers.getItems().remove(fileInfo);
			});
			logger.logf("File sending %s has been terminated by server!",
				value.getFile().getName());
		});
		client.setTimeout(selectedTimeout);
		new Thread(() -> {
			logger.logf("Connecting to %s:%d...",
				ipAddress, port);
			client.connect();
		}).start();
	}
	
	public void sendMessage() {
		if(!client.isConnected())
			return;
		
		String text;
		if((text = txtInput.getText()) != null && !text.isEmpty()) {
			Message message = new Message(text, client.getUsername());
			logger.log(Formatter.formatMessage(message));
			client.send(message);
			txtInput.setText("");
		}
	}
	
	public void updateClients(DataList<ServerClientInfo> list) {
		ObservableList<ClientTableInfo> tableItems
			= tableClients.getItems();
		tableItems.clear();
		clientsInfo.clear();
		for(Serializable s : list.getData()) {
			if(s instanceof ServerClientInfo) {
				ServerClientInfo info
					= (ServerClientInfo) s;
				ClientTableInfo ci = new ClientTableInfo(
					info.getIP(),
					info.getUUID(),
					info.getUsername());
				tableItems.add(ci);
				clientsInfo.add(ci);
			}
		}
	}
	
	public void stop() {
		for(Runnable r : runOnClose) r.run();
		if(client != null) client.disconnect();
		if(logger != null) logger.dispose();
		if(stage  != null) stage.close();
	}
}