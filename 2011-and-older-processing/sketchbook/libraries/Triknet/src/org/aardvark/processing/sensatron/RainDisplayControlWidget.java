package org.aardvark.processing.sensatron;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.aardvark.processing.ACheckbox;
import org.aardvark.processing.Slider;
import org.aardvark.processing.delegate.ActionDelegate;
import org.aardvark.processing.sensatron.RainDisplay.RainMode;

import processing.core.PApplet;

public class RainDisplayControlWidget extends DisplayControlWidget<RainDisplay> {
	
	Slider viscositySlider;
	Slider precipSlider;
	Slider hueSlider;
	Slider randomHueSlider;
	Slider singleHueSlider;
	Slider sbSlider;
	Slider saturationSlider;
	Slider brightnessSlider;
	ACheckbox beatBox;
	ACheckbox modeBox;
	ACheckbox randomBox;
	
	public RainDisplayControlWidget(int x, int y, RainDisplay display, PApplet parent) {
		super(x, y, display, parent);
		
		viscositySlider = new Slider(parent, x + 10, y + 20, 100, 1);
		precipSlider = new Slider(parent, x + 10, y + 60, 100, 1);
		randomHueSlider = new Slider(parent, x + 10, y + 100, 100, 2);
		singleHueSlider = new Slider(parent, x + 10, y + 100, 100, 1);
		hueSlider = singleHueSlider;
		saturationSlider = new Slider(parent, x + 10, y + 140, 100, 1);
		brightnessSlider = new Slider(parent, x + 10, y + 140, 100, 1);
		sbSlider = saturationSlider;
		beatBox = new ACheckbox(parent, x + 120, y + 20, 10, 10, "Show Beat");
		modeBox = new ACheckbox(parent, x+120, y + 40, 10, 10, "Mode");
		randomBox = new ACheckbox(parent, x+120, y + 60, 10, 10, "Random");
		
		viscositySlider.setLabel("Viscosity");
		precipSlider.setLabel("Precip Rate");
		singleHueSlider.setLabel("Hue");
		randomHueSlider.setLabel("Hue Range");
		saturationSlider.setLabel("Saturation");
		brightnessSlider.setLabel("Brightness");
		
		if (display != null) {
			viscositySlider.setHandlePosition(0, (int) (display.getViscosity() * viscositySlider.getLength()));
			precipSlider.setHandlePosition(0, (int) (display.getPrecip() * precipSlider.getLength()));
			singleHueSlider.setHandlePosition(0, (int) (display.getHue() * singleHueSlider.getLength()));
			randomHueSlider.setHandlePosition(0, (int) (display.getMinHue() * randomHueSlider.getLength()));
			randomHueSlider.setHandlePosition(1, (int) (display.getMaxHue() * randomHueSlider.getLength()));
			saturationSlider.setHandlePosition(0, (int) (display.getSaturation() * saturationSlider.getLength()));
			brightnessSlider.setHandlePosition(0, (int) (display.getBrightness() * brightnessSlider.getLength()));
			if (display.getMode() == RainMode.SATURATION) {
				modeBox.setChecked(true);
				sbSlider = brightnessSlider;
			}
			if (display.isRandom()) {
				randomBox.setChecked(true);
				hueSlider = randomHueSlider;
			}
		}
		
		viscositySlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				getDisplay().setViscosity(viscositySlider.getNormalizedPositions()[0]);
			}
		});
		precipSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				getDisplay().setPrecip(precipSlider.getNormalizedPositions()[0]);
			}
		});
		singleHueSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				float position = hueSlider.getPositions()[0] / (float) hueSlider.getLength();
				getDisplay().setHue(position);
//				System.out.println("Hue is " + position);
			}
		});
		randomHueSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				float position = hueSlider.getPositions()[0] / (float) hueSlider.getLength();
				getDisplay().setMinHue(position);
				position = hueSlider.getPositions()[1] / (float) hueSlider.getLength();
				getDisplay().setMaxHue(position);
			}
		});
		saturationSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				float position = saturationSlider.getPositions()[0] / (float) saturationSlider.getLength();
				getDisplay().setSaturation(position);
//				System.out.println("Saturation is " + position);
			}
		});
		brightnessSlider.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				float position = brightnessSlider.getPositions()[0] / (float) brightnessSlider.getLength();
				getDisplay().setBrightness(position);
//				System.out.println("Saturation is " + position);
			}
		});
		beatBox.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				getDisplay().setShowBeats(beatBox.isChecked());
			}
		});
		modeBox.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				if (modeBox.isChecked()) {
					getDisplay().setMode(RainMode.SATURATION);
					sbSlider = brightnessSlider;
				} else {
					getDisplay().setMode(RainMode.BRIGHTNESS);
					sbSlider = saturationSlider;
				}
			}
		});
		randomBox.setActionDelegate(new ActionDelegate() {
			public void doAction(InputEvent event) {
				if (randomBox.isChecked()) {
					hueSlider = randomHueSlider;
//					float position = hueSlider.getPositions()[0] / (float) hueSlider.getLength();
//					getDisplay().setMinHue(position);
//					position = hueSlider.getPositions()[1] / (float) hueSlider.getLength();
//					getDisplay().setMaxHue(position);
					getDisplay().setRandom(true);
				} else {
					hueSlider = singleHueSlider;
//					float position = hueSlider.getPositions()[0] / (float) hueSlider.getLength();
//					getDisplay().setHue(position);
					getDisplay().setRandom(false);
				}
			}
		});
	}
	
	protected void drawMaximized() {
	    parent.stroke(0);
	    parent.fill(0, 0);
	    parent.rect(x, y, WIDTH, HEIGHT);

		viscositySlider.draw();
		precipSlider.draw();
		hueSlider.draw();
		sbSlider.draw();
		beatBox.draw();
		modeBox.draw();
		randomBox.draw();
	}

	public void keyEvent(KeyEvent event) {
		viscositySlider.keyEvent(event);
		precipSlider.keyEvent(event);
		hueSlider.keyEvent(event);
		sbSlider.keyEvent(event);
		beatBox.keyEvent(event);
		modeBox.keyEvent(event);
		randomBox.keyEvent(event);
	}
	
	public void mouseEvent(MouseEvent event) {
		viscositySlider.mouseEvent(event);
		precipSlider.mouseEvent(event);
		hueSlider.mouseEvent(event);
		sbSlider.mouseEvent(event);
		beatBox.mouseEvent(event);
		modeBox.mouseEvent(event);
		randomBox.mouseEvent(event);
	}
	
}
