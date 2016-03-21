package gui;

import javafx.scene.control.ComboBox;
import javafx.scene.input.ScrollEvent;

public class ScrollComboBox<T> extends ComboBox<T> {
	
	private int index;
	
	public ScrollComboBox() {
		super();
		addEventHandler(ScrollEvent.SCROLL, (event) -> {
			if(isFocused()) {
				if(event.getDeltaY() > 0) index--;
				else					  index++;
				index = Math.max(0, Math.min(index, getItems().size()-1));
				getSelectionModel().select(index);
			}
		});
		
		setOnAction((event) -> {
			index = getSelectionModel().getSelectedIndex();
		});
	}
}