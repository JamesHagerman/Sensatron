package org.aardvark.processing.sensatron;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.aardvark.processing.Slider;
import org.aardvark.processing.delegate.ActionDelegate;

import processing.core.PApplet;

public class ColorEqDisplayControlWidget extends DisplayControlWidget<ColorEqDisplay> {
	
	private float[] topColorHSB = new float[] {1, 1, 1};
	private float[] bottomColorHSB = new float[] {1, 1, 1};
	
	Slider topHueSlider;
	Slider topSaturationSlider;
	Slider topBrightnessSlider;
	Slider bottomHueSlider;
	Slider bottomSaturationSlider;
	Slider bottomBrightnessSlider;
	
	public ColorEqDisplayControlWidget(int x, int y, ColorEqDisplay display, PApplet parent) {
		super(x, y, display, parent);
		
		topHueSlider = new Slider(parent, x + 20, y + 20, 100, 1);
		topSaturationSlider = new Slider(parent, x + 20, y + 50, 100, 1);
		topBrightnessSlider = new Slider(parent, x + 20, y + 80, 100, 1);
		bottomHueSlider = new Slider(parent, x + 20, y + 110, 100, 1);
		bottomSaturationSlider = new Slider(parent, x + 20, y + 140, 100, 1);
		bottomBrightnessSlider = new Slider(parent, x + 20, y + 170, 100, 1);
		
		topHueSlider.setLabel("Color 2 Hue");
		topSaturationSlider.setLabel("Color 2 Saturation");
		topBrightnessSlider.setLabel("Color 2 Brightness");
		bottomHueSlider.setLabel("Color 1 Hue");
		bottomSaturationSlider.setLabel("Color 1 Saturation");
		bottomBrightnessSlider.setLabel("Color 1 Brightness");
		
		if (display != null) {
			Color topColor = new Color(display.getTopColor());
			Color bottomColor = new Color(display.getBottomColor());
			Color.RGBtoHSB(topColor.getRed(), topColor.getGreen(), topColor.getBlue(), topColorHSB);
			Color.RGBtoHSB(bottomColor.getRed(), bottomColor.getGreen(), bottomColor.getBlue(), bottomColorHSB);
			topHueSlider.setHandlePosition(0, topColorHSB[0]);
			topSaturationSlider.setHandlePosition(0, topColorHSB[1]);
			topBrightnessSlider.setHandlePosition(0, display.getInactiveBrightness() / 255d);
			bottomHueSlider.setHandlePosition(0, bottomColorHSB[0]);
			bottomSaturationSlider.setHandlePosition(0, bottomColorHSB[1]);
			bottomBrightnessSlider.setHandlePosition(0, display.getActiveBrightness() / 255d);
		}
		
		topHueSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				setTopHue(topHueSlider.getNormalizedPositions()[0]);
			}
		});
		topSaturationSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				setTopSaturation(topSaturationSlider.getNormalizedPositions()[0]);
			}
		});
		topBrightnessSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				setTopBrightness(topBrightnessSlider.getNormalizedPositions()[0]);
			}
		});
		bottomHueSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				float position = bottomHueSlider.getNormalizedPositions()[0];
				setBottomHue(position);
//				System.out.println("Hue is " + position);
			}
		});
		bottomSaturationSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				float position = bottomSaturationSlider.getNormalizedPositions()[0];
				setBottomSaturation(position);
//				System.out.println("Saturation is " + position);
			}
		});
		bottomBrightnessSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				setBottomBrightness(bottomBrightnessSlider.getNormalizedPositions()[0]);
			}
		});
	}
	
	public void drawMaximized() {
	    parent.stroke(0);
	    parent.fill(0, 0);
	    parent.rect(x, y, WIDTH, HEIGHT);

		topHueSlider.draw();
		topSaturationSlider.draw();
		topBrightnessSlider.draw();
		bottomHueSlider.draw();
		bottomSaturationSlider.draw();
		bottomBrightnessSlider.draw();
		
		parent.stroke(0);
		parent.fill(Color.HSBtoRGB(topColorHSB[0], topColorHSB[1], topColorHSB[2]));
		parent.rect(x+140, y+20, 50, 50);

		parent.fill(Color.HSBtoRGB(bottomColorHSB[0], bottomColorHSB[1], bottomColorHSB[2]));
		parent.rect(x+140, y+120, 50, 50);

	}

	public void keyEvent(KeyEvent event) {
		topHueSlider.keyEvent(event);
		topSaturationSlider.keyEvent(event);
		topBrightnessSlider.keyEvent(event);
		bottomHueSlider.keyEvent(event);
		bottomSaturationSlider.keyEvent(event);
		bottomBrightnessSlider.keyEvent(event);
	}
	
	public void mouseEvent(MouseEvent event) {
		topHueSlider.mouseEvent(event);
		topSaturationSlider.mouseEvent(event);
		topBrightnessSlider.mouseEvent(event);
		bottomHueSlider.mouseEvent(event);
		bottomSaturationSlider.mouseEvent(event);
		bottomBrightnessSlider.mouseEvent(event);
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public ColorEqDisplay getDisplay() {
		return display;
	}

	public void setDisplay(ColorEqDisplay display) {
		this.display = display;
	}
	
	public void setTopHue(float hue) {
		getDisplay().setTopHue(hue);
		topColorHSB[0] = hue;
	}
	
	public void setTopSaturation(float saturation) {
		getDisplay().setTopSaturation(saturation);
		topColorHSB[1] = saturation;
	}
	
	public void setTopBrightness(float brightness) {
		getDisplay().setInactiveBrightness((int) (brightness * 255));
		topColorHSB[2] = brightness;
	}
	
	public void setBottomHue(float hue) {
		getDisplay().setBottomHue(hue);
		bottomColorHSB[0] = hue;
	}
	
	public void setBottomSaturation(float saturation) {
		getDisplay().setBottomSaturation(saturation);
		bottomColorHSB[1] = saturation;
	}
	
	public void setBottomBrightness(float brightness) {
		getDisplay().setActiveBrightness((int) (brightness * 255));
		bottomColorHSB[2] = brightness;
	}
	
}
