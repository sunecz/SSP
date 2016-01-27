package sune.ssp.editor;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Whisperer extends VBox {
	
	private static final List<Item0> EMPTY_ITEM_LIST = new ArrayList<>();
	
	private static final String STYLE_CLASS_NAME 	   = "editor-whisperer";
	private static final String STYLE_CLASS_NAME_ITEM  = "editor-item";
	private static final String STYLE_CLASS_NAME_HOVER = "hover";
	
	private static final Label LABEL_NO_OPTION;
	static {
		LABEL_NO_OPTION = new Label("No option available");
		LABEL_NO_OPTION.setId("label-no-option");
	}
	
	public static class Item {
		
		private final String text;
		// Events
		private EventHandler<? super ActionEvent> actionPerformed;
		
		public Item(String text) {
			this.text = text;
		}
		
		public void setOnAction(
				EventHandler<? super ActionEvent> listener) {
			actionPerformed = listener;
		}
		
		protected void fireAction(ActionEvent event) {
			if(actionPerformed != null) {
				actionPerformed.handle(event);
			}
		}
		
		public String getText() {
			return text;
		}
	}
	
	public static class InsertItem extends Item {
		
		private final String insert;
		private final int caret;
		
		public InsertItem(String text, String insert) {
			this(text, insert, -1);
		}
		
		public InsertItem(String text, String insert, int caret) {
			super(text);
			this.insert = insert;
			this.caret  = caret;
		}
		
		public String getInsert() {
			return insert;
		}
		
		public int getCaret() {
			return caret;
		}
	}
	
	public static class InsertCondItem extends InsertItem {
		
		private final String before;
		public InsertCondItem(String text, String insert, String before) {
			this(text, insert, before, -1);
		}
		
		public InsertCondItem(String text, String insert, String before, int caret) {
			super(text, insert, caret);
			this.before = before;
		}
		
		public String getBefore() {
			return before;
		}
	}
	
	public static class InsertTabItem extends InsertItem {
		
		private int[] carets;
		private int current;
		
		public InsertTabItem(String text, String insert, int... carets) {
			super(text, insert, carets[0]);
			this.carets  = carets;
			this.current = 0;
		}
		
		public void reset() {
			current = 0;
		}
		
		public int getNextCaret() {
			return ++current < carets.length ?
				carets[current] : -1;
		}
		
		public int getCurrentCaret() {
			return carets[current];
		}
	}
	
	public static class InsertTabCondItem extends InsertTabItem {
		
		private final String before;
		public InsertTabCondItem(String text, String insert, String before, int... carets) {
			super(text, insert, carets);
			this.before = before;
		}
		
		public String getBefore() {
			return before;
		}
	}
	
	public static class Item0 {
		
		public final Item item;
		public final int trim;
		
		public Item0(Item item, int trim) {
			if(item == null) {
				throw new IllegalArgumentException(
					"Item cannot be null!");
			}
			this.item = item;
			this.trim = trim;
		}
	}
	
	private final ObservableList<Item> items;
	private final ObservableList<Item0> currentItems;
	
	private int selectedIndex  = -1;
	private Item0 selectedItem = null;
	
	private Runnable onItemSelected;
	void setOnItemSelected(Runnable listener) {
		onItemSelected = listener;
	}
	
	public Whisperer() {
		super();
		getStyleClass().add(STYLE_CLASS_NAME);
		currentItems = FXCollections.observableList(new ArrayList<>());
		items 		 = FXCollections.observableList(new ArrayList<>());
	}
	
	static List<Item0> castItemList(List<? extends Item> list) {
		List<Item0> nlist = new ArrayList<>();
		for(Item item : list) nlist.add(new Item0(item, 0));
		return nlist;
	}
	
	synchronized void updateList(List<Item0> list) {
		currentItems.clear();
		getChildren().clear();
		selectedIndex = -1;
		selectedItem  = null;
		if(!list.isEmpty()) {
			for(Item0 item0 : list) {
				getChildren().add(
					createItem(item0));
				currentItems.add(item0);
			}
			select(0);
		} else {
			getChildren().add(LABEL_NO_OPTION);
		}
	}
	
	synchronized void clearList() {
		updateList(EMPTY_ITEM_LIST);
	}
	
	HBox createItem(Item0 item0) {
		Item item = item0.item;
		HBox box  = new HBox();
		box.getStyleClass().add(STYLE_CLASS_NAME_ITEM);
		box.setOnMouseClicked((event) -> {
			if(event.getClickCount() <= 1)
				return;
			item.fireAction(null);
			if(onItemSelected != null)
				onItemSelected.run();
		});
		box.setOnMouseEntered((event) -> {
			select(currentItems.indexOf(item0));
		});
		Label label = new Label(item.getText());
		box.getChildren().add(label);
		return box;
	}
	
	void selectItem(Item0 item) {
		if(item == null) return;
		item.item.fireAction(null);
		if(onItemSelected != null)
			onItemSelected.run();
	}
	
	void selectCurrentItem() {
		selectItem(selectedItem);
	}
	
	public synchronized void select(int index) {
		int length = currentItems.size();
		if(length == 0 || index < -1 || index >= length)
			return;
		int prev 	  = selectedIndex;
		selectedIndex = index;
		selectedItem  = currentItems.get(index);
		if(prev > -1 && prev < length) {
			getChildren().get(prev).getStyleClass()
				.remove(STYLE_CLASS_NAME_HOVER);
		}
		getChildren().get(index).getStyleClass()
			.add(STYLE_CLASS_NAME_HOVER);
	}
	
	public void selectPrevious() {
		int index = selectedIndex-1;
		if(index > -1) {
			select(index);
		}
	}
	
	public void selectNext() {
		int index = selectedIndex+1;
		if(index < currentItems.size()) {
			select(index);
		}
	}
	
	public Item0 getSelectedItem() {
		return selectedItem;
	}
	
	public ObservableList<Item> getItems() {
		return items;
	}
	
	public void translate(double x, double y) {
		Scene scene = getScene();
		double sw = scene.getWidth();
		double sh = scene.getHeight();
		double bw = getWidth();
		double bh = getHeight();
		setTranslateX(x+bw > sw ? sw-bw : x);
		setTranslateY(y+bh > sh ? sh-bh : y);
	}
	
	public void show() {
		setVisible(true);
	}
	
	public void hide() {
		setVisible(false);
	}
}