/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package gui;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class NumberTextField extends TextField {
	
	public NumberTextField() {
		this("");
	}
	
	public NumberTextField(String text) {
		super(text);
		addEventFilter(KeyEvent.KEY_TYPED, (event) -> {
			char c = event.getCharacter().charAt(0);
			if(!((c >= '0' && c <= '9') || (getSelection().getStart() == 0 && c == '-') ||
			   (getText().length() > 0 && c == '.' && !getText().contains("."))) ||
			   (getText().length() >= getPrefColumnCount() && getSelection().getLength() == 0))
				event.consume();
		});
	}
}