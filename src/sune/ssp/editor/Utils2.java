package sune.ssp.editor;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.Path;

public class Utils2 {
	
	/* This code has been taken from:
	 * https://community.oracle.com/thread/2534556?tstart=0
	 * AND WAS MODIFIED!*/
	public static final Node getCaretNode(Parent parent) {
		for(Node n : parent.getChildrenUnmodifiable()) {
			if(n instanceof Path) return n; else
			if(n instanceof Parent) {
				Node k = getCaretNode((Parent) n);
				if(k != null) return k;
			}
		}
		return null;
	}
	
	/* This code has been taken from:
	 * https://community.oracle.com/thread/2534556?tstart=0
	 * AND WAS MODIFIED!*/
	public static final Point2D getNodeLocation(Node node) {
		double x = 0, y = 0;
		for(Node n = node; n != null; n = n.getParent()) {
			Bounds parentBounds = n.getBoundsInParent();
			x += parentBounds.getMinX();
			y += parentBounds.getMinY();
		}
		return new Point2D(x, y);
	}
}