package sune.ssp.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.beans.Observable;
import javafx.geometry.Point2D;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sune.ssp.editor.Whisperer.InsertCondItem;
import sune.ssp.editor.Whisperer.InsertItem;
import sune.ssp.editor.Whisperer.InsertTabItem;
import sune.ssp.editor.Whisperer.Item;
import sune.ssp.editor.Whisperer.Item0;

public class Editor extends TextArea {
	
	private final Whisperer whisperer;
	private FontMetrics metrics;
	
	private boolean tabCaretMode;
	private int tabModeCaretPos;
	private Item0 tabModeItem;
	
	static boolean isDivider(char c) {
		return c != ' ' && !(Character.isAlphabetic(c) || Character.isDigit(c));
	}
	
	public Editor(Whisperer whisperer) {
		super();
		setWrapText(true);
		getStyleClass().add("editor-input");
		addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
			switch(event.getCode()) {
				case ESCAPE:
					if(tabCaretMode) {
						disableTabMode();
					}
					whisperer.hide();
					break;
				case SPACE:
					if(event.isControlDown()) {
						whisperer.setVisible(
							!whisperer.isVisible());
					}
					break;
				case UP:
					if(whisperer.isVisible()) {
						whisperer.selectPrevious();
						event.consume();
					}
					break;
				case DOWN:
					if(whisperer.isVisible()) {
						whisperer.selectNext();
						event.consume();
					}
					break;
				case ENTER:
					if(whisperer.isVisible()) {
						whisperer.selectCurrentItem();
						event.consume();
					}
					break;
				case TAB:
					if(tabCaretMode) {
						if(tabModeItem != null) {
							Item item = tabModeItem.item;
							if(item instanceof InsertTabItem) {
								InsertTabItem tabItem = (InsertTabItem) item;
								int current = tabItem.getCurrentCaret();
								int next 	= tabItem.getNextCaret();
								if(next == -1) {
									disableTabMode();
								} else {
									positionCaret(
										getCaretPosition() - current + next);
									tabModeCaretPos = next;
									event.consume();
								}
							}
						}
					} else {
						if(whisperer.isVisible()) {
							whisperer.selectCurrentItem();
							event.consume();
						}
					}
					break;
				default:
					break;
			}
		});
		addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(whisperer.isVisible()) {
				switch(event.getCode()) {
					case UP:
					case DOWN:
					case ENTER:
						event.consume();
						break;
					default:
						update();
						break;
				}
			} else {
				if(event.getCode() == KeyCode.ENTER) {
					int currentLine = getCurrentLine();
					int currentCol  = getCurrentColumn();
					String line 	= getLineText(currentLine-1);
					String before 	= line.substring(0, currentCol-1);
					if(before.trim().isEmpty()) {
						insertAtCaret(
							getLineTabIndent(currentLine-2));
					}
				}
			}
		});
		caretPositionProperty().addListener((o) -> {
			if(tabModeCaretPos != -1 &&
			   getCaretPosition() < tabModeCaretPos) {
				disableTabMode();
			}
		});
		whisperer.getChildren().addListener((Observable o) -> {
			relocateWhisperer();
		});
		whisperer.visibleProperty().addListener((o) -> {
			if(whisperer.isVisible()) {
				if(metrics == null) {
					metrics = Toolkit.getToolkit()
						.getFontLoader().getFontMetrics(getFont());
				}
				whisperer.select(0);
				relocateWhisperer();
			} else {
				whisperer.clearList();
			}
		});
		whisperer.setOnItemSelected(() -> {
			Item0 item0 = whisperer.getSelectedItem();
			Item item   = item0.item;
			if(item instanceof InsertItem) {
				InsertItem iitem = ((InsertItem) item);
				String text = iitem.getInsert();
				int trimLength = item0.trim;
				if(trimLength > 0) {
					text = text.substring(trimLength);
				}
				insertAtCaret(text);
				int caret = iitem.getCaret();
				if(caret > -1) {
					positionCaret(
						getCaretPosition()
						-iitem.getInsert().length()+caret);
				}
				if(item instanceof InsertTabItem) {
					tabCaretMode 	= true;
					tabModeCaretPos = getCaretPosition();
					tabModeItem 	= item0;
				}
			}
			whisperer.hide();
		});
		this.whisperer = whisperer;
	}
	
	void disableTabMode() {
		tabCaretMode 	= false;
		tabModeCaretPos = -1;
		((InsertTabItem) tabModeItem.item).reset();
		tabModeItem		= null;
	}
	
	final synchronized void update() {
		int caret 	   = getCaretPosition();
		String text    = "";
		String before  = "";
		int trimLength = 0;
		String current = getText();
		if(caret > 0 && caret <= current.length()) {
			int pos = 0;
			char[] chars = current.toCharArray();
			for(int i = caret-1; i > -1; --i) {
				char c = chars[i];
				if(c == '\n' || c == '\r') {
					pos = i != 0 ? i+1 : -1;
					break;
				}
				if((i == 0) || (isDivider(c) &&
				   (i > 0 && !isDivider(chars[i-1])))) {
					pos = i;
					break;
				}
			}
			if(pos == -1) text = "";
			else 		  text = current.substring(pos, caret);
			text 	   = text.replace("\t", "");
			trimLength = text.length();
			int posb = 0;
			for(int i = caret-1; i > -1; --i) {
				char c = chars[i];
				if(c == '\n' || c == '\r') {
					posb = i+1;
					break;
				}
			}
			before = current.substring(posb, caret);
		}
		List<Item0> priority = new ArrayList<>();
		List<Item0> list 	 = new ArrayList<>();
		boolean emptyText 	 = text.isEmpty();
		for(Item item : whisperer.getItems()) {
			if(item instanceof InsertItem) {
				boolean add   = true;
				String insert = ((InsertItem) item).getInsert();
				if(!insert.startsWith(text) && !insert.endsWith(text)) {
					add = false;
				}
				boolean isCond = false;
				if(item instanceof InsertCondItem) {
					isCond = true;
					String befText  = ((InsertCondItem) item).getBefore();
					Pattern pattern = Pattern.compile(
						"^(?:.*?)" + befText + "$",
						Pattern.CASE_INSENSITIVE);
					if(pattern.matcher(before).matches()) {
						priority.add(new Item0(item, 0));
						continue;
					}
				}
				if(!isCond && (add || emptyText)) {
					list.add(new Item0(item, trimLength));
				}
			}
		}
		list.addAll(0, priority);
		whisperer.updateList(list);
	}
	
	public void relocateWhisperer() {
		if(!whisperer.isVisible()) return;
		Point2D point = Utils2.getNodeLocation(
        	Utils2.getCaretNode(this));
        double x = point.getX();
        double y = point.getY();
        String text = getText();
		if(text != null && !text.isEmpty()) {
			text = text.substring(text.length()-1);
		}
		x += metrics.computeStringWidth(text);
		y += metrics.getLineHeight();
		if(y+whisperer.getHeight() > getHeight()) {
			y -= metrics.getLineHeight();
			y -= whisperer.getHeight();
		}
		whisperer.translate(x, y);
	}
	
	public void insertAtCaret(String text) {
		insertText(getCaretPosition(), text);
	}
	
	public void removeAtCaret(int length) {
		if(length <= 0) return;
		StringBuilder sb = new StringBuilder();
		String text = getText();
		int caret = getCaretPosition();
		sb.append(text.substring(0, caret));
		int end = caret+length;
		if(end <= text.length())
			sb.append(text.substring(caret+length));
		setText(sb.toString());
		positionCaret(caret);
	}
	
	public String currentLineText() {
		return getLineText(getCurrentLine()-1);
	}
	
	public String getLineTabIndent(int line) {
		String text = getLineText(line);
		if(text.startsWith("\t")) {
			StringBuilder sb = new StringBuilder();
			char[] chars 	 = text.toCharArray();
			for(int i = 0, l = chars.length; i < l; ++i) {
				char c = chars[i];
				if(c == '\t') sb.append('\t');
				else		  break;
			}
			return sb.toString();
		}
		return "";
	}
	
	public String getLineText(int line) {
		char[] chars = getText().toCharArray();
		if(chars.length <= 0) return null;
		StringBuilder sb = new StringBuilder();
		for(int i = 0, l = chars.length, k = line; i < l; ++i) {
			char c = chars[i];
			if(c == '\n' || c == '\r') --k;
			if(k == 0) 				   sb.append(c);
		}
		return sb.toString().replaceAll("[\n|\r]", "");
	}
	
	public int getCurrentLine() {
		int counter  = 0;
		char[] chars = getText().toCharArray();
		if(chars.length <= 0) return 1;
		int max = Math.min(chars.length,
			getCaretPosition());
		for(int i = 0; i < max; ++i) {
			char c = chars[i];
			if(c == '\n' || c == '\r') {
				++counter;
			}
		}
		return counter+1;
	}
	
	public int getCurrentColumn() {
		int counter  = 0;
		char[] chars = getText().toCharArray();
		if(chars.length <= 0) return 1;
		for(int i = getCaretPosition()-1;
				i > -1; --i, ++counter) {
			char c = chars[i];
			if(c == '\n' || c == '\r') {
				break;
			}
		}
		return counter+1;
	}
	
	public int getSelectionStart() {
		return getSelection().getStart()+1;
	}
	
	public int getSelectionEnd() {
		return getSelection().getEnd()+1;
	}
}