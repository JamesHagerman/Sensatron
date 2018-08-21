package org.aardvark.processing.sensatron;

import processing.core.PApplet;

public class DisplayControlWidgetFactory {

	public static RainDisplayControlWidget createWidget(RainDisplay display, PApplet parent, int x, int y) {
		return new RainDisplayControlWidget(x, y, display, parent);
	}
	
	public static ColorEqDisplayControlWidget createWidget(ColorEqDisplay display, PApplet parent, int x, int y) {
		return new ColorEqDisplayControlWidget(x, y, display, parent);
	}
	
	public static DisplayControlWidget<? extends LightDisplay> createWidget(LightDisplay display, PApplet parent, int x, int y) {
		if (display instanceof ColorEqDisplay) {
			return new ColorEqDisplayControlWidget(x, y, (ColorEqDisplay) display, parent);
		} else if (display instanceof RainDisplay) {
			return new RainDisplayControlWidget(x, y, (RainDisplay) display, parent);
		} else if (display instanceof OverlayDisplay) {
			return new OverlayDisplayControlWidget(x, y, (OverlayDisplay) display, parent);
		} else {
			return new EmptyControlWidget(x, y, display, parent);
		}
	}
	
}
