package gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

public class MouseSlider extends VBox {
	
	private double min;
	private double max;
	private double val;
	private double step;
	private double factor;
	
	private Pane mainPane;
	private Pane paneValue;
	private Label lblValue;
	private Paint foreground;
	private Paint background;
	private String valueFormat;
	
	private Insets paneValueMargin;
	
	public MouseSlider(double min, double max) {
		this(min, max, min, 1.0);
	}
	
	public MouseSlider(double min, double max, double val) {
		this(min, max, val, 1.0);
	}
	
	public MouseSlider(double min, double max, double val, double step) {
		this.min  = min;
		this.max  = max;
		this.step = step;
		mainPane  = new Pane();
		paneValue = new Pane();
		lblValue  = new Label();
		paneValueMargin = new Insets(0);
		paneValue.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		paneValue.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		valueFormat = "%.2f";
		setValue(val);
		setBackground(Color.WHITE);
		widthProperty().addListener((o) -> update(true));
		heightProperty().addListener((o) -> update(true));
		addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> setValueByX(event.getX()));
		addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> setValueByX(event.getX()));
		visibleProperty().addListener((o) -> { if(isVisible()) update(true); });
		mainPane.getChildren().addAll(paneValue, lblValue);
		getChildren().add(mainPane);
	}
	
	private final void update(boolean updateBack) {
		double width  = getWidth();
		double height = getHeight();
		if(updateBack) updateForeground(width, height);
		paneValue.setPrefSize(
			width*factor - paneValueMargin.getLeft() - paneValueMargin.getRight(),
			height 		 - paneValueMargin.getTop()  - paneValueMargin.getBottom());
		double offx = paneValueMargin.getLeft();
		double offy = paneValueMargin.getTop();
		if(offx != 0) paneValue.setTranslateX(offx);
		if(offy != 0) paneValue.setTranslateY(offy);
		lblValue.setText(String.format(valueFormat, val));
		lblValue.setLayoutX((width - lblValue.getWidth()) / 2.0);
		lblValue.setLayoutY((height - lblValue.getHeight()) / 2.0);
	}
	
	private final void updateForeground(double width, double height) {
		paneValue.setBackground(new Background(new BackgroundFill(
			foreground != null ? foreground :
			new LinearGradient(0.0, 0.0, width, height, false, CycleMethod.NO_CYCLE,
				new Stop(0.1, Color.LIGHTBLUE.deriveColor(-9.0, 1.0, 1.0, 1.0)),
				new Stop(0.5, Color.LIGHTBLUE.deriveColor(-3.0, 1.2, 1.0, 1.0)),
				new Stop(0.9, Color.LIGHTBLUE.deriveColor(+0.0, 1.5, 0.9, 1.0))),
			CornerRadii.EMPTY, new Insets(0))));
	}
	
	public void refresh() {
		update(true);
	}
	
	public void setBackground(Paint paint) {
		setBackground(new Background(
			new BackgroundFill(
				background = paint,
				CornerRadii.EMPTY,
				new Insets(0))));
	}
	
	public void setForeground(Paint paint) {
		foreground = paint;
		updateForeground(getWidth(), getHeight());
	}
	
	private final void setValueByX(double x) {
		setValue((x / getWidth()) * max);
	}
	
	public void setValue(double value) {
		if(value % step != 0) 
			value = Math.floor(value / step) * step;
		val    = Math.max(min, Math.min(value, max));
		factor = (val - min) / (max - min);
		update(false);
	}
	
	public Paint getSliderBackground() {
		return background;
	}
	
	public Paint getSliderForeground() {
		return foreground;
	}
	
	public void setValueFormat(String format) {
		valueFormat = format;
	}
	
	public void setFont(Font font) {
		lblValue.setFont(font);
		update(false);
	}
	
	public void setStep(double step) {
		this.step = step;
	}
	
	public void setLabelVisible(boolean value) {
		lblValue.setVisible(value);
	}
	
	public void setForegroundMargin(Insets margin) {
		paneValueMargin = margin;
		update(true);
	}
	
	public double getValue() {
		return val;
	}
	
	public double getMinimum() {
		return min;
	}
	
	public double getMaximum() {
		return max;
	}
}