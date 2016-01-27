package sune.ssp.editor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sune.ssp.editor.Whisperer.InsertCondItem;
import sune.ssp.editor.Whisperer.InsertItem;
import sune.ssp.editor.Whisperer.InsertTabItem;
import sune.ssp.editor.Whisperer.Item;
import sune.ssp.util.Resource;

public class Window extends Application {
	
	private Stage stage;
	private Scene scene;
	private Pane pane0;
	private BorderPane pane;
	private Editor editor;
	private Whisperer whisperer;
	
	private MenuBar menuBar;
	private Menu menuEdit;
	private MenuItem menuItemUndo;
	private MenuItem menuItemRedo;
	
	private HBox boxBottom;
	private Label lblPosition;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		pane0 = new Pane();
		pane  = new BorderPane();
		scene = new Scene(pane0, 600, 400);
		scene.getStylesheets().add(
			Resource.stylesheet("/sune/ssp/editor/editor.css"));
		
		whisperer = new Whisperer();
		editor 	  = new Editor(whisperer);
		
		menuBar 	 = new MenuBar();
		menuEdit 	 = new Menu("Edit");
		menuItemUndo = new MenuItem("Undo changes");
		menuItemRedo = new MenuItem("Redo changes");
		
		menuItemUndo.setOnAction((event) -> {
			if(editor.isUndoable()) {
				editor.undo();
				menuItemUndo.setDisable(
					!editor.isUndoable());
			}
		});
		
		menuItemRedo.setOnAction((event) -> {
			if(editor.isRedoable()) {
				editor.redo();
				menuItemRedo.setDisable(
					!editor.isRedoable());
			}
		});
		
		editor.textProperty().addListener((o) -> {
			menuItemUndo.setDisable(!editor.isUndoable());
			menuItemRedo.setDisable(!editor.isRedoable());
		});
		
		menuItemUndo.setDisable(true);
		menuItemRedo.setDisable(true);
		menuEdit.getItems().addAll(menuItemUndo, menuItemRedo);
		menuBar.getMenus().addAll(menuEdit);
		
		boxBottom   = new HBox(5);
		lblPosition = new Label();
		
		whisperer.hide();
		whisperer.visibleProperty().addListener((o) -> {
			editor.relocateWhisperer();
		});
		
		Item[] availableItems = {
			new InsertItem(
				"Send message",
				"send::Message(\"\")", 15),
			new InsertItem(
				"Send status",
				"send::Status(Status::)", 21),
			new InsertTabItem(
				"Prompt to variable",
				"prompt::Dialog(\"\", var::)", 16, 24),
			new InsertItem(
				"Define variable",
				"define(var::)", 12),
			new InsertItem(
				"Require variable",
				"require(var::)", 13),
			new InsertTabItem(
				"Set variable",
				"set(var::, )", 9, 11),
			new InsertTabItem(
				"Simple condition",
				"if  then\n\t\nend", 3, 10),
			// Statuses
			new InsertCondItem(
				"Status 'Disconnected by server'",
				"DISCONNECTED_BY_SERVER",
				"Status::"),
			new InsertCondItem(
				"Status 'Disconnected by user'",
				"DISCONNECTED_BY_USER",
				"Status::"),
			new InsertCondItem(
				"Status 'Disconnected by spam'",
				"DISCONNECTED_BY_SPAM",
				"Status::"),
			new InsertCondItem(
				"Status 'Sucessfully connected'",
				"SUCCESSFULLY_CONNECTED",
				"Status::"),
			new InsertCondItem(
				"Status 'Client already connected'",
				"CLIENT_ALREADY_CONNECTED",
				"Status::"),
			new InsertCondItem(
				"Status 'Server stopped'",
				"SERVER_STOPPED",
				"Status::"),
		};
		whisperer.getItems().addAll(availableItems);
		editor.caretPositionProperty().addListener((o) -> {
			updatePosition();
		});
		
		boxBottom.setAlignment(Pos.CENTER_RIGHT);
		boxBottom.getChildren().add(lblPosition);
		
		BorderPane.setMargin(editor, new Insets(10));
		boxBottom.setPadding(new Insets(-5, 10, 5, 10));
		
		pane.setTop(menuBar);
		pane.setCenter(editor);
		pane.setBottom(boxBottom);
		pane0.getChildren().addAll(pane, whisperer);
		
		scene.widthProperty().addListener((o) -> resize());
		scene.heightProperty().addListener((o) -> resize());
		
		stage.setScene(scene);
		stage.setTitle("SSP Editor");
		stage.getIcons().add(
			Resource.image("/gui/icon.png"));
		stage.centerOnScreen();
		stage.show();
		resize();
		updatePosition();
	}
	
	void resize() {
		pane0.setPrefSize(scene.getWidth(), scene.getHeight());
		pane.setPrefSize(pane0.getWidth(), pane0.getHeight());
	}
	
	void updatePosition() {
		lblPosition.setText(String.format(
			"line: %d, col: %d",
			editor.getCurrentLine(),
			editor.getCurrentColumn()));
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}